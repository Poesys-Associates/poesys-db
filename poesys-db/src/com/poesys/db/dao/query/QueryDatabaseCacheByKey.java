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


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.poesys.db.ConstraintViolationException;
import com.poesys.db.DbErrorException;
import com.poesys.db.Message;
import com.poesys.db.NoPrimaryKeyException;
import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDtoCache;
import com.poesys.db.pk.IPrimaryKey;


/**
 * An implementation of the IQueryByKey interface that implements the basic
 * elements of a query by primary key including caching, but with direct query
 * of the database instead of checking the cache first, thus refreshing the
 * cache. If the object is not cached, the method queries and caches the object.
 * This subclass overrides the queryByKey method to add caching to the logic,
 * but with direct query of the database rather than checking the cache first;
 * the code replaces all the code in the superclass because the caching has to
 * happen right in the middle of the method and there's no easy way to split
 * apart the shared code from the caching-specific code.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to query
 */
public class QueryDatabaseCacheByKey<T extends IDbDto> extends QueryByKey<T>
    implements IQueryByKey<T> {
  /** Logger for debugging */
  private static final Logger logger =
    Logger.getLogger(QueryDatabaseCacheByKey.class);
  /** Reference to the DTO cache of data transfer objects (DTOs) */
  IDtoCache<T> cache;

  /** Error on executing SQL query */
  private static final String SQL_ERROR =
    "com.poesys.db.dto.msg.unexpected_sql_error";
  /** Error message resource for the no-object-cache error message */
  private static final String NO_CACHE_ERROR =
    "com.poesys.db.dao.query.msg.no_object_cache";

  /**
   * Create a QueryCacheByKey object.
   * 
   * @param sql the SQL statement specification
   * @param cache the IDto cache to set for the query
   * @param subsystem the subsystem that owns the object being queried
   */
  public QueryDatabaseCacheByKey(IKeyQuerySql<T> sql,
                                 IDtoCache<T> cache,
                                 String subsystem) {
    super(sql, subsystem);
    if (cache == null) {
      throw new RuntimeException(Message.getMessage(NO_CACHE_ERROR, null));
    }
    this.cache = cache;
  }

  @Override
  protected T getDto(IPrimaryKey key, PoesysTrackingThread thread) {
    PreparedStatement stmt = null;

    // Make sure the key is there.
    if (key == null) {
      throw new NoPrimaryKeyException(Message.getMessage(NO_PRIMARY_KEY_ERROR,
                                                         null));
    }

    // Check the cache for the object.
    T dto = null;
    if (cache != null) {
      dto = cache.get(key);
    }

    // Always get the data from the database.
    try {
      Connection connection = thread.getConnection();
      stmt = connection.prepareStatement(sql.getSql(key));
      key.setParams(stmt, 1);
      logger.debug("Querying uncached object by key: " + sql.getSql(key));
      logger.debug("Setting key value: " + key.getValueList());
      ResultSet rs = stmt.executeQuery();

      // Get a single result from the ResultSet and create the DTO.
      if (rs.next()) {
        dto = sql.getData(key, rs);
        // Only cache if successfully retrieved.
        if (dto != null) {
          // Set status to existing to indicate DTO is fresh from the
          // database; do this before caching and adding to the thread so
          // any further access from those places will get the right status.
          dto.setExisting();
          // Cache the object. This must be done here before processing
          // nested objects to avoid infinite loops.
          cache.cache(dto);
          // Add the DTO to the tracking thread to track processing.
          thread.addDto(dto);
        }
      }
    } catch (ConstraintViolationException e) {
      throw new DbErrorException(e.getMessage(), thread, e);
    } catch (SQLException e) {
      // Log the message and the SQL statement, then throw a standard DB
      // exception.
      logger.error("Caching query by key error: " + e.getMessage());
      logger.error("Caching query by key sql: " + sql.getSql(key) + "\n");
      logger.debug("SQL statement in class: " + sql.getClass().getName());
      String message = Message.getMessage(SQL_ERROR, null);
      throw new DbErrorException(message, thread, e);
    } finally {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          // ignore
        }
      }
    }

    // Query any nested objects. This is outside the fetch above to make sure
    // that the statement and result set are closed before recursing. The
    if (dto != null) {
      // Only query nested objects if the thread hasn't already processed this
      // DTO, and thus already queried them.
      if (!thread.isProcessed(key)) {
        // TODO need to find a way to tell the logic to query from database
        // directly instead of using cache
        dto.queryNestedObjects();
        thread.setProcessed(dto, true);
      }
    }

    return dto;
  }
}
