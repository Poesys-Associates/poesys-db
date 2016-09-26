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
import java.io.NotSerializableException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.ClientMode;
import net.spy.memcached.DefaultConnectionFactory;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.transcoders.SerializingTranscoder;

import org.apache.log4j.Logger;

import com.poesys.db.DbErrorException;
import com.poesys.db.Message;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDtoCache;
import com.poesys.db.pk.IPrimaryKey;
import com.poesys.db.pool.ObjectPool;


/**
 * <p>
 * A singleton object that manages data access objects in the system. This
 * implementation of the DAO Manager enables the use of memcached, a distributed
 * cache system. This class contains the actual calls to the spymemcached Java
 * memcached client, implementing the Bridge design pattern to wrap the
 * spymemcached API. It also contains the DTO registry that contains the
 * de-serialized Java objects currently being retrieved. This registry prevents
 * infinite loops and object duplication during deserialization of nested object
 * hierarchies by checking the registry for objects before getting them from the
 * external cache, so that once an object is retrieved, that tree branch will
 * not be duplicated by a later cache query.
 * <p>
 * <strong>Note: memcached does take a little time to store a cached object. All
 * store operations are asynchronous with respect to this storing operation, so
 * you need to avoid making a request to the cache for the object immediately
 * after putting the object in the cache.</strong>
 * </p>
 * <p>
 * There is a singleton static cache map of SQL connections indexed on primary
 * key. This cache contains the SQL connections in use during a single object
 * retrieval initiated by the getCachedObject() method. Deserialized transient
 * lists use this connection to query objects as required, thus reducing the
 * need for multiple connections during object deserialization. When the method
 * finishes, it removes the connection from the cache.
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

  private IDbDto dto = null;

  /** Singleton memcached manager instance */
  private static IDaoManager memcachedManager = null;

  /** Memcached client pool */
  private static ObjectPool<MemcachedClient> clients = null;

  /** Name of the memcached properties resource bundle */
  protected static final String BUNDLE = "com.poesys.db.memcached";

  /** The resource bundle containing the memcached properties. */
  protected static final ResourceBundle properties =
    ResourceBundle.getBundle(BUNDLE);

  // Error messages from the Poesys resource bundle.

  private static final String INVALID_PORT =
    "com.poesys.db.dao.query.msg.memcached_invalid_port";
  private static final String MEMCACHED_QUEUE_FULL =
    "com.poesys.db.dao.query.msg.memcached_queue_full";
  private static final String MEMCACHED_CLIENT =
    "com.poesys.db.dao.query.msg.memcached_client";
  private static final String SHUTDOWN_CLIENT_ERROR =
    "com.poesys.db.dao.query.msg.memcached_client_shutdown";
  private static final String MEMCACHED_UNKNOWN_PROTOCOL =
    "com.poesys.db.dao.query.msg.memcached_unknown_protocol";
  private static final String MEMCACHED_GET_ERROR =
    "com.poesys.db.dao.query.msg.memcached_get";
  private static final String MEMCACHED_RETRY_WARNING =
    "com.poesys.db.dao.query.msg.memcached_retry";
  private static final String STATS_COMPLETE =
    "com.poesys.db.dao.query.msg.memcached_stats_complete";
  private static final String STATS_ERROR =
    "com.poesys.db.dao.query.msg.memcached_stats_error";
  /** Error message when thread is interrupted or timed out */
  private static final String THREAD_ERROR = "com.poesys.db.dao.msg.thread";
  /** Error message when thread is interrupted or timed out */
  private static final String NONSERIALIZABLE_ERROR =
    "com.poesys.db.dao.msg.nonserializable";

  // Memcached options from property file

  /** Memcached configuration property servers */
  private static final String MEMCACHED_PROP_SERVERS = "servers";
  /** Memcached configuration property protocol */
  private static final String MEMCACHED_PROP_PROTOCOL = "protocol";
  /** Memcached configuration property for retries after client get timeout */
  private static final String MEMCACHED_PROP_RETRIES = "client_retries";
  /** Memcached configuration property for min number of clients in pool */
  private static final String MEMCACHED_PROP_POOL_MIN = "min_clients";
  /** Memcached configuration property for max number of clients in pool */
  private static final String MEMCACHED_PROP_POOL_MAX = "max_clients";
  /**
   * Memcached configuration property for time interval in seconds for client
   * pool maintenance
   */
  private static final String MEMCACHED_PROP_POOL_INTERVAL =
    "pool_maintenance_interval";
  /** Memcached configuration value binary protocol */
  private static final String BINARY = "binary";
  /** Memcached configuration value text protocol */
  private static final String TEXT = "text";

  /* Memcached configuration value for 5-second timeout retries */
  private static final int TIMEOUT_RETRIES =
    new Integer(properties.getString(MEMCACHED_PROP_RETRIES));
  private static final int MIN =
    new Integer(properties.getString(MEMCACHED_PROP_POOL_MIN));
  private static final int MAX =
    new Integer(properties.getString(MEMCACHED_PROP_POOL_MAX));
  private static final int INTERVAL =
    new Integer(properties.getString(MEMCACHED_PROP_POOL_INTERVAL));

  /** Period in milliseconds to sleep before re-trying memcached get */
  private static final long RETRY_SLEEP_PERIOD = 5L * 1000L;
  /** timeout for the query thread */
  private static final int TIMEOUT = 1000 * 60;

  /**
   * Disable the default constructor.
   */
  MemcachedDaoManager() {
  }

  /**
   * Get the singleton instance of the IDaoManager for a memcached cache. This
   * also creates the memcached client pool.
   * 
   * @return the DAO manager
   */
  public static IDaoManager getInstance() {
    if (memcachedManager == null) {
      memcachedManager = new MemcachedDaoManager();
      clients = new ObjectPool<MemcachedClient>(MIN, MAX, INTERVAL) {

        @Override
        protected String toString(MemcachedClient object) {
          return object.toString();
        }

        @Override
        protected MemcachedClient createObject() {
          MemcachedClient client = null;
          String protocol = properties.getString(MEMCACHED_PROP_PROTOCOL);

          try {
            if (BINARY.equalsIgnoreCase(protocol)) {
              client =
                new MemcachedClient(new BinaryConnectionFactory(ClientMode.Static),
                                    getSockets());
            } else if (TEXT.equalsIgnoreCase(protocol)) {
              client =
                new MemcachedClient(new DefaultConnectionFactory(ClientMode.Static),
                                    getSockets());
            } else {
              Object[] args = new Object[1];
              args[0] = protocol;
              String msg = Message.getMessage(MEMCACHED_UNKNOWN_PROTOCOL, args);
              throw new DbErrorException(msg);
            }
          } catch (IOException e) {
            throw new DbErrorException(MEMCACHED_CLIENT, e);
          }
          return client;
        }

        @Override
        protected void closeObject(MemcachedClient object) {
          try {
            object.shutdown();
          } catch (Throwable e) {
            // Ignore
            logger.warn(SHUTDOWN_CLIENT_ERROR, e);
          }
        }
      };
    }
    return memcachedManager;
  }

  @Override
  public <T extends IDbDto, C extends Collection<T>> IDaoFactory<T> getFactory(String name,
                                                                               String subsystem,
                                                                               Integer expiration) {
    return new DaoMemcachedFactory<T>(name, subsystem, expiration);
  }

  /**
   * Get the hostname/port combinations for the sockets to which the client will
   * connect. The sockets are in a single property string in the format
   * &lt;hostname&gt;:&lt;port&gt;,... where &lt;hostname&gt; is a valid
   * authority (domain or IP address) and &lt;port&gt; is an integer between 0
   * and 65,535.
   *
   * @return a list of Internet Socket Address objects
   */
  private static List<InetSocketAddress> getSockets() {
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
    MemcachedClient client = clients.getObject();
    try {
      if (object != null) {
        // update the access time in the cache to keep the object around
        client.touch(key.getStringKey(), expireTime);
      }
    } finally {
      clients.returnObject(client);
    }
    return object;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends IDbDto> T getCachedObject(IPrimaryKey key) {
    // Check for the separate thread and create it if it's not already there.
    PoesysTrackingThread thread = null;
    if (Thread.currentThread() instanceof PoesysTrackingThread) {
      thread = (PoesysTrackingThread)Thread.currentThread();
      getFromMemcached(key);
    } else {
      Runnable query = new Runnable() {
        public void run() {
          getFromMemcached(key);
        }
      };
      thread = new PoesysTrackingThread(query);
      thread.start();
      // Join the thread, blocking until the thread completes or
      // until the query times out.
      try {
        thread.join(TIMEOUT);
      } catch (InterruptedException e) {
        Object[] args = { "update", dto.getPrimaryKey().getStringKey() };
        String message = Message.getMessage(THREAD_ERROR, args);
        logger.error(message, e);
      }
    }

    return (T)dto;
  }

  /**
   * Get the object from memcached. This always runs in a PoesysTrackingThread
   * container.
   * 
   * @param key the primary key to look up in memcached
   */
  private void getFromMemcached(IPrimaryKey key) {
    PoesysTrackingThread thread = (PoesysTrackingThread)Thread.currentThread();
    MemcachedClient client = clients.getObject();

    try {
      if (dto == null) {
        // Not previously de-serialized, get it from the cache.
        logger.debug("Getting object " + key.getStringKey() + " from the cache");

        // Get the object synchronously but check for exceptions and retry to
        // allow for memcached server being unavailable for a short period.

        int retries = TIMEOUT_RETRIES;
        while (retries > 0) {
          try {
            dto = (IDbDto)client.get(key.getStringKey());
            if (dto != null) {
              // object found, track and set processed
              thread.addDto(dto);
              thread.setProcessed(key.getStringKey(), true);
            }
            // Break out of loop after no-exception get
            break;
          } catch (Exception e) {
            retries--;
            if (retries == 0) {
              // InterruptedException, ExecutionException, or RuntimeException
              // Retries exhausted, fail with exception
              Object[] args = new Object[1];
              args[0] = key.getStringKey();
              logger.error(Message.getMessage(MEMCACHED_GET_ERROR, args), e);
              throw new DbErrorException(Message.getMessage(MEMCACHED_GET_ERROR,
                                                            args));
            } else {
              // More retries, sleep for a short time and try again.

              // First warn in log so as not to lose the exception sequence.
              Object[] args1 = new Object[2];
              args1[0] = key.getStringKey(); // object key
              args1[1] = e.getMessage(); // exception message
              logger.warn(Message.getMessage(MEMCACHED_RETRY_WARNING, args1), e);
              try {
                Thread.sleep(RETRY_SLEEP_PERIOD);
              } catch (InterruptedException e1) {
                // Externally interrupted sleep, something's wrong
                Object[] args2 = new Object[1];
                args2[0] = key.getStringKey();
                logger.error(Message.getMessage(MEMCACHED_GET_ERROR, args2), e1);
                throw new DbErrorException(Message.getMessage(MEMCACHED_GET_ERROR,
                                                              args2));
              }
            }
          }
        }

        if (dto != null) {
          logger.debug("Retrieved object " + key.getStringKey()
                       + " from the cache");
          // Iterate through the setters to process nested objects.
          dto.deserializeNestedObjects();
        } else {
          logger.debug("No object " + key.getStringKey() + " in the cache");
        }
      }
    } finally {
      clients.returnObject(client);
    }
  }

  @Override
  public <T extends IDbDto> void putObjectInCache(String cacheName,
                                                  int expireTime, T object) {

    MemcachedClient client = clients.getObject();

    try {
      String key = object.getPrimaryKey().getStringKey();
      client.set(key, expireTime, object, new SerializingTranscoder());
      logger.debug("Cached object \"" + key + "\" of type "
                   + object.getClass().getName()
                   + " in memcached with expiration time " + expireTime + "ms");
      // Get the in-memory cache manager. Adding the new object to this
      // cache means that setters getting nested objects will get this
      // object rather than getting a different one by deserializing from
      // memcached.
      IDaoManager localCacheManager = CacheDaoManager.getInstance();
      localCacheManager.putObjectInCache(object.getPrimaryKey().getCacheName(),
                                         expireTime,
                                         object);
    } catch (IllegalStateException e) {
      List<String> errors = new ArrayList<String>(1);
      errors.add(object.getPrimaryKey().getStringKey());
      DbErrorException e1 = new DbErrorException(MEMCACHED_QUEUE_FULL, e);
      e1.setParameters(errors);
      throw e1;
    } catch (IllegalArgumentException e) {
      Throwable cause = e.getCause();
      if (cause instanceof NotSerializableException) {
        Object[] args = { object.getClass().getName() };
        String message = Message.getMessage(NONSERIALIZABLE_ERROR, args);
        logger.error(message, cause);
        DbErrorException e1 = new DbErrorException(message, e);
        throw e1;
      }
    } finally {
      clients.returnObject(client);
    }
  }

  @Override
  public void removeObjectFromCache(String cacheName, IPrimaryKey key) {
    MemcachedClient client = clients.getObject();

    try {
      // asynch, object may get deleted after delay
      client.delete(key.getStringKey());
      logger.debug("Removed cached object " + key.getStringKey());
    } catch (IllegalStateException e) {
      // log and ignore
      String[] args = new String[1];
      args[0] = key.getStringKey();
      logger.warn(Message.getMessage(MEMCACHED_QUEUE_FULL, args));
    } finally {
      clients.returnObject(client);
    }
  }

  @Override
  public void clearTemporaryCaches() {
    // Nothing to do, no temp caches for memcached support
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
    MemcachedClient client = clients.getObject();
    Map<SocketAddress, Map<String, String>> stats;
    try {
      stats = client.getStats();
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
    } finally {
      clients.returnObject(client);
    }
  }
}
