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


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.poesys.db.BatchException;
import com.poesys.db.Message;
import com.poesys.db.NoRequiredValueException;
import com.poesys.db.connection.ConnectionFactoryFactory;
import com.poesys.db.connection.IConnectionFactory;
import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * An implementation of the IQueryListWithParameters interface that queries a
 * list of data transfer objects (DTOs) based on a parameterized SQL statement.
 * The list is guaranteed to be thread safe, and the DTOs should also be thread
 * safe if you are going to use them in a multi-threaded environment.
 * 
 * @see com.poesys.db.dto.IDbDto
 * @see IParameterizedQuerySql
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to query
 * @param <S> the type of IDbDto that contains the parameters
 * @param <C> the collection type of the set of queried DTOs
 */
public class QueryListWithParameters<T extends IDbDto, S extends IDbDto, C extends Collection<T>>
    implements IQueryListWithParameters<T, S, C> {
  /** Logger for debugging */
  private static final Logger logger =
    Logger.getLogger(QueryListWithParameters.class);

  /** Internal Strategy-pattern object containing the SQL query with parameters */
  protected final IParameterizedQuerySql<T, S> sql;
  /** the client subsystem owning the queried object */
  protected final String subsystem;
  /** Number of rows to fetch at once, optimizes query fetching */
  protected final int rows;

  /** the collection or list of query result DTOs */
  @SuppressWarnings("unchecked")
  protected C list = (C)new ArrayList<T>();

  /** timeout for the query thread */
  private static final int TIMEOUT = 1000 * 60;

  /** Error getting resource bundle, can't resolve to bundle text so a constant */
  private static final String RESOURCE_BUNDLE_ERROR =
    "Problem getting Poesys/DB resource bundle";

  /** Error message for query with no parameter object */
  protected static final String PARAM_ERROR =
    "com.poesys.db.dto.msg.no_parameter";
  /** Error message when thread is interrupted or timed out */
  protected static final String THREAD_ERROR = "com.poesys.db.dao.msg.thread";
  /** Error message when query returns SQL exception querying list */
  protected static final String SQL_ERROR =
    "com.poesys.db.dao.query.msg.sql_parameter_list";

  /**
   * Create a QueryListWithParameters object.
   * 
   * @param sql the SQL statement specification
   * @param subsystem the subsystem that owns the object to query
   * @param rows the number of rows to fetch at once; optimizes results fetching
   */
  public QueryListWithParameters(IParameterizedQuerySql<T, S> sql,
                                 String subsystem,
                                 int rows) {
    this.sql = sql;
    this.subsystem = subsystem;
    this.rows = rows;
  }

  @SuppressWarnings("unchecked")
  @Override
  public C query(S parameters) throws SQLException, BatchException {
    // Make sure the set of parameters exists.
    if (parameters == null) {
      throw new NoRequiredValueException(PARAM_ERROR);
    }

    PoesysTrackingThread thread = null;

    // If the current thread is a PoesysTrackingThread, just run the query in
    // that thread directly; if not, start a new thread to run it.
    if (Thread.currentThread() instanceof PoesysTrackingThread) {
      thread = (PoesysTrackingThread)Thread.currentThread();
      logger.debug("Using existing TrackingThread " + thread.getId());
      doDatabaseQuery(parameters, thread);
    } else {
      thread = new PoesysTrackingThread(getRunnableQuery(parameters));
      logger.debug("Starting new TrackingThread " + thread.getId());

      thread.start();
      // Join the thread, blocking until the thread completes or
      // until the query times out.
      try {
        thread.join(TIMEOUT);
      } catch (InterruptedException e) {
        Object[] args =
          { "parameterized query", parameters.getPrimaryKey().getStringKey() };
        String message = Message.getMessage(THREAD_ERROR, args);
        logger.error(message, e);
      }
    }

    // Reinitialize the list to make this method reentrant
    C returnedList = list;
    list = (C)new ArrayList<T>();

    return returnedList;
  }

  /**
   * Create a runnable query object that runs within a PoesysTrackingThread.
   * This method does a simple database query within the thread.
   * 
   * @param parameters the parameters object
   * @return the runnable query
   */
  private Runnable getRunnableQuery(S parameters) {
    Runnable query = new Runnable() {
      public void run() {
        // Get the current tracking thread in which this is running.
        PoesysTrackingThread thread =
          (PoesysTrackingThread)Thread.currentThread();
        doDatabaseQuery(parameters, thread);
      }
    };

    return query;
  }

  /**
   * Execute the query with the parameters using a specified tracking thread.
   * 
   * @param parameters the query parameters
   * @param thread the tracking thread
   */
  private void doDatabaseQuery(S parameters, PoesysTrackingThread thread) {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Connection connection = null;

    // Query the list of objects based on the parameters.
    try {
      validateParameters(parameters);
      logger.debug("Querying list with parameters: " + sql.getSql());
      IConnectionFactory factory =
        ConnectionFactoryFactory.getInstance(subsystem);
      connection = factory.getConnection();
      stmt = connection.prepareStatement(sql.getSql());
      stmt.setFetchSize(rows);
      logger.debug("Binding parameters: " + sql.getParameterValues(parameters));
      sql.bindParameters(stmt, parameters);
      rs = stmt.executeQuery();

      // Loop through and fetch all the results, adding each to the result
      // list.
      int count = 0;
      while (rs.next()) {
        T dto = getObject(rs, thread);
        if (dto != null) {
          list.add(dto);
          count++;
        }
        thread.addDto(dto);
      }
      logger.debug("Fetched " + count + (count == 1 ? " object" : " objects"));
    } catch (SQLException e) {
      // Log the message and the SQL statement, then rethrow the exception.
      Object[] args = { e.getMessage() };
      String message = Message.getMessage(SQL_ERROR, args);
      logger.error(message, e);
      // Log a debugging message for the "already been closed" error
      if ("PooledConnection has already been closed.".equals(e.getMessage())) {
        logger.debug("Closed connection: " + connection);
      }
      logger.debug("SQL statement in class: " + sql.getClass().getName());
      throw new RuntimeException(message, e);
    } catch (IOException e) {
      logger.error(RESOURCE_BUNDLE_ERROR, e);
      throw new RuntimeException(RESOURCE_BUNDLE_ERROR);
    } catch (BatchException e) {
      Object[] args = { e.getMessage() };
      String message = Message.getMessage(SQL_ERROR, args);
      logger.error(message, e);
    } finally {
      closeAllResources(stmt, connection);
    }

    // Process nested objects after DTOs tracked and SQL connection closed.
    queryNestedObjectsForList(thread);
  }

  /**
   * Close the statement and connection for the query.
   * 
   * @param stmt the SQL statement object
   * @param connection the SQL connection object
   */
  private void closeAllResources(PreparedStatement stmt, Connection connection) {
    // Close the statement and result set as required.
    if (stmt != null) {
      try {
        stmt.close();
      } catch (SQLException e) {
        // Log and ignore
        logger.warn("Error closing SQL statement", e);
      }
    }
    if (connection != null) {
      String connectionString = connection.toString();
      try {
        connection.close();
      } catch (SQLException e) {
        // Log and ignore
        logger.warn("Error closing connection " + connectionString, e);
      }
      logger.debug("Closed connection " + connectionString);
    }
  }

  /**
   * Query the nested objects for all the DTOs in a list of DTOs. Call this
   * outside of a block that contains SQL resources (statement, result set,
   * connection) to avoid memory leaks and connection exhaustion.
   * 
   * @param thread the tracking thread for this query
   */
  protected void queryNestedObjectsForList(PoesysTrackingThread thread) {
    if (list != null) {
      for (T dto : list) {
        try {
          dto.queryNestedObjects();
        } catch (SQLException e) {
          // Log the message and the SQL statement, then rethrow the exception.
          Object[] args = { e.getMessage() };
          String message = Message.getMessage(SQL_ERROR, args);
          logger.error(message, e);
          // Log a debugging message for the "already been closed" error
          if ("PooledConnection has already been closed.".equals(e.getMessage())) {
            logger.debug("Closed pooled connection while getting nested objects");
          }
          logger.debug("SQL statement in class: " + sql.getClass().getName());
          throw new RuntimeException(message, e);
        } catch (BatchException e) {
          Object[] args = { e.getMessage() };
          String message = Message.getMessage(SQL_ERROR, args);
          logger.error(message, e);
        } // object is complete, set it as processed.
        thread.setProcessed(dto.getPrimaryKey().getStringKey(), true);
        logger.debug("Retrieved all nested objects for "
                     + dto.getPrimaryKey().getStringKey());
      }
    }
  }

  /**
   * Validate the parameters. You can override this method in a subclass to
   * provide a valid session id for caching sessions.
   * 
   * @param parameters the parameters objects
   * @throws SQLException when there is a validation problem
   */
  protected void validateParameters(S parameters) throws SQLException {
    // Validate the parameters.
    parameters.validateForQuery();
  }

  /**
   * Get a DTO from a SQL result set and add the DTO to the tracking thread.
   * Subclasses can override this method to provide caching or other services
   * for the object.
   * 
   * @param rs the result set from an executed SQL statement
   * @param thread the tracking thread for the query
   * @return the object
   * @throws SQLException when there is a database access problem
   * @throws BatchException when batch processing fails for a nested object
   */
  protected T getObject(ResultSet rs, PoesysTrackingThread thread)
      throws SQLException, BatchException {
    IPrimaryKey key = sql.getPrimaryKey(rs);
    T dto = sql.getData(rs);
    // Set the new and changed flags to show this object exists and is
    // unchanged from the version in the database.
    dto.setExisting();
    // If tracking and there is a DTO, track the DTO.
    if (thread != null && dto != null
        && thread.getDto(key.getStringKey()) == null) {
      thread.addDto(dto);
      thread.setProcessed(key.getStringKey(), true);
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
