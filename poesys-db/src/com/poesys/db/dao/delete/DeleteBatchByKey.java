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


import java.sql.BatchUpdateException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.poesys.db.DbErrorException;
import com.poesys.db.Message;
import com.poesys.db.NoPrimaryKeyException;
import com.poesys.db.dao.AbstractBatch;
import com.poesys.db.dao.DataEvent;
import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An implementation of the IDeleteBatch generic interface that contains the
 * base code for deleting a batch of objects in the database. The IDto interface
 * contains the methods this class uses to delete the data. To use this class,
 * you must implement the IDeleteSql interface for the concrete class that
 * contains the SQL DELETE statement and the JDBC code to set the fields in the
 * DTO from the JDBC result set, then pass that object into the DeleteBatchByKey
 * constructor. The delete() method will mark for delete all objects in the
 * input collection that have DELETED status. After the delete operation, the
 * instance sets each deleted DTO status to DELETED_FROM_DATABASE.
 * </p>
 * <p>
 * Batch processing can improve performance dramatically for large DELETE
 * statements and large collections because it reduces network latency overhead
 * and provides single-statement support for multiple deletes processed in a
 * single batch. There may be some rare circumstance where batching doesn't
 * work, in which case you can use com.poesys.db.dao.DeleteCollectionByKey.
 * </p>
 * 
 * @see com.poesys.db.dto.IDbDto
 * @see IDeleteSql
 * @see DeleteCollectionByKey
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to delete
 */
