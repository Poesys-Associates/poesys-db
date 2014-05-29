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


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import com.poesys.db.BatchException;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDtoCache;


/**
 * A subclass of InsertNoKeyCollection that caches the object after key
 * generation
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to insert
 */
public class InsertCacheNoKeyCollection<T extends IDbDto> extends
    InsertNoKeyCollection<T> implements IInsertCollection<T> {
  /** The DTO cache */
  private IDtoCache<T> cache;

  /**
   * Create an InsertCacheNoKeyCollection object.
   * 
   * @param sql the SQL insert statement object
   * @param cache the DTO cache in which to cache the inserted objects
   */
  public InsertCacheNoKeyCollection(IInsertSql<T> sql, IDtoCache<T> cache) {
    super(sql);
    this.cache = cache;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.poesys.db.dao.insert.InsertNoKeyCollection#insert(java.sql.Connection,
   * java.util.Collection)
   */
  @Override
  public void insert(Connection connection, Collection<T> dtos)
      throws SQLException, BatchException {
    super.insert(connection, dtos);
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
