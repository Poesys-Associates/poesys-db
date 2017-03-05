/*
 * Copyright (c) 2016 Poesys Associates. All rights reserved.
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
import com.poesys.db.pk.IPrimaryKey;
import com.poesys.db.pool.ObjectPool;


/**
 * A service class for memcached, providing all the services that Poesys/DB uses
 * from the spymemcached client.
 * 
 * @author Robert J. Muller
 *
 * @param <T> the type of the object to cache
 */
public class MemcachedService<T extends IDbDto> {
  /** Logger for debugging */
  private static final Logger logger = Logger.getLogger(MemcachedService.class);

  /** Name of the memcached properties resource bundle */
  protected static final String BUNDLE = "com.poesys.db.memcached";

  /** The resource bundle containing the memcached properties. */
  protected static final ResourceBundle properties =
    ResourceBundle.getBundle(BUNDLE);

  // Memcached options from property file

  /** Memcached configuration property servers */
  private static final String MEMCACHED_PROP_SERVERS = "servers";
  /** Memcached configuration property protocol */
  private static final String MEMCACHED_PROP_PROTOCOL = "protocol";
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

  /** client pool minimum clients */
  private static final int MIN =
    new Integer(properties.getString(MEMCACHED_PROP_POOL_MIN));
  /** client pool maximum clients */
  private static final int MAX =
    new Integer(properties.getString(MEMCACHED_PROP_POOL_MAX));
  /** client pool number of clients to create at once */
  private static final int INTERVAL =
    new Integer(properties.getString(MEMCACHED_PROP_POOL_INTERVAL));
  /** the default time until which an object stays in the cache (8 hours) */
  private static final int DEFAULT_EXPIRE_TIME = 1000 * 60 * 60 * 8;

  // error/warning/info messages
  private static final String UNKNOWN_PROTOCOL =
    "com.poesys.db.dao.query.msg.memcached_unknown_protocol";
  private static final String CLIENT_ERROR =
    "com.poesys.db.dao.query.msg.memcached_client";
  private static final String SHUTDOWN_CLIENT_ERROR =
    "com.poesys.db.dao.query.msg.memcached_client_shutdown";
  private static final String INVALID_PORT_ERROR =
    "com.poesys.db.dao.query.msg.memcached_invalid_port";
  private static final String QUEUE_FULL_ERROR =
    "com.poesys.db.dao.query.msg.memcached_queue_full";
  private static final String STATS_COMPLETE_ERROR =
    "com.poesys.db.dao.query.msg.memcached_stats_complete";
  private static final String STATS_ERROR =
    "com.poesys.db.dao.query.msg.memcached_stats_error";

  /** static memcached client pool */
  private static final ObjectPool<MemcachedClient> clients =
    new ObjectPool<MemcachedClient>(MIN, MAX, INTERVAL) {

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
            String msg = Message.getMessage(UNKNOWN_PROTOCOL, args);
            throw new DbErrorException(msg);
          }
        } catch (IOException e) {
          throw new DbErrorException(CLIENT_ERROR, e);
        }
        return client;
      }

      @Override
      protected void closeObject(MemcachedClient object) {
        try {
          object.shutdown();
        } catch (Throwable e) {
          // Ignore
          logger.warn(Message.getMessage(SHUTDOWN_CLIENT_ERROR, null), e);
        }
      }
    };

  /**
   * Get the object of type T based on the primary key and update the object
   * expiration time in the cache.
   * 
   * @param key the primary key value
   * @param expireTime the time until cache expiration of the object touched
   * @return the object of type T specified by primary key
   */
  @SuppressWarnings("unchecked")
  public T getObject(IPrimaryKey key, int expireTime) {
    T object = null;

    if (expireTime < 0) {
      // invalid, use default
      expireTime = DEFAULT_EXPIRE_TIME;
    }
    MemcachedClient client = clients.getObject();
    try {
      // cast required to convert "Object" object returned by memcached client
      object = (T)client.get(key.getStringKey());
      // reset the expiration time of the object in the cache
      client.touch(key.getStringKey(), DEFAULT_EXPIRE_TIME);
    } finally {
      clients.returnObject(client);
    }
    return object;
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
          DbErrorException e1 =
            new DbErrorException(Message.getMessage(INVALID_PORT_ERROR, null),
                                 e);
          e1.setParameters(errors);
          throw e1;
        }
      }
    }
    return addresses;
  }

  /**
   * Put the object into the cache with specified expiration time.
   * 
   * @param expireTime the time until expiration of the object in the cache
   * @param object the object to cache
   */
  public void putObjectInCache(int expireTime, T object) {
    if (expireTime < 0) {
      // invalid, use default
      expireTime = DEFAULT_EXPIRE_TIME;
    }

    MemcachedClient client = clients.getObject();
    try {
      String key = object.getPrimaryKey().getStringKey();
      client.set(key, expireTime, object, new SerializingTranscoder());
      logger.debug("Cached object \"" + key + "\" of type "
                   + object.getClass().getName()
                   + " in memcached with expiration time " + expireTime + "ms");
    } catch (IllegalStateException e) {
      List<String> errors = new ArrayList<String>(1);
      errors.add(object.getPrimaryKey().getStringKey());
      DbErrorException e1 =
        new DbErrorException(Message.getMessage(QUEUE_FULL_ERROR, null), e);
      e1.setParameters(errors);
      throw e1;
    } catch (IllegalArgumentException e) {
      Throwable cause = e.getCause();
      if (cause instanceof NotSerializableException) {
        logger.error("Non-serializable object: " + object.getClass().getName(),
                     cause);
        DbErrorException e1 =
          new DbErrorException("Non-serializable object: "
                               + object.getClass().getName(), e);
        throw e1;
      }
    } finally {
      clients.returnObject(client);
    }
  }

  /**
   * Mark the object for deletion in the cache.
   * 
   * @param key the primary key of the object to mark for deletion
   */
  public void removeObjectFromCache(IPrimaryKey key) {
    MemcachedClient client = clients.getObject();

    try {
      // asynch, object may get deleted after delay
      client.delete(key.getStringKey());
      logger.debug("Removed cached object " + key.getStringKey());
    } catch (IllegalStateException e) {
      // log and ignore
      String[] args = new String[1];
      args[0] = key.getStringKey();
      logger.warn(Message.getMessage(QUEUE_FULL_ERROR, args));
    } finally {
      clients.returnObject(client);
    }
  }

  /**
   * Log the memcached meta data.
   */
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
        logger.info(Message.getMessage(STATS_COMPLETE_ERROR, args));
      }
    } catch (Exception e) {
      // log and ignore
      logger.warn(STATS_ERROR, e);
    } finally {
      clients.returnObject(client);
    }
  }
}
