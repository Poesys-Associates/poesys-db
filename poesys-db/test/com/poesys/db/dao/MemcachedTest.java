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
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.ClientMode;
import net.spy.memcached.DefaultConnectionFactory;
import net.spy.memcached.MemcachedClient;

import org.apache.log4j.Logger;

import com.poesys.db.DbErrorException;
import com.poesys.db.Message;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;
import com.poesys.db.pool.ObjectPool;


/**
 * An abstract superclass that enables both database connection and memcached
 * cache access for integration testing. The parent superclass, ConnectionTest,
 * enables Poesys/DB connections based on the database.properties file. This
 * class enables memcached client operations based on the memcached.properties
 * file. Use this class as the superclass for integration tests that require
 * direct access to the memcached cache for verification of cache operations.
 * 
 * @author Robert J. Muller
 */
public abstract class MemcachedTest extends ConnectionTest {
  private static final Logger logger = Logger.getLogger(MemcachedTest.class);

  /** Memcached client pool */
  protected static ObjectPool<MemcachedClient> clients = null;
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

  // Error messages from the Poesys resource bundle.

  private static final String INVALID_PORT =
    "com.poesys.db.dao.query.msg.memcached_invalid_port";
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

  /** Period in milliseconds to sleep before re-trying memcached get */
  private static final long RETRY_SLEEP_PERIOD = 5L * 1000L;

  /**
   * Create a MemcachedTest object.
   */
  public MemcachedTest() {
    super();

    // Set up the memcached client pool for use by subclasses.
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

  /**
   * Get the hostname/port combinations for the sockets to which the client will
   * connect. The sockets are in a single property string in the format
   * &lt;hostname&gt;:&lt;port&gt;,... where &lt;hostname&gt; is a valid
   * authority (domain or IP address) and &lt;port&gt; is an integer between 0
   * and 65,535.
   *
   * @return a list of Internet Socket Address objects
   */
  protected static List<InetSocketAddress> getSockets() {
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

  /**
   * Get the object from memcached. This always runs in a PoesysTrackingThread
   * container.
   * 
   * @param key the primary key to look up in memcached
   * @return the DTO object
   */
  protected IDbDto getFromMemcached(IPrimaryKey key) {
    MemcachedClient client = clients.getObject();
    IDbDto dto = null;

    try {
      // Not previously de-serialized, get it from the cache.
      logger.debug("Getting object " + key.getStringKey() + " from the cache");

      // Get the object synchronously but check for exceptions and retry to
      // allow for memcached server being unavailable for a short period.

      int retries = TIMEOUT_RETRIES;
      while (retries > 0) {
        logger.debug("Getting object from cache, retries left " + retries);
        try {
          dto = (IDbDto)client.get(key.getStringKey());
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
            sleep(key);
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
    } finally {
      clients.returnObject(client);
    }
    return dto;
  }

  /**
   * Sleep for the retry sleep period.
   * 
   * @param key the DTO key, for error reporting
   */
  private void sleep(IPrimaryKey key) {
    try {
      Thread.sleep(RETRY_SLEEP_PERIOD);
    } catch (InterruptedException e1) {
      // Externally interrupted sleep, something's wrong
      Object[] args2 = new Object[1];
      args2[0] = key.getStringKey();
      logger.error(Message.getMessage(MEMCACHED_GET_ERROR, args2), e1);
      throw new DbErrorException(Message.getMessage(MEMCACHED_GET_ERROR, args2));
    }
  }
}