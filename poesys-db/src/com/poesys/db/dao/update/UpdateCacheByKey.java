/*
 * Copyright (c) 2010 Poesys Associates. All rights reserved.
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


import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDtoCache;


/**
 * A subclass of the UpdateByKey class that removes the updated data transfer
 * object (DTO) from the cache.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to update and cache
 */
public class UpdateCacheByKey<T extends IDbDto> extends UpdateByKey<T>
    implements IUpdate<T> {
  /** The cache */
  private IDtoCache<T> cache;

  /**
   * Create a UpdateCacheByKey object.
   * 
   * @param sql the SQL UPDATE statement specification
   * @param cache the cache of IDtos from which to remove the updated DTO
   * @param subsystem the subsystem of class T
   */
  public UpdateCacheByKey(IUpdateSql<T> sql,
                          IDtoCache<T> cache,
                          String subsystem) {
    super(sql, subsystem);
    this.cache = cache;
  }

  @Override
  public void update(T dto) {
    super.update(dto);
    // Only remove from cache if DTO exists.
    if (dto != null) {
      cache.remove(dto.getPrimaryKey());
    }
  }
}
