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
package com.poesys.db.dao;


import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.poesys.db.dto.DtoCache;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDtoCache;
import com.poesys.db.pk.IPrimaryKey;


/**
 * A singleton object that manages data access objects in the system. The
 * manager maintains a singleton map of caches which is available on demand. The
 * caching scheme is a two-level one that maintains a singleton cache of caches,
 * each holding objects of a certain IDbDto type.
 * 
 * @author Robert J. Muller
 */
public class CacheDaoManager implements IDaoManager {
  /** Logger for debugging */
  private static final Logger logger = Logger.getLogger(CacheDaoManager.class);

  /** The singleton manager */
  protected static CacheDaoManager manager = null;

  /** The thread-safe singleton cache map */
  protected static Map<String, IDtoCache<IDbDto>> map = null;

  /** The database subsystem for the DTO */
  protected final String subsystem;

  /**
   * Disable the default constructor.
   */
  CacheDaoManager() {
    subsystem = null;
  }

  CacheDaoManager(String subsystem) {
    this.subsystem = subsystem;
  }

  /**
   * Get the DAO Manager singleton implementing a map cache.
   * 
   * @param subsystem the database subsystem for the DTOs
   * @return the DAO Manager
   */
  public static IDaoManager getInstance(String subsystem) {
    if (manager == null) {
      manager = new CacheDaoManager(subsystem);
      map = new ConcurrentHashMap<String, IDtoCache<IDbDto>>();
    }
    return manager;
  }

  @Override
  public <T extends IDbDto, C extends Collection<T>> IDaoFactory<T> getFactory(String name,
                                                                               String subsystem,
                                                                               Integer expiration) {
    return new DaoCacheFactory<T>(name, manager, subsystem);
  }

  @Override
  public void logMetaData() {
    logger.debug("Logging map of caches with " + map.size() + " caches:");
    for (String cacheName : map.keySet()) {
      logger.debug("Cache: " + cacheName);
    }
  }

  @Override
  public boolean isCached(String name) {
    return map.get(name) != null;
  }

  @Override
  public IDtoCache<IDbDto> createCache(String name) {
    IDtoCache<IDbDto> cache = null;
    if (map != null) {
      cache = new DtoCache<IDbDto>(name);
      map.put(name, cache);
      logger.debug("Created Java map cache " + name);
    }

    return cache;
  }

  @Override
  public IDtoCache<IDbDto> getCache(String name) {
    IDtoCache<IDbDto> cache = null;
    if (map != null) {
      cache = map.get(name);
    }
    return cache;
  }

  @Override
  public void clearCache(String name) {
    IDtoCache<IDbDto> cache = map.get(name);
    if (cache != null) {
      cache.clear();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public synchronized <T extends IDbDto> T getCachedObject(IPrimaryKey key,
                                                           String subsystem) {
    T object = null;
    // Only proceed if cache name and key are not null
    if (key.getCacheName() != null && key != null) {
      IDtoCache<IDbDto> cache = (IDtoCache<IDbDto>)getCache(key.getCacheName());
      if (cache == null) {
        // No cache yet, create it.
        cache = createCache(key.getCacheName());
      }
      object = (T)cache.get(key);
    }
    return object;
  }

  @Override
  public <T extends IDbDto> T getCachedObject(IPrimaryKey key, int expireTime,
                                              String subsystem) {
    // expire time ignored for Java cache
    return getCachedObject(key, subsystem);
  }

  @Override
  public synchronized <T extends IDbDto> void putObjectInCache(String cacheName,
                                                               int expireTime,
                                                               T object) {
    // expire time ignored for Java cache

    // Only proceed if cache name and object are not null
    if (cacheName != null && object != null) {
      IDtoCache<IDbDto> cache = getCache(cacheName);
      if (cache == null) {
        // No cache yet, create it.
        cache = createCache(cacheName);
      }
      cache.cache(object);
      logger.debug("Cached " + object.getPrimaryKey().getStringKey()
                   + " in Java cache " + cacheName);
    }
  }

  @Override
  public synchronized void removeObjectFromCache(String cacheName,
                                                 IPrimaryKey key) {
    // Only proceed if cache name and key are not null
    if (cacheName != null && key != null) {
      IDtoCache<IDbDto> cache = getCache(cacheName);
      cache.remove(key);
      logger.debug("Removed object from " + cacheName + " cache: "
                   + key.getStringKey());
    }
  }

  @Override
  public void clearTemporaryCaches() {
    // No temp caches to clear
  }

  @Override
  public void clearAllCaches() {
    // Clear all the in-memory caches.
    map.clear();
  }
}
