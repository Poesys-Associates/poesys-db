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


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import com.poesys.db.BatchException;
import com.poesys.db.dto.IDbDto;


/**
 * <p>
 * An implementation of the IUpdateCollection interface that contains the base
 * code for updating a collection of objects in the database. To use this class,
 * you must implement the IUpdateSql generic interface for the concrete class
 * that contains the SQL UPDATE statement and the JDBC code to set the fields in
 * the DTO from the JDBC result set, then pass that object into the
 * UpdateCollectionByKey constructor. The implementation should update in the
 * database only if isChanged() is true.
 * </p>
 * <p>
 * Batch processing can improve performance dramatically for large collections
 * of UPDATE statements because it reduces network latency overhead and provides
 * single-statement support for multiple updates processed in a single batch.
 * Consider using the com.poesys.db.dao.update.UpdateBatchByKey class instead of
 * this one unless there is some reason why batch processing would not work
 * well.
 * </p>
 * 
 * @see com.poesys.db.dto.AbstractDto
 * @see com.poesys.db.dto.IDbDto
 * @see IUpdateSql
 * @see UpdateBatchByKey
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to update
 */
public class UpdateCollectionByKey<T extends IDbDto> implements
    IUpdateCollection<T> {
  /** Internal Strategy-pattern object containing the SQL query */
  private IUpdateSql<T> sql;
  
  /** Indicates whether this is a leaf update */
  private boolean leaf = false;

  /**
   * Create an UpdateCollectionByKey object by supplying the concrete
   * implementation of the SQL-statement generator and JDBC setter.
   * 
   * @param sql the SQL UPDATE statement specification
   */
  public UpdateCollectionByKey(IUpdateSql<T> sql) {
    this.sql = sql;
  }

  @Override
  public void update(Connection connection, Collection<T> dtos)
      throws SQLException, BatchException {
    UpdateByKey<T> updater = new UpdateByKey<T>(sql);

    // Iterate only if there are DTOs to iterate over.
    if (dtos != null) {
      for (T dto : dtos) {
        updater.update(connection, dto);
      }
    }
  }

  @Override
  public boolean isLeaf() {
    return leaf;
  }

  @Override
  public void setLeaf(boolean isLeaf) {
    leaf = isLeaf;
  }

  @Override
  public void close() {
    // Nothing to do    
  }
}
