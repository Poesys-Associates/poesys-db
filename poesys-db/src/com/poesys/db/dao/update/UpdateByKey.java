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
 * An implementation of the IUpdate interface that updates a data transfer
 * object (DTO) using a SQL statement defined in an IUpdateSql object,
 * identifying the object in the database using the primary key of the DTO. The
 * implementation should update in the database only if isChanged() is true. The
 * caller should set the DTO status to existing once <strong>all</strong>
 * processing is complete (over the entire inheritance hierarchy).
 * 
 * @see com.poesys.db.dto.AbstractDto
 * @see IUpdateSql
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to update
 */
public class UpdateByKey<T extends IDbDto> implements IUpdate<T> {
  private static final Logger logger = Logger.getLogger(UpdateByKey.class);
  /** Internal Strategy-pattern object containing the SQL query */
  private IUpdateSql<T> sql;
  /** Error message when no DTO supplied */
  private static final String NO_DTO_ERROR =
    "com.poesys.db.dao.update.msg.no_dto";
  /** Error message when insert throws exception */
  private static final String UPDATE_ERROR =
    "com.poesys.db.dao.update.msg.update";
  private static final String THREAD_ERROR = "com.poesys.db.dao.msg.thread";

  /** timeout for the cache thread */
  private static final int TIMEOUT = 1000 * 60;

  /** the subsystem of class T */
  protected final String subsystem;

  /**
   * Create an UpdateByKey object by supplying the concrete implementation of
   * the SQL-statement generator and JDBC setter.
   * 
   * @param sql the SQL UPDATE statement specification
   * @param subsystem of class T
   */
  public UpdateByKey(IUpdateSql<T> sql, String subsystem) {
    this.sql = sql;
    this.subsystem = subsystem;
  }

  @Override
  public void update(T dto) {
    // Check that the DTO is there and is CHANGED.
    if (dto == null) {
      throw new InvalidParametersException(Message.getMessage(NO_DTO_ERROR,
                                                              null));
    } else if (dto.getStatus() == IDbDto.Status.CHANGED) {
      // Process CHANGED DTOs only
      // Check for the tracking thread and create it if it's not already there.
      if (Thread.currentThread() instanceof PoesysTrackingThread) {
        PoesysTrackingThread thread =
          (PoesysTrackingThread)Thread.currentThread();
        // Process only if not already processed
        if (thread.isProcessed(dto.getPrimaryKey().getStringKey())) {
          updateInDatabase(dto);
        }
      } else {
        Runnable query = new Runnable() {
          public void run() {
            PoesysTrackingThread thread =
              (PoesysTrackingThread)Thread.currentThread();
            try {
              updateInDatabase(dto);
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
        } catch (InterruptedException e) {
          Object[] args = { "update", dto.getPrimaryKey().getStringKey() };
          String message = Message.getMessage(THREAD_ERROR, args);
          logger.error(message, e);
        }
      }

    }
  }

  /**
   * Update the DTO in the database. This method must only be called for a DTO
   * that has status CHANGED and has not already been processed.
   * 
   * @param dto the DTO to update in the database
   */
  private void updateInDatabase(T dto) {
    PoesysTrackingThread thread = (PoesysTrackingThread)Thread.currentThread();
    PreparedStatement stmt = null;

    if (dto == null) {
      throw new InvalidParametersException(NO_DTO_ERROR);
    }

    String sqlStmt = null;

    try {
      dto.validateForUpdate();
      dto.preprocessNestedObjects();
      IPrimaryKey key = dto.getPrimaryKey();
      sqlStmt = sql.getSql(key);
      if (sqlStmt != null) {
        stmt = thread.getConnection().prepareStatement(sqlStmt);
        sql.setParams(stmt, 1, dto);

        logger.debug("Executing update with key " + key);
        logger.debug("SQL: " + sqlStmt);
        logger.debug(sql.getParamString(dto));

        stmt.executeUpdate();

        // Note that the caller must set the DTO status to EXISTING once ALL
        // processing is complete (over the entire inheritance hierarchy).
      }
    } catch (SQLException e) {
      dto.setFailed();
      Object[] args = { dto.getPrimaryKey().getStringKey() };
      String message = Message.getMessage(UPDATE_ERROR, args);
      logger.error(message, e);
      throw new DbErrorException(message, thread, e);
    } catch (RuntimeException e) {
      dto.setFailed();
      throw e;
    } finally {
      // Close the statement as required.
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          // ignore
        }
      }
    }

    // Add the DTO to the tracking thread if not tracked.
    if (thread.getDto(dto.getPrimaryKey().getStringKey()) == null) {
      thread.addDto(dto);
    }
    // Process the nested objects.
    postprocess(dto);
    // Set the object as processed.
    thread.setProcessed(dto.getPrimaryKey().getStringKey(), true);
  }

  /**
   * Post-process the nested objects of a DTO. Override this method to supply
   * extra information like a session ID.
   * 
   * @param dto the DTO containing the nested objects
   */
  protected void postprocess(T dto) {
    dto.postprocessNestedObjects();
  }

  @Override
  public void close() {
    // Nothing to do
  }
}
