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


import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.poesys.db.DbErrorException;
import com.poesys.db.Message;
import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.dto.IDbDto;


/**
 * An implementation of the IQueryCount interface that performs the basic count
 * query given a parameter data transfer object (DTO) of type P
 * 
 * @see IParameterizedCountSql
 * 
 * @author Robert J. Muller
 * @param <P> the class for the parameter DTO
 */
public class QueryCount<P extends IDbDto> implements IQueryCount<P> {
  /** Logger for debugging */
  private static final Logger logger = Logger.getLogger(QueryCount.class);

  /** The parameterized count SQL statement object */
  private final IParameterizedCountSql<P> sql;

  /** the query count; must always be null at the end of the method */
  private static BigInteger count = null;

  /** timeout for the query thread */
  private static final int TIMEOUT = 1000 * 60;

  /** Error message when thread is interrupted or timed out */
  private static final String THREAD_ERROR = "com.poesys.db.dao.msg.thread";

  /**
   * Create a parameterized count query.
   * 
   * @param sql the SQL generator for the parameterized query
   */
  public QueryCount(IParameterizedCountSql<P> sql) {
    this.sql = sql;
  }

  @Override
  public BigInteger queryCount(P parameters, String subsystem) {
    Runnable query = getRunnable(parameters);
    PoesysTrackingThread thread = new PoesysTrackingThread(query, subsystem);
    thread.start();
    // Join the thread, blocking until the thread completes or
    // until the query times out.
    try {
      thread.join(TIMEOUT);
      // Check for problems.
      if (thread.getThrowable() != null) {
        Object[] args = { "query", sql.getSql() };
        String message = Message.getMessage(THREAD_ERROR, args);
        logger.error(message, thread.getThrowable());
        throw new DbErrorException(message, thread.getThrowable());
      }
    } catch (InterruptedException e) {
      Object[] args = { "get query count", parameters, subsystem };
      String message = Message.getMessage(THREAD_ERROR, args);
      logger.error(message, e);
    } finally {
      thread.closeConnection();
    }

    // Make method reentrant by copying returned static variable.
    BigInteger countCopy = count;
    count = null;
    return countCopy;
  }

  /**
   * Create a Runnable query object that runs the count query. The run method
   * runs the query and stores the count result in the static count variable.
   * The method that runs the Runnable must be reentrant.
   * 
   * @param parameters the parameters object setting up the count
   * @return the Runnable query object
   */
  private Runnable getRunnable(P parameters) {
    // Create a runnable query object that does the query.
    Runnable query = new Runnable() {
      /**
       * Run the count query.
       */
      public void run() {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        PoesysTrackingThread thread =
          (PoesysTrackingThread)Thread.currentThread();

        // Query the count based on the parameters.
        try {
          // Validate the parameters.
          parameters.validateForQuery();

          stmt = thread.getConnection().prepareStatement(sql.getSql());
          sql.bindParameters(stmt, parameters);
          rs = stmt.executeQuery();

          // Fetch the result, adding each to the result list.
          if (rs.next()) {
            count = rs.getBigDecimal("count").toBigInteger();
          }
        } catch (SQLException e) {
          thread.setThrowable(e);
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
      }
    };
    return query;
  }
}
