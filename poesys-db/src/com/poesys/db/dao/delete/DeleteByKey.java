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
import com.poesys.db.dao.DataEvent;
import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * An implementation of the IDelete interface that updates an object,
 * identifying the object in the database using the primary key of the data
 * transfer object (DTO). Note that the SQL statement can be null for deletes
 * that are done through the database rather than through the application.
 * 
 * @see IDeleteSql
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to delete
 */
public class DeleteByKey<T extends IDbDto> implements IDelete<T> {
  private static final Logger logger = Logger.getLogger(DeleteByKey.class);

  /** Error message when no DTO supplied */
  private static final String NO_DTO_ERROR =
    "com.poesys.db.dao.delete.msg.no_dto";
  /** Error message when delete throws exception */
  private static final String DELETE_ERROR =
    "com.poesys.db.dao.delete.msg.delete";
  /** Error message when thread is interrupted or timed out */
  private static final String THREAD_ERROR = "com.poesys.db.dao.msg.thread";
  /** timeout for the cache thread */
  private static final int TIMEOUT = 1000 * 60;

  /** The Strategy-pattern object for the SQL statement */
  IDeleteSql<T> sql;

  protected final String subsystem;

  /**
   * Create a DeleteByKey object by supplying the concrete implementation of the
   * SQL-statement generator and JDBC setter.
   * 
   * @param sql the SQL UPDATE statement generator object
   * @param subsystem the database subsystem for the DTO being processed
   */
  public DeleteByKey(IDeleteSql<T> sql, String subsystem) {
    this.sql = sql;
    this.subsystem = subsystem;
  }

  @Override
  public void delete(T dto) {
    if (dto == null) {
      throw new InvalidParametersException(Message.getMessage(NO_DTO_ERROR,
                                                              null));
    } else if (dto.getStatus() == IDbDto.Status.DELETED && sql != null) {
      // If the current thread is a PoesysTrackingThread, just process in that
      // thread; if not, start a new thread.
      if (Thread.currentThread() instanceof PoesysTrackingThread) {
        doDelete(dto);
        postprocess(dto);
      } else {
        Runnable process = new Runnable() {
          public void run() {
            PoesysTrackingThread thread =
              (PoesysTrackingThread)Thread.currentThread();
            try {
              doDelete(dto);
              postprocess(dto);
            } catch (Exception e) {
              Object[] args = { "delete", dto.getPrimaryKey().getStringKey() };
              String message = Message.getMessage(THREAD_ERROR, args);
              logger.error(message, e);
              throw e;
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
        } catch (InterruptedException e) {
          Object[] args = { "delete", dto.getPrimaryKey().getStringKey() };
          String message = Message.getMessage(THREAD_ERROR, args);
          logger.error(message, e);
        }
      }
      dto.notify(DataEvent.DELETE);
    } else if (dto.getStatus() == IDbDto.Status.CASCADE_DELETED) {
      // Just notify DTO to update its observer parents of the delete.
      dto.notify(DataEvent.DELETE);
    }
  }

  /**
   * Execute the delete.
   * 
   * @param dto the DTO to delete
   */
  private void doDelete(T dto) {
    PreparedStatement stmt = null;
    String sqlText = null;
    PoesysTrackingThread thread = (PoesysTrackingThread)Thread.currentThread();
    IPrimaryKey key = dto.getPrimaryKey();
    sqlText = sql.getSql(key);

    try {
      if (thread.getDto(dto.getPrimaryKey().getStringKey()) == null) {
        // DTO not in history, add it so we can track processing.
        thread.addDto(dto);
      }

      dto.validateForDelete();
      dto.preprocessNestedObjects();

      stmt = thread.getConnection().prepareStatement(sqlText);
      logger.debug("Delete by key: " + sqlText);
      sql.setParams(stmt, 1, dto);
      logger.debug("Key: " + key.getStringKey());

      stmt.executeUpdate();
    } catch (SQLException e) {
      Object[] args = { dto.getPrimaryKey().getStringKey() };
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

  /**
   * Post-process the nested objects of a DTO. Override this method if you want
   * to add information such as the session ID.
   * 
   * @param dto the DTO containing the nested objects
   */
  protected void postprocess(T dto) {
    String keyString = dto.getPrimaryKey().getStringKey();
    PoesysTrackingThread thread = (PoesysTrackingThread)Thread.currentThread();
    if (!thread.isProcessed(keyString)) {
      dto.postprocessNestedObjects();
      thread.setProcessed(keyString, true);
    }
  }

  @Override
  public void close() {
    // Nothing to do
    // Note that parameter-based delete does not affect caches at all.
  }
}
