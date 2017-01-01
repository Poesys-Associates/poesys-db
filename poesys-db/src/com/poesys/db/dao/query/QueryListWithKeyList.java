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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.poesys.db.DbErrorException;
import com.poesys.db.Message;
import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * An implementation of the IQueryList interface that queries a list of data
 * transfer objects (DTOs) based on a parameterized SQL statement and a list of
 * keys. The list is guaranteed to be thread safe, and the DTOs should also be
 * thread safe if you are going to use them in a multi-threaded environment.
 * 
 * @see com.poesys.db.dto.IDbDto
 * @see IParameterizedQuerySql
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to query
 */
public class QueryListWithKeyList<T extends IDbDto> implements IQueryList<T> {

  /** Logger for debugging */
  private static final Logger logger =
    Logger.getLogger(QueryListWithKeyList.class);

  /** the list of query result DTOs */
  private List<T> list = new ArrayList<T>();

  /** Internal Strategy-pattern object containing the SQL query with key list */
  protected final IKeyListQuerySql<T> sql;
  /** Number of rows to fetch at once, optimizes query fetching */
  protected final int rows;
  /** the client subsystem owning the queried object */
  protected final String subsystem;

  /** timeout for the query thread */
  private static final int TIMEOUT = 1000 * 60;

  /** Error message when thread is interrupted or timed out */
  protected static final String THREAD_ERROR = "com.poesys.db.dao.msg.thread";
  /** Error message when query returns SQL exception querying list */
  protected static final String SQL_ERROR =
    "com.poesys.db.dao.query.msg.sql_parameter_list";
  /** Error message when running query within thread */
  private static final String QUERYING_LIST_WITH_KEY_ERROR =
    "com.poesys.db.dao.query.msg.query_key_list";

  /**
   * Create a QueryListWithParameters object.
   * 
   * @param sql the SQL statement specification
   * @param subsystem the subsystem that owns the object to query
   * @param rows the number of rows to fetch at once; optimizes results fetching
   */
  public QueryListWithKeyList(IKeyListQuerySql<T> sql,
                              String subsystem,
                              int rows) {
    this.sql = sql;
    this.subsystem = subsystem;
    this.rows = rows;
  }

  @Override
  public List<T> query() {
    PoesysTrackingThread thread = null;

    // If the current thread is a PoesysTrackingThread, just run the query in
    // that thread directly; if not, start a new thread to run it.
    if (Thread.currentThread() instanceof PoesysTrackingThread) {
      thread = (PoesysTrackingThread)Thread.currentThread();
      logger.debug("Using existing TrackingThread " + thread.getId());
      doQuery(thread);
    } else {
      thread = new PoesysTrackingThread(getRunnableQuery(), subsystem);

      thread.start();
      // Join the thread, blocking until the thread completes or
      // until the query times out.
      try {
        thread.join(TIMEOUT);
        // Check for problems.
        if (thread.getThrowable() != null) {
          Object[] args = { sql.getKeyValues() };
          String message =
            Message.getMessage(QUERYING_LIST_WITH_KEY_ERROR, args);
          logger.error(message, thread.getThrowable());
          throw new DbErrorException(message, thread.getThrowable());
        }
      } catch (InterruptedException e) {
        Object[] args = { "key list query", sql.getKeyValues() };
        String message = Message.getMessage(THREAD_ERROR, args);
        logger.error(message, e);
      }
    }

    // Reinitialize the list to make this method reentrant
    List<T> returnedList = list;
    list = new ArrayList<T>();

    return returnedList;
  }

  /**
   * Create a runnable query object that runs within a PoesysTrackingThread.
   * This method runs the database query within the thread.
   * 
   * @return the runnable query
   */
  private Runnable getRunnableQuery() {
    Runnable query = new Runnable() {
      public void run() {
        // Get the current tracking thread in which this is running.
        PoesysTrackingThread thread =
          (PoesysTrackingThread)Thread.currentThread();
        try {
          doQuery(thread);
        } catch (Exception e) {
          thread.setThrowable(e);
        } finally {
          thread.closeConnection();
        }
      }
    };

    return query;
  }

  /**
   * Execute the query with the parameters using a specified tracking thread.
   * 
   * @param thread the tracking thread
   */
  protected void doQuery(PoesysTrackingThread thread) {
    PreparedStatement stmt = null;
    ResultSet rs = null;

    // Query the list of objects based on the parameters.
    try {
      Connection connection = thread.getConnection();
      String sqlStatement = sql.getSql();
      stmt = connection.prepareStatement(sqlStatement);
      stmt.setFetchSize(rows);
      sql.bindKeys(stmt);
      logger.debug("Querying list with key list: " + sql.getSql());
      logger.debug("Binding key list: " + sql.getKeyValues());
      rs = stmt.executeQuery();

      // Loop through and fetch all the results, adding each to the result list
      // class member.
      int count = 0;
      while (rs.next()) {
        T object = getObject(rs, thread);
        if (object != null) {
          list.add(object);
          count++;
        }
      }
      logger.debug("Fetched " + count + " objects");
    } catch (SQLException e) {
      // Log the message and the SQL statement, then rethrow the exception.
      Object[] args = { sql.getSql() };
      String message = Message.getMessage(SQL_ERROR, args);
      logger.error(message, e);
      // Log a debugging message for the "already been closed" error
      logger.debug("SQL statement in class: " + sql.getClass().getName());
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

    queryNestedObjectsForList(list, thread);
  }

  /**
   * Query the nested objects for all the objects in a list of objects of type
   * T. Call this outside of a block that contains SQL resources (statement,
   * result set, connection) to avoid memory leaks and connection exhaustion.
   * 
   * @param list the list of objects of type C
   * @param thread the Poesys tracking thread for this retrieval
   */
  protected void queryNestedObjectsForList(List<T> list,
                                           PoesysTrackingThread thread) {
    if (list != null) {
      for (T dto : list) {
        // Query only first time as optimization.
        if (!thread.isProcessed(dto.getPrimaryKey())) {
          dto.queryNestedObjects();
        }
        // Set processed after first nested-object query.
        thread.setProcessed(dto, true);
        // Set status to existing to indicate DTO is fresh from the database.
        dto.setExisting();
      }
    }
  }

  /**
   * Get a DTO from a SQL result set. Subclasses can override this method to
   * provide caching or other services for the object.
   * 
   * @param rs the result set from an executed SQL statement
   * @param thread the tracking thread for the current query process
   * @return the object
   */
  protected T getObject(ResultSet rs, PoesysTrackingThread thread) {
    IPrimaryKey key = sql.getPrimaryKey(rs);
    @SuppressWarnings("unchecked")
    // Try getting the queried object from the tracking thread.
    T dto = (T)thread.getDto(key);
    if (dto == null) {
      // Get the queried object from the result set.
      dto = sql.getData(rs);
      logger.debug("Retrieved DTO from database: "
                   + dto.getPrimaryKey().getStringKey());
    } else {
      logger.debug("Retrieved DTO from tracking thread: "
                   + dto.getPrimaryKey().getStringKey());
      // Set object as processed to prevent infinite recursion.
      thread.setProcessed(dto, true);
    }
    return dto;
  }

  @Override
  public void close() {
    // Nothing to do
  }

  @Override
  public void setExpiration(int expiration) {
    // Does nothing in this class, no expiration
  }
}
