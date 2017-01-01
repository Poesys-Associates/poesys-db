/*
 * Copyright (c) 2012 Poesys Associates. All rights reserved.
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

package com.poesys.db.dao.delete;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.poesys.db.DbErrorException;
import com.poesys.db.Message;
import com.poesys.db.dao.PoesysTrackingThread;


/**
 * An implementation of the IDelete interface that deletes data from the
 * database using just a SQL where clause. This class provides the system with
 * the capability to do arbitrary deletes in the database. Note that the SQL
 * statement can be null for deletes that are done through the database rather
 * than through the application (that is, cascaded deletes). As there are no
 * DTOs involved, no status values get set and there are no tracking-thread
 * changes.
 * 
 * @see IDeleteQuery
 * @author Robert J. Muller
 */
public class DeleteByQuery implements IDeleteQuery {

  /** Log4j logger for this class */
  private static final Logger logger = Logger.getLogger(DeleteByQuery.class);

  /** SQL statement object */
  private IDeleteSqlWithQuery sql;

  /** Error message when delete throws exception */
  private static final String DELETE_ERROR =
    "com.poesys.db.dao.delete.msg.delete";
  /** Error message when thread has a problem */
  private static final String THREAD_ERROR = "com.poesys.db.dao.msg.thread";

  /** timeout for the query thread */
  private static final int TIMEOUT = 1000 * 60;

  /** the subsystem of class T */
  protected final String subsystem;

  /**
   * Create a DeleteByKey object by supplying the concrete implementation of the
   * SQL-statement generator and JDBC setter.
   * 
   * @param sql the SQL UPDATE statement generator object
   * @param subsystem the subsystem of the DTOs to delete
   */
  public DeleteByQuery(IDeleteSqlWithQuery sql, String subsystem) {
    this.sql = sql;
    this.subsystem = subsystem;
  }

  @Override
  public void delete() {
    if (sql != null) {
      if (Thread.currentThread() instanceof PoesysTrackingThread) {
        doDelete((PoesysTrackingThread)Thread.currentThread());
      } else {
        Runnable query = new Runnable() {
          public void run() {
            PoesysTrackingThread thread =
                (PoesysTrackingThread)Thread.currentThread();
            try {
              doDelete(thread);
            } catch (Throwable e) {
              thread.setThrowable(e);
            } finally {
              thread.closeConnection();
            }
          }
        };
        PoesysTrackingThread thread =
          new PoesysTrackingThread(query, subsystem);
        thread.start();
        // Join the thread, blocking until the thread completes or
        // until the query times out.
        try {
          thread.join(TIMEOUT);
          // Check for problems.
          if (thread.getThrowable() != null) {
            Object[] args = { "delete", "collection of DTOs" };
            String message = Message.getMessage(THREAD_ERROR, args);
            logger.error(message, thread.getThrowable());
            throw new DbErrorException(message, thread.getThrowable());
          }
        } catch (InterruptedException e) {
          Object[] args = { "insert", sql.getSql() };
          String message = Message.getMessage(THREAD_ERROR, args);
          logger.error(message, e);
        }
      }
    }
  }

  /**
   * Do the delete.
   * 
   * @param thread the tracking thread
   */
  private void doDelete(PoesysTrackingThread thread) {
    PreparedStatement stmt = null;
    String sqlText = null;
    Connection connection = thread.getConnection();

    try {
      sqlText = sql.getSql();
      stmt = connection.prepareStatement(sqlText);
      logger.debug("Delete by query: " + sqlText);

      stmt.executeUpdate();
    } catch (SQLException e) {
      Object[] args = { sql.getSql() };
      String message = Message.getMessage(DELETE_ERROR, args);
      logger.error(message, e);
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
  }

  @Override
  public void close() {
    // Nothing to do
    // Note that query-based delete does not affect caches at all.
  }
}
