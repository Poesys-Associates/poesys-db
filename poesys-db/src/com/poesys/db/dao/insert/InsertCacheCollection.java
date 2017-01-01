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


import java.util.Collection;

import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDtoCache;


/**
 * A subclass of the InsertCollection class that adds the collection of objects
 * to a cache after inserting them in the database.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to insert
 */
public class InsertCacheCollection<T extends IDbDto> extends
    InsertCollection<T> implements IInsertCollection<T> {
  /** the DTO cache */
  private IDtoCache<T> cache;

  /**
   * Create an InsertCacheCollection object.
   * 
   * @param sql the SQL insert statement object
   * @param cache the DTO cache
   * @param subsystem the subsystem of class T
   */
  public InsertCacheCollection(IInsertSql<T> sql,
                               IDtoCache<T> cache,
                               String subsystem) {
    super(sql, subsystem);
    this.cache = cache;
  }

  @Override
  public void insert(Collection<T> dtos) {
    super.insert(dtos);
    if (dtos != null) {
      for (T dto : dtos) {
        if (dto.getStatus() == IDbDto.Status.NEW
            || dto.getStatus() == IDbDto.Status.EXISTING) {
          // Cache NEW and EXISTING objects (those just inserted and those
          // unchanged from what is already in the cache).
          cache.cache(dto);
        }
      }
    }
  }

}
