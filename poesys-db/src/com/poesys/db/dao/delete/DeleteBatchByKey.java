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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.poesys.db.BatchException;
import com.poesys.db.NoPrimaryKeyException;
import com.poesys.db.dao.AbstractBatch;
import com.poesys.db.dao.DataEvent;
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
 * constructor. The delete() method will delete all objects in the input
 * collection that have DELETED status.
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
  private static final String NO_KEY_MSG =
    "com.poesys.db.dao.delete.msg.no_delete_key";
  /** Builder for error string built in batch processing */
  private StringBuilder builder = new StringBuilder();
  /** Flag indicating whether there are batch errors */
  private boolean hasErrors = false;

  /**
   * Create a DeleteBatchByKey object by supplying the concrete implementation
   * of the SQL-statement generator and JDBC setter.
   * 
   * @param sql the SQL DELETE statement generator object
   */
  public DeleteBatchByKey(IDeleteSql<T> sql) {
    this.sql = sql;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dao.insert.IUpdateBatch#update(java.sql.Connection,
   * java.util.Collection, int)
   */
  public void delete(Connection connection, Collection<T> dtos, int size)
      throws SQLException, BatchException {
    PreparedStatement stmt = null;
    // array of return codes from JDBC batch processing
    int[] codes = null;
    // Current DTOs for error processing
    List<T> list = new ArrayList<T>();
    int count = 0; // counter for number of objects processed in batch

    // Iterate only if there are DTOs to iterate over.
    if (dtos != null) {
      try {
        for (T dto : dtos) {
          if (dto.getPrimaryKey() == null) {
            // Something's very wrong, so abort the whole delete.
            throw new NoPrimaryKeyException(NO_KEY_MSG);
          }

          // Validate and preprocess children for either DELETED or
          // CASCADE-DELETED objects.
          if (dto.getStatus() == IDbDto.Status.DELETED
              || dto.getStatus() == IDbDto.Status.CASCADE_DELETED) {
            dto.validateForDelete();
            dto.preprocessNestedObjects(connection);
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
              stmt = connection.prepareStatement(sqlStmt);
            }
            // Set the updating fields first, then the key in the WHERE clause.
            sql.setParams(stmt, 1, dto);
            stmt.addBatch();
            // Add the DTO to the current batch list for error processing.
            list.add(dto);
            logger.debug("Adding delete to batch with key " + key);
            logger.debug("SQL: " + sqlStmt);
            if (count == size) {
              // end of batch, execute
              try {
                stmt.executeBatch();
              } catch (BatchUpdateException e) {
                codes = e.getUpdateCounts();
                builder.append(e.getMessage() + ": ");
                hasErrors = processErrors(codes, list, builder);
              }

              // Reset the batch variables for the next batch.
              count = 0;
              list.clear();
            }
          } else {
            logger.debug("Object marked as cascaded delete, clearing cache but no database delete: "
                         + dto.getPrimaryKey().getValueList());
          }

        }
      } finally {
        // Execute the last batch, if any.
        if (count > 0 && stmt != null) {
          try {
            codes = stmt.executeBatch();
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
       * batch processing, you must first process ALL the deleted parent DTOs so
       * that the database reflects the changes for the nested operations, hence
       * the child processing must be in a completely separate loop after the
       * first loop that processes the parents. Only process DELETED DTOs here;
       * don't process FAILED, NEW, or CHANGED DTOs. Also notify parent
       * observers of the delete for both DELETED and CASCADE_DELETED DTOs.
       */
      for (IDbDto dto : dtos) {
        if (!dto.isProcessed() && dto.getStatus() == IDbDto.Status.DELETED) {
            dto.setProcessed(true);
          
          postprocess(connection, dto);

          // Set processed flag off after postprocessing.
            dto.setProcessed(false);
          dto.notify(DataEvent.DELETE);
        } else if (dto.getStatus() == IDbDto.Status.CASCADE_DELETED) {
          // Just notify DTO to update its observer parents of the delete.
          dto.notify(DataEvent.DELETE);
        }
      }

      // If there are errors, throw a batch exception.
      if (hasErrors) {
        throw new BatchException(builder.toString());
      }
    }
  }

  /**
   * Post-process the nested objects of the DTO. Override this method to add
   * things to post processing, such as a memcached session ID.
   * 
   * @param connection the connection with which to process
   * @param dto the DTO containing the nested objects
   * @throws SQLException when there is a database problem
   * @throws BatchException when there is a batch processing problem
   */
  protected void postprocess(Connection connection, IDbDto dto)
      throws SQLException, BatchException {
    dto.postprocessNestedObjects(connection);
  }

  @Override
  public void close() {
    // Nothing to do
    // Note that parameter-based delete does not affect caches at all.
  }
}
