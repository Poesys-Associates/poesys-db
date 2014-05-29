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
package com.poesys.db.dto;


import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.poesys.db.Message;
import com.poesys.db.pk.IPrimaryKey;


/**
 * The DtoMemcached class implements a caching scheme using the open source
 * memcached distributed caching system. Memcached provides a straightforward
 * object cache based on serialized Java objects. The DTO generation process
 * generates DTOs with primitive members and transient arrays of nested
 * objects along with non-transient arrays of memcached String keys. The
 * keys contain the fully qualified class name and the values string of the
 * object. When you retrieve an object from the cache, the DTO calls a
 * setter to populate the transient arrays with cached or queried objects.
 * 
 * @see com.poesys.db.dao.DaoCacheFactory
 * @see com.poesys.db.dao.CacheDaoManager
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to cache
 */
public class DtoMemcached<T extends IDbDto> implements IDtoCache<T> {
  /** Logger for debugging */
  private static final Logger logger = Logger.getLogger(DtoMemcached.class);
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
  public DtoMemcached(String name) {
    if (name == null) {
      String message = Message.getMessage(NULL_CACHE_NAME, null);
      throw new IllegalArgumentException(message);
    }
    cacheName = name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dto.IDtoCache#cache(T)
   */
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
          logger.debug("Object was of class " + object.getClass().getName());
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

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dto.IDtoCache#get(com.poesys.db.pk.IPrimaryKey)
   */
  public T get(IPrimaryKey key) {
    T dto = null;
    if (key != null) {
      dto = cache.get(key);
    }
    return dto;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dto.IDtoCache#remove(com.poesys.db.pk.IPrimaryKey)
   */
  public void remove(IPrimaryKey key) {
    // Remove locally, no messaging with this class
    removeLocally(key);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.poesys.db.dto.IDtoCache#removeLocally(com.poesys.db.pk.IPrimaryKey)
   */
  public void removeLocally(IPrimaryKey key) {
    if (key != null) {
      cache.remove(key);
      logger.debug("Removing key " + key.getValueList() + " from cache "
                   + cacheName);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dto.IDtoCache#clear()
   */
  public void clear() {
    cache.clear();
    logger.debug("Cleared cache " + cacheName);
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
