/*
 * Copyright (c) 2008 Poesys Associates. All rights reserved.
 * 
 * This file is part of Poesys-DB.
 * 
 * Poesys-DB is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Poesys-DB is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Poesys-DB. If not, see <http://www.gnu.org/licenses/>.
 */
package com.poesys.db.dao.query;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.poesys.db.ConstraintViolationException;
import com.poesys.db.DbErrorException;
import com.poesys.db.Message;
import com.poesys.db.NoPrimaryKeyException;
import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDbDto.Status;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An implementation of the IQueryByKey interface that implements the basic
 * elements of a query by primary key. The class creates data transfer objects
 * (DTOs) with a Strategy-pattern object that contains the SQL required to query
 * the data, and the query method is a factory method that produces a single
 * data transfer object containing the data from the database, including any
 * nested objects. The DTO should be thread-safe if it is going to be cached or
 * otherwise used in a multi-threaded environment.
 * </p>
 * <p>
 * Use an IDtoFactory class to generate the appropriate kind of object (cached
 * or direct) om the IKeyQuerySql implementation.
 * </p>
 * 
 * @see com.poesys.db.dto.IDbDto
 * @see com.poesys.db.pk.IPrimaryKey
 * @see com.poesys.db.dao.IDaoFactory
 * @see IKeyQuerySql
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to query
 */
public class QueryByKey<T extends IDbDto> implements IQueryByKey<T> {
  /** Logger for debugging */
  private static final Logger logger = Logger.getLogger(QueryByKey.class);
  /** Internal Strategy-pattern object containing the SQL query */
  protected final IKeyQuerySql<T> sql;
  /** the client subsystem owning the queried object */
  protected final String subsystem;

  /** timeout for the query thread */
  private static final int TIMEOUT = 10000 * 60;

  /** Error on executing SQL query */
  private static final String SQL_ERROR =
    "com.poesys.db.dto.msg.unexpected_sql_error";
  /** Error message when thread is interrupted or timed out */
  private static final String THREAD_ERROR = "com.poesys.db.dao.msg.thread";
  /** Error message when thread can't get the DTO specified by the key */
  private static final String GET_DTO_ERROR = "com.poesys.db.dao.query.msg.get";
  /** Error message about not having a primary key with which to query */
  protected static final String NO_PRIMARY_KEY_ERROR =
    "com.poesys.db.dao.query.msg.no_primary_key";

