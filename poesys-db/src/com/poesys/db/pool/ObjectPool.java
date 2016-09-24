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


import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;


/**
 * A generic pool of objects that supports a minimum number of objects, a
 * maximum number of objects, and a validation interval for an executor thread
 * that checks those states
 * 
 * @author Robert J. Muller
 */
public abstract class ObjectPool<T> {
  /** Log4j logger for this class */
  private static final Logger logger = Logger.getLogger(ObjectPool.class);
  /** the pool data structure */
  private ConcurrentLinkedQueue<T> pool;
  /** the thread service for checking pool status */
  private ScheduledExecutorService executorService;

  /**
   * property for error message indicating an object had a problem while closing
   */
  private static final String OBJECT_CLOSE_ERROR =
    "com.poesys.db.pool.msg.object_close";

  /**
   * Create a ObjectPool object, specifying the settings.
   * 
   * @param minimum the minimum number of objects in the pool
   * @param maximum the maximum number of objects in the pool
   * @param interval the interval in seconds between checks for minimum and
   *          maximum objects
   */
  public ObjectPool(final int minimum, final int maximum, final long interval) {
    initialize(minimum);
    executorService = Executors.newSingleThreadScheduledExecutor();
    executorService.scheduleWithFixedDelay(new Runnable() {
      // This run() method could be synchronized to block
      // gets and returns while maintaining the queue size.
      // For now, go for performance over accuracy--no need
      // to have the queue exactly min or max.
      @Override
      public void run() {
        int size = pool.size();
        if (size < minimum) {
          for (int i = 0; i < minimum - size; i++) {
            pool.add(createObject());
          }
        } else if (size > maximum) {
          for (int i = 0; i < size - maximum; i++) {
            pool.poll();
          }
        }
      }
    }, interval, interval, TimeUnit.SECONDS);
  }

  /**
   * Initialize the pool; called from constructors to share initialization
   * processes. Sets up the pool and fills it with the specified minimum number
   * of objects.
   *
   * @param minimum the number of objects to create for the initial pool
   */
  private void initialize(final int minimum) {
    pool = new ConcurrentLinkedQueue<T>();
    for (int i = 0; i < minimum; i++) {
      pool.add(createObject());
    }
  }

  /**
   * Get an object from the pool, removing it from the pool.
   *
   * @return the object
   */
  public T getObject() {
    T object = pool.poll();

    // If no object in pool, create one.
    if (object == null) {
      object = createObject();
    }

    return object;
  }

  /**
   * Return an object to the pool, making it available for other clients. If the
   * object is null, the pool is unchanged.
   *
   * @param object the object to return to the pool
   */
  public void returnObject(final T object) {
    if (object != null) {
      pool.offer(object);
    }
  }

  /**
   * Shut down the pool, doing whatever is needed to close each object and
   * shutting down the executor. Calling this method results in a
   * non-functioning pool, so you should not use the pool anymore after calling
   * this method.
   * 
   * @throws PoolShutdownException when there is a problem shutting down the
   *           executor or a pooled object
   */
  public void shutdown() throws PoolShutdownException {
    if (executorService != null) {
      executorService.shutdown();
    }
    boolean failed = false;
    while (pool.size() > 0) {
      T object = pool.poll();
      try {
        closeObject(object);
      } catch (Throwable e) {
        // Warn about this object, then continue closing objects.
        logger.warn("Problem closing pooled object " + toString(object), e);
        failed = true;
      }
      if (failed) {
        throw new PoolShutdownException(OBJECT_CLOSE_ERROR);
      }
    }
  }

  /**
   * The number of objects currently in the pool.
   *
   * @return the size of the pool
   */
  public int size() {
    return pool.size();
  }

  /**
   * Does the pool contain the specified object? This method has package access
   * for use in unit testing.
   *
   * @param object the object to test
   * @return true if the object is in the pool, false if not
   */
  boolean contains(final T object) {
    boolean inPool = pool.contains(object);
    return inPool;
  }

  /**
   * Get a string representation of the pool.
   */
  public String toString() {
    StringBuilder builder = new StringBuilder();
    return builder.toString();
  }

  /**
   * Create an object. The concrete class implements this method to create
   * objects before pooling them.
   *
   * @return the new object
   */
  protected abstract T createObject();

  /**
   * Close or shut down an object. The concrete class implements this method to
   * close objects before shutting down the pool. For example, you can close
   * connections quietly or gracefully.
   *
   * @param object the object to close
   */
  protected abstract void closeObject(final T object);

  /**
   * Generate a string representation for the object. The concrete class
   * implements this as a wrapper that may call toString() on the object or
   * implement a separate string generation algorithm if the object does not
   * have it's own string representation.
   *
   * @param object the object for which to generate a string representation
   * @return the string representation of the object
   */
  protected abstract String toString(final T object);
}
