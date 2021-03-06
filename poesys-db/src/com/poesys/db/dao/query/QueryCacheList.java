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
package com.poesys.db.dao.query;


import java.sql.ResultSet;

import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDtoCache;
import com.poesys.db.pk.IPrimaryKey;


/**
 * An implementation of the IQueryList interface that implements the basic
 * elements of a query of multiple objects including caching. The query method
 * executes the query and retrieves the results, but it first gets the primary
 * key and looks up the object in the cache and uses any object that already
 * exists rather than creating a new one.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to query
 */
public class QueryCacheList<T extends IDbDto> extends QueryList<T> {
  /** The cache of data transfer objects (DTOs) */
  IDtoCache<T> cache;

  /**
   * Create a QueryCacheList object.
   * 
   * @param sql the SQL statement specification
   * @param cache the DTO cache
   * @param subsystem the subsystem that owns the object being queried
   * @param rows the number of rows to fetch at once; optimizes the query
   *          results fetching
   */
  public QueryCacheList(IQuerySql<T> sql,
                        String subsystem,
                        IDtoCache<T> cache,
                        int rows) {
    super(sql, subsystem, rows);
    this.cache = cache;
  }

  @Override
  protected T getObject(ResultSet rs, PoesysTrackingThread thread) {
    IPrimaryKey key = sql.getPrimaryKey(rs);
    // Look the DTO up in the cache, create if not there and cache it.
    T dto = cache.get(key);
    if (dto == null) {
      // Use the standard list query to get the DTO.
      dto = super.getObject(rs, thread);
      // Only cache if successfully retrieved.
      if (dto != null) {
        // Cache the object before querying nested objects to avoid loops.
        cache.cache(dto);
      }
    }
    return dto;
  }
}
