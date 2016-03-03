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

import org.apache.log4j.Logger;

import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDtoCache;
import com.poesys.db.pk.IPrimaryKey;


/**
 * A singleton object that manages data access objects in the system with no
 * caching, implying direct access to a persistent store for all data access.
 * 
 * @author Robert J. Muller
 */
public final class DirectDaoManager implements IDaoManager {
  /** Logger for debugging */
  private static final Logger logger = Logger.getLogger(DirectDaoManager.class);

  private static IDaoManager manager = null;

  /**
   * Create a DirectDaoManager object.
   */
  DirectDaoManager() {
  }

  /**
   * Get the singleton DAO Manager implemented with no caching
   * 
   * @return the DAO Manager
   */
  public static IDaoManager getInstance() {
    if (manager == null) {
      manager = new DirectDaoManager();
    }
    return manager;
  }

  @Override
  public <T extends IDbDto, C extends Collection<T>> IDaoFactory<T> getFactory(
                                                                               String name,
                                                                               String subsystem,
                                                                               Integer expiration) {
    return new DaoDirectFactory<T>();
  }

  @Override
  public void logMetaData() {
    logger.debug("System is using direct data access");
  }

  @Override
  public boolean isCached(String name) {
    return false;
  }

  @Override
  public void clearCache(String name) {
    // Does nothing, no caching

  }

  @Override
  public <T extends IDbDto> IDtoCache<T> createCache(String name) {
    // No caching, no cache created
    return null;
  }

  @Override
  public <T extends IDbDto> IDtoCache<? extends IDbDto> getCache(String name) {
    // No caching, no cache cleared or returned
    return null;
  }


  @Override
  public <T extends IDbDto> T getCachedObject(IPrimaryKey key, int expireTime) {
    // no caching
    return null;
  }
  
  @Override
  public synchronized <T extends IDbDto> T getCachedObject(IPrimaryKey key) {
    return null; // no caching
  }

  @Override
  public synchronized <T extends IDbDto> void putObjectInCache(
                                                               String cacheName,
                                                               int expireTime,
                                                               T object) {
    // Does nothing, no caching
  }

  @Override
  public synchronized void removeObjectFromCache(String cacheName,
                                                 IPrimaryKey key) {
    // Does nothing, no caching
  }

  @Override
  public void clearTemporaryCaches() {
    // No temp caches to clear
  }

  @Override
  public void clearAllCaches() {
    // No caches to clear
  }
}
