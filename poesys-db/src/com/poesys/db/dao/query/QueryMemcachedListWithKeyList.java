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
import java.util.List;

import org.apache.log4j.Logger;

import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * An implementation of the IQueryList interface that implements the basic
 * elements of a query of multiple objects including caching using the
 * distributing caching system memcached. The query method executes the query
 * and retrieves the results, but it first gets the primary key and looks up the
 * object in the cache and uses any object that already exists rather than
 * creating a new one.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to query
 */
public class QueryMemcachedListWithKeyList<T extends IDbDto> extends
    QueryListWithKeyList<T> {
  private static final Logger logger =
    Logger.getLogger(QueryMemcachedListWithKeyList.class);
  /** the name of the subsystem of class T */
  private final String subsystem;
  /** the memcached expiration time in milliseconds for T objects */
  private final int expiration;

  /**
   * Create a QueryCacheList object.
   * 
   * @param sql the SQL statement specification
   * @param subsystem the name of the subsystem of class T
   * @param expiration the memcached expiration time in milliseconds for T
   *          objects
   * @param rows the number of rows to fetch at once; optimizes the query
   *          results fetching
   */
  public QueryMemcachedListWithKeyList(IKeyListQuerySql<T> sql,
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
    // Look the object up in the tracking thread first.
    @SuppressWarnings("unchecked")
    T dto = (T)thread.getDto(key);
    if (dto == null) {
      // Look the object up in the cache, create if not there and cache it.
      DaoManagerFactory.initMemcachedManager(subsystem);
      IDaoManager manager = DaoManagerFactory.getManager(subsystem);
      dto = manager.getCachedObject(key, subsystem);
      if (dto == null) {
        // Use the standard list query to get the DTO.
        dto = super.getObject(rs, thread);
        // Only cache if successfully retrieved.
        if (dto != null) {
          // Set queried status to tell nested-object method to cache.
          dto.setQueried(true);
        }
      } else {
        dto.setQueried(false);
        logger.debug("Retrieved DTO from memcached for memcached list: "
                     + key.getStringKey());
      }
    } else {
      logger.debug("Retrieved DTO from tracking thread for memcached list: "
                   + key.getStringKey());
    }

    return dto;
  }

  @Override
  protected void queryNestedObjectsForList(List<T> list,
                                           PoesysTrackingThread thread) {
    DaoManagerFactory.initMemcachedManager(subsystem);
    IDaoManager manager = DaoManagerFactory.getManager(subsystem);
    if (list != null) {
      for (T dto : list) {
        dto.queryNestedObjects();
        // Cache the object to ensure all nested object keys get serialized.
        if (dto.isQueried()) {
          manager.putObjectInCache(dto.getPrimaryKey().getCacheName(),
                                   expiration,
                                   dto);
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
}
