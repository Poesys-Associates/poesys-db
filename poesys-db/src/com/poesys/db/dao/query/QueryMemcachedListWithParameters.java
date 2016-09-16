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
import java.util.Collection;

import org.apache.log4j.Logger;

import com.poesys.db.BatchException;
import com.poesys.db.dao.CacheDaoManager;
import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoManager;
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
  /** the name of the subsystem containing the T class */
  private final String subsystem;
  /** the memcached expiration time in milliseconds for T objects */
  private int expiration;

  /**
   * Create a QueryCacheList object with the appropriate SQL class, the name of
   * the subsystem that contains T, the memcached expiration time for objects of
   * type T in the cache, and the number of rows to fetch at once.
   * 
   * @param sql the SQL statement specification
   * @param subsystem the name of the subsystem containing the T class
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
  protected T getObject(Connection connection, ResultSet rs)
      throws SQLException, BatchException {
    IPrimaryKey key = sql.getPrimaryKey(rs);
    // Look the object up in the local Java cache first.
    DaoManagerFactory.initMemcachedManager(subsystem);
    T object = DaoManagerFactory.getManager(subsystem).getCachedObject(key);
    if (object == null) {
      // Not locally cached, extract from list query result set.
      object = sql.getData(rs);
      logger.debug("Queried " + key.getStringKey()
                   + " from database for list (parameterized)");
      // Only cache if successfully retrieved
      if (object != null) {
        // Set the new and changed flags to show this object exists and is
        // unchanged from the version in the database.
        object.setExisting();
        object.setQueried(true);
        // Cache the object in memory before getting nested objects.
        CacheDaoManager.getInstance().putObjectInCache(object.getClass().getName(),
                                                       0,
                                                       object);
        // Cache object in memcached.
        DaoManagerFactory.getManager(subsystem).putObjectInCache(object.getPrimaryKey().getCacheName(),
                                                                 expiration,
                                                                 object);
      }
    } else {
      object.setQueried(false);
      logger.debug("Retrieved " + key.getStringKey()
                   + " from cache for list (parameterized)");
    }
    return object;
  }

  @Override
  protected void validateParameters(S parameters) throws SQLException {
    // Validate the parameters.
    parameters.validateForQuery();
  }

  @Override
  protected void queryNestedObjectsForList(Connection connection, C list)
      throws SQLException, BatchException {
    DaoManagerFactory.initMemcachedManager(subsystem);
    IDaoManager manager = DaoManagerFactory.getManager(subsystem);
    // Query any nested objects. This is outside the fetch above to make sure
    // that the statement and result set are closed before recursing.
    for (T object : list) {
      object.queryNestedObjects();
      // Cache the object to ensure all nested object keys get serialized.
      if (object.isQueried()) {
        manager.putObjectInCache(object.getPrimaryKey().getCacheName(),
                                 expiration,
                                 object);
      }
    }
  }

  @Override
  public void close() {
  }

  @Override
  public void setExpiration(int expiration) {
    this.expiration = expiration;
  }
}
