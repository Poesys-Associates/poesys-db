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


import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.TimeoutException;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.command.TextCommandFactory;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.impl.KetamaMemcachedSessionLocator;
import net.rubyeye.xmemcached.utils.AddrUtil;

import org.apache.log4j.Logger;

import com.poesys.db.DbErrorException;
import com.poesys.db.Message;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDtoCache;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * A singleton object that manages data access objects in the system. This
 * implementation of the DAO Manager enables the use of memcached, a distributed
 * cache system. This class contains the actual calls to the xmemcached Java
 * memcached client, implementing the Bridge design pattern to wrap the
 * xmemcached API. It also contains the DTO registry that contains the
 * de-serialized Java objects currently being retrieved. This registry prevents
 * infinite loops and object duplication during serialization of nested object
 * hierarchies by checking the registry for objects before getting them from the
 * external cache, so that once an object is retrieved, that tree branch will
 * not be duplicated by a later cache query.
 * </p>
 * <p>
 * <strong> You should minimize the lifetime of the Java DTOs retrieved from the
 * cache after completion of a query. The longer you hold these objects, the
 * more likely it is they will be duplicated in memory allocated by different
 * queries and the more likely it is for them to go stale. If the DTO is read
 * only, the lifetime is not critical, but for read/write DTOs, you should get
 * rid of the Java objects as soon as you can after using the data they contain.
 * Then memcached will be the source for the most current data, and if it's not
 * there, you will get it from the ultimate source, the database. </strong>
 * </p>
 * <p>
 * Originally the cache access methods were synchronized. I have removed the
 * synchronization to see if it solves our concurrency problem. There really
 * isn't any reason for synchronization in the methods, as the data is stored
 * in threadsafe data structures and is mostly read only. I may need to reanalyze
 * this as time goes on :) .
 * </p>
 * 
 * @author Robert J. Muller
 * @see DaoMemcachedFactory
 */
public final class MemcachedDaoManager implements IDaoManager {
  /** Logger for debugging */
  private static final Logger logger =
    Logger.getLogger(MemcachedDaoManager.class);

  /** Singleton manager instance */
  private static IDaoManager manager = null;

  /** Singleton in-memory cache manager instance */
  private static IDaoManager cacheManager = null;

  /** Singleton client instance */
  private static MemcachedClient client = null;

  /** Name of the memcached properties resource bundle */
  protected static final String BUNDLE = "com.poesys.db.memcached";

  /** The resource bundle containing the memcached properties. */
  protected static final ResourceBundle properties =
    ResourceBundle.getBundle(BUNDLE);

  // Error messages from the Poesys resource bundle.

  /** Error message resource for the memcached-io-error error message */
  private static final String MEMCACHED_IO =
    "com.poesys.db.dao.query.msg.memcached_io";

  /** Error message resource for the memcached-error error message */
  private static final String MEMCACHED_GET =
    "com.poesys.db.dao.query.msg.memcached_get";

  /** Error message resource for the memcached-timeout error message */
  private static final String MEMCACHED_TIMEOUT =
    "com.poesys.db.dao.query.msg.memcached_timeout";

  /** Error message resource for the memcached-interrupt error message */
  private static final String MEMCACHED_INTERRUPT =
    "com.poesys.db.dao.query.msg.memcached_interrupt";

  /** Error message resource for failed attempt to get xmemcached client */
  private static final String MEMCACHED_CLIENT =
    "com.poesys.db.dao.query.msg.memcached_client";

  /** Error message resource for failed attempt to set protocol */
  private static final String MEMCACHED_UNKNOWN_PROTOCOL =
    "com.poesys.db.dao.query.msg.memcached_unknown_protocol";

  /** Memcached configuration property servers */
  private static final String MEMCACHED_PROP_SERVERS = "servers";
  /** Memcached configuration property minimum clients */
  private static final String MEMCACHED_PROP_MIN_CLIENTS = "min_clients";
  /** Memcached configuration property protocol */
  private static final String MEMCACHED_PROP_PROTOCOL = "protocol";
  /** Memcached configuration property protocol */
  private static final String MEMCACHED_PROP_TIMEOUT = "timeout";
  /** Memcached configuration value binary protocol */
  private static final String BINARY = "binary";
  /** Memcached configuration value text protocol */
  private static final String TEXT = "text";

  /**
   * Disable the default constructor.
   */
  MemcachedDaoManager() {
  }

  /**
   * Get the singleton instance of the IDaoManager for a memcached cache.
   * 
   * @return the DAO manager
   */
  public static IDaoManager getInstance() {
    if (manager == null) {
      manager = new MemcachedDaoManager();
    }
    if (cacheManager == null) {
      // Get the singleton cache manager for in-memory storage management.
      cacheManager = CacheDaoManager.getInstance();
    }
    return manager;
  }

  @Override
  public <T extends IDbDto, C extends Collection<T>> IDaoFactory<T> getFactory(String name,
                                                                               String subsystem,
                                                                               Integer expiration) {
    return new DaoMemcachedFactory<T>(name, subsystem, expiration);
  }

