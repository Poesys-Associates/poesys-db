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
package com.poesys.db.dao.delete;


import java.util.Collection;

import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDtoCache;


/**
 * A subclass of the DeleteCollectionByKey class that removes the deleted data
 * transfer object (DTO) from the cache.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to delete
 */
public class DeleteCacheCollectionByKey<T extends IDbDto> extends
    DeleteCollectionByKey<T> implements IDeleteCollection<T> {
  /** The cache */
  private IDtoCache<T> cache;

  /**
   * Create a DeleteCacheCollectionByKey object.
   * 
   * @param sql the SQL DELETE statement specification
   * @param cache the DTO cache from which to remove the deleted DTO
   * @param subsystem the subsystem of class T
   */
  public DeleteCacheCollectionByKey(IDeleteSql<T> sql,
                                    IDtoCache<T> cache,
                                    String subsystem) {
    super(sql, subsystem);
    this.cache = cache;
  }

  @Override
  public void delete(Collection<T> dtos) {
    // Delete only happens for DELETED objects, not CASCADE_DELETED.
    super.delete(dtos);
    for (IDbDto dto : dtos) {
      // Only proceed if the dto is DELETED or CASCADE_DELETED.
      if (dto.getStatus() == IDbDto.Status.DELETED
          || dto.getStatus() == IDbDto.Status.CASCADE_DELETED) {
        cache.remove(dto.getPrimaryKey());
      }
    }
  }
}
