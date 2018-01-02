/*
 * Copyright (c) 2016 Poesys Associates. All rights reserved.
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
package com.poesys.db.dao;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.poesys.db.DbErrorException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.Message;
import com.poesys.db.connection.ConnectionFactoryFactory;
import com.poesys.db.connection.IConnectionFactory;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * A Thread object that contains the retrieval and processing state of a
 * Poesys/DB object tree retrieval. The class tracks the set of retrieved
 * objects as a history of objects retrieved. Each object is in a container
 * object that has attributes related to Poesys/DB processing. The Thread
 * subclass thus provides a container for operations involving multiple objects
 * and provides a place outside the objects to track processing. The thread
 * class also contains a SQL exception that objects running in the thread can
 * use to query the database through Poesys/DB DAOs. It also contains a
 * Throwable object that gets set if the tracking thread throws an exception;
 * the thread itself finishes normally but sets this Throwable so the main
 * thread can test for any problem and get the thrown exception.
 * </p>
 * <p>
 * Note: the Runnable you pass in must have a finally block in the run() method
 * that calls the closeConnection() method on the thread.
 * 
 * @author Robert J. Muller
 */
public class PoesysTrackingThread extends Thread {
  /** Logger for debugging */
  private static final Logger logger =
    Logger.getLogger(PoesysTrackingThread.class);

  /**
   * map of DTOs indexed by global primary key (string version of DTO primary
   * key)
   */
  private final Map<String, DtoTrackingObject> history =
    new HashMap<String, DtoTrackingObject>();

  /** the database connection */
  private final Connection connection;

  /** the optional exception thrown by the thread before finishing */
  private Throwable throwable = null;

  // Error messages

  /** No cached DTO error */
  private static final String NO_DTO_ERROR =
    "com.poesys.db.dao.query.msg.no_cached_dto_error";
  /** SQL exception initializing connection */
  private static final String SQL_EXCEPTION_ERROR =
    "com.poesys.db.dao.msg.connection_sql";
  /** IO exception initializing connection */
  private static final String IO_ERROR_ERROR =
    "com.poesys.db.dao.msg.connection_io";
  /** SQL error for operation */
  private static final String SQL_ERROR =
    "com.poesys.db.dto.msg.unexpected_sql_error";
  /** Invalid parameters to the connection factory */
  private static final String INVALID_PARAMETERS_ERROR =
    "com.poesys.db.dao.msg.connection_invalid_parameters";
  private static final String NO_KEY_ERROR =
    "com.poesys.db.dto.msg.no_primary_key";

  /**
   * Create a PoesysTrackingThread object with a task.
   *
   * @param target the Runnable task
   * @param subsystem the database subsystem for the DTO being processed
   */
  public PoesysTrackingThread(Runnable target, String subsystem) {
    super(target);
    logger.debug("Starting new tracking thread " + getId());
    connection = initConnection(subsystem);
  }

  /**
   * Create a CachedThread object with a name.
   *
   * @param name the thread name
   * @param subsystem the database subsystem for the DTO being processed
   */
  public PoesysTrackingThread(String name, String subsystem) {
    super(name);
    connection = initConnection(subsystem);
  }

  /**
   * Create a CachedThread object within a group.
   *
   * @param group the group of threads
   * @param target the Runnable task
   * @param subsystem the database subsystem for the DTO being processed
   */
  public PoesysTrackingThread(ThreadGroup group,
                              Runnable target,
                              String subsystem) {
    super(group, target);
    connection = initConnection(subsystem);
  }

  /**
   * Create a named CachedThread object within a group.
   *
   * @param group a group of threads
   * @param name the thread name
   * @param subsystem the database subsystem for the DTO being processed
   */
  public PoesysTrackingThread(ThreadGroup group, String name, String subsystem) {
    super(group, name);
    connection = initConnection(subsystem);
  }

  /**
   * Create a named CachedThread object with a Runnable task.
   *
   * @param target the task
   * @param name the thread name
   * @param subsystem the database subsystem for the DTO being processed
   */
  public PoesysTrackingThread(Runnable target, String name, String subsystem) {
    super(target, name);
    connection = initConnection(subsystem);
  }

  /**
   * Create a named CachedThread object with a Runnable task within a group.
   *
   * @param group the thread group
   * @param target the task
   * @param name the thread name
   * @param subsystem the database subsystem for the DTO being processed
   */
  public PoesysTrackingThread(ThreadGroup group,
                              Runnable target,
                              String name,
                              String subsystem) {
    super(group, target, name);
    connection = initConnection(subsystem);
  }

