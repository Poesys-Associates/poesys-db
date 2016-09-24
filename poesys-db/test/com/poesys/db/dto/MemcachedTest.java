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
package com.poesys.db.dto;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.NotSerializableException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import net.spy.memcached.ClientMode;
import net.spy.memcached.DefaultConnectionFactory;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.transcoders.SerializingTranscoder;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.poesys.db.dao.MemcachedDaoManager;
import com.poesys.db.pool.ObjectPool;


/**
 * 
 * @author Robert J. Muller
 */
public class MemcachedTest {
  private static final String TEST_VALUE = "test";
  private static final String TESTKEY = "testkey";
  /** Logger for debugging */
  private static final Logger logger =
    Logger.getLogger(MemcachedDaoManager.class);
  /** Memcached client pool */
  private static ObjectPool<MemcachedClient> clients = null;

  /**
   * Set up the environment for the test.
   *
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    setClientPool();
  }

  /**
   * Create an object pool for the memcached clients.
   */
  private void setClientPool() {
    clients = new ObjectPool<MemcachedClient>(5, 100, 5) {

      @Override
      protected String toString(MemcachedClient object) {
        return object.toString();
      }

      @Override
      protected MemcachedClient createObject() {
        MemcachedClient client = null;

        try {
          client =
            new MemcachedClient(new DefaultConnectionFactory(ClientMode.Static),
                                getSockets());
        } catch (IOException e) {
          fail("Could not create memcached client");
        }
        return client;
      }

      @Override
      protected void closeObject(MemcachedClient object) {
        try {
          object.shutdown();
        } catch (Throwable e) {
          fail("Could not shut down client");
        }
      }
    };
  }

  /**
   * Get the hostname/port combinations for the sockets to which the client will
   * connect.
   *
   * @return a list with a single address object for the local memcached service
   */
  private static List<InetSocketAddress> getSockets() {
    List<InetSocketAddress> addresses = new ArrayList<InetSocketAddress>();
    try {
      addresses.add(new InetSocketAddress("localhost", new Integer(11211)));
    } catch (NumberFormatException e) {
      fail("Bad port number: 11211");
    }
    return addresses;
  }

  /**
   * Test method for
   * {@link com.poesys.db.dto.ConcreteSubClass#readObject(java.io.ObjectInputStream)}
   * .
   */
  @Test
  public void testReadObject() {

    // create object
    ConcreteSubClass c1 = new ConcreteSubClass(TEST_VALUE);
    // put object into cache
    putObjectInCache(TESTKEY, c1);
    // Wait a bit to allow memcached to store.
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // Do nothing
    }
    // get object from cache
    ConcreteSubClass c2 = getCachedObject(TESTKEY);
    assertTrue("No concrete class object in cache", c2 != null);
    // test object contents
    assertTrue("Object value not retrieved properly", c2.getNestedObject().equals(TEST_VALUE));
  }

  /**
   * Put the object in the memcached cache by its key.
   *
   * @param key the key for the object
   * @param object the object itself
   */
  public void putObjectInCache(String key, ConcreteSubClass object) {

    MemcachedClient client = clients.getObject();

    try {
      client.set(key, 60, object, new SerializingTranscoder());
      logger.debug("Cached object \"" + key + "\" of type "
                   + object.getClass().getName()
                   + " with expiration time 60 seconds");
    } catch (IllegalStateException e) {
      fail("Memcached queue full");
    } catch (IllegalArgumentException e) {
      Throwable cause = e.getCause();
      if (cause instanceof NotSerializableException) {
        fail("Non-serializable object: " + object.getClass().getName());
      }
    } finally {
      clients.returnObject(client);
    }
  }

  /**
   * Get the cached object using the key.
   *
   * @param key the key for the object
   * @return the object
   */
  @SuppressWarnings("unchecked")
  public <T> T getCachedObject(String key) {
    MemcachedClient client = clients.getObject();
    T object = null;

    try {
      // Not previously de-serialized, get it from the cache.
      logger.debug("Getting object " + key + " from the cache");

      // Get the object synchronously but check for exceptions and retry to
      // allow for memcached server being unavailable for a short period.

      int retries = 5;
      while (retries > 0) {
        try {
          object = (T)client.get(key);
          // Break out of loop after no-exception get; no need to check
          // object for null, just means not cached
          break;
        } catch (Exception e) {
          retries--;
          logger.error("Exception getting key " + key, e);
          if (retries == 0) {
            // InterruptedException, ExecutionException, or RuntimeException
            // Retries exhausted, fail with exception
            fail("Too many retries");
          } else {
            try {
              Thread.sleep(5 * 60);
            } catch (InterruptedException e1) {
              // Externally interrupted sleep, something's wrong
              fail("Externally interrupted sleep");
            }
          }
        }
      }

      if (object != null) {
        logger.info("Retrieved object " + key + " from the cache");
      } else {
        logger.info("No object " + key + " in the cache");
      }

    } finally {
      clients.returnObject(client);
    }

    return object;
  }
}
