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


import com.poesys.db.pk.IPrimaryKey;


/**
 * An interface for a cache of data transfer objects (DTOs); you can cache a
 * DTO, get a DTO from the cache using its primary key, remove a DTO from the
 * cache using its primary key, or clear the entire cache.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to cache
 */
public interface IDtoCache<T extends IDbDto> {

  /**
   * Cache the object.
   * 
   * @param object the object to cache
   */
  void cache(T object);

  /**
   * Get a cached object using a primary key. If the object is not cached, the
   * method returns null.
   * 
   * @param key the primary key for the object
   * @return the object or null if the object is not cached
   */
  T get(IPrimaryKey key);

  /**
   * Remove a cached object using a primary key. If the object is not cached,
   * the method does nothing. If messaging is enabled, the method requests that
   * the object be removed from any remote caches. If distributed caching is
   * used, the method removes the cached object from the distributed cache.
   * 
   * @param key the key that identifies the object to remove
   */
  void remove(IPrimaryKey key);

  /**
   * Remove a cached object from the cache using a primary key. If the object is
   * not cached, the method does nothing. This method removes only a locally
   * cached object and does not request removal of the object from remote
   * caches. This method is for use in messaging-based caches, where receipt of
   * a subscribed topic results in local removal.
   * 
   * @param key the key that identifies the object to remove
   */
  void removeLocally(IPrimaryKey key);

  /**
   * Clear the cache, removing all cached objects of type T. This leaves the
   * cache itself but makes it empty. This method has no effect on a
   * distributed cache that has no iteration or query API, such as memcached.
   */
  void clear();
}