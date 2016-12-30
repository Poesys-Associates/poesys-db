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


import java.sql.BatchUpdateException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.poesys.db.DbErrorException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.Message;
import com.poesys.db.NoPrimaryKeyException;
import com.poesys.db.dao.AbstractBatch;
import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDbDto.Status;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An implementation of the IUpdateBatch interface that contains the base code
 * for updating a batch of objects in the database. To use this class, you must
 * implement the IUpdateSql interface for the concrete class that contains the
 * SQL UPDATE statement and the JDBC code to set the fields in the DTO from the
 * JDBC result set, then pass that object into the UpdateBatchByKey constructor.
 * The implementation should update in the database only if isChanged() is true.
 * The caller should update the status to EXISTING once <strong>all</strong>
 * processing is complete (over the entire inheritance hierarchy).
 * </p>
 * <p>
 * Batch processing can improve performance dramatically for large collections
 * of UPDATE statements because it reduces network latency overhead and provides
 * single-statement support for multiple updates processed in a single batch.
 * There may be some rare circumstance where batching doesn't work, in which
 * case you can use com.poesys.db.dao.update.UpdateCollectionByKey.
 * </p>
 * 
 * @see com.poesys.db.dto.AbstractDto
 * @see com.poesys.db.dto.IDbDto
 * @see IUpdateSql
 * @see UpdateCollectionByKey
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to update
 */
