/*
 * Copyright (c) 2011 Poesys Associates. All rights reserved.
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


import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDbDto.Status;


/**
 * A subclass of the UpdateByKey class that removes the updated data transfer
 * object (DTO) from the cache.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to update and cache
 */
public class UpdateMemcachedByKey<T extends IDbDto> extends UpdateByKey<T>
    implements IUpdate<T> {

  /**
   * Create a UpdateCacheByKey object.
   * 
   * @param sql the SQL UPDATE statement specification
   * @param subsystem the name of the subsystem containing the T class
   */
  public UpdateMemcachedByKey(IUpdateSql<T> sql, String subsystem) {
    super(sql, subsystem);
  }

  @Override
  public void update(T dto) {
    // Remove CHANGED DTOs from the cache.
    if (dto != null && dto.getStatus() == Status.CHANGED) {
      DaoManagerFactory.initMemcachedManager(subsystem);
      IDaoManager manager = DaoManagerFactory.getManager(subsystem);

      manager.removeObjectFromCache(dto.getPrimaryKey().getCacheName(),
                                    dto.getPrimaryKey());
    }
    super.update(dto);
  }

  @Override
  public void close() {
  }
}
