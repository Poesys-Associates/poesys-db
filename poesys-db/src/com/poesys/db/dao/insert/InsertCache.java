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

import com.poesys.db.BatchException;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDtoCache;


/**
 * A subclass of Insert that caches the inserted object.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to query
 */
public class InsertCache<T extends IDbDto> extends Insert<T> implements
    IInsert<T> {
  /**
   * Cache for the T DTO
   */
  private IDtoCache<T> cache;

  /**
   * Create an InsertCache object.
   * 
   * @param sql the SQL insert statement object
   * @param cache the DTO cache into which to cache the inserted object
   */
  public InsertCache(IInsertSql<T> sql, IDtoCache<T> cache) {
    super(sql);
    this.cache = cache;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void insert(Connection connection, IDbDto dto) throws SQLException,
      BatchException {
    super.insert(connection, dto);
    cache.cache((T)dto);
  }
}
