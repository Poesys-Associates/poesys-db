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


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.poesys.db.BatchException;
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
  private static final String NO_DTO_MSG =
    "com.poesys.db.dao.delete.msg.no_dto";
  /** Error message when post-processing throws an exception */
  private static final String POST_PROCESSING_ERROR =
    "com.poesys.db.dao.delete.msg.postprocessing";
  /** Error message when thread is interrupted or timed out */
  private static final String THREAD_ERROR = "com.poesys.db.dao.msg.thread";
  /** timeout for the cache thread */
  private static final int TIMEOUT = 1000 * 60;

  /** The Strategy-pattern object for the SQL statement */
  IDeleteSql<T> sql;

  /**
   * Create a DeleteByKey object by supplying the concrete implementation of the
   * SQL-statement generator and JDBC setter.
   * 
   * @param sql the SQL UPDATE statement generator object
   */
  public DeleteByKey(IDeleteSql<T> sql) {
    this.sql = sql;
  }

  @Override
  public void delete(Connection connection, T dto) throws SQLException,
      BatchException {
    PreparedStatement stmt = null;

    if (dto == null) {
      throw new InvalidParametersException(NO_DTO_MSG);
    } else if (dto.getStatus() == IDbDto.Status.DELETED && sql != null) {
      dto.validateForDelete();
      dto.preprocessNestedObjects(connection);

      String sqlText = null;

      try {
        IPrimaryKey key = dto.getPrimaryKey();
        sqlText = sql.getSql(key);
        stmt = connection.prepareStatement(sqlText);
        logger.debug("Delete by key: " + sqlText);
        sql.setParams(stmt, 1, dto);
        logger.debug("Key: " + key.getStringKey());

        stmt.executeUpdate();

        PoesysTrackingThread thread = null;

        // If the current thread is a PoesysTrackingThread, just postprocess in
        // that
        // thread; if not, start a new thread for the postprocessing.
        if (Thread.currentThread() instanceof PoesysTrackingThread) {
          thread = (PoesysTrackingThread)Thread.currentThread();
          if (thread.getDto(dto.getPrimaryKey().getStringKey()) == null) {
            // DTO not in history, add it so we can track processing.
            thread.addDto(dto);
          }
          thread.setProcessed(dto.getPrimaryKey().getStringKey(), true);
          postprocess(connection, dto);
        } else {
          Runnable process = new Runnable() {
            public void run() {
              try {
                postprocess(connection, dto);
              } catch (SQLException e) {
                // Log and let the thread complete immediately
                Object[] args = { dto.getPrimaryKey().getStringKey() };
                String message =
                  Message.getMessage(POST_PROCESSING_ERROR, args);
                logger.error(message, e);
                throw new RuntimeException(message, e);
              } catch (BatchException e) {
                // Log and let the thread complete immediately
                Object[] args = { dto.getPrimaryKey().getStringKey() };
                String message =
                  Message.getMessage(POST_PROCESSING_ERROR, args);
                logger.error(message, e);
                throw new RuntimeException(message, e);
              }
            }
          };
          thread = new PoesysTrackingThread(process);
          thread.addDto(dto);
          thread.setProcessed(dto.getPrimaryKey().getStringKey(), true);
          thread.start();

          // Join the thread, blocking until the thread completes or
          // until the query times out.
          try {
            thread.join(TIMEOUT);
          } catch (InterruptedException e) {
            Object[] args = {"insert", dto.getPrimaryKey().getStringKey()};
            String message = Message.getMessage(THREAD_ERROR, args);
            logger.error(message, e);
          }
        }
        postprocess(connection, dto);
        // Notify DTO to update its observer parents of the delete.

        dto.notify(DataEvent.DELETE);
      } catch (SQLException e) {
        dto.setFailed();
        throw e;
      } catch (RuntimeException e) {
        dto.setFailed();
        throw e;
      } finally {
        if (stmt != null) {
          stmt.close();
        }
      }
    } else if (dto.getStatus() == IDbDto.Status.CASCADE_DELETED) {
      // Just notify DTO to update its observer parents of the delete.
      dto.notify(DataEvent.DELETE);
    }
  }

  /**
   * Post-process the nested objects of a DTO. Override this method if you want
   * to add information such as the session ID.
   * 
   * @param connection the JDBC connection
   * @param dto the DTO containing the nested objects
   * @throws SQLException when there is a database problem
   * @throws BatchException when there is a batch processing problem
   */
  protected void postprocess(Connection connection, T dto) throws SQLException,
      BatchException {
    dto.postprocessNestedObjects(connection);
  }

  @Override
  public void close() {
    // Nothing to do
    // Note that parameter-based delete does not affect caches at all.
  }
}
