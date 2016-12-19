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
 * 
 */
package com.poesys.db.dao.insert;


import com.poesys.db.dto.IDbDto;


/**
 * The InsertFactory lets you generate an insert object appropriate to your
 * desired insert framework. This class generates objects that directly insert
 * objects into the database. Use the InsertCacheFactory class to insert the
 * objects into the database and into an in-memory cache.
 * 
 * @author Robert J. Muller
 * @param <T> the database DTO type to insert
 */
public class InsertFactory<T extends IDbDto> {
  
  /** The database subsystem of DTO class T */
  protected final String subsystem;
  
  /**
   * Create a InsertFactory object.
   *
   * @param subsystem the subsystem of class T
   */
  public InsertFactory(String subsystem) {
    this.subsystem = subsystem;
  }

  /**
   * Generate a simple single-object insert.
   * 
   * @param sql the SQL insert statement object
   * @return the insert object
   */
  public IInsert<T> getInsert(IInsertSql<T> sql) {
    return new Insert<T>(sql, subsystem);
  }

  /**
   * Generate a single-object insert for use with a sequence key or an identity
   * key, both of which generate the key value.
   * 
   * @param sql the SQL insert statement object
   * @return the insert object
   */
  public IInsert<T> getInsertNoKey(IInsertSql<T> sql) {
    return new InsertNoKey<T>(sql, subsystem);
  }

  /**
   * Generate a batched multiple-object insert. Note that you cannot batch
   * inserts that use a sequence or identity key.
   * 
   * 
   * @param sql the SQL insert statement object
   * @return the insert object
   */
  public IInsertBatch<T> getInsertBatch(IInsertSql<T> sql) {
    return new InsertBatch<T>(sql, subsystem);
  }

  /**
   * Generate a collection multiple-object insert.
   * 
   * @param sql the SQL insert statement object
   * @return the insert object
   */
  public IInsertCollection<T> getInsertCollection(IInsertSql<T> sql) {
    return new InsertCollection<T>(sql, subsystem);
  }

  /**
   * Generate a collection multiple-object insert for use with a sequence key or
   * an identiy key, both of which generate the key value. Note that you cannot
   * batch such inserts.
   * 
   * @param sql the SQL insert statement object
   * @return the insert object
   */
  public IInsertCollection<T> getInsertNoKeyCollection(IInsertSql<T> sql) {
    return new InsertNoKeyCollection<T>(sql, subsystem);
  }
}
