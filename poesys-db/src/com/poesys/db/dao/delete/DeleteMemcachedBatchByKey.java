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
package com.poesys.db.dao.delete;


import java.util.Collection;

import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDbDto.Status;


/**
 * A subclass of the DeleteBatchByKey class that removes the deleted data
 * transfer object (DTO) from the memcached cache.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to delete
 */
public class DeleteMemcachedBatchByKey<T extends IDbDto> extends
    DeleteBatchByKey<T> implements IDeleteBatch<T> {

  /**
   * Create a DeleteCacheBatchByKey object.
   * 
   * @param sql the SQL DELETE statement specification
   * @param subsystem the name of the subsystem of class T
   */
  public DeleteMemcachedBatchByKey(IDeleteSql<T> sql, String subsystem) {
    super(sql, subsystem);
  }

  @Override
  public void delete(Collection<T> dtos, int size) {
    super.delete(dtos, size);
    // Only remove from cache if collection exists and has objects.
    if (dtos != null && dtos.size() > 0) {
      DaoManagerFactory.initMemcachedManager(subsystem);
      IDaoManager manager = DaoManagerFactory.getManager(subsystem);
      for (IDbDto dto : dtos) {
        // Only proceed if the DTO is DELETED_FROM_DATABASE.
        if (dto.getStatus() == Status.DELETED_FROM_DATABASE) {
          manager.removeObjectFromCache(dto.getPrimaryKey().getCacheName(),
                                        dto.getPrimaryKey());
        }
      }
    }
  }
}