  /**
   * Get a xmemcached client from the client pool. If there are no clients
   * available, the pool manager will throw a database exception.
   * 
   * @return an xmemcached client
   * @throws IOException when there is a problem creating an xmemcached client
   * @throws DbErrorException when the client pool is exhausted
   * @throws MemcachedException when the properties file specifies an unknown
   *           protocol (binary or text are the possible values)
   */
  private MemcachedClient getClient() throws DbErrorException, IOException,
      MemcachedException {
    // If client singleton not yet built, build it. Use xmemcached internal
    // connection pool to minimize clients and solve too-many-open-files
    // problem. Use the optimized hash-based Ketama session locator as well.
    if (client == null) {
      String servers = properties.getString(MEMCACHED_PROP_SERVERS);
      MemcachedClientBuilder builder =
        new XMemcachedClientBuilder(AddrUtil.getAddresses(servers));
      Integer poolSize =
        new Integer(properties.getString(MEMCACHED_PROP_MIN_CLIENTS));
      String protocol = properties.getString(MEMCACHED_PROP_PROTOCOL);
      if (BINARY.equalsIgnoreCase(protocol)) {
        builder.setCommandFactory(new BinaryCommandFactory());
      } else if (TEXT.equalsIgnoreCase(protocol)) {
        builder.setCommandFactory(new TextCommandFactory());
      } else {
        Object[] args = new Object[1];
        args[0] = protocol;
        String msg = Message.getMessage(MEMCACHED_UNKNOWN_PROTOCOL, args);
        throw new MemcachedException(msg);
      }
      builder.setConnectionPoolSize(poolSize);
      builder.setSessionLocator(new KetamaMemcachedSessionLocator());

      client = builder.build();
      // Set timeout of operations to a millisecond value.
      long opTimeout = new Long(properties.getString(MEMCACHED_PROP_TIMEOUT));
      client.setOpTimeout(opTimeout);
    }

    checkClient(client);
    return client;
  }

  /**
   * Check that the client exists and throw an appropriate runtime exception if
   * not.
   * 
   * @param client the client to check
   */
  private void checkClient(MemcachedClient client) {
    if (client == null) {
      DbErrorException e1 = new DbErrorException(MEMCACHED_CLIENT);
      throw e1;
    }
  }

  /**
   * Release a client back to the client pool. This makes the memcached
   * connection available for use by other tasks.
   * 
   * @param client the client to release.
   */
  private void releaseClient(MemcachedClient client) {
    // Does nothing, no external connection pool used
  }

  @Override
  public synchronized void logMetaData() {
    MemcachedClient memcachedClient = null;
    try {
      memcachedClient = getClient();
    } catch (IOException e) {
      DbErrorException e1 = new DbErrorException(MEMCACHED_IO, e);
      throw e1;
    } catch (MemcachedException e) {
      DbErrorException e1 = new DbErrorException(e.getMessage(), e);
      throw e1;
    }

    Map<InetSocketAddress, Map<String, String>> stats;
    try {
      stats = memcachedClient.getStats();
      for (InetSocketAddress address : stats.keySet()) {
        logger.info("Statistics for server address "
                    + address.getHostName()
                    + ":"
                    + address.getPort()
                    + ":");
        Map<String, String> statMap = stats.get(address);
        for (String stat : statMap.keySet()) {
          logger.info(stat + ":" + statMap.get(stat));
        }
        logger.info("Statistics complete for server address "
                    + address.getHostName()
                    + ":"
                    + address.getPort());
      }
    } catch (MemcachedException e) {
      List<String> errors = new ArrayList<String>(1);
      errors.add("None");
      DbErrorException e1 = new DbErrorException(MEMCACHED_GET, e);
      e1.setParameters(errors);
      throw e1;
    } catch (InterruptedException e) {
      // log and ignore
      String[] args = new String[1];
      args[0] = "None";
      logger.warn(Message.getMessage(MEMCACHED_INTERRUPT, args));
    } catch (TimeoutException e) {
      List<String> errors = new ArrayList<String>(1);
      errors.add("None");
      DbErrorException e1 = new DbErrorException(MEMCACHED_TIMEOUT, e);
      e1.setParameters(errors);
      throw e1;
    }
  }

  @Override
  public boolean isCached(String name) {
    // A specific class is always cached, as memcached does not implement a
    // separate cache for classes, just for individual objects.
    return true;
  }

  @Override
  public void clearCache(String name) {
    // TODO Clear objects beginning with the name
    // Memcached does not allow cache clearing, just clear temp cache
    clearTemporaryCaches();
  }

  @Override
  public <T extends IDbDto> IDtoCache<T> createCache(String name) {
    // No caching of class-specific objects, returns no cache
    return null;
  }

  @Override
  public <T extends IDbDto> IDtoCache<? extends IDbDto> getCache(String name) {
    // No caching of class-specific objects, does nothing and returns no cache
    return null;
  }