  /**
   * Create a QueryByKey object.
   * 
   * @param sql the SQL SELECT statement object for the query
   * @param subsystem the subsystem that owns the object to query
   */
  public QueryByKey(IKeyQuerySql<T> sql, String subsystem) {
    this.sql = sql;
    this.subsystem = subsystem;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T queryByKey(IPrimaryKey key) {
    // Make sure the key is there.
    if (key == null) {
      throw new NoPrimaryKeyException(Message.getMessage(NO_PRIMARY_KEY_ERROR,
                                                         null));
    }

    PoesysTrackingThread thread = null;

    // If the current thread is a PoesysTrackingThread, just run the query in
    // that thread; if not, start a new thread to get the objects and its
    // dependents.
    if (Thread.currentThread() instanceof PoesysTrackingThread) {
      thread = (PoesysTrackingThread)Thread.currentThread();
      IDbDto dto = thread.getDto(key);
      // Only query if DTO not already queried in this thread.
      if (dto == null) {
        getDto(key, thread);
      }
    } else {
      Runnable query = getRunnableQuery(key);
      thread = new PoesysTrackingThread(query, subsystem);
      thread.start();
      // Join the thread, blocking until the thread completes or
      // until the query times out.
      try {
        thread.join(TIMEOUT);
      } catch (InterruptedException e) {
        Object[] args = { "update", key.getStringKey() };
        String message = Message.getMessage(THREAD_ERROR, args);
        logger.error(message, e);
      }
    }

    return (T)thread.getDto(key);
  }

  /**
   * Create a runnable query object that runs within a PoesysTrackingThread. The
   * run method checks the cache for the DTO. If it is cached, it gets it from
   * the cache; if not, it queries the object from the database. The method then
   * queries nested objects. All these activities happen in a single run of the
   * query in the tracking thread.
   * 
   * @param key the primary key of the DTO
   * @return the runnable query
   */
  protected Runnable getRunnableQuery(IPrimaryKey key) {
    // Create a runnable query object that does the query.
    Runnable query = new Runnable() {
      public void run() {
        PoesysTrackingThread thread = null;
        try {
          // Get the current tracking thread in which this is running.
          thread = (PoesysTrackingThread)Thread.currentThread();
          // Get the DTO, storing it in the tracking thread; this is the top
          // level of a possible nested-object tree, so the tracking thread is
          // empty at this point. No need to check it for DTO existence.
          getDto(key, thread);
        } catch (Exception e) {
          Object[] args = { key.getStringKey() };
          String message = Message.getMessage(GET_DTO_ERROR, args);
          logger.error(message, e);
          throw new DbErrorException(message, thread, e);
        } finally {
          if (thread != null) {
            thread.closeConnection();
          }
        }
      }
    };
    return query;
  }

  /**
   * Get a DTO based on a primary key value using the current tracking thread.
   * 
   * @param key the key to look up
   * @param thread the tracking thread
   * @return the DTO corresponding to the primary key
   */
  protected T getDto(IPrimaryKey key, PoesysTrackingThread thread) {
    PreparedStatement stmt = null;
    T dto = null;

    // Make sure the key is there.
    if (key == null) {
      throw new NoPrimaryKeyException(Message.getMessage(NO_PRIMARY_KEY_ERROR,
                                                         null));
    }

    try {
      Connection connection = thread.getConnection();
      stmt = connection.prepareStatement(sql.getSql(key));
      key.setParams(stmt, 1);

      logger.debug("Querying by key: " + sql.getSql(key));
      logger.debug("Setting key value: " + key.getValueList());

      ResultSet rs = stmt.executeQuery();

      // Get a single result from the ResultSet and create the DTO.
      if (rs.next()) {
        dto = sql.getData(key, rs);
        // Only proceed if DTO retrieved.
        if (dto != null) {
          // Set status to existing to indicate DTO is fresh from the database.
          dto.setExisting();
          // Add the DTO to the tracking thread.
          thread.addDto(dto);
        }
        logger.debug("Queried object by key: "
                     + dto.getPrimaryKey().getValueList());
      }
    } catch (ConstraintViolationException e) {
      throw new DbErrorException(e.getMessage(), thread, e);
    } catch (SQLException e) {
      // Log the message and the SQL statement, then rethrow the exception.
      logger.error("Query by key error: " + e.getMessage());
      logger.error("Query by key sql: " + sql.getSql(key) + "\n");
      logger.debug("SQL statement in class: " + sql.getClass().getName());
      String message = Message.getMessage(SQL_ERROR, null);
      throw new DbErrorException(message, thread, e);
    } finally {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          // ignore
        }
      }
    }

    // Query any nested objects. This is outside the fetch above to make sure
    // that the statement and result set are closed before recursing.
    if (dto != null) {
      // Only query nested objects if the thread hasn't already processed this
      // DTO, and thus already queried them. This is an optimization that avoids
      // unnecessary parameterized queries that will result in querying objects
      // already queried and available in the thread.
      if (!thread.isProcessed(key)) {
        dto.queryNestedObjects();
        // Mark the DTO fully processed.
        if (thread.getDto(key) == null) {
          // Add to tracking thread now.
          thread.addDto(dto);
        }
        thread.setProcessed(dto, true);
      }
      // Undo any status changes due to nested-object processing.
      if (dto.getStatus() != Status.EXISTING) {
        dto.undoStatus();
      }
    }

    return dto;
  }

  @Override
  public void close() {
    // Nothing to do
  }

  @Override
  public void setExpiration(int expiration) {
    // Does nothing, no expiration on objects here
  }
}
