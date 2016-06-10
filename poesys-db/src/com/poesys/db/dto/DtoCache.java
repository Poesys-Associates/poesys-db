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
package com.poesys.db.dto;


import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.poesys.db.Message;
import com.poesys.db.pk.IPrimaryKey;


/**
 * The DtoCache provides a concurrent map cache for the objects of the concrete
 * class. The cache is a threadsafe Map that keys data transfer objects on keys
 * of interface IPrimaryKey. The cache thus contains DTO objects of a specific
 * class and its subclasses keyed on the primary key of the class. To invalidate
 * the entire cache, remove all references to the cache and let the garbage
 * collector collect it. Any objects currently in use will remain valid for
 * their lifetime. For that reason, you should generally get objects from the
 * cache whenever you need them, then drop the reference as soon as possible.
 * The cache factory stores the cache in the DaoManager under the name of the
 * cached class.
 * 
 * @see com.poesys.db.dao.DaoCacheFactory
 * @see com.poesys.db.dao.CacheDaoManager
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to cache
 */
public class DtoCache<T extends IDbDto> implements IDtoCache<T> {

  /** Logger for debugging */
  private static final Logger logger = Logger.getLogger(DtoCache.class);
  /** The cache */
  private ConcurrentHashMap<IPrimaryKey, T> cache =
    new ConcurrentHashMap<IPrimaryKey, T>();

  /** The name of the cache (fully-qualified class name of type T) */
  private final String cacheName;

  /** Message when null passed for cache name */
  private static final String NULL_CACHE_NAME =
    "com.poesys.db.dto.null_cache_name_msg";

  /**
   * Create a DtoCache object.
   * 
   * @param name the name of the cache (fully-qualified class name of type T)
   */
  public DtoCache(String name) {
    if (name == null) {
      String message = Message.getMessage(NULL_CACHE_NAME, null);
      throw new IllegalArgumentException(message);
    }
    cacheName = name;
  }

  @Override
  public void cache(T object) {
    IPrimaryKey key = object.getPrimaryKey();
    if (key != null && object != null) {
      if (cache.get(key) == null) {
        cache.put(key, object);
        logger.debug("Caching object of class " + object.getClass().getName()
                     + " with primary key "
                     + object.getPrimaryKey().getValueList());
      } else {
        logger.debug("Object of class " + object.getClass().getName()
                     + " already in cache, request to cache ignored");
      }
    } else {
      if (key == null) {
        logger.debug("Tried to cache object with null key");
        if (object != null) {
          logger.debug("Object with null key was of class "
                       + object.getClass().getName());
        }
      }

      if (object == null) {
        logger.debug("Tried to cache null object");
        if (key != null) {
          logger.debug("Object had primary key " + key.getValueList());
        }
      }
    }
  }

  @Override
  public T get(IPrimaryKey key) {
    T dto = null;
    if (key != null) {
      dto = cache.get(key);
    }
    return dto;
  }

  @Override
  public void remove(IPrimaryKey key) {
    // Remove locally, no messaging with this class
    removeLocally(key);
  }

  @Override
  public void removeLocally(IPrimaryKey key) {
    if (key != null) {
      cache.remove(key);
      logger.debug("Removing key " + key.getValueList() + " from cache "
                   + cacheName);
    }
  }

  @Override
  public void clear() {
    cache.clear();
    logger.debug("Cleared cache " + cacheName);
  }
  
  @Override
  public void clearProcessedFlags() {
    for (IDbDto dto : cache.values()) {
      dto.setProcessed(false);
    }
  }

  /**
   * Send the primary key values in the cache to the debug log.
   */
  public void logCache() {
    logger.debug("Logging the cache (object id " + this + ": ");
    for (IPrimaryKey key : cache.keySet()) {
      logger.debug("\tContains object " + key.getValueList());
    }
  }

  /**
   * Get the cacheName.
   * 
   * @return a cacheName
   */
  public String getCacheName() {
    return cacheName;
  }
}
