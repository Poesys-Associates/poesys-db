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
package com.poesys.db.dao.insert;


import java.util.Collection;

import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dto.IDbDto;


/**
 * A subclass of InsertNoKeyCollection that caches the object after key
 * generation
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to insert
 */
public class InsertMemcachedNoKeyCollection<T extends IDbDto> extends
    InsertNoKeyCollection<T> implements IInsertCollection<T> {
  /** the memcached expiration time in milliseconds for T objects */
  private final Integer expiration;

  /**
   * Create an InsertCacheNoKeyCollection object.
   * 
   * @param sql the SQL insert statement object
   * @param subsystem the name of the subsystem containing the T class
   * @param expiration the memcached expiration time in milliseconds for T
   *          objects
   */
  public InsertMemcachedNoKeyCollection(IInsertSql<T> sql,
                                        String subsystem,
                                        Integer expiration) {
    super(sql, subsystem);
    this.expiration = expiration;
  }

  @Override
  public void insert(Collection<T> dtos) {
    super.insert(dtos);
    DaoManagerFactory.initMemcachedManager(subsystem);
    IDaoManager manager = DaoManagerFactory.getManager(subsystem);
    for (T dto : dtos) {
      if (dto.getStatus() == IDbDto.Status.NEW
          || dto.getStatus() == IDbDto.Status.EXISTING) {
        // Cache NEW and EXISTING objects (those just inserted and those
        // unchanged from what is already in the cache).
        manager.putObjectInCache(dto.getPrimaryKey().getCacheName(),
                                 expiration,
                                 dto);
      }
    }
  }

  @Override
  public void close() {
  }
}
