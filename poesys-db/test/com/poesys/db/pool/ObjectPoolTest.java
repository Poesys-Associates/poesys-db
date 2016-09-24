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
package com.poesys.db.pool;


import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * CUT: ObjectPool
 * 
 * @author Robert J. Muller
 */
public class ObjectPoolTest {

  private ObjectPool<TestObject> pool;

  /** minimum number of objects */
  private static final int MIN = 5;
  /** maximum number of objects */
  private static final int MAX = 10;
  /** time interval in seconds between validation checks */
  private static final long INTERVAL = 2;
  private static final String VALUE = "value";

  /**
   * Set up the CUT object. Test the constructor.
   *
   * @throws java.lang.Exception when there is a problem initializing the object
   */
  @Before
  public void setUp() throws Exception {
    pool = new ObjectPool<TestObject>(MIN, MAX, INTERVAL) {

      @Override
      protected String toString(TestObject object) {
        return object.toString();
      }

      @Override
      protected TestObject createObject() {
        return new TestObject(VALUE);
      }

      @Override
      protected void closeObject(TestObject object) {
        object.close();
      }
    };
  }

  /**
   * Shut down the CUT object. Test the shutdown() method. Note that the
   * shutdown unit tests should not cause this to fail.
   *
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    pool.shutdown();
  }

  /**
   * Test method for {@link com.poesys.db.pool.ObjectPool#getObject()}.
   */
  @Test
  public void testGetObject() {
    TestObject object = pool.getObject();
    assertTrue("Could not get object from pool", object != null);
    assertTrue("Object not correct", VALUE.equals(object.value));
    assertTrue("Object still in pool", !pool.contains(object));
  }

  /**
   * Test method for {@link com.poesys.db.pool.ObjectPool#getObject()}.
   */
  @Test
  public void testGetObjectFromEmptyPool() {
    // First empty the pool.
    for (int i = 0; i < MIN; i++) {
      pool.getObject();
    }
    // Immediately get another object from the empty pool.
    TestObject object = pool.getObject();
    assertTrue("Couldn't get object from empty pool", object != null);
  }

  /**
   * Test method for
   * {@link com.poesys.db.pool.ObjectPool#returnObject(java.lang.Object)}.
   */
  @Test
  public void testReturnObject() {
    TestObject object = pool.getObject();
    assertTrue("Object still in pool", !pool.contains(object));
    pool.returnObject(object);
    assertTrue("Returned object not in pool", pool.contains(object));
  }

  /**
   * Test method for
   * {@link com.poesys.db.pool.ObjectPool#returnObject(java.lang.Object)}.
   */
  @Test
  public void testReturnNullObject() {
    int size = pool.size();
    pool.returnObject(null);
    assertTrue("Null return resized pool; initial " + size + ", after return "
               + pool.size(), pool.size() == size);
  }

  /**
   * Test method for {@link com.poesys.db.pool.ObjectPool#shutdown()}.
   */
  @Test
  public void testShutdownWithException() {
    TestObject object = pool.getObject();
    assertTrue("Couldn't get pooled object", object != null);
    object.setCloseException(true);
    pool.returnObject(object);
    try {
      pool.shutdown();
      fail("Pool did not throw exception on shutdown as expected");
    } catch (PoolShutdownException e) {
      // success
    }
  }

  /**
   * Test method for {@link com.poesys.db.pool.ObjectPool#shutdown()}.
   */
  @Test
  public void testShutdownAfterShutdown() {
    try {
      pool.shutdown();
      pool.shutdown();
    } catch (Throwable e) {
      fail("Pool did not gracefully ignore second attempt to shut down");
    }
  }

  /**
   * Test method for {@link com.poesys.db.pool.ObjectPool#size()}.
   */
  @Test
  public void testSize() {
    assertTrue("Initial pool not set to min", pool.size() == MIN);
    pool.returnObject(new TestObject(VALUE));
    pool.returnObject(new TestObject(VALUE));
    assertTrue("Revised pool not set to min + 2", pool.size() == MIN + 2);
  }

  /**
   * Test method for {@link com.poesys.db.pool.ObjectPool#toString()}.
   */
  @Test
  public void testToString() {
    String poolString = pool.toString();
    assertTrue("Did not produce correct string representation",
               poolString.equals(""));
  }

  /**
   * Test method for minimum size maintenance.
   * 
   * @throws InterruptedException when the thread receives an interrupt
   */
  @Test
  public void testMinSizeMaint() throws InterruptedException {
    assertTrue("Initial pool not set to min", pool.size() == MIN);
    pool.getObject();
    pool.getObject();
    assertTrue("Revised pool not set to min -2 2", pool.size() == MIN - 2);
    Thread.sleep(1000 * (INTERVAL + 2));
    assertTrue("Maintained pool not set to min", pool.size() == MIN);
  }

  /**
   * Test method for {@link com.poesys.db.pool.ObjectPool#size()}.
   * 
   * @throws InterruptedException when the thread receives an interrupt
   */
  @Test
  public void testMaxSizeMaint() throws InterruptedException {
    assertTrue("Initial pool not set to min", pool.size() == MIN);
    for (int i = MIN; i < MAX + 2; i++) {
      pool.returnObject(new TestObject(VALUE));
    }
    Thread.sleep(1000 * (INTERVAL + 2));
    assertTrue("Revised pool not set to max: " + pool.size(),
               pool.size() == MAX);
  }
}
