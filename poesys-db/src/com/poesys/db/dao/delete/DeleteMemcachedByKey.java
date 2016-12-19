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


import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dto.IDbDto;


/**
 * A subclass of the DeleteByKey class that removes the deleted data transfer
 * object from the memcached distributed cache.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to delete
 */
public class DeleteMemcachedByKey<T extends IDbDto> extends DeleteByKey<T>
    implements IDelete<T> {
  /**
   * Create a DeleteCacheByKey object, supplying an appropriate kind of SQL
   * class object and the name of the subsystem that contains objects of type T.
   * 
   * @param sql the SQL DELETE statement specification
   * @param subsystem the name of the subsystem of class T
   */
  public DeleteMemcachedByKey(IDeleteSql<T> sql, String subsystem) {
    super(sql, subsystem);
  }

  @Override
  public void delete(T dto) {
    // Delete only happens for DELETED objects, not CASCADE_DELETED.
    super.delete(dto);
    // Only proceed if the dto is DELETED or CASCADE_DELETED.
    if (dto.getStatus() == IDbDto.Status.DELETED
        || dto.getStatus() == IDbDto.Status.CASCADE_DELETED) {
      DaoManagerFactory.initMemcachedManager(subsystem);
      IDaoManager manager = DaoManagerFactory.getManager(subsystem);
      manager.removeObjectFromCache(dto.getPrimaryKey().getCacheName(),
                                    dto.getPrimaryKey());
    }
  }

  @Override
  protected void postprocess(T dto) {
    dto.postprocessNestedObjects();
  }

  @Override
  public void close() {
  }
}
