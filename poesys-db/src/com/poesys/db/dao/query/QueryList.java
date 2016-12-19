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


/**
 * An implementation of the IQueryList interface that queries a list of data
 * transfer objects (DTOs) based on a SQL statement with no parameters. The list
 * is guaranteed to be thread safe, and the DTOs should also be thread safe if
 * you are going to use them in a multi-threaded environment.
 * 
 * @see com.poesys.db.dto.IDbDto
 * @see IQuerySql
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to query
 */
public class QueryList<T extends IDbDto> implements IQueryList<T> {
  /** Logger for debugging */
  private static final Logger logger = Logger.getLogger(QueryList.class);

  /**
   * The list containing the queried objects, must be null at the end of any
   * using method
   */
  private List<T> list = null;

  /** Error message when thread is interrupted or timed out */
  protected static final String THREAD_ERROR = "com.poesys.db.dao.msg.thread";
  /** Error message when thread gets exception during query */
  protected static final String QUERY_ERROR =
    "com.poesys.db.dao.query.msg.parameter_list";
  /** Error message when query returns SQL exception querying list */
  protected static final String SQL_ERROR =
    "com.poesys.db.dao.query.msg.sql_parameter_list";

  /** timeout for the query thread */
  private static final int TIMEOUT = 1000 * 60;

  /**
   * Internal Strategy-pattern object containing the SQL query with no
   * parameters
   */
  protected final IQuerySql<T> sql;
  /** the client subsystem owning the queried object */
  protected final String subsystem;
  /** Number of rows to fetch at once, optimizes query fetching */
  protected final int rows;

  /**
   * Create a QueryList object.
   * 
   * @param sql the SQL statement specification
   * @param subsystem the subsystem that owns the object being queried
   * @param rows the number of rows to fetch at once; optimizes results fetching
   */
  public QueryList(IQuerySql<T> sql, String subsystem, int rows) {
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
      } catch (InterruptedException e) {
        Object[] args = { "list query", sql.getSql() };
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
   * This method does a simple database query within the thread.
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
          String message = Message.getMessage(QUERY_ERROR, null);
          logger.error(message, e);
          throw new DbErrorException(message, thread, e);
        } finally {
          thread.closeConnection();
        }
      }
    };

    return query;
  }

  /**
   * Execute the query, allocating the list and querying all the objects.
   * 
   * @param thread the tracking thread
   */
  private void doQuery(PoesysTrackingThread thread) {
    list = new ArrayList<T>();
    PreparedStatement stmt = null;
    ResultSet rs = null;
    int counter = 0;
    int total = 0;
    long time = System.currentTimeMillis();
    long startTime = time;

    // Query the list of objects.
    try {
      stmt = thread.getConnection().prepareStatement(sql.getSql());
      logger.debug("Querying list without parameters with SQL: " + sql.getSql());
      stmt.setFetchSize(rows);
      rs = stmt.executeQuery();

      // Loop through and fetch all the results, adding each to the result list.
      while (rs.next()) {
        T object = getObject(rs);
        if (object != null) {
          list.add(object);
        }
        counter++;
        if (counter >= 1000) {
          long now = System.currentTimeMillis();
          long diff = now - time;
          time = now;
          total += counter;
          logger.debug("Fetching " + object.getClass().getName() + ", count: "
                       + counter + ", total: " + total + ", average ms: "
                       + (diff / counter) + ", total ms: " + (now - startTime));
          counter = 0;
        }
      }
    } catch (SQLException e) {
      // Log the message and the SQL statement, then rethrow the exception.
      logger.error(e.getMessage());
      logger.error(sql.getSql());
      logger.debug("SQL statement in class: " + sql.getClass().getName());
      String[] args = { sql.getSql() };
      String message = Message.getMessage(SQL_ERROR, args);
      throw new DbErrorException(message, e);
    } finally {
      // Close the statement and result set as required.
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
   * T. You can override this method in subclasses to provide a session ID in
   * the call to queryNestedObjects.
   * 
   * @param list the list of DTOs for which to query nested objects
   * @param thread the Poesys tracking thread for this retrieval
   */
  protected void queryNestedObjectsForList(List<T> list,
                                           PoesysTrackingThread thread) {
    // Query any nested objects. This is outside the fetch above to make sure
    // that the statement and result set are closed before recursing.
    for (T object : list) {
      object.queryNestedObjects();
    }
  }

  /**
   * Get a DTO from a SQL result set. Subclasses can override this method to
   * provide caching or other services for the object.
   * 
   * @param rs the result set from an executed SQL statement
   * @return the database DTO
   */
  protected T getObject(ResultSet rs) {
    T object = sql.getData(rs);
    // Set the status flag to show the object is synchronized with the database.
    object.setExisting();
    return object;
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
