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


import java.util.Collection;

import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDbDto.Status;


/**
 * A subclass of the UpdateBatchByKey class that removes all the data transfer
 * objects (DTOs) in the updated list from the DTO cache
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to update
 */
public class UpdateMemcachedBatchByKey<T extends IDbDto> extends
    UpdateBatchByKey<T> implements IUpdateBatch<T> {

  /**
   * Create a UpdateCacheBatchByKey object.
   * 
   * @param sql the SQL UPDATE statement specification
   * @param subsystem the name of the subsystem containing the T class
   */
  public UpdateMemcachedBatchByKey(IUpdateSql<T> sql, String subsystem) {
    super(sql, subsystem);
  }

  @Override
  public void update(Collection<T> dtos, int size) {
    // Remove any CHANGED DTOs from the cache, then do the update, which resets
    // status to EXISTING.
    if (dtos != null && dtos.size() > 0) {
      DaoManagerFactory.initMemcachedManager(subsystem);
      IDaoManager manager = DaoManagerFactory.getManager(subsystem);
      for (T dto : dtos) {
        if (dto != null && dto.getStatus() == Status.CHANGED) {
          manager.removeObjectFromCache(dto.getPrimaryKey().getCacheName(),
                                        dto.getPrimaryKey());
        }
      }
    }
    super.update(dtos, size);
  }

  @Override
  public void close() {
  }
}
