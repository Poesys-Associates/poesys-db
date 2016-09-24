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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.poesys.db.BatchException;
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
  /** Error message when no primary key supplied */
  private static final String NO_KEY_MSG =
    "com.poesys.db.dao.update.msg.no_update_key";
  /** Error message when post-processing throws an exception */
  private static final String POST_PROCESSING_ERROR =
    "com.poesys.db.dao.update.msg.postprocessing";
  /** Error message updating a DTO already processed */
  private static final String ALREADY_PROCESSED_WARNING =
    "com.poesys.db.dao.delete.msg.processed";
  /** Error message when thread is interrupted or timed out */
  private static final String THREAD_ERROR = "com.poesys.db.dao.msg.thread";
  /** Builder for the batch processing error strings */
  private StringBuilder builder = new StringBuilder();
  /** Flag indicating whether a batch has encountered errors */
  private boolean hasErrors = false;
  /** timeout for the cache thread */
  private static final int TIMEOUT = 1000 * 60;

  /**
   * Create an UpdateBatchByKey object by supplying the concrete implementation
   * of the SQL-statement generator and JDBC setter.
   * 
   * @param sql the SQL UPDATE statement generator object
   */
  public UpdateBatchByKey(IUpdateSql<T> sql) {
    this.sql = sql;
  }

  @Override
  public void update(Connection connection, Collection<T> dtos, int size)
      throws SQLException, BatchException {
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
          if (dto.getPrimaryKey() == null) {
            // Something's very wrong, so abort the whole update.
            throw new NoPrimaryKeyException(NO_KEY_MSG);
          }

          // Pre-process nested objects to handle any changes there regardless
          // of parent object status.
          dto.preprocessNestedObjects(connection);

          // Only proceed if the dto is CHANGED and unprocessed.
          if (dto.getStatus() == IDbDto.Status.CHANGED) {
            if (Thread.currentThread() instanceof PoesysTrackingThread) {
              PoesysTrackingThread thread =
                (PoesysTrackingThread)Thread.currentThread();
              if (thread.getDto(dto.getPrimaryKey().getStringKey()) != null) {
                if (thread.isProcessed(dto.getPrimaryKey().getStringKey())) {
                  // update already processed, warn then skip this DTO
                  Object[] args = { dto.getPrimaryKey().getStringKey() };
                  String message =
                    Message.getMessage(ALREADY_PROCESSED_WARNING, args);
                  logger.warn(message);
                  continue;
                }
              }
            }
            dto.validateForUpdate();

            // Everything is valid, so proceed to the main update.
            count++;

            IPrimaryKey key = dto.getPrimaryKey();
            /*
             * The first time through the loop, build the batched SQL statement
             * and prepare it. The statement will track the batch and send it to
             * the database when the size is reached.
             */
            String sqlStmt = sql.getSql(key);
            if (sqlStmt != null) {
              if (stmt == null) {
                stmt = connection.prepareStatement(sql.getSql(key).toString());
              }
              // Set the updating fields first, then the key in the WHERE
              // clause.
              sql.setParams(stmt, 1, dto);
              stmt.addBatch();
              // Add the DTO to the current batch list for error processing.
              list.add(dto);
              logger.debug("Adding update to batch with key " + key);
              logger.debug("SQL: " + sqlStmt);
              if (count == size) {
                // end of batch, execute
                try {
                  stmt.executeBatch();
                  // Reset the batch variables for the next batch.
                  count = 0;
                  list.clear();
                } catch (BatchUpdateException e) {
                  codes = e.getUpdateCounts();
                  builder.append(e.getMessage() + ": ");
                  hasErrors = processErrors(codes, list, builder);
                  // Reset the batch variables for the next batch.
                  count = 0;
                  list.clear();
                }
              }
            }
          }
        }
      } finally {
        // Execute the last batch, if any.
        if (count > 0 && stmt != null) {
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
            builder.append(e.getMessage() + ": ");
            hasErrors = processErrors(codes, list, builder);
          }
        }
        // Close the statement as required.
        if (stmt != null) {
          stmt.close();
        }
      }

      /*
       * Post-process any nested objects for successfully processed DTOs. In
       * batch processing, you must first process ALL the parent DTOs so that
       * the data is in the database for the nested operations, hence the child
       * processing must be in a completely separate loop after the first loop
       * that processes the parents. Only process CHANGED or EXISTING DTOs here;
       * don't process FAILED, NEW, or DELETED DTOs. Note that the caller should
       * set the DTO status to EXISTING once all processing for the DTO is
       * complete (that is, after all updates have been done for all levels of
       * the inheritance hierarchy).
       */
      for (T dto : dtos) {
        PoesysTrackingThread thread = null;

        // If the current thread is a PoesysTrackingThread, just postprocess in
        // that thread; if not, start a new thread for the postprocessing.
        if (Thread.currentThread() instanceof PoesysTrackingThread) {
          thread = (PoesysTrackingThread)Thread.currentThread();
          if (thread.getDto(dto.getPrimaryKey().getStringKey()) == null) {
            // DTO not in history, add it so we can track processing.
            thread.addDto(dto);
          }
          if (!thread.isProcessed(dto.getPrimaryKey().getStringKey())
              && (dto.getStatus() == IDbDto.Status.CHANGED || dto.getStatus() == IDbDto.Status.EXISTING)) {
            thread.setProcessed(dto.getPrimaryKey().getStringKey(), true);
            postprocess(connection, dto);
          }
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
            Object[] args = { "update", dto.getPrimaryKey().getStringKey() };
            String message = Message.getMessage(THREAD_ERROR, args);
            logger.error(message, e);
          }
        }
      }

      // If there are errors, throw a batch exception.
      if (hasErrors) {
        throw new BatchException(builder.toString());
      }
    }
  }

  /**
   * Post-process the nested objects of a DTO. Override this method if you want
   * to add things such as a session ID.
   * 
   * @param connection the JDBC connection
   * @param dto the DTO containing the nested objects
   * @throws SQLException when there is a database problem
   * @throws BatchException when there is a batch exception
   */
  protected void postprocess(Connection connection, T dto) throws SQLException,
      BatchException {
    dto.postprocessNestedObjects(connection);
  }

  @Override
  public void close() {
    // Nothing to do
  }
}
