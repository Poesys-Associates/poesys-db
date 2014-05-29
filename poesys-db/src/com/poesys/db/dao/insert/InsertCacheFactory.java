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


import java.util.Collection;

import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDtoCache;


/**
 * The InsertCacheFactory lets you generate an insert object appropriate to your
 * desired insert framework. This class subclasses the general InsertFactory and
 * generates objects that insert objects into the database and into an in-memory
 * cache. Use the InsertFactory class to insert the objects directly into the
 * database without caching.
 * 
 * @author Robert J. Muller
 * @param <T> the database DTO type to insert
 * @param <C> the type of collection of DTOs to insert
 */
public class InsertCacheFactory<T extends IDbDto, C extends Collection<T>> extends InsertFactory<T> {
  /** The cache of objects of type T * */
  private IDtoCache<T> cache;

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dao.insert.InsertFactory#getInsert(com.poesys.db.dao.insert.IInsertSql)
   */
  @Override
  public IInsert<T> getInsert(IInsertSql<T> sql) {
    return new InsertCache<T>(sql, cache);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dao.insert.InsertFactory#getInsertNoKey(com.poesys.db.dao.insert.IInsertSql)
   */
  @Override
  public IInsert<T> getInsertNoKey(IInsertSql<T> sql) {
    return new InsertCacheNoKey<T>(sql, cache);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dao.insert.InsertFactory#getInsertBatch(com.poesys.db.dao.insert.IInsertSql)
   */
  @Override
  public IInsertBatch<T> getInsertBatch(IInsertSql<T> sql) {
    return new InsertCacheBatch<T>(sql, cache);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dao.insert.InsertFactory#getInsertCollection(com.poesys.db.dao.insert.IInsertSql)
   */
  @Override
  public IInsertCollection<T> getInsertCollection(IInsertSql<T> sql) {
    return new InsertCacheCollection<T>(sql, cache);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dao.insert.InsertFactory#getInsertNoKeyCollection(com.poesys.db.dao.insert.IInsertSql)
   */
  @Override
  public IInsertCollection<T> getInsertNoKeyCollection(IInsertSql<T> sql) {
    return new InsertCacheNoKeyCollection<T>(sql, cache);
  }
}
