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
package com.poesys.db.dao.update;


import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.poesys.db.DbErrorException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.Message;
import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An implementation of the IUpdateWithParameters interface that updates the
 * database with a single UPDATE statement parameterized with the contents of a
 * data transfer object (DTO). Use this class to update a set of objects with a
 * single update or to update a single column in one or a range of objects. You
 * need to construct a special DTO that contains just the parameters along with
 * the IUpdateSql strategy object that processes the parameter values. The
 * validators validate the parameters, but there are no nested objects and
 * therefore no setters executed for this update. The status of the DTO is not
 * relevant either; the update will always happen.
 * </p>
 * <p>
 * <em>
 * Note: There is no implementation of the IUpdateWithParameters interface that
 * takes the DTO cache into account, as this command is not related to DTOs
 * specifically but rather is intended for use in database maintenance. If the
 * updated data affects DTOs in some way, the client must take care of the
 * cache by removing those DTOs from the cache.
 * </em>
 * </p>
 * 
 * @see com.poesys.db.dto.AbstractDto
 * @see com.poesys.db.dto.IDbDto
 * @see IUpdateSql
 * 
 * @author Robert J. Muller
 * @param <P> the type of DTO that contains the parameters for the update
 */
public class UpdateWithParameters<P extends IDbDto> implements
    IUpdateWithParameters<P> {
  private static final Logger logger =
    Logger.getLogger(UpdateWithParameters.class);
  /** Internal Strategy-pattern object containing the SQL query */
  private IUpdateSql<P> sql;
  private final String subsystem;

  /** timeout for the cache thread */
  private static final int TIMEOUT = 1000 * 60;

  /** Error message when no DTO supplied */
  private static final String NO_DTO_ERROR =
    "com.poesys.db.dao.update.msg.no_dto";
  /** Error message when update gets a SQL exception */
  private static final String SQL_ERROR =
    "com.poesys.db.dto.msg.unexpected_sql_error";
  /** Error message when no SQL object supplied */
  private static final String NULL_SQL_ERROR = "com.poesys.db.dao.msg.null_sql";
  /** Error message when no subsystem supplied */
  private static final String NULL_SUBSYSTEM_ERROR =
    "com.poesys.db.dao.msg.null_subsystem";
  /** Error message when thread is interrupted or timed out */
  private static final String THREAD_ERROR = "com.poesys.db.dao.msg.thread";

  /**
   * Create an UpdateWithParameters object by supplying the concrete
   * implementation of the SQL-statement generator and JDBC setter.
   * 
   * @param sql the SQL UPDATE statement specification
   * @param subsystem the database subsystem for the DTO being processed
   */
  public UpdateWithParameters(IUpdateSql<P> sql, String subsystem) {
    if (sql == null) {
      throw new InvalidParametersException(Message.getMessage(NULL_SQL_ERROR,
                                                              null));
    }
    if (subsystem == null) {
      throw new InvalidParametersException(Message.getMessage(NULL_SUBSYSTEM_ERROR,
                                                              null));
    }
    this.sql = sql;
    this.subsystem = subsystem;
  }

  @Override
  public void update(P parameters) {
    if (parameters == null) {
      throw new InvalidParametersException(Message.getMessage(NO_DTO_ERROR,
                                                              null));
    } else if (sql != null) {
      // If the current thread is a PoesysTrackingThread, just process in that
      // thread; if not, start a new thread.
      if (Thread.currentThread() instanceof PoesysTrackingThread) {
        doUpdate(parameters, (PoesysTrackingThread)Thread.currentThread());
      } else {
        Runnable process = new Runnable() {
          public void run() {
            PoesysTrackingThread thread =
              (PoesysTrackingThread)Thread.currentThread();
            try {
              doUpdate(parameters, thread);
            } catch (Throwable e) {
              thread.setThrowable(e);
            } finally {
              thread.closeConnection();
            }
          }
        };
        PoesysTrackingThread thread =
          new PoesysTrackingThread(process, subsystem);
        thread.start();

        // Join the thread, blocking until the thread completes or
        // until the query times out.
        try {
          thread.join(TIMEOUT);
          // Check for problems.
          if (thread.getThrowable() != null) {
            Object[] args =
              { "delete", parameters.getPrimaryKey().getStringKey() };
            String message = Message.getMessage(THREAD_ERROR, args);
            logger.error(message, thread.getThrowable());
            throw new DbErrorException(message, thread.getThrowable());
          }
        } catch (InterruptedException e) {
          Object[] args =
            { "delete", parameters.getPrimaryKey().getStringKey() };
          String message = Message.getMessage(THREAD_ERROR, args);
          logger.error(message, e);
        }
      }
    }
  }

  /**
   * Do the update based on the parameters object and the current thread.
   * 
   * @param parameters the update parameters
   * @param thread the current tracking thread with the SQL connection
   */
  private void doUpdate(P parameters, PoesysTrackingThread thread) {
    PreparedStatement stmt = null;

    if (parameters == null) {
      throw new InvalidParametersException(NO_DTO_ERROR);
    } else {
      parameters.validateForUpdate();
    }

    try {
      IPrimaryKey key = parameters.getPrimaryKey();
      String sqlStmt = sql.getSql(key);
      if (sqlStmt != null) {
        stmt = thread.getConnection().prepareStatement(sqlStmt);
        sql.setParams(stmt, 1, parameters);

        logger.debug("Executing update with parameters key " + key);
        logger.debug("SQL: " + sqlStmt);

        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      parameters.setFailed();
      throw new DbErrorException(Message.getMessage(SQL_ERROR, null), thread, e);
    } catch (RuntimeException e) {
      parameters.setFailed();
      throw e;
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
}
