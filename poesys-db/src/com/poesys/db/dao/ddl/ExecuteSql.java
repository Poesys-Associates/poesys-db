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
package com.poesys.db.dao.ddl;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.poesys.db.DbErrorException;
import com.poesys.db.Message;
import com.poesys.db.dao.PoesysTrackingThread;


/**
 * An implementation of the ISql interface that executes the SQL statement on
 * demand.
 * 
 * @author Robert J. Muller
 */
public class ExecuteSql implements IExecuteSql {
  /** The logger for this class */
  private static final Logger logger = Logger.getLogger(ExecuteSql.class);
  /** The SQL statement to execute */
  private ISql sql = null;

  /** The database subsystem for the SQL execution */
  private final String subsystem;

  /** timeout for the query thread */
  private static final int TIMEOUT = 1000 * 60;

  /**
   * Error message when there is an unexpected SQL exception from the DDL
   * statement
   */
  private static final String SQL_ERROR =
    "com.poesys.db.dto.msg.unexpected_sql_error";
  /** Error message when thread is interrupted or timed out */
  private static final String THREAD_ERROR = "com.poesys.db.dao.msg.thread";
  /** Error message when instantiated with a null SQL object */
  private static final String NULL_SQL_ERROR = "com.poesys.db.dao.msg.null_sql";
  /** Error message when instantiated with a null database subsystem */
  private static final String NULL_SUBSYSTEM_ERROR =
    "com.poesys.db.connection.msg.no_subsystem";

  /**
   * Create an ExecuteSql object, supplying the SQL statement object.
   * 
   * @param sql the SQL statement object
   * @param subsystem the database subsystem in which to execute the SQL DDL
   */
  public ExecuteSql(ISql sql, String subsystem) {
    if (sql == null) {
      throw new DbErrorException(Message.getMessage(NULL_SQL_ERROR, null));
    }
    if (subsystem == null || subsystem.isEmpty()) {
      throw new DbErrorException(Message.getMessage(NULL_SUBSYSTEM_ERROR, null));
    }
    this.sql = sql;
    this.subsystem = subsystem;
  }

  @Override
  public void execute() {
    Runnable runnable = getRunnableObject();
    PoesysTrackingThread thread = new PoesysTrackingThread(runnable, subsystem);
    thread.start();
    // Join the thread, blocking until the thread completes or
    // until the query times out.
    try {
      thread.join(TIMEOUT);
    } catch (InterruptedException e) {
      Object[] args = { "update", sql.getSql() };
      String message = Message.getMessage(THREAD_ERROR, args);
      logger.error(message, e);
    }
  }

  /**
   * Create a runnable query object that runs within a PoesysTrackingThread. The
   * run method executes the SQL DDL statement. The thread then terminates after
   * committing and closing the connection.
   * 
   * @return the runnable object
   */
  protected Runnable getRunnableObject() {
    // Create a runnable query object that does the query.
    Runnable query = new Runnable() {
      public void run() {
        PoesysTrackingThread thread =
          (PoesysTrackingThread)Thread.currentThread();
        Statement statement = null;
        try {
          Connection connection = thread.getConnection();
          statement = connection.createStatement();
          if (statement != null) {
            statement.execute(sql.getSql());
          }
        } catch (SQLException e) {
          throw new DbErrorException(Message.getMessage(SQL_ERROR, null), e);
        } finally {
          if (statement != null) {
            try {
              statement.close();
            } catch (SQLException e) {
              // ignore;
            }
          }
          if (thread != null) {
            thread.closeConnection();
          }
        }
      }
    };
    return query;
  }
}
