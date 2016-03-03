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
package com.poesys.db.dao;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.ClientMode;
import net.spy.memcached.DefaultConnectionFactory;
import net.spy.memcached.MemcachedClient;

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
 * cache system. This class contains the actual calls to the spymemcached Java
 * memcached client, implementing the Bridge design pattern to wrap the
 * spymemcached API. It also contains the DTO registry that contains the
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
 * Note that no methods are synchronized, as most cached objects are stored in
 * threadsafe data structures or are used and disposed of in the same method
 * with no opportunity for concurrent access.
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

  private static final String MEMCACHED_IO =
    "com.poesys.db.dao.query.msg.memcached_io";
  private static final String INVALID_PORT =
    "com.poesys.db.dao.query.msg.memcached_invalid_port";
  private static final String MEMCACHED_QUEUE_FULL =
    "com.poesys.db.dao.query.msg.memcached_queue_full";
  private static final String MEMCACHED_CLIENT =
    "com.poesys.db.dao.query.msg.memcached_client";
  /** Error message resource for failed attempt to set protocol */
  private static final String MEMCACHED_UNKNOWN_PROTOCOL =
    "com.poesys.db.dao.query.msg.memcached_unknown_protocol";
  private static final String MEMCACHED_GET_ERROR =
    "com.poesys.db.dao.query.msg.memcached_get";
  private static final String TIMEOUT_MSG =
    "com.poesys.db.dao.query.msg.memcached_timeout";
  private static final String STATS_COMPLETE =
    "com.poesys.db.dao.query.msg.memcached_stats_complete";
  private static final Object STATS_ERROR =
    "com.poesys.db.dao.query.msg.memcached_stats_error";

  // Memcached options from property file

  /** Memcached configuration property servers */
  private static final String MEMCACHED_PROP_SERVERS = "servers";
  /** Memcached configuration property protocol */
  private static final String MEMCACHED_PROP_PROTOCOL = "protocol";
  /** Memcached configuration property for client get timeout */
  private static final String MEMCACHED_PROP_TIMEOUT = "client_timeout";
  /** Memcached configuration property for retries after client get timeout */
  private static final String MEMCACHED_PROP_RETRIES = "client_retries";
  /** Memcached configuration value binary protocol */
  private static final String BINARY = "binary";
  /** Memcached configuration value text protocol */
  private static final String TEXT = "text";

  /* Memcached configuration value for 5-second timeout retries */
  private static final int TIMEOUT =
    new Integer(properties.getString(MEMCACHED_PROP_TIMEOUT));
  /* Memcached configuration value for 5-second timeout retries */
  private static final int TIMEOUT_RETRIES =
    new Integer(properties.getString(MEMCACHED_PROP_RETRIES));

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
   * Get a spymemcached client. The client will be a text or binary client
   * depending on the protocol property in the property file.
   * 
   * @return a spymemcached client
   * @throws IOException when there is a problem creating a spymemcached client
   */
  private MemcachedClient getClient() throws IOException {
    List<InetSocketAddress> sockets = getSockets();

    // If client singleton not yet built, build it.
    if (client == null) {
      String protocol = properties.getString(MEMCACHED_PROP_PROTOCOL);
      if (BINARY.equalsIgnoreCase(protocol)) {
        client = new MemcachedClient(new BinaryConnectionFactory(ClientMode.Static), sockets);
      } else if (TEXT.equalsIgnoreCase(protocol)) {
        client = new MemcachedClient(new DefaultConnectionFactory(ClientMode.Static), sockets);
      } else {
        Object[] args = new Object[1];
        args[0] = protocol;
        String msg = Message.getMessage(MEMCACHED_UNKNOWN_PROTOCOL, args);
        throw new DbErrorException(msg);
      }
      if (client == null) {
        throw new DbErrorException(MEMCACHED_CLIENT);
      }
    }

    return client;
  }

  /**
   * Get the hostname/port combinations for the sockets to which the client will
   * connect. The sockets are in a single property string in the format
   * <hostname>:<port>,... where <hostname> is a valid authority (domain or IP
   * address) and <port> is an integer between 0 and 65,535.
   *
   * @return a list of Internet Socket Address objects
   */
  private List<InetSocketAddress> getSockets() {
    List<InetSocketAddress> addresses = new ArrayList<InetSocketAddress>();
    String[] sockets = properties.getString(MEMCACHED_PROP_SERVERS).split(",");
    for (String socket : sockets) {
      String[] parts = socket.split(":");
      if (parts.length == 2 && !parts[0].isEmpty() && !parts[1].isEmpty()) {
        // server and port both present, go ahead with creating socket
        try {
          addresses.add(new InetSocketAddress(parts[0], new Integer(parts[1])));
        } catch (NumberFormatException e) {
          List<String> errors = new ArrayList<String>(1);
          errors.add(parts[1]);
          DbErrorException e1 = new DbErrorException(INVALID_PORT, e);
          e1.setParameters(errors);
          throw e1;
        }
      }
    }
    return addresses;
  }

  @Override
  public boolean isCached(String name) {
    // A specific class is always cached, as memcached does not implement a
    // separate cache for classes, just for individual objects.
    return true;
  }

  @Override
  public void clearCache(String name) {
    // Memcached does not support cache clearing, just clear temp cache
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
  public <T extends IDbDto> T getCachedObject(IPrimaryKey key, int expireTime) {
    T object = getCachedObject(key);
    if (object != null) {
      client.touch(key.getStringKey(), expireTime);
    }
    return object;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends IDbDto> T getCachedObject(IPrimaryKey key) {
    MemcachedClient client = null;
    T object = null;

    try {
      // Check the in-memory cache for the object first.
      object = cacheManager.getCachedObject(key);
      if (object == null) {
        // Not previously de-serialized, get it from the cache.
        logger.debug("Getting object " + key.getStringKey() + " from the cache");
        client = getClient();

        // Get the object asynchronously to handle the server being unavailable.
        Future<Object> future = client.asyncGet(key.getStringKey());
        int retries = TIMEOUT_RETRIES;
        while (object == null && retries > 0) {
          try {
            object = (T)future.get(TIMEOUT, TimeUnit.SECONDS);
            retries--;
          } catch (TimeoutException e) {
            Object[] args = new Object[1];
            args[0] = key.getStringKey();
            logger.debug(Message.getMessage(TIMEOUT_MSG, args));
            future.cancel(true);
            retries--;
          } catch (Exception e) {
            // InterruptedException, ExecutionException, or RuntimeException
            Object[] args = new Object[1];
            args[0] = key.getStringKey();
            logger.error(Message.getMessage(MEMCACHED_GET_ERROR, args), e);
            future.cancel(true);
            throw new DbErrorException(Message.getMessage(MEMCACHED_GET_ERROR, args));
          }
        }

        if (object != null) {
          logger.debug("Retrieved object " + key.getStringKey()
                       + " from the cache");
        } else {
          logger.debug("No object " + key.getStringKey() + " in the cache");
        }
      }
    } catch (IOException e) {
      throw new DbErrorException(MEMCACHED_IO, e);
    } finally {
      client.shutdown();
    }

    return object;
  }

  @Override
  public <T extends IDbDto> void putObjectInCache(String cacheName,
                                                  int expireTime, T object) {

    MemcachedClient client = null;
    try {
      client = getClient();
    } catch (IOException e) {
      throw new DbErrorException(MEMCACHED_IO, e);
    }

    try {
      String key = object.getPrimaryKey().getStringKey();
      client.set(key, expireTime, object);
      logger.debug("Cached object \"" + key + "\" of type "
                   + object.getClass().getName() + " with expiration time "
                   + expireTime + "ms");
    } catch (IllegalStateException e) {
      List<String> errors = new ArrayList<String>(1);
      errors.add(object.getPrimaryKey().getStringKey());
      DbErrorException e1 = new DbErrorException(MEMCACHED_QUEUE_FULL, e);
      e1.setParameters(errors);
      throw e1;
    }
  }

  @Override
  public void removeObjectFromCache(String cacheName, IPrimaryKey key) {
    MemcachedClient memcachedClient = null;
    try {
      memcachedClient = getClient();
    } catch (IOException e) {
      DbErrorException e1 = new DbErrorException(MEMCACHED_IO, e);
      throw e1;
    }

    // Check the cache for the object first, remove it if it's there.
    IDbDto object = cacheManager.getCachedObject(key);
    if (object != null) {
      cacheManager.removeObjectFromCache(cacheName, key);
    }

    try {
      // asynch, object may get deleted after delay
      memcachedClient.delete(key.getStringKey());
      logger.debug("Removed cached object " + key.getStringKey());
    } catch (IllegalStateException e) {
      // log and ignore
      String[] args = new String[1];
      args[0] = key.getStringKey();
      logger.warn(Message.getMessage(MEMCACHED_QUEUE_FULL, args));
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
    // Note: memcached does not support clearing a set of objects from the
    // cache. You need to restart the individual servers, then restart the app.
  }

  @Override
  public void logMetaData() {
    MemcachedClient memcachedClient = null;
    try {
      memcachedClient = getClient();
    } catch (IOException e) {
      throw new DbErrorException(MEMCACHED_IO, e);
    }

    Map<SocketAddress, Map<String, String>> stats;
    try {
      stats = memcachedClient.getStats();
      for (SocketAddress address : stats.keySet()) {
        Map<String, String> statMap = stats.get(address);
        for (String stat : statMap.keySet()) {
          logger.info(stat + ":" + statMap.get(stat));
        }
        String[] args = new String[1];
        args[0] = new Integer(address.hashCode()).toString();
        logger.info(Message.getMessage(STATS_COMPLETE, args));
      }
    } catch (Exception e) {
      // log and ignore
      logger.warn(STATS_ERROR, e);
    }
  }
}
