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
package com.poesys.db.dao.insert;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.poesys.db.DbErrorException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.Message;
import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An implementation of the IInsert generic interface that contains the base
 * code for inserting a single object into the database. To use this class, you
 * must implement the IInsertSql interface for the concrete class that contains
 * the SQL INSERT statement and the JDBC code to set the fields in the DTO from
 * the JDBC result set, then pass that object into the Insert constructor. You
 * should supply a validator that checks for a primary key.
 * </p>
 * 
 * @see IInsertSql
 * @see com.poesys.db.dto.HasPrimaryKey
 * @see com.poesys.db.dto.AbstractDto
 * @see com.poesys.db.dto.IDbDto
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to insert
 */
public class Insert<T extends IDbDto> implements IInsert<T> {
  /** The logger for this class. */
  private static final Logger logger = Logger.getLogger(Insert.class);
  /** The helper class for generating the SQL statement */
  private IInsertSql<T> sql;
  /** Error message when no DTO supplied */
  private static final String NO_DTO_ERROR =
    "com.poesys.db.dao.insert.msg.no_dto";
  /** Error message when DTO has no primary key */
  private static final String NO_KEY_ERROR =
    "com.poesys.db.dao.insert.msg.no_primary_key_for_insert";
  /** Error message when insert throws exception */
  private static final String INSERT_ERROR =
    "com.poesys.db.dao.insert.msg.insert";
  private static final String THREAD_ERROR = "com.poesys.db.dao.msg.thread";

  /** timeout for the query thread */
  private static final int TIMEOUT = 1000 * 60;

  /** the subsystem of class T */
  protected final String subsystem;

  /**
   * Create an Insert object by supplying the concrete implementation of the
   * SQL-statement generator and JDBC setter.
   * 
   * @param sql the SQL INSERT statement generator object for type T
   * @param subsystem the subsystem of class T
   */
  public Insert(IInsertSql<T> sql, String subsystem) {
    this.sql = sql;
    this.subsystem = subsystem;
  }

  @Override
  public void insert(IDbDto dto) {

    // Check that the DTO is there and is new.
    if (dto == null) {
      throw new InvalidParametersException(Message.getMessage(NO_DTO_ERROR,
                                                              null));
    } else if (dto.getStatus() == IDbDto.Status.NEW) {
      // Process NEW DTOs only
      // Check for the separate thread and create it if it's not already there.
      if (Thread.currentThread() instanceof PoesysTrackingThread) {
        insertInDatabase(dto);
      } else {
        Runnable query = new Runnable() {
          public void run() {
            try {
              insertInDatabase(dto);
            } finally {
              PoesysTrackingThread thread =
                (PoesysTrackingThread)Thread.currentThread();
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
          Object[] args = { "insert", dto.getPrimaryKey().getStringKey() };
          String message = Message.getMessage(THREAD_ERROR, args);
          logger.error(message, e);
        }
      }
    }
  }

  /**
   * Insert a DTO into the database. You can call this only from within a
   * PoesysTrackingThread.
   * 
   * @param dto the DTO to insert
   */
  @SuppressWarnings("unchecked")
  private void insertInDatabase(IDbDto dto) {
    PreparedStatement stmt = null;
    PoesysTrackingThread thread = (PoesysTrackingThread)Thread.currentThread();
    Connection connection = thread.getConnection();

    // Only process if not already processed within this thread
    if (!thread.isProcessed(dto.getPrimaryKey().getStringKey())) {
      try {
        // Query nested objects to be able to use them in validation.
        dto.queryNestedObjectsForValidation();
        dto.validateForInsert();

        // Get the primary key.
        IPrimaryKey key = dto.getPrimaryKey();

        // Test the key to make sure there is one.
        if (key == null) {
          throw new InvalidParametersException(Message.getMessage(NO_KEY_ERROR,
                                                                  null));
        }

        stmt =
          connection.prepareStatement(sql.getSql(key),
                                      Statement.RETURN_GENERATED_KEYS);
        int next = setKeyParams(stmt, key);
        sql.setParams(stmt, next, (T)dto);
        // Log the insert.
        logger.debug("Inserting object with key " + key);
        logger.debug("SQL: " + sql.getSql(key));
        stmt.executeUpdate();
        // Finalize the insert by setting any auto-generated values.
        key.finalizeInsert(stmt);
        // Finalize the insert by setting any auto-generated attributes.
        dto.finalizeInsert(stmt);

        // Set processed flag to avoid further processing of the inserted
        // object.
        if (thread.getDto(dto.getPrimaryKey().getStringKey()) == null) {
          // Not in thread yet, add it to set processed flag.
          thread.addDto(dto);
        }
        thread.setProcessed(key.getStringKey(), true);

        /*
         * For a concrete class, insert any nested objects (composite children
         * or associations) Only need to insert here, not update or delete, as
         * parent is being inserted.
         */
        if (!dto.isAbstractClass()) {
          dto.insertNestedObjects();
        }
      } catch (SQLException e) {
        dto.setFailed();
        Object[] args = { dto.getPrimaryKey().getStringKey() };
        String message = Message.getMessage(INSERT_ERROR, args);
        logger.error(message, e);
        throw new DbErrorException(message, thread, e);
      } catch (DbErrorException e) {
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
    }
  }

  /**
   * Set the key parameters. This method can be overriden in a concrete subclass
   * that does not set key parameters, for auto-generated keys for example.
   * 
   * @param stmt the prepared statement into which to set the parameters
   * @param key the primary key object that contains the parameter-setting logic
   * @return the next parameter number to set
   */
  protected int setKeyParams(PreparedStatement stmt, IPrimaryKey key) {
    int next;
    next = key.setInsertParams(stmt, 1);
    return next;
  }

  @Override
  public void close() {
    // Nothing to do
  }
}
