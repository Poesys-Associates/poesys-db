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

package com.poesys.db.dto;


import org.apache.log4j.Logger;

import com.poesys.db.DbErrorException;
import com.poesys.db.Message;
import com.poesys.db.dao.PoesysTrackingThread;


/**
 * An abstract class that centralizes things for all setters. In this version of
 * Poesys/DB, setters use the PoesysTrackingThread to manage connections, and
 * this abstract class implements the basic thread logic to either operate
 * within an existing tracking thread or create a new one.
 * 
 * @author Robert J. Muller
 * @param <T> the kind of IDbDto processed by the setter
 */
public abstract class AbstractSetter<T extends IDbDto> implements ISet {
  /** Logger for this class */
  private static final Logger logger = Logger.getLogger(AbstractSetter.class);
  /** Serial version UID for Serializable object */
  private static final long serialVersionUID = 1L;

  /** The subsystem in which the setter accomplishes its tasks */
  protected String subsystem;
  /** The cache expiration time in milliseconds for T objects */
  protected final Integer expiration;
  /** The class name of the class or subclass for error reporting */
  protected String setterName;

  /** timeout for the query thread */
  private static final int TIMEOUT = 1000 * 60;

  /** Error message when thread is interrupted or timed out */
  private static final String THREAD_ERROR = "com.poesys.db.dao.msg.thread";
  /** Error message when strategy method throws an exception */
  protected static final String SET_DTO_FIELD_ERROR =
    "com.poesys.db.dto.msg.set_field";

  /**
   * Create an AbstractSetter object. Sets the setter class name for thread
   * error handling.
   * 
   * @param subsystem the subsystem for the setter
   * @param expiration the cache expiration time in milliseconds for T objects
   */
  public AbstractSetter(String subsystem, Integer expiration) {
    this.subsystem = subsystem;
    this.expiration = expiration == null ? Integer.MAX_VALUE : expiration;
    this.setterName = AbstractSetter.class.getName();
  }

  @Override
  public void set() {
    // Figure out whether the setter is operating within an existing tracking
    // thread, and create a new one if not, then call the abstract
    // implementation of the Strategy to run the appropriate logic for the
    // subclass.
    if (Thread.currentThread() instanceof PoesysTrackingThread) {
      doSet((PoesysTrackingThread)Thread.currentThread());
    } else {
      Runnable query = getRunnable();
      PoesysTrackingThread thread = new PoesysTrackingThread(query, subsystem);
      thread.start();
      // Join the thread, blocking until the thread completes or
      // until the query times out.
      try {
        thread.join(TIMEOUT);
      } catch (InterruptedException e) {
        Object[] args = { "set", setterName };
        String message = Message.getMessage(THREAD_ERROR, args);
        logger.error(message, e);
      }
    }
  }

  /**
   * Get a Runnable object for the tracking thread to run.
   * 
   * @return the Runnable object
   */
  private Runnable getRunnable() {
    Runnable runnable = new Runnable() {
      public void run() {
        // Get the tracking thread.
        PoesysTrackingThread thread =
          (PoesysTrackingThread)Thread.currentThread();
        try {
          doSet(thread);
        } catch (Exception e) {
          Object[] args = { setterName };
          String message = Message.getMessage(SET_DTO_FIELD_ERROR, args);
          logger.error(message, e);
          throw new DbErrorException(message, thread, e);
        } finally {
          if (thread != null) {
            thread.closeConnection();
          }
        }
      }
    };
    return runnable;
  }

  /**
   * An abstract strategy method; the concrete subclass implements this method
   * with the required logic for the subclass.
   * 
   * @param poesysTrackingThread the tracking thread
   */
  protected abstract void doSet(PoesysTrackingThread poesysTrackingThread);

  /**
   * Get the class name to use to look up a cached DTO.
   * 
   * @return the DTO class name
   */
  abstract protected String getClassName();
}
