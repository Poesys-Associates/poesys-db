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


import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDtoCache;


/**
 * A singleton object that manages data access objects in the system. The
 * manager maintains a singleton map of caches which is available on demand and
 * message-handling for the JMS PoesysCacheDelete messaging topic, which enables
 * clustered node cache object removal. This implementation of DAO Manager
 * returns the same DAO factory singleton as the CacheDaoManager.
 * 
 * @author Robert J. Muller
 * 
 * @see CacheDaoManager
 */
public final class MessagingDaoManager extends CacheDaoManager implements
    IDaoManager {
  /** Logger for debugging */
  private static final Logger logger =
    Logger.getLogger(MessagingDaoManager.class);
  
  private static String subsystem;

  /**
   * Create a MessagingDaoManager object.
   * 
   * @param classSubsystem the subsystem of the DTO class
   */
  private MessagingDaoManager(String classSubsystem) {
    subsystem = classSubsystem;
  }

  /**
   * Get the manager instance.
   * 
   * @return the instance
   */
  public static IDaoManager getInstance() {
    if (manager == null) {
      manager = new MessagingDaoManager(subsystem);
      map = new ConcurrentHashMap<String, IDtoCache<IDbDto>>();
      // Start the cache listener for the delete topic, registering the
      // manager with the listener for later cache access.
      CacheListenerExecutor exec = new CacheListenerExecutor();
      exec.execute(new CacheMessageListener(subsystem));
    }
    return manager;
  }

  @Override
  public void logMetaData() {
    super.logMetaData();
    logger.debug("Messaging enabled");
  }
}
