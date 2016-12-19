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


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A Factory Method static wrapper that makes singleton DAO managers for
 * subsystems available through a static method (globally). The system needs to
 * call the initialization method of the appropriate type for each subsystem,
 * then all code can access the manager directly through the getInstance()
 * method. The default is the CacheDaoManager. You can call the initialization
 * methods any number of times, but only the first allocates a singleton for the
 * subsystem unless you subsequently clear that manager. The collection of
 * managers is thread safe. Note that the underlying managers are actually
 * singletons for each kind of manager implementation, so subsystems may
 * share the same manager--they are reentrant.
 * 
 * @author Robert J. Muller
 */
public class DaoManagerFactory {
  /** The singleton manager */
  private static Map<String, IDaoManager> managers =
    new ConcurrentHashMap<String, IDaoManager>();

  /**
   * Disable instance creation.
   */
  private DaoManagerFactory() {
  }

  /**
   * Initialize a direct-data-access DAO manager.
   * 
   * @param subsystem the name of the subsystem, a fully qualified package name
   * @return the DAO Manager
   */
  public static IDaoManager initDirectManager(String subsystem) {
    if (managers.get(subsystem) == null) {
      managers.put(subsystem, new DirectDaoManager());
    }
    return getManager(subsystem);
  }

  /**
   * Initialize a cache-map DAO manager.
   * 
   * @param subsystem the subsystem of the DTO classes, a fully-qualified package name
   * @return the DAO Manager
   */
  public static IDaoManager initCacheManager(String subsystem) {
    if (managers.get(subsystem) == null) {
      managers.put(subsystem, CacheDaoManager.getInstance(subsystem));
    }
    return getManager(subsystem);
  }

  /**
   * Initialize a messaging, cache-map DAO manager.
   * 
   * @param subsystem the subsystem of the DTO classes, a fully-qualified package name
   * @return the DAO manager
   */
  public static IDaoManager initMessagingManager(String subsystem) {
    if (managers.get(subsystem) == null) {
      managers.put(subsystem, MessagingDaoManager.getInstance());
    }
    return getManager(subsystem);
  }

  /**
   * Initialize a memcached DAO manager.
   * 
   * @param subsystem the subsystem of the DTO classes, a fully-qualified package name
   * @return the DAO manager
   */
  public static IDaoManager initMemcachedManager(String subsystem) {
    if (managers.get(subsystem) == null) {
      managers.put(subsystem, MemcachedDaoManager.getInstance());
    }
    return getManager(subsystem);
  }

  /**
   * Get the manager, defaulting to initializing a cache-map manager if no
   * manager yet exists.
   * @param subsystem the subsystem of the DTO classes, a fully-qualified package name
   * 
   * @return the singleton manager instance for the subsystem
   */
  public static IDaoManager getManager(String subsystem) {
    IDaoManager manager = managers.get(subsystem);
    if (manager == null) {
      manager = initCacheManager(subsystem);
    }
    return manager;
  }

  /**
   * Clear the manager for a specific subsystem. You should initialize the
   * subsystem manager again as soon as possible after calling this method.
   * 
   * @param subsystem the name of the subsystem, a fully qualified package name
   */
  public static void clearManager(String subsystem) {
    if (managers.get(subsystem) != null) {
      managers.remove(subsystem);
    }
  }
}
