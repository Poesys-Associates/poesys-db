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


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.poesys.db.BatchException;
import com.poesys.db.ConstraintViolationException;
import com.poesys.db.DbErrorException;
import com.poesys.db.Message;
import com.poesys.db.NoPrimaryKeyException;
import com.poesys.db.connection.ConnectionFactoryFactory;
import com.poesys.db.connection.IConnectionFactory;
import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dao.MemcachedService;
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

  private T dto = null;

  /** timeout for the query thread */
  private static final int TIMEOUT = 1000 * 60;

  /** error getting resource bundle, can't resolve to bundle text so a constant */
  private static final String RESOURCE_BUNDLE_ERROR =
    "Problem getting Poesys/DB resource bundle";
  /** Error message when thread is interrupted or timed out */
  private static final String THREAD_ERROR = "com.poesys.db.dao.msg.thread";

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
    // Make sure the key is there.
    if (key == null) {
      throw new NoPrimaryKeyException(NO_PRIMARY_KEY_MSG);
    }

    // Get the PoesysTrackingThread for later history addition.
    // Set the DTO object from the cache if it is cached.
     PoesysTrackingThread thread = getThreadAndDto(key);

    // Only proceed if the DTO object was not in the cache.
    if (dto == null) {
      queryDtoFromDatabase(key, thread);
    } else {
      dto.setQueried(false);
      logger.debug("Found object " + key.getCacheName() + " with key "
                   + key.getStringKey() + " in memcached ");
      // Add the DTO to the thread history before getting nested objects.
      thread.addDto(dto);
    }

    // For queried objects, get the nested objects and cache the DTO.
    // This is done outside the query method to ensure that the
    // SQL resources are completely closed.
    if (dto != null && dto.isQueried()) {
      dto.queryNestedObjects();
      // Get the memcached cache manager.
      DaoManagerFactory.initMemcachedManager(subsystem);
      IDaoManager memcachedManager = DaoManagerFactory.getManager(subsystem);
      memcachedManager.putObjectInCache(dto.getPrimaryKey().getCacheName(),
                                        expiration,
                                        dto);
    }

    return dto;
  }

  /**
   * Set the DTO object by querying the object from the database, caching the
   * object and setting it into the thread history.
   * 
   * @param key the primary key of the DTO to query
   * @param thread the cache thread holding the DTO query history
   * @throws SQLException if there is a problem querying the database
   */
  private void queryDtoFromDatabase(IPrimaryKey key, PoesysTrackingThread thread)
      throws SQLException {
    Connection connection = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;

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
        dto = sql.getData(key, rs);
        if (dto != null) {
          // Set the new and changed flags to show this object exists and is
          // unchanged from the version in the database.
          dto.setExisting();
          dto.setQueried(true);
          logger.debug("Queried " + key.getStringKey() + " from database");
          // Add the DTO to the thread history before getting nested objects.
          thread.addDto(dto);
        }
      } else {
        logger.debug("Object " + key.getStringKey() + " not found in database");
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
      if (connection != null) {
        String connectionString = connection.toString();
        connection.close();
        logger.debug("Closed connection " + connectionString);
      }
    }
  }

  /**
   * Get the cache thread and set the dto data member based on cache content.
   * 
   * @param key the primary key of the DTO to get
   * @return the
   */
  private PoesysTrackingThread getThreadAndDto(IPrimaryKey key) {
    PoesysTrackingThread thread = null;

    // If the current thread is a PoesysTrackingThread, just run the query in that
    // thread; if not, start a new thread.
    if (Thread.currentThread() instanceof PoesysTrackingThread) {
      getObjectByKeyFromCache(key);
      thread = (PoesysTrackingThread)Thread.currentThread();
    } else {
      Runnable query = new Runnable() {
        public void run() {
          dto = getObjectByKeyFromCache(key);
        }
      };
      thread = new PoesysTrackingThread(query);
      thread.start();
      // Join the thread, blocking until the thread completes or
      // until the query times out.
      try {
        thread.join(TIMEOUT);
      } catch (InterruptedException e) {
        Object[] args = {"update", dto.getPrimaryKey().getStringKey()};
        String message = Message.getMessage(THREAD_ERROR, args);
        logger.error(message, e);
      }
    }
    return thread;
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
    String keyString = null;
    if (key == null) {
      throw new NoPrimaryKeyException(NO_PRIMARY_KEY_MSG);
    } else {
      keyString = key.getStringKey();
    }

    // Check in memory for the object.
    logger.debug("Checking thread history for DTO " + keyString);
    PoesysTrackingThread thread = (PoesysTrackingThread)Thread.currentThread();
    // unchecked: required cast for non-generic getDto method
    object = (T)thread.getDto(keyString);

    if (object == null) {
      logger.debug("Object not found in thread DTO history, checking memcached with key \""
                   + keyString + "\"");
      // Check the cache for the object.
      object = service.getObject(key, expiration);
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
    this.expiration = expiration;
  }
}
