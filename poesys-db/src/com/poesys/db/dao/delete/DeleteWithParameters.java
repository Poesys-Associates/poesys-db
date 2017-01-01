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
package com.poesys.db.dao.delete;


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
 * An implementation of the IDeleteWithParameters interface that deletes data
 * from the database with a single DELETE statement parameterized with the
 * contents of a data transfer object. Use this class to delete arbitrary data
 * (as opposed to DTOs) with a single delete. You need to construct a special
 * DTO that contains just the parameters along with the IDeleteSql helper that
 * processes the parameter values. The validators validate the parameters, but
 * there are no nested objects and therefore no setters executed for this
 * delete. The status of the DTO is not relevant either; the delete will always
 * happen.
 * </p>
 * <p>
 * <em>
 * Note: There is no implementation of the IDeleteWithParameters interface that
 * takes the DTO cache into account, as this command is not related to DTOs
 * specifically but rather is intended for use in database maintenance. <strong>If the
 * deleted data affects DTOs in some way, the client must take care of the
 * cache by removing those DTOs from the cache.</strong>
 * </em>
 * </p>
 * 
 * @see IDeleteSql
 * 
 * @author Robert J. Muller
 * @param <T> the type of DTO to delete
 * @param <P> the type of DTO that contains the identifying parameters
 */
public class DeleteWithParameters<T extends IDbDto, P extends IDbDto>
    implements IDeleteWithParameters<T, P> {
  private static final Logger logger =
    Logger.getLogger(DeleteWithParameters.class);
  /** The strategy-pattern object for the SQL statement */
  private final IDeleteSqlWithParameters<T, P> sql;
  /** the subsystem of the objects to delete */
  private final String subsystem;
  /** timeout for the cache thread */
  private static final int TIMEOUT = 1000 * 60;

  /** Error message when no DTO supplied */
  private static final String NO_DTO_ERROR =
    "com.poesys.db.dao.delete.msg.no_dto";
  /** Error message when delete throws exception */
  private static final String DELETE_ERROR =
    "com.poesys.db.dao.delete.msg.delete";
  /** Error message when thread is interrupted or timed out */
  private static final String THREAD_ERROR = "com.poesys.db.dao.msg.thread";

  /**
   * Create a DeleteWithParameters object by supplying the concrete
   * implementation of the SQL-statement generator and JDBC setter.
   * 
   * @param sql the SQL DELETE statement generator object
   * @param subsystem the subsystem of the T DTO class
   */
  public DeleteWithParameters(IDeleteSqlWithParameters<T, P> sql,
                              String subsystem) {
    this.sql = sql;
    this.subsystem = subsystem;
  }

  @Override
  public void delete(P parameters) {
    if (parameters == null) {
      throw new InvalidParametersException(NO_DTO_ERROR);
    } else if (sql != null) {
      // If the current thread is a PoesysTrackingThread, just process in that
      // thread; if not, start a new thread.
      if (Thread.currentThread() instanceof PoesysTrackingThread) {
        doDelete(parameters, (PoesysTrackingThread)Thread.currentThread());
      } else {
        Runnable process = new Runnable() {
          public void run() {
            PoesysTrackingThread thread =
              (PoesysTrackingThread)Thread.currentThread();
            try {
              doDelete(parameters, thread);
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
            Object[] args = { "delete", "collection of DTOs" };
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
   * Do the delete operation on the database as specified by the parameters.
   * 
   * @param parameters the parameters DTO
   * @param thread the tracking thread
   */
  private void doDelete(P parameters, PoesysTrackingThread thread) {
    PreparedStatement stmt = null;

    if (parameters == null) {
      throw new InvalidParametersException(Message.getMessage(NO_DTO_ERROR,
                                                              null));
    } else {
      parameters.validateForDelete();
    }

    String sqlText = null;

    try {
      IPrimaryKey key = parameters.getPrimaryKey();
      sqlText = sql.getSql(key);
      stmt = thread.getConnection().prepareStatement(sqlText);
      logger.debug("Deleting with parameters: " + sqlText);
      logger.debug(sql.getParamString(parameters));
      sql.setParams(stmt, 1, parameters);
      stmt.executeUpdate();
    } catch (SQLException e) {
      Object[] args = { parameters.getPrimaryKey().getStringKey() };
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
    // Note that parameter-based delete does not affect caches at all.
  }
}
