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


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.poesys.db.BatchException;
import com.poesys.db.ConstraintViolationException;
import com.poesys.db.DbErrorException;
import com.poesys.db.NoPrimaryKeyException;
import com.poesys.db.connection.ConnectionFactoryFactory;
import com.poesys.db.connection.IConnectionFactory;
import com.poesys.db.dao.CacheDaoManager;
import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * An implementation of the IQueryByKey interface that implements the basic
 * elements of a query by primary key including caching in the distributed
 * memcached server. If the object is already cached, the queryByKey method
 * de-serializes it and returns it without querying; if the object is not
 * cached, the method queries and caches the object. This subclass overrides the
 * queryByKey method to add caching to the logic; the code replaces all the code
 * in the superclass because the caching has to happen right in the middle of
 * the method and there's no easy way to split apart the shared code from the
 * caching-specific code.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to query
 */
public class QueryMemcachedByKey<T extends IDbDto> extends QueryByKey<T>
    implements IQueryByKey<T> {
  /** Logger for debugging */
  private static final Logger logger =
    Logger.getLogger(QueryMemcachedByKey.class);
  /** the subsystem containing the T class */
  private final String subsystem;
  /** expiration time in milliseconds for cached objects */
  private Integer expiration;

  /** Error getting resource bundle, can't resolve to bundle text so a constant */
  private static final String RESOURCE_BUNDLE_ERROR =
    "Problem getting Poesys/DB resource bundle";

  /**
   * Create a QueryCacheByKey object with the appropriate SQL class, the
   * subsystem that contains the T class, and the memcached expiration time for
   * objects of type T in the cache.
   * 
   * @param sql the SQL statement specification
   * @param subsystem the subsystem name for the subsystem containing the T
   *          class
   * @param expiration the memcached expiration time in milliseconds for the
   *          cached object
   */
  public QueryMemcachedByKey(IKeyQuerySql<T> sql,
                             String subsystem,
                             Integer expiration) {
    super(sql, subsystem);
    this.subsystem = subsystem;
    this.expiration = expiration;
  }

  @Override
  public T queryByKey(IPrimaryKey key) throws SQLException, BatchException {
    PreparedStatement stmt = null;
    ResultSet rs = null;

    // Make sure the key is there.
    if (key == null) {
      throw new NoPrimaryKeyException(NO_PRIMARY_KEY_MSG);
    }

    // Get the external cache manager.
    IDaoManager memcachedManager = DaoManagerFactory.getManager(subsystem);
    // Get the in-memory cache manager that keeps track of the
    // already-deserialized objects, to avoid infinite-loop cache checks.
    IDaoManager localCacheManager = CacheDaoManager.getInstance();

    // Check in memory for the object.
    logger.debug("Checking in-memory cache " + key.getCacheName()
                 + " for object " + key.getStringKey());
    T object = localCacheManager.getCachedObject(key);

    if (object == null) {
      logger.debug("Object not found in in-memory cache " + key.getCacheName()
                   + ", checking memcached with key \"" + key.getStringKey()
                   + "\"");
      // Check the cache for the object.
      object = memcachedManager.getCachedObject(key);

      // Only proceed if memcached did not return an object from its cache.
      if (object == null) {
        Connection connection = null;

        try {
          IConnectionFactory factory =
            ConnectionFactoryFactory.getInstance(subsystem);
          connection = factory.getConnection();
          logger.debug("Object not found in memcached: " + key.getStringKey()
                       + ", querying with connection " + connection);
          stmt = connection.prepareStatement(sql.getSql(key));
          key.setParams(stmt, 1);
          logger.debug("Querying uncached object by key: " + sql.getSql(key));
          logger.debug("Setting key value: " + key.getValueList());
          rs = stmt.executeQuery();

          // Get a single result from the ResultSet and create the IDto.
          if (rs.next()) {
            object = sql.getData(key, rs);
            if (object != null) {
              // Set the new and changed flags to show this object exists and is
              // unchanged from the version in the database.
              object.setExisting();
              object.setQueried(true);
              logger.debug("Queried " + key.getStringKey() + " from database");
              // Cache the object in memory before getting nested objects.
              localCacheManager.putObjectInCache(object.getPrimaryKey().getCacheName(),
                                            expiration,
                                            object);
            }
          } else {
            logger.debug("Object " + key.getStringKey()
                         + " not found in database");
          }
        } catch (ConstraintViolationException e) {
          throw new DbErrorException(e.getMessage(), e);
        } catch (SQLException e) {
          // Log the message, the SQL statement, the key value parameters, and
          // the SQL statement class, then rethrow the exception.
          logger.error("Memcached query by key error: " + e.getMessage());
          logger.error("Memcached query by key sql: " + sql.getSql(key) + "\n");
          logger.error("Memcached query by key parameter values: "
                       + key.getValueList());
          logger.debug("SQL statement in class: " + sql.getClass().getName());
          throw e;
        } catch (IOException e) {
          // Problem with resource bundle, rethrow as SQLException
          throw new SQLException(RESOURCE_BUNDLE_ERROR);
        } finally {
          if (stmt != null) {
            stmt.close();
          }
          if (rs != null) {
            rs.close();
          }
          if (connection != null) {
            connection.close();
          }
        }
      } else {
        object.setQueried(false);
        logger.debug("Found object " + key.getCacheName() + " with key " +key.getStringKey() + " in memcached ");
        // Cache the object in memory before getting nested objects.
        localCacheManager.putObjectInCache(object.getPrimaryKey().getCacheName(),
                                      expiration,
                                      object);
      }
    } else {
      object.setQueried(false);
      logger.debug("Found object in in-memory cache " + key.getCacheName()
                   + ": " + key.getStringKey());
    }

    // Query any nested objects. This is outside the fetch above to make sure
    // that the statement and result set are closed before recursing.
    if (object != null) {
      // If the object was queried, complete processing nested objects and cache it.
      if (object.isQueried()) {
        // Get all the nested objects.
        object.queryNestedObjects();
        // Now cache the object as all the details have been filled in.
        memcachedManager.putObjectInCache(object.getPrimaryKey().getCacheName(),
                                 expiration,
                                 object);
      }
    }

    return object;
  }

  @Override
  public void close() {
  }

  @Override
  public void setExpiration(int expiration) {
    this.expiration = expiration;
  }
}
