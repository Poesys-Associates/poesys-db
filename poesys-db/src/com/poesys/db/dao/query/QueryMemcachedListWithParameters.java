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
package com.poesys.db.dao.query;


import java.sql.ResultSet;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.dto.IDbDto;
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
public class QueryMemcachedListWithParameters<T extends IDbDto, S extends IDbDto, C extends Collection<T>>
    extends QueryListWithParameters<T, S, C> {
  private static final Logger logger =
    Logger.getLogger(QueryMemcachedListWithParameters.class);
  /** the name of the subsystem of class T */
  private final String subsystem;
  /** the memcached expiration time in milliseconds for T objects */
  private final int expiration;

  /**
   * Create a QueryCacheList object with the appropriate SQL class, the name of
   * the subsystem that contains T, the memcached expiration time for objects of
   * type T in the cache, and the number of rows to fetch at once.
   * 
   * @param sql the SQL statement specification
   * @param subsystem the name of the subsystem of class T
   * @param expiration the memcached expiration time in milliseconds for T
   *          objects
   * @param rows the number of rows to fetch at once; optimizes query fetching
   */
  public QueryMemcachedListWithParameters(IParameterizedQuerySql<T, S> sql,
                                          String subsystem,
                                          Integer expiration,
                                          int rows) {
    super(sql, subsystem, rows);
    this.subsystem = subsystem;
    this.expiration = expiration;
  }

  @Override
  protected T getObject(ResultSet rs, PoesysTrackingThread thread) {
    IPrimaryKey key = sql.getPrimaryKey(rs);
    logger.debug("Primary key for cache lookup: " + key.getStringKey());
    // Look the object up in the thread history first.
    @SuppressWarnings("unchecked")
    T dto = (T)thread.getDto(key);
    if (dto == null) {
      IDaoManager manager = DaoManagerFactory.initMemcachedManager(subsystem);
      dto = manager.getCachedObject(key, subsystem);
      if (dto == null) {
        // Not previously retrieved, extract from list query result set.
        dto = sql.getData(rs);
        logger.debug("Retrieved DTO from database for memcached parameterized list: "
                     + key.getStringKey());
        // Only cache if successfully retrieved; defer caching until after
        // retrieving the nested objects.
        if (dto != null) {
          // Set queried flag to tell nested objects method to cache the object
          // when the nested objects are retrieved.
          dto.setQueried(true);
        }
      } else {
        logger.debug("Retrieved DTO from memcached for memcached parameterized list: "
                     + key.getStringKey());
        thread.setProcessed(key, true);
      }
    } else {
      dto.setQueried(false);
      logger.debug("Retrieved DTO from tracking thread for memcached parameterized list");
    }

    return dto;
  }

  @Override
  protected void validateParameters(S parameters) {
    // Validate the parameters.
    parameters.validateForQuery();
  }

  @Override
  protected void queryNestedObjectsForList(PoesysTrackingThread thread) {
    DaoManagerFactory.initMemcachedManager(subsystem);
    IDaoManager manager = DaoManagerFactory.getManager(subsystem);
    // Query any nested objects. This is outside the fetch above to make sure
    // that the statement and result set are closed before recursing.
    for (T dto : list) {
      dto.queryNestedObjects();
      // Cache the object if not already cached.
      if (thread.getDto(dto.getPrimaryKey()) == null && dto.isQueried()) {
        manager.putObjectInCache(dto.getPrimaryKey().getCacheName(),
                                 expiration,
                                 dto);
        thread.addDto(dto);
      }

      // object is complete, set it as processed.
      thread.setProcessed(dto.getPrimaryKey(), true);
      logger.debug("Retrieved all nested objects for "
                   + dto.getPrimaryKey().getStringKey());
      // Set status to existing to indicate DTO is fresh from the database.
      dto.setExisting();
    }
  }
}
