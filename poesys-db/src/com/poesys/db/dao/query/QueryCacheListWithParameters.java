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
import java.util.Collection;

import org.apache.log4j.Logger;

import com.poesys.db.DbErrorException;
import com.poesys.db.Message;
import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDtoCache;
import com.poesys.db.pk.IPrimaryKey;


/**
 * An implementation of the IQueryListWithParameters interface that implements
 * the basic elements of a query of multiple objects including caching. The
 * query method executes the query and retrieves the results, but it first gets
 * the primary key and looks up the object in the cache and uses any object that
 * already exists rather than creating a new one.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to query and cache
 * @param <S> the type of IDbDto that contains parameters
 * @param <C> the collection type of the set of queried DTOs
 */
public class QueryCacheListWithParameters<T extends IDbDto, S extends IDbDto, C extends Collection<T>>
    extends QueryListWithParameters<T, S, C> {
  /** Logger for debugging */
  private static final Logger logger =
    Logger.getLogger(QueryCacheListWithParameters.class);

  /** The cache of data transfer objects (DTOs) */
  private IDtoCache<T> cache;

  private static final String NO_PRIMARY_KEY_ERROR =
    "com.poesys.db.dto.msg.no_primary_key";

  /**
   * Create a QueryCacheList object.
   * 
   * @param sql the SQL statement specification
   * @param cache the DTO cache for the queried objects
   * @param subsystem the subsystem that owns the object to query
   * @param rows the number of rows to fetch at once; optimizes query fetching
   */
  public QueryCacheListWithParameters(IParameterizedQuerySql<T, S> sql,
                                      IDtoCache<T> cache,
                                      String subsystem,
                                      int rows) {
    super(sql, subsystem, rows);
    this.cache = cache;
  }

  @Override
  protected T getObject(ResultSet rs, PoesysTrackingThread thread) {
    IPrimaryKey key = sql.getPrimaryKey(rs);
    if (key == null) {
      String message = Message.getMessage(NO_PRIMARY_KEY_ERROR, null);
      logger.error(message);
      throw new DbErrorException(message);
    }
    // Look the object up in the cache, create if not there and cache it.
    T dto = cache.get(key);
    if (dto == null) {
      // Use the standard list query to get the DTO.
      dto = super.getObject(rs, thread);
      // Only cache if successfully retrieved
      if (dto != null) {
        cache.cache(dto);
      }
    }
    return dto;
  }
}
