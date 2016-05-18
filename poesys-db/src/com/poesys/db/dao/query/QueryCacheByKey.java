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
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDtoCache;
import com.poesys.db.pk.IPrimaryKey;


/**
 * An implementation of the IQueryByKey interface that implements the basic
 * elements of a query by primary key including caching. If the object is
 * already cached, the queryByKey method returns it without querying; if the
 * object is not cached, the method queries and caches the object. This subclass
 * overrides the queryByKey method to add caching to the logic; the code
 * replaces all the code in the superclass because the caching has to happen
 * right in the middle of the method and there's no easy way to split apart the
 * shared code from the caching-specific code.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to query
 */
public class QueryCacheByKey<T extends IDbDto> extends QueryByKey<T> implements
    IQueryByKey<T> {
  /** Logger for debugging */
  private static final Logger logger = Logger.getLogger(QueryCacheByKey.class);
  /** Reference to the DTO cache of data transfer objects (DTOs) */
  IDtoCache<T> cache;

  /** Error getting resource bundle, can't resolve to bundle text so a constant */
  private static final String RESOURCE_BUNDLE_ERROR =
    "Problem getting Poesys/DB resource bundle";
  /** Error message resource for the no-object-cache error message */
  private static final String NO_CACHE =
    "com.poesys.db.dao.query.msg.no_object_cache";

  /**
   * Create a QueryCacheByKey object.
   * 
   * @param sql the SQL statement specification
   * @param cache the DTO cache
   * @param subsystem the subsystem that owns the object being queried
   */
  public QueryCacheByKey(IKeyQuerySql<T> sql,
                         IDtoCache<T> cache,
                         String subsystem) {
    super(sql, subsystem);
    if (cache == null) {
      throw new RuntimeException(NO_CACHE);
    }
    this.cache = cache;
  }

  @Override
  public T queryByKey(IPrimaryKey key) throws SQLException, BatchException {
    PreparedStatement stmt = null;
    ResultSet rs = null;

    // Make sure the key is there.
    if (key == null) {
      throw new NoPrimaryKeyException(NO_PRIMARY_KEY_MSG);
    }

    // Check the cache for the object.
    T object = null;
    if (cache != null) {
      object = cache.get(key);
    }

    Connection connection = null;

    // Only proceed if the object is not cached.
    if (object == null) {
      try {
        IConnectionFactory factory =
          ConnectionFactoryFactory.getInstance(subsystem);
        connection = factory.getConnection();
        stmt = connection.prepareStatement(sql.getSql(key));
        key.setParams(stmt, 1);
        logger.debug("Querying uncached object by key: " + sql.getSql(key));
        logger.debug("Setting key value: " + key.getValueList());
        rs = stmt.executeQuery();

        // Get a single result from the ResultSet and create the IDto.
        if (rs.next()) {
          object = sql.getData(key, rs);
          // Only cache if successfully retrieved.
          if (object != null) {
            // Cache the object. This must be done here before processing nested
            // objects to avoid infinite loops.
            cache.cache(object);
            // Set the new and changed flags to show this object exists and is
            // unchanged from the version in the database.
            object.setExisting();
          }
        }
      } catch (ConstraintViolationException e) {
        throw new DbErrorException(e.getMessage(), e);
      } catch (SQLException e) {
        // Log the message and the SQL statement, then rethrow the exception.
        logger.error("Caching query by key error: " + e.getMessage());
        logger.error("Caching query by key sql: " + sql.getSql(key) + "\n");
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
        // Close the connection.
        if (connection != null) {
          connection.close();
        }
      }
    }

    // Query any nested objects. This is outside the fetch above to make sure
    // that the statement and result set are closed before recursing.
    if (object != null) {
      object.queryNestedObjects();
    }

    return object;
  }
}