  @Override
  public <T extends IDbDto> T getCachedObject(IPrimaryKey key) {
    MemcachedClient memcachedClient = null;
    T object = null;

    try {
      // Check the in-memory cache for the object first.
      object = cacheManager.getCachedObject(key);
      if (object == null) {
        // Not previously de-serialized, get it from the cache.
        logger.debug("Getting object " + key.getStringKey() + " from the cache");
        memcachedClient = getClient();
        object = memcachedClient.get(key.getStringKey(), 3000);
        if (object != null) {
          logger.debug("Retrieved object "
                       + key.getStringKey()
                       + " from the cache");
        } else {
          logger.debug("No object " + key.getStringKey() + " in the cache");
        }
      }
    } catch (IOException e) {
      DbErrorException e1 = new DbErrorException(MEMCACHED_IO, e);
      throw e1;
    } catch (MemcachedException e) {
      List<String> errors = new ArrayList<String>(1);
      errors.add(key.getStringKey());
      DbErrorException e1 = new DbErrorException(MEMCACHED_GET, e);
      e1.setParameters(errors);
      logger.error(Message.getMessage(MEMCACHED_GET, errors.toArray()), e);
      throw e1;
    } catch (TimeoutException e) {
      List<String> errors = new ArrayList<String>(1);
      errors.add(key.getStringKey());
      DbErrorException e1 = new DbErrorException(MEMCACHED_TIMEOUT, e);
      e1.setParameters(errors);
      throw e1;
    } catch (InterruptedException e) {
      // log and ignore
      String[] args = new String[1];
      args[0] = key.getStringKey();
      logger.warn(Message.getMessage(MEMCACHED_INTERRUPT, args));
    } finally {
      if (memcachedClient != null) {
        releaseClient(memcachedClient);
      }
    }

    return object;
  }

  @Override
  public <T extends IDbDto> void putObjectInCache(String cacheName,
                                                               int expireTime,
                                                               T object) {

    MemcachedClient memcachedClient = null;
    try {
      memcachedClient = getClient();
    } catch (IOException e) {
      DbErrorException e1 = new DbErrorException(MEMCACHED_IO, e);
      throw e1;
    } catch (MemcachedException e) {
      DbErrorException e1 = new DbErrorException(e.getMessage(), e);
      throw e1;
    }

    try {
      String key = object.getPrimaryKey().getStringKey();
      memcachedClient.set(key, expireTime, object);
      logger.debug("Cached object \""
                   + key
                   + "\" of type "
                   + object.getClass().getName()
                   + " with expiration time "
                   + expireTime
                   + "ms");
    } catch (TimeoutException e) {
      List<String> errors = new ArrayList<String>(1);
      errors.add(object.getPrimaryKey().getStringKey());
      DbErrorException e1 = new DbErrorException(MEMCACHED_TIMEOUT, e);
      e1.setParameters(errors);
      throw e1;
    } catch (InterruptedException e) {
      // log and ignore
      String[] args = new String[1];
      args[0] = object.getPrimaryKey().getStringKey();
      logger.warn(Message.getMessage(MEMCACHED_INTERRUPT, args));
    } catch (MemcachedException e) {
      List<String> errors = new ArrayList<String>(1);
      errors.add(object.getPrimaryKey().getStringKey());
      DbErrorException e1 = new DbErrorException(MEMCACHED_GET, e);
      e1.setParameters(errors);
      throw e1;
    } finally {
      releaseClient(memcachedClient);
    }
  }

  @Override
  public void removeObjectFromCache(String cacheName,
                                                 IPrimaryKey key) {
    MemcachedClient memcachedClient = null;
    try {
      memcachedClient = getClient();
    } catch (IOException e) {
      DbErrorException e1 = new DbErrorException(MEMCACHED_IO, e);
      throw e1;
    } catch (MemcachedException e) {
      DbErrorException e1 = new DbErrorException(e.getMessage(), e);
      throw e1;
    }

    // Check the cache for the object first, remove it if it's there.
    IDbDto object = cacheManager.getCachedObject(key);
    if (object != null) {
      cacheManager.removeObjectFromCache(cacheName, key);
    }

    try {
      memcachedClient.deleteWithNoReply(key.getStringKey());
      logger.debug("Removed cached object " + key.getStringKey());
    } catch (InterruptedException e) {
      // log and ignore
      String[] args = new String[1];
      args[0] = key.getStringKey();
      logger.warn(Message.getMessage(MEMCACHED_INTERRUPT, args));
    } catch (MemcachedException e) {
      List<String> errors = new ArrayList<String>(1);
      errors.add(key.getStringKey());
      DbErrorException e1 = new DbErrorException(MEMCACHED_GET, e);
      e1.setParameters(errors);
      throw e1;
    } finally {
      releaseClient(memcachedClient);
    }
  }

  @Override
  public void clearTemporaryCaches() {
    // Clear all the in-memory caches
    cacheManager.clearAllCaches();
  }

  @Override
  public void clearAllCaches() {
    // Clear the temp caches.
    clearTemporaryCaches();
    // Note: memcached does not support clearing the cache.
    // You need to restart the individual servers, then restart the app.
  }
}
