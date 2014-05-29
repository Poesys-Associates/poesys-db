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
import java.sql.SQLException;
import java.util.Collection;

import com.poesys.db.BatchException;
import com.poesys.db.dto.IDbDto;


/**
 * <p>
 * An implementation of the IInsertCollection generic interface that contains
 * the base code for inserting a collection of objects that do not contain
 * primary key values (identity or auto-generated keys) into the database. To
 * use this class, you must implement the IInsertSql interface for the concrete
 * class that contains the SQL INSERT statement and the JDBC code to set the
 * fields in the DTO from the JDBC result set, then pass that object into the
 * Insert constructor.
 * </p>
 * <p>
 * <em>Note: This implementation inserts the entire collection in a series of
 * single inserts. You cannot insert no-key objects in batches because the underlying
 * JDBC standard does not support that operation.</em>
 * </p>
 * 
 * @see com.poesys.db.dto.IDbDto
 * @see com.poesys.db.dto.AbstractDto
 * @see IInsertSql
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to insert
 */
public class InsertNoKeyCollection<T extends IDbDto> implements
    IInsertCollection<T> {
  /**
   * The single-object DAO for inserting one of the collection elements at a
   * time
   */
  private final InsertNoKey<T> dao;

  /**
   * Create an InsertNoKeyCollection object by supplying the concrete
   * implementation of the SQL-statement generator and JDBC setter.
   * 
   * @param sql the SQL INSERT statement generator object
   */
  public InsertNoKeyCollection(IInsertSql<T> sql) {
    dao = new InsertNoKey<T>(sql);
  }

  @Override
  public void insert(Connection connection, Collection<T> dtos)
      throws SQLException, BatchException {

    if (dtos != null) {
      for (T dto : dtos) {
        dao.insert(connection, dto);
      }
    }
  }

  @Override
  public boolean isLeaf() {
    return dao.isLeaf();
  }

  @Override
  public void setLeaf(boolean isLeaf) {
    dao.setLeaf(isLeaf);
  }

  @Override
  public void close() {
    // Nothing to do    
  }
}