public class UpdateBatchByKey<T extends IDbDto> extends AbstractBatch<T>
    implements IUpdateBatch<T> {
  /** Logger for this class */
  private static final Logger logger = Logger.getLogger(UpdateBatchByKey.class);
  /** Internal Strategy-pattern object containing the SQL query */
  private IUpdateSql<T> sql;

  /** timeout for the cache thread */
  private static final int TIMEOUT = 1000 * 60;

  /** Error message when no DTO is supplied */
  private static final String NO_DTO_ERROR = "com.poesys.db.dao.msg.no_dto";
  /** Error message when no primary key supplied */
  private static final String NO_KEY_ERROR =
    "com.poesys.db.dao.update.msg.no_update_key";
  /** Error message when no SQL object supplied */
  private static final String SQL_ERROR =
    "com.poesys.db.dto.msg.unexpected_sql_errorl";
  /** Error message when no SQL object supplied */
  private static final String NULL_SQL_ERROR = "com.poesys.db.dao.msg.null_sql";
  /** Error message when post-processing throws an exception */
  private static final String POST_PROCESSING_ERROR =
    "com.poesys.db.dao.update.msg.postprocessing";
  /** Error message when insert throws exception */
  private static final String UPDATE_ERROR =
    "com.poesys.db.dao.update.msg.update";
  /** Error message when thread is interrupted or timed out */
  private static final String THREAD_ERROR = "com.poesys.db.dao.msg.thread";

  /**
   * Create an UpdateBatchByKey object by supplying the concrete implementation
   * of the SQL-statement generator and JDBC setter.
   * 
   * @param sql the SQL UPDATE statement generator object
   * @param subsystem the subsystem of class T
   */
  public UpdateBatchByKey(IUpdateSql<T> sql, String subsystem) {
    super(subsystem);
    if (sql == null) {
      throw new InvalidParametersException(Message.getMessage(NULL_SQL_ERROR,
                                                              null));
    }

    this.sql = sql;
  }

  @Override
  public void update(Collection<T> dtos, int size) {
    // If the current thread is a PoesysTrackingThread, just batch insert in
    // that thread; if not, start a new thread for the inserts.
    if (Thread.currentThread() instanceof PoesysTrackingThread) {
      PoesysTrackingThread thread =
        (PoesysTrackingThread)Thread.currentThread();
      processUpdateBatches(dtos, size, thread);
    } else {
      Runnable process = new Runnable() {
        public void run() {
          PoesysTrackingThread thread =
            (PoesysTrackingThread)Thread.currentThread();
          try {
            processUpdateBatches(dtos, size, thread);
            // Post process the DTOs here, as the client is not in the tracking
            // thread.
            postProcessNestedObjects(dtos);
          } finally {
            thread.closeConnection();
          }
        }

        private void postProcessNestedObjects(Collection<T> dtos) {
          for (T dto : dtos) {
            // Process nested objects, as the caller is not in the tracking
            // thread.
            dto.postprocessNestedObjects();
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
        Object[] args = { "insert", "batch of DTOs" };
        String message = Message.getMessage(THREAD_ERROR, args);
        logger.error(message, e);
      }
    }
  }

  /**
   * Update a collection of DTOs using batch processing, including pre- and
   * post-processing.
   * 
   * @param dtos a collection of DTOs to update
   * @param size the batch size
   * @param thread the Poesys tracking thread for the update
   */
  private void processUpdateBatches(Collection<T> dtos, int size,
                                    PoesysTrackingThread thread) {
    PreparedStatement stmt = null;
    // array of return codes from JDBC batch processing
    int[] codes = null;
    // list of current DTOs for error processing
    List<T> list = new ArrayList<T>();
    // counter for number of objects processed in batch
    int count = 0;

    // Iterate only if there are DTOs to iterate over.
    if (dtos != null) {
      try {
        for (T dto : dtos) {
          IPrimaryKey key = dto.getPrimaryKey();
          String sqlStmt = sql.getSql(key);
          if (sqlStmt != null) {
            if (stmt == null) {
              stmt =
                thread.getConnection().prepareStatement(sql.getSql(key).toString());
            }
          }
          count = processDto(dto, stmt, sqlStmt, list, size, codes, count);
        }
      } catch (SQLException e) {
        throw new DbErrorException(Message.getMessage(SQL_ERROR, null));
      } finally {
        // Execute the last batch, if any.
        if (count > 0 && stmt != null) {
          processFinalBatch(dtos, stmt, list);
        }
      }
    }
  }

  /**
   * Process the final batch.
   * 
   * @param dtos the list of DTOs being processed
   * @param stmt the prepared statement containing the final batch of SQL
   *          updates
   * @param list the list of processed DTOs, for error handling
   */
  @SuppressWarnings("unchecked")
  private void processFinalBatch(Collection<T> dtos, PreparedStatement stmt,
                                 List<T> list) {
    PoesysTrackingThread thread = (PoesysTrackingThread)Thread.currentThread();
    int[] codes;
    try {
      codes = stmt.executeBatch();
      // Set status of all processed DTOs from CHANGED to EXISTING
      for (T dto : dtos) {
        if (dto.getStatus() == Status.CHANGED) {
          dto.setExisting();
        }
      }
    } catch (BatchUpdateException e) {
      codes = e.getUpdateCounts();
      thread.processErrors(codes, (Collection<IDbDto>)dtos);
    } catch (SQLException e) {
      // Log and let the thread complete immediately
      Object[] args = { "batch of DTOs" };
      String message = Message.getMessage(POST_PROCESSING_ERROR, args);
      logger.error(message, e);
      throw new DbErrorException(message, thread, e);
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

  /**
   * Process the DTO. The method processes a DTO with status CHANGED that the
   * thread has not already processed.
   * 
   * @param dto the DTO to process
   * @param stmt the prepared SQL statement that contains the batch
   * @param sqlStmt the text of the SQL statement, for debugging output
   * @param list the list of current DTOs for error processing
   * @param size the batch size
   * @param codes an array of codes for the batched statements
   * @param count the count of DTOs processed
   * @return the updated count of DTOs processed (0 or 1 more than count input)
   */
  private int processDto(T dto, PreparedStatement stmt, String sqlStmt,
                         List<T> list, int size, int[] codes, int count) {
    PoesysTrackingThread thread = (PoesysTrackingThread)Thread.currentThread();

    try {
      if (dto == null) {
        throw new InvalidParametersException(NO_DTO_ERROR, null);
      }
      if (dto.getPrimaryKey() == null) {
        // Something's very wrong, so abort the whole update.
        throw new NoPrimaryKeyException(Message.getMessage(NO_KEY_ERROR, null));
      }
      if (sqlStmt == null || sql == null) {
        throw new InvalidParametersException(Message.getMessage(NULL_SQL_ERROR,
                                                                null));
      }

      // Pre-process nested objects to handle any changes there regardless
      // of parent object status.
      dto.preprocessNestedObjects();

      // Process only CHANGED DTOs.
      if (dto.getStatus() == Status.CHANGED) {
        dto.validateForUpdate();

        // Everything is valid, so proceed to the main update.
        count++;

        IPrimaryKey key = dto.getPrimaryKey();

        // Set the updating fields first, then the key in the WHERE
        // clause.
        sql.setParams(stmt, 1, dto);
        stmt.addBatch();
        // Add the DTO to the current batch list for error processing.
        list.add(dto);
        // Add the DTO to the tracking thread.
        thread.addDto(dto);

        // Note that the caller must set the DTO status to EXISTING once ALL
        // processing is complete (over the entire inheritance hierarchy).

        logger.debug("Adding update to batch with key " + key);
        logger.debug("SQL: " + sqlStmt);
        logger.debug(sql.getParamString(dto));

        if (count == size) {
          count =
            processBatch(dto.getPrimaryKey().getStringKey(), stmt, list, count);
        }
      }
    } catch (InvalidParametersException | SQLException e) {
      Object[] args = { dto.getPrimaryKey().getStringKey() };
      String message = Message.getMessage(UPDATE_ERROR, args);
      logger.error(message, e);
      throw new DbErrorException(message, thread, e);
    }
    return count;
  }

  /**
   * Take a complete batch of statements and execute the batch.
   * 
   * @param key the string representation of the primary key of the DTO, used
   *          for error messages
   * @param stmt the prepared SQL statement containing the batch of updates
   * @param list the list of current DTOs processed
   * @param count the count of DTOs processed in this batch
   * @return the count after completion of batch processing, normally 0
   */
  @SuppressWarnings("unchecked")
  private int processBatch(String key, PreparedStatement stmt, List<T> list,
                           int count) {
    PoesysTrackingThread thread = (PoesysTrackingThread)Thread.currentThread();
    int[] codes;
    // end of batch, execute
    try {
      stmt.executeBatch();
      // Reset the batch variables for the next batch.
      count = 0;
      list.clear();
    } catch (BatchUpdateException e) {
      codes = e.getUpdateCounts();
      thread.processErrors(codes, (Collection<IDbDto>)list);
      // Reset the batch variables for the next batch.
      count = 0;
      list.clear();
    } catch (SQLException e) {
      Object[] args = { key };
      String message = Message.getMessage(UPDATE_ERROR, args);
      logger.error(message, e);
      throw new DbErrorException(message, thread, e);
    }
    return count;
  }

  @Override
  public void close() {
    // Nothing to do
  }
}
