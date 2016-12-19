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


import java.util.Collection;

import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDtoCache;


/**
 * A subclass of the UpdateCollectionByKey class that removes all the data
 * transfer objects (DTOs) in the updated list from the DTO cache
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to update
 */
public class UpdateCacheCollectionByKey<T extends IDbDto> extends
    UpdateCollectionByKey<T> implements IUpdateCollection<T> {
  /** The cache */
  private IDtoCache<T> cache;

  /**
   * Create a UpdateCacheCollectionByKey object.
   * 
   * @param sql the SQL UPDATE statement specification
   * @param cache the cache of IDtos from which to remove the updated DTOs
   * @param subsystem the subsystem of class T
   */
  public UpdateCacheCollectionByKey(IUpdateSql<T> sql,
                                    IDtoCache<T> cache,
                                    String subsystem) {
    super(sql, subsystem);
    this.cache = cache;
  }

  @Override
  public void update(Collection<T> dtos) {
    super.update(dtos);
    // Only remove from cache if there are dtos.
    if (dtos != null && dtos.size() > 0) {
      for (IDbDto dto : dtos) {
        cache.remove(dto.getPrimaryKey());
      }
    }
  }
}