  /**
   * Create a named CachedThread object with a Runnable task within a group with
   * a specific stack size.
   *
   * @param group the thread group
   * @param target the task
   * @param name the thread name
   * @param stackSize integer, size of the thread stack
   * @param subsystem the database subsystem for the DTO being processed
   */
  public PoesysTrackingThread(ThreadGroup group,
                              Runnable target,
                              String name,
                              long stackSize,
                              String subsystem) {
    super(group, target, name, stackSize);
    connection = initConnection(subsystem);
  }

  /**
   * Get a DTO from the internal tracking data, or null if the DTO has not been
   * retrieved from the cache.
   * 
   * @param key the globally unique primary key
   * @return the DTO, or null if not yet retrieved in this thread
   */
  public IDbDto getDto(IPrimaryKey key) {
    IDbDto dto = null;
    DtoTrackingObject obj = history.get(key.getStringKey());
    if (obj != null) {
      dto = obj.getDto();
    }
    return dto;
  }

  /**
   * Initialize a database connection to a Poesys/DB subsystem
   * 
   * @param subsystem the subsystem name
   * @return the connection
   */
  private Connection initConnection(String subsystem) {
    Connection connection = null;
    try {
      IConnectionFactory factory =
        ConnectionFactoryFactory.getInstance(subsystem);
      connection = factory.getConnection();
    } catch (InvalidParametersException e) {
      String message = Message.getMessage(INVALID_PARAMETERS_ERROR, null);
      logger.error(message, e);
      throw new DbErrorException(message, e);
    } catch (IOException e) {
      String message = Message.getMessage(IO_ERROR_ERROR, null);
      logger.error(message, e);
      throw new DbErrorException(message, e);
    } catch (SQLException e) {
      String message = Message.getMessage(SQL_EXCEPTION_ERROR, null);
      logger.error(message, e);
      throw new DbErrorException(message, e);
    }
    return connection;
  }

  /**
   * Add a DTO to the cached DTO retrieval history for this thread.
   * 
   * @param dto the DTO to add
   */
  public void addDto(IDbDto dto) {
    if (dto == null) {
      throw new InvalidParametersException(Message.getMessage(NO_DTO_ERROR,
                                                              null));
    }
    DtoTrackingObject obj = new DtoTrackingObject(dto);
    try {
      history.put(dto.getPrimaryKey().getStringKey(), obj);
      logger.debug("Tracked DTO " + dto.getPrimaryKey().getStringKey());
    } catch (Throwable e) {
      logger.warn("Warning: exception adding tracking object to history", e);
      if (history == null) {
        logger.warn("Null history in tracking thread");
      }
      if (dto.getPrimaryKey() == null) {
        logger.warn("Null DTO primary key in tracking thread");
      }
      if (dto.getPrimaryKey().getStringKey() == null) {
        logger.warn("Null DTO primary key string in tracking thread");
      }
    }
  }

  /**
   * Has a specified DTO been processed? If the DTO is not in the history, the
   * method returns false.
   * 
   * @param key the primary key for the DTO to test
   * @return true if processed, false if not or not in history
   */
  public boolean isProcessed(IPrimaryKey key) {
    boolean processed = false;
    DtoTrackingObject obj = history.get(key.getStringKey());
    if (obj != null) {
      processed = obj.isProcessed;
    } else {
      logger.debug(key.getStringKey() + " not in history");
    }
    logger.debug("Check processed flag in thread " + getId() + " for DTO "
                 + key.getStringKey() + ": " + processed);

    return processed;
  }

  /**
   * Mark a DTO in the retrieval history as processed; if the DTO is not in the
   * history, add it.
   * 
   * @param dto the DTO to mark
   * @param processed true for processed, false for not processed
   */
  public void setProcessed(IDbDto dto, boolean processed) {
    IPrimaryKey key = dto.getPrimaryKey();
    if (key == null) {
      String message = Message.getMessage(NO_KEY_ERROR, null);
      throw new DbErrorException(message);
    }

    DtoTrackingObject object = history.get(key.getStringKey());

    if (object == null) {
      // Not yet tracked, add dto to history before setting processed
      addDto(dto);
      object = history.get(key.getStringKey());
    }

    if (!processed && dto.getPrimaryKey().getStringKey().contains("Checking")) {
      logger.debug("Setting Checking account processed flag false.");
    }
    object.setProcessed(processed);
    logger.debug("Set processed flag in thread " + getId() + " for DTO "
                 + key.getStringKey() + " to value " + processed);
  }

