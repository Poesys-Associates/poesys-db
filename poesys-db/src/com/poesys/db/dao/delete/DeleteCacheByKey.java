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


import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDtoCache;


/**
 * A subclass of the DeleteByKey class that removes the deleted data transfer
 * object from the cache.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to delete
 */
public class DeleteCacheByKey<T extends IDbDto> extends DeleteByKey<T>
    implements IDelete<T> {
  /** The cache */
  private IDtoCache<T> cache;

  /**
   * Create a DeleteCacheByKey object.
   * 
   * @param sql the SQL DELETE statement specification
   * @param cache the DTO cache from which to remove the deleted DTO
   * @param subsystem the subsystem of class T
   */
  public DeleteCacheByKey(IDeleteSql<T> sql,
                          IDtoCache<T> cache,
                          String subsystem) {
    super(sql, subsystem);
    this.cache = cache;
  }

  @Override
  public void delete(T dto) {
    // Delete only happens for DELETED objects, not CASCADE_DELETED.
    super.delete(dto);
    // Only proceed if the DTO is DELETED_FROM_DATABASE.
    if (dto.getStatus() == IDbDto.Status.DELETED_FROM_DATABASE) {
      cache.remove(dto.getPrimaryKey());
    }
  }
}
