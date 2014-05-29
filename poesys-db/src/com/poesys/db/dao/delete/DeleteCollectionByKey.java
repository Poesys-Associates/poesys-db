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
import java.sql.SQLException;
import java.util.Collection;

import com.poesys.db.BatchException;
import com.poesys.db.dto.IDbDto;


/**
 * <p>
 * An implementation of the IDeleteCollection generic interface that contains
 * the base code for deleting a collection of objects in the database. To use
 * this class, you must implement the IDeleteSqlinterface for the concrete class
 * that contains the SQL DELETE statement and the JDBC code to set the fields in
 * the DTO from the JDBC result set, then pass that object into the
 * DeleteCollectionByKey constructor. The delete() method will delete all
 * objects in the input collection that have DELETED status.
 * </p>
 * <p>
 * Batch processing can improve performance dramatically for large collections
 * of DELETE statements because it reduces network latency overhead and provides
 * single-statement support for multiple deletes processed in a single batch.
 * Consider using the com.poesys.db.dao.DeleteBatchByKey class instead of this
 * one unless there is some reason why batch processing would not work well.
 * </p>
 * 
 * @see IDeleteSql
 * @see DeleteBatchByKey
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to delete
 */
public class DeleteCollectionByKey<T extends IDbDto> implements
    IDeleteCollection<T> {
  /** The Strategy-pattern object for the SQL statement */
  IDeleteSql<T> sql;

  /**
   * Create a DeleteCollectionByKey object by supplying the concrete
   * implementation of the SQL-statement generator and JDBC setter.
   * 
   * @param sql the SQL DELETE statement generator object
   */
  public DeleteCollectionByKey(IDeleteSql<T> sql) {
    this.sql = sql;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dao.insert.IUpdateBatch#update(java.sql.Connection,
   * java.util.Collection, int)
   */
  public void delete(Connection connection, Collection<T> dtos)
      throws SQLException, BatchException {
    DeleteByKey<T> deleter = new DeleteByKey<T>(sql);

    // Iterate only if there are DTOs to iterate over.
    if (dtos != null) {
      for (T dto : dtos) {
        deleter.delete(connection, dto);
      }
    }
  }

  @Override
  public void close() {
    // Nothing to do
    // Note that parameter-based delete does not affect caches at all.
  }
}
