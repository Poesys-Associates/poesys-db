/*
 * Copyright (c) 2011, 2016 Poesys Associates. All rights reserved.
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


import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.poesys.db.BatchException;
import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * An implementation of the IQueryByKey interface and a subclass of the
 * memcached query-by-key that implements querying the DTO from the database
 * regardless of cache status, caching the returned DTO. The runnable always
 * queries and caches the object. This subclass overrides the getRunnableQuery()
 * method to remove the cache check before the query. You use this method when
 * you want to guarantee that the value is synchronized with the database--for
 * example, during testing that updates the data through direct JDBC, or when
 * testing the actual changes to the database.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to query
 */
public class QueryDatabaseMemcachedByKey<T extends IDbDto> extends
    QueryMemcachedByKey<T> implements IQueryByKey<T> {
  /** Logger for debugging */
  private static final Logger logger =
    Logger.getLogger(QueryDatabaseMemcachedByKey.class);
  /** the subsystem containing the T class */
  private final String subsystem;
  /** expiration time in milliseconds for cached objects */
  private Integer expiration;

  /** Error message when thread gets SQL error */
  private static final String SQL_ERROR =
    "com.poesys.db.dto.msg.unexpected_sql_error";

  /**
   * Create a QueryDatabaseMemcachedByKey object with the appropriate SQL class,
   * the subsystem that contains the T class, and the memcached expiration time
   * for objects of type T in the cache.
   * 
   * @param sql the SQL statement specification
   * @param subsystem the subsystem name for the subsystem containing the T
   *          class
   * @param expiration the memcached expiration time in milliseconds for the
   *          cached object
   */
  public QueryDatabaseMemcachedByKey(IKeyQuerySql<T> sql,
                                     String subsystem,
                                     Integer expiration) {
    super(sql, subsystem, expiration);
    this.subsystem = subsystem;
    this.expiration = expiration;
  }

  /**
   * Create a runnable query object that runs within a PoesysTrackingThread. The
   * run method always queries the object from the database. The method then
   * queries nested objects. All these activities happen in a single run of the
   * query in the tracking thread.
   * 
   * @param key the primary key of the DTO
   * @return the runnable query
   */
  @Override
  protected Runnable getRunnableQuery(IPrimaryKey key) {
    // Create a runnable query object that does the query.
    Runnable query = new Runnable() {
      public void run() {
        // Get the current tracking thread in which this is running.
        PoesysTrackingThread thread =
          (PoesysTrackingThread)Thread.currentThread();
        try {
          dto = queryDtoFromDatabase(key);
          thread.addDto(dto);
        } catch (SQLException e) {
          logger.error(SQL_ERROR, e);
          throw new RuntimeException(SQL_ERROR, e);
        }

        // For queried objects, get the nested objects and cache the DTO.
        // This is done outside the query method to ensure that the
        // SQL resources are completely closed.
        if (dto != null && dto.isQueried()) {
          try {
            dto.queryNestedObjects();
          } catch (SQLException e) {
            logger.error(SQL_ERROR, e);
            throw new RuntimeException(SQL_ERROR, e);
          } catch (BatchException e) {
            logger.error(SQL_ERROR, e);
            throw new RuntimeException(SQL_ERROR, e);
          }
          // Get the memcached cache manager.
          DaoManagerFactory.initMemcachedManager(subsystem);
          IDaoManager memcachedManager =
            DaoManagerFactory.getManager(subsystem);
          memcachedManager.putObjectInCache(dto.getPrimaryKey().getCacheName(),
                                            expiration,
                                            dto);
        }
      }
    };
    return query;
  }
}
