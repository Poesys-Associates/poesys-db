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
 * A subclass of the InsertBatch class that adds all the inserted objects to a
 * cache after inserting them.
 * 
 * @author Robert J. Muller
 * @param <T> the database DTO type
 */
public class InsertCacheBatch<T extends IDbDto> extends InsertBatch<T>
    implements IInsertBatch<T> {
  /** DTO cache */
  private IDtoCache<T> cache;

  /**
   * Create an InsertCacheBatch object.
   * 
   * @param sql the SQL insert statement object
   * @param cache the DTO cache into which to cache the objects
   * @param subsystem the subsystem of class T
   */
  public InsertCacheBatch(IInsertSql<T> sql,
                          IDtoCache<T> cache,
                          String subsystem) {
    super(sql, subsystem);
    this.cache = cache;
  }

  @Override
  public void insert(Collection<T> dtos, int size) {
    super.insert(dtos, size);
    // Only cache if list exists and has objects.
    if (dtos != null && dtos.size() > 0) {
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
