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


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.poesys.db.ConstraintViolationException;
import com.poesys.db.DbErrorException;
import com.poesys.db.NoPrimaryKeyException;
import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dao.MemcachedService;
import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * An implementation of the IQueryByKey interface that implements the basic
 * elements of a query by primary key including caching in the distributed
 * memcached server. If the object is already cached, the queryByKey method
 * de-serializes it and returns it without querying; if the object is not
 * cached, the method queries and caches the object. This subclass overrides the
 * queryByKey method to add caching to the logic; the code replaces all the code
 * in the superclass as the code must run in a separate tracking thread.
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
  private final Integer expiration;

  /**
   * Create a QueryMemcachedByKey object with the appropriate SQL class, the
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

  /**
   * Get the DTO identified by a primary key either from memcached or by
   * querying it from the database. Update the tracking thread with the DTO.
   * 
   * @param key the primary key
   * @param thread the tracking thread
   */
  @SuppressWarnings("unchecked")
  @Override
  protected T getDto(IPrimaryKey key, PoesysTrackingThread thread) {
    // Make sure the key is there.
    if (key == null) {
      throw new NoPrimaryKeyException(NO_PRIMARY_KEY_ERROR);
    }

    // First check tracking thread for DTO.
    T dto = (T)thread.getDto(key);
    if (dto == null) {
      // Next check memcached for DTO.
      dto = getObjectByKeyFromCache(key);
      // Only proceed if the DTO object was not in the tracking thread or cache.
      if (dto == null) {
        dto = queryDtoFromDatabase(key, thread);
      } else {
        dto.setQueried(false);
        logger.debug("Found object " + key.getCacheName() + " with key "
                     + key.getStringKey() + " in memcached ");
      }

      // Add the DTO to the thread history before getting nested objects.
      if (dto != null) {
        thread.addDto(dto);
      }

      // For queried objects, get the nested objects and cache the DTO.
      // This is done outside the query method to ensure that the
      // SQL resources are completely closed. Note that the memcached
      // retrieval processes nested objects regardless of whether the
      // object comes from the cache or the database.
      if (dto != null) {
        if (!thread.isProcessed(key)) {
          dto.queryNestedObjects();
          thread.setProcessed(key, true);
        }
        // Get the memcached cache manager.
        DaoManagerFactory.initMemcachedManager(subsystem);
        IDaoManager memcachedManager = DaoManagerFactory.getManager(subsystem);
        memcachedManager.putObjectInCache(dto.getPrimaryKey().getCacheName(),
                                          expiration,
                                          dto);
      }
    }
    
    // Set status to existing to indicate DTO is fresh from the database.
    dto.setExisting();
    
    return dto;
  }

  /**
   * Set the DTO object by querying the object from the database, caching the
   * object and setting it into the thread history.
   * 
   * @param key the primary key of the DTO to query
   * @param thread the tracking thread with the SQL connection
   * @return the DTO
   */
  protected T queryDtoFromDatabase(IPrimaryKey key, PoesysTrackingThread thread) {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    T dto = null;

    try {
      Connection connection = thread.getConnection();
      logger.debug("Object not found in memcached: " + key.getStringKey()
                   + ", querying with connection " + connection);
      String sqlStatement = sql.getSql(key);
      stmt = connection.prepareStatement(sqlStatement);
      key.setParams(stmt, 1);
      logger.debug("Querying uncached object by key: " + sqlStatement);
      logger.debug("Setting key value: " + key.getValueList());
      rs = stmt.executeQuery();

      // Get a single result from the ResultSet and create the IDto.
      if (rs.next()) {
        dto = sql.getData(key, rs);
        if (dto != null) {
          dto.setQueried(true);
          logger.debug("Queried " + key.getStringKey() + " from database");
        }
      } else {
        logger.debug("Object " + key.getStringKey() + " not found in database");
      }
    } catch (ConstraintViolationException e) {
      throw new DbErrorException(e.getMessage(), thread, e);
    } catch (SQLException e) {
      // Log the message, the SQL statement, the key value parameters, and
      // the SQL statement class, then rethrow the exception.
      logger.error("Memcached query by key error: " + e.getMessage());
      logger.error("Memcached query by key sql: " + sql.getSql(key) + "\n");
      logger.error("Memcached query by key parameter values: "
                   + key.getValueList());
      logger.debug("SQL statement in class: " + sql.getClass().getName());
      throw new DbErrorException(e.getMessage(), thread, e);
    } finally {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          // ignore
        }
      }
    }
    return dto;
  }

  /**
   * Get the object from the memcached cache by key, or from the thread's
   * history of objects already deserialized from the cache in this thread.
   * 
   * @param key the primary key to query
   * @return the object of type T
   */
  @SuppressWarnings("unchecked")
  private T getObjectByKeyFromCache(IPrimaryKey key) {
    T object = null;
    MemcachedService<T> service = new MemcachedService<T>();

    // Make sure the key is there.
    if (key == null) {
      throw new NoPrimaryKeyException(NO_PRIMARY_KEY_ERROR);
    }
    
    String keyString = key.getStringKey();

    // Check in memory for the object.
    logger.debug("Checking thread history for DTO " + keyString);
    PoesysTrackingThread thread = (PoesysTrackingThread)Thread.currentThread();
    // unchecked: required cast for non-generic getDto method
    object = (T)thread.getDto(key);

    if (object == null) {
      logger.debug("Object not found in thread DTO history, checking memcached with key \""
                   + keyString + "\"");
      // Check the cache for the object.
      object = service.getObject(key, expiration);
      if (object != null) {
        logger.debug("Object found in memcached cache: \"" + keyString + "\"");
      }
    } else {
      object.setQueried(false);
      logger.debug("Found object in thread history: " + keyString);
    }

    return object;
  }

  @Override
  public void close() {
  }

  @Override
  public void setExpiration(int expiration) {
    // Do nothing, expiration is final for reentrancy
  }
}
