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

import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDtoCache;
import com.poesys.db.pk.IPrimaryKey;


/**
 * The interface for the singleton Data Access Object (DAO) Manager for the
 * Poesys/DB system. Various implementations of this class implement the
 * varieties of data access for Poesys/DB (for example, no caching, memory
 * mapped caching, memcached distributed caching).
 * 
 * @author Robert J. Muller
 */
public interface IDaoManager {
  /**
   * Get a DAO factory of an appropriate type based on the implementation. A DAO
   * factory is an Abstract Factory that generates various kinds of
   * data-access-related objects relating to DTOs of a particular type.
   * 
   * @param <T> the type of IDbDto that the factory caches
   * @param <C> the type of collection used for inserting the DTOs
   * 
   * @param name the class name of the object that the DAO processes, used for
   *          naming a potential cache of such objects; the name may be null if
   *          the system does not cache objects.
   * @param subsystem the name of the subsystem that contains the class T
   * @param expiration the time in milliseconds that an object remains cached; 0
   *          means expire immediately, and the default is MAX_INT
   * @return the DAO factory
   * @see IDaoFactory
   * @see IDbDto
   */
  <T extends IDbDto, C extends Collection<T>> IDaoFactory<T> getFactory(String name,
                                                                        String subsystem,
                                                                        Integer expiration);

  /**
   * Log the meta data, if there are any, for the data-access scheme. Cache meta
   * data includes any descriptive information likely to be of use in the
   * implemented caching scheme.
   * 
   */
  void logMetaData();

  /**
   * <p>
   * Is a class of IDbDto objects (identified by fully qualified class name)
   * cached in memory?
   * </p>
   * <p>
   * <strong>Note:</strong> a distributed caching scheme may cache the objects
   * of the class but not the class, in which case this method returns false, as
   * there is no need for the information.
   * </p>
   * 
   * @param name the fully qualified class name
   * @return true if the system caches objects of the class, false if not
   */
  boolean isCached(String name);

  /**
   * Get the cache for a named class, if such a cache exists.
   * 
   * @param <T> the type of IDbDto cached
   * 
   * @param name the name of the class
   * @return the cache of IDbDto objects
   */
  <T extends IDbDto> IDtoCache<? extends IDbDto> getCache(String name);

  /**
   * Create a cache of IDbDto objects of class T with a specified name. If the
   * manager is not a caching manager, or if the caching scheme in the
   * implementation does not implement class-specific caching, this method
   * returns null.
   * 
   * @param <T> the specific class of object contained in the cache
   * @param name the fully qualified name of the cached class T
   * @return the new cache of objects of type T or null if the implementation is
   *         not a class-caching manager
   */
  <T extends IDbDto> IDtoCache<T> createCache(String name);

  /**
   * Clear the cache of objects specified by a fully qualified class name. This
   * will clear the cache only if the caching scheme has a way to identify the
   * objects of a specific class. If there is no caching, this method does
   * nothing.
   * 
   * @param name the fully qualified name of the cached class
   */
  void clearCache(String name);

  /**
   * Get an object identified by a primary key out of a named cache. The object
   * may be of any type. The cache name is optional for caching systems that do
   * not have separate caches for each class. This method does not reset the
   * expire time for the object in the cache.
   * 
   * @param <T> the type of object to look up
   * @param key the unique identifier of the object you want to retrieve; also
   *          contains the cache name for in-memory cache lookup
   * @param subsystem the subsystem of the DTO class
   * @return the object
   */
  <T extends IDbDto> T getCachedObject(IPrimaryKey key, String subsystem);

  /**
   * Get an object identified by a primary key out of a named cache. The object
   * may be of any type. The cache name is optional for caching systems that do
   * not have separate caches for each class. This method sets the expire time
   * to the amount specified, so that accessed objects stay in the cache.
   * 
   * @param <T> the type of object to look up
   * @param key the unique identifier of the object you want to retrieve; also
   *          contains the cache name for in-memory cache lookup
   * @param subsystem the subsystem of the DTO class
   * @param expireTime the milliseconds until the object expires from the cache
   * @return the object
   */
  <T extends IDbDto> T getCachedObject(IPrimaryKey key, int expireTime,
                                       String subsystem);

  /**
   * Put an object into a named cache. The object may be of any type. The cache
   * name is optional for caching systems that do not have separate caches for
   * each class. The expire time tells the cache manager to remove the object
   * from the cache after the specified number of milliseconds; 0 means expire
   * the object immediately, MAX_INT will keep it as long as possible. The
   * default value is MAX_INT.
   * 
   * @param <T> the type of object to cache
   * @param cacheName the name of the class cache
   * @param expireTime the milliseconds until the object expires from the cache
   * @param object the object to cache
   */
  <T extends IDbDto> void putObjectInCache(String cacheName, int expireTime,
                                           T object);

  /**
   * Remove an object specified by a primary key from the object cache. The
   * cacheName will be null if there are no separate caches for different
   * classes.
   * 
   * @param cacheName the name of the cache from which to remove the object
   * @param key the unique identifier for the object
   */
  void removeObjectFromCache(String cacheName, IPrimaryKey key);

  /**
   * Clear any temporary caches created inside the DAO Manager.
   */
  void clearTemporaryCaches();

  /**
   * Clear all caches associated with the DAO Manager.
   */
  void clearAllCaches();
}