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


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import com.poesys.db.BatchException;
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
public class QueryMemcachedList<T extends IDbDto> extends QueryList<T> {
  private static final Logger logger =
    Logger.getLogger(QueryMemcachedList.class);
  /** the name of the subsystem containing the T class */
  private final String subsystem;
  /** the memcached expiration time in milliseconds for T objects */
  private final int expiration;

  /**
   * Create a QueryCacheList object.
   * 
   * @param sql the SQL statement specification
   * @param subsystem the name of the subsystem containing the T class
   * @param expiration the memcached expiration time in milliseconds for T
   *          objects
   * @param rows the number of rows to fetch at once; optimizes the query
   *          results fetching
   */
  public QueryMemcachedList(IQuerySql<T> sql,
                            String subsystem,
                            Integer expiration,
                            int rows) {
    super(sql, subsystem, rows);
    this.subsystem = subsystem;
    this.expiration = expiration;
  }

  @Override
  protected T getObject(Connection connection, ResultSet rs)
      throws SQLException, BatchException {
    IPrimaryKey key = sql.getPrimaryKey(rs);
    // Look the object up in the cache, create if not there and cache it.
    DaoManagerFactory.initMemcachedManager(subsystem);
    IDaoManager manager = DaoManagerFactory.getManager(subsystem);
    T object = manager.getCachedObject(key);
    if (object == null) {
      object = sql.getData(rs);
      logger.debug("Queried " + key.getStringKey() + " from database for list");
      // Only cache if successfully retrieved.
      if (object != null) {
        // Set the new and changed flags to show this object exists and is
        // unchanged from the version in the database.
        object.setExisting();
        object.setQueried(true);
      }
    } else {
      object.setQueried(false);
      logger.debug("Retrieved " + key.getStringKey() + " from cache for list");
    }

    if (object != null && Thread.currentThread() instanceof PoesysTrackingThread) {
      PoesysTrackingThread thread =
        (PoesysTrackingThread)Thread.currentThread();
      thread.addDto(object);
    }
    
    return object;
  }

  @Override
  protected void queryNestedObjectsForList(List<T> list) throws SQLException,
      BatchException {
    DaoManagerFactory.initMemcachedManager(subsystem);
    IDaoManager manager = DaoManagerFactory.getManager(subsystem);
    // Query any nested objects using the current memcached session. This is
    // outside the fetch above to make sure that the statement and result set
    // are closed before recursing.
    for (T object : list) {
      object.queryNestedObjects();
      // Cache the object to ensure all nested object keys get serialized.
      if (object.isQueried()) {
        manager.putObjectInCache(object.getPrimaryKey().getCacheName(),
                                 expiration,
                                 object);
        if (Thread.currentThread() instanceof PoesysTrackingThread) {
          PoesysTrackingThread thread =
            (PoesysTrackingThread)Thread.currentThread();
          thread.setProcessed(object.getPrimaryKey().getStringKey(), true);
        }
      }
    }
  }

  @Override
  public void close() {
  }

  @Override
  public void setExpiration(int expiration) {
    // Do nothing, expiration is final for memcached implementation
  }
}