public class DeleteBatchByKey<T extends IDbDto> extends AbstractBatch<T>
    implements IDeleteBatch<T> {
  private static final Logger logger = Logger.getLogger(DeleteBatchByKey.class);
  /** The Strategy-pattern object for the SQL statement */
  IDeleteSql<T> sql;
  /** Error message when no primary key supplied */
  private static final String NO_KEY_ERROR =
    "com.poesys.db.dao.delete.msg.no_delete_key";
  /** Error message when delete throws exception */
  private static final String DELETE_ERROR =
    "com.poesys.db.dao.delete.msg.delete";
  /** Error message when thread is interrupted or timed out */
  private static final String THREAD_ERROR = "com.poesys.db.dao.msg.thread";
  /** timeout for the cache thread */
  private static final int TIMEOUT = 1000 * 60;

  /**
   * Create a DeleteBatchByKey object by supplying the concrete implementation
   * of the SQL-statement generator and JDBC setter.
   * 
   * @param sql the SQL DELETE statement generator object
   * @param subsystem the subsystem of class T
   */
  public DeleteBatchByKey(IDeleteSql<T> sql, String subsystem) {
    super(subsystem);
    this.sql = sql;
  }

  @Override
  public void delete(Collection<T> dtos, int size) {
    // Iterate only if there are DTOs to iterate over.
    if (dtos != null) {
      if (Thread.currentThread() instanceof PoesysTrackingThread) {
        doDelete(dtos, size, (PoesysTrackingThread)Thread.currentThread());
        notifySubscribers(dtos);
      } else {
        Runnable process = new Runnable() {
          public void run() {
            PoesysTrackingThread thread =
              (PoesysTrackingThread)Thread.currentThread();
            try {
              doDelete(dtos, size, thread);
              // Process nested objects, as the caller is not in the tracking
              // thread.
              postProcessNestedObjects(dtos);
              notifySubscribers(dtos);
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
          Object[] args = { "delete", "collection of DTOs" };
          String message = Message.getMessage(THREAD_ERROR, args);
          logger.error(message, e);
        }
      }
    }
  }

  /**
   * Delete a collection of DTOs.
   * 
   * @param dtos the collection of DTOs
   * @param size the batch size
   * @param thread the tracking thread
   */
  @SuppressWarnings("unchecked")
  private void doDelete(Collection<T> dtos, int size,
                        PoesysTrackingThread thread) {
    PreparedStatement stmt = null;

    // array of return codes from JDBC batch processing
    int[] codes = null;
    // list of DTOs having errors
    List<T> list = new ArrayList<T>();
    // error count
    int count = 0;

    try {
      if (dtos != null) {
        for (T dto : dtos) {
          if (dto.getPrimaryKey() == null) {
            // Something's very wrong, so abort the whole delete.
            throw new NoPrimaryKeyException(Message.getMessage(NO_KEY_ERROR,
                                                               null));
          }

          // Validate and preprocess children for either DELETED or
          // CASCADE-DELETED objects.
          if (dto.getStatus() == IDbDto.Status.DELETED
              || dto.getStatus() == IDbDto.Status.CASCADE_DELETED) {
            dto.validateForDelete();
            dto.preprocessNestedObjects();
            // Add the DTO to the tracking thread if not already tracked.
            if (thread.getDto(dto.getPrimaryKey()) == null) {
              thread.addDto(dto);
            }
          }

          // Only proceed to an actual delete if the dto is DELETED.
          if (dto.getStatus() == IDbDto.Status.DELETED) {

            count++;

            IPrimaryKey key = dto.getPrimaryKey();
            /*
             * The first time through the loop, build the batched SQL statement
             * and prepare it. The statement will track the batch and send it to
             * the database when the size is reached.
             */
            String sqlStmt = sql.getSql(key).toString();
            if (stmt == null) {
              stmt = thread.getConnection().prepareStatement(sqlStmt);
            }
            // Set the updating fields first, then the key in the WHERE clause.
            sql.setParams(stmt, 1, dto);
            stmt.addBatch();
            // Mark the DTO deleted from the database.
            dto.setDeletedFromDatabase();
            // Add the DTO to the current batch list for error processing.
            list.add(dto);
            logger.debug("Adding delete to batch with key " + key);
            logger.debug("SQL: " + sqlStmt);
            if (count == size) {
              // end of batch, execute
              try {
                stmt.executeBatch();
              } catch (BatchUpdateException e) {
                logger.error("Batch delete exception", e);
                codes = e.getUpdateCounts();
                thread.processErrors(codes, (Collection<IDbDto>)list);
              }

              // Reset the batch variables for the next batch.
              count = 0;
              list.clear();
            }
          } else if (dto.getStatus() == IDbDto.Status.CASCADE_DELETED) {
            logger.debug("Object marked as cascade-deleted, clearing cache but no database delete: "
                         + dto.getPrimaryKey().getValueList());
            // Mark the DTO deleted from the database.
            dto.setDeletedFromDatabase();
          }
        }
      }
    } catch (SQLException e) {
      // Log and let the thread complete immediately
      Object[] args = { "Batch of deletes" };
      String message = Message.getMessage(DELETE_ERROR, args);
      logger.error(message, e);
      throw new DbErrorException(message, thread, e);
    } finally {
      // Execute the last batch, if any.
      if (count > 0 && stmt != null) {
        try {
          codes = stmt.executeBatch();
        } catch (BatchUpdateException e) {
          logger.error("Batch delete exception", e);
          codes = e.getUpdateCounts();
          thread.processErrors(codes, (Collection<IDbDto>)list);
        } catch (SQLException e) {
          Object[] args = { "Batch of deletes" };
          String message = Message.getMessage(DELETE_ERROR, args);
          logger.error(message, e);
          throw new DbErrorException(message, thread, e);
        }
      }
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
   * Notify message subscribers of the delete event.
   * 
   * @param dtos the collection of DTOs being deleted
   */
  private void postProcessNestedObjects(Collection<T> dtos) {
    if (dtos != null) {
      for (T dto : dtos) {
        dto.postprocessNestedObjects();
      }
    }
  }

  /**
   * Notify message subscribers of the delete event.
   * 
   * @param dtos the collection of DTOs being deleted
   */
  private void notifySubscribers(Collection<T> dtos) {
    if (dtos != null) {
      for (T dto : dtos) {
        dto.notify(DataEvent.DELETE);
      }
    }
  }

  @Override
  public void close() {
    // Nothing to do
    // Note that parameter-based delete does not affect caches at all.
  }
}