  /**
   * Get the thread's SQL connection.
   * 
   * @return a connection
   */
  public Connection getConnection() {
    return connection;
  }

  /**
   * Close the SQL connection. You should call this method as the last method
   * call for the thread object, usually in a finally clause in the run()
   * method. The method commits the transaction, then closes the database. You
   * should roll back the transaction in the error handling code before calling
   * this method, as appropriate.
   */
  public void closeConnection() {
    if (connection != null) {
      try {
        logger.debug("Committing transaction and closing connection "
                     + connection.hashCode());
        connection.commit();
        connection.close();
      } catch (SQLException e) {
        // log and ignore
        logger.error(SQL_ERROR + " committing and closing connection "
                     + connection.hashCode(), e);
      }
    }
  }

  /**
   * Roll back the current transaction in the thread.
   */
  public void rollback() {
    if (connection != null) {
      try {
        logger.debug("Rolling back transaction and closing connection "
                     + connection.hashCode());
        connection.rollback();
      } catch (SQLException e) {
        // log and ignore
        logger.error(SQL_ERROR + " rolling back connection "
                         + connection.hashCode(),
                     e);
      }
    }
  }

  /**
   * Process any errors from a batch of DTOs of type T. The JDBC batch
   * processing sets an array of integers with codes, and this method determines
   * which DTOs have errors and sets the message in the DTO. The logic is a
   * little involved because of the way JDBC constructs the array. It will have
   * an empty array if processing fails on the first DTO; it will have an array
   * shorter than the list of DTOs if the error caused batch processing to stop
   * on a DTO other than the first DTO; or it will have an array equal to the
   * length of the DTO list if processing completed but there were errors.
   * 
   * @param codes the JDBC error code array
   * @param dtos the batch of DTOs
   */
  public void processErrors(int[] codes, Collection<IDbDto> dtos) {
    // Process only if there are codes and DTOs.
    if (codes != null && codes.length > 0 && dtos != null && dtos.size() > 0) {
      // Check the sizes of the two arrays.
      if (codes.length != dtos.size()) {
        // Statement processing stopped at the first error, get failed DTO
        IDbDto dto = (IDbDto)dtos.toArray()[codes.length];
        failDto(dto, codes[codes.length - 1]);
      } else {
        // Statement processing continued, check for FAILED
        int i = 0;
        for (IDbDto dto : dtos) {
          if (codes[i] == PreparedStatement.EXECUTE_FAILED) {
            failDto(dto, codes[i]);
          }
          i++; // increment the counter to check the next code
        }
      }
    } else if (codes != null && codes.length == 0 && dtos.size() > 0) {
      IDbDto dto = (IDbDto)dtos.toArray()[0];
      failDto(dto, codes[0]);
    }
  }

  /**
   * Mark the DTO failed by marking the actual DTO status FAILED and by setting
   * the error code in the DTO tracking object.
   * 
   * @param dto the DTO to fail
   * @param code the error code
   */
  private void failDto(IDbDto dto, int code) {
    // Undo the last status change, which resets the DTO to its pre-execution
    // status.
    dto.undoStatus();
    // Mark the DTO status as FAILED, with the original status as the undo
    // target.
    dto.setFailed();
    // Mark the DTO as not processed because of the error.
    setProcessed(dto, false);
    history.get(dto.getPrimaryKey().getStringKey()).setBatchError(code);
  }

  /**
   * Return a list of DTO primary key strings for DTOs that had batch processing
   * errors.
   * 
   * @return a list of DTO primary key strings, possibly empty
   */
  public List<String> getBatchErrors() {
    List<String> errors = new ArrayList<String>();
    for (DtoTrackingObject dto : history.values()) {
      if (dto.getBatchError() != null) {
        errors.add(dto.getPrimaryKeyValue());
      }
    }
    return errors;
  }

  /**
   * Get a Throwable exception thrown during the run() method. The main thread
   * should check this value after the run completes to determine whether an
   * error has occurred in the tracking thread processing.
   * 
   * @return the exception or null if there was no exception thrown
   */
  public Throwable getThrowable() {
    return throwable;
  }

  /**
   * Record a Throwable thrown during the run() method before finishing.
   * 
   * @param throwable a Throwable exception
   */
  public void setThrowable(Throwable throwable) {
    this.throwable = throwable;
  }
}
