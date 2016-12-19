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


import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDtoCache;


/**
 * A subclass of InsertNoKey that caches the inserted object after key
 * generation.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to insert and cache
 */
public class InsertCacheNoKey<T extends IDbDto> extends InsertNoKey<T>
    implements IInsert<T> {
  /**
   * Cache for the T DTO
   */
  private IDtoCache<T> cache;

  /**
   * Create a InsertCacheNoKey object.
   * 
   * @param sql the SQL statement specification for INSERT
   * @param cache the object cache
   * @param subsystem the subsystem of the DTO class T
   */
  public InsertCacheNoKey(IInsertSql<T> sql,
                          IDtoCache<T> cache,
                          String subsystem) {
    super(sql, subsystem);
    this.cache = cache;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void insert(IDbDto dto) {
    super.insert((T)dto);
    cache.cache((T)dto);
  }

}
