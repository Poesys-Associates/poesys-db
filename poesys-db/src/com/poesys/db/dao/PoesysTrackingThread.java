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


import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.poesys.db.InvalidParametersException;
import com.poesys.db.dto.IDbDto;


/**
 * A Thread object that contains the retrieval and processing state of a
 * Poesys/DB object tree retrieval. The class tracks the set of retrieved
 * objects as a history of objects retrieved. Each object is in a container
 * object that has attributes related to Poesys/DB processing. The Thread
 * subclass thus provides a container for operations involving multiple objects
 * and provides a place outside the objects to track processing.
 * 
 * @author Robert J. Muller
 */
public class PoesysTrackingThread extends Thread {
  /** Logger for debugging */
  private static final Logger logger =
    Logger.getLogger(PoesysTrackingThread.class);

  private static final String NO_DTO_FOR_KEY_ERR =
    "No retrieved DTO with this key: ";

  /**
   * map of DTOs indexed by global primary key (string version of DTO primary
   * key)
   */
  private final Map<String, DtoTrackingObject> history =
    new HashMap<String, DtoTrackingObject>();

  // Error messages
  private static final String NO_DTO_ERR =
    "com.poesys.db.dao.query.msg.no_cached_dto_error";

  /**
   * Create a PoesysTrackingThread object.
   *
   */
  public PoesysTrackingThread() {
  }

  /**
   * Create a PoesysTrackingThread object with a task.
   *
   * @param target the Runnable task
   */
  public PoesysTrackingThread(Runnable target) {
    super(target);
  }

  /**
   * Create a CachedThread object with a name.
   *
   * @param name the thread name
   */
  public PoesysTrackingThread(String name) {
    super(name);
  }

  /**
   * Create a CachedThread object within a group.
   *
   * @param group the group of threads
   * @param target the Runnable task
   */
  public PoesysTrackingThread(ThreadGroup group, Runnable target) {
    super(group, target);
  }

  /**
   * Create a named CachedThread object within a group.
   *
   * @param group a group of threads
   * @param name the thread name
   */
  public PoesysTrackingThread(ThreadGroup group, String name) {
    super(group, name);
  }

  /**
   * Create a named CachedThread object with a Runnable task.
   *
   * @param target the task
   * @param name the thread name
   */
  public PoesysTrackingThread(Runnable target, String name) {
    super(target, name);
  }

  /**
   * Create a named CachedThread object with a Runnable task within a group.
   *
   * @param group the thread group
   * @param target the task
   * @param name the thread name
   */
  public PoesysTrackingThread(ThreadGroup group, Runnable target, String name) {
    super(group, target, name);
  }

  /**
   * Create a named CachedThread object with a Runnable task within a group with
   * a specific stack size.
   *
   * @param group the thread group
   * @param target the task
   * @param name the thread name
   * @param stackSize integer, size of the thread stack
   */
  public PoesysTrackingThread(ThreadGroup group,
                              Runnable target,
                              String name,
                              long stackSize) {
    super(group, target, name, stackSize);
  }

  /**
   * Get a DTO from the internal tracking data, or null if the DTO has not been
   * retrieved from the cache.
   * 
   * @param key the globally unique primary key
   * @return the DTO, or null if not yet retrieved in this thread
   */
  public IDbDto getDto(String key) {
    IDbDto dto = null;
    DtoTrackingObject obj = history.get(key);
    if (obj != null) {
      dto = obj.getDto();
    }
    return dto;
  }

  /**
   * Add a DTO to the cached DTO retrieval history for this thread.
   * 
   * @param dto the DTO to add
   */
  public void addDto(IDbDto dto) {
    if (dto == null) {
      throw new InvalidParametersException(NO_DTO_ERR);
    }
    DtoTrackingObject obj = new DtoTrackingObject(dto);
    try {
      history.put(dto.getPrimaryKey().getStringKey(), obj);
    } catch (Throwable e) {
      logger.warn("Warning: exception adding tracking object to history", e);
      if (history == null) {
        logger.warn("Null history in tracking thread");
      }
      if (dto.getPrimaryKey() == null) {
        logger.warn("Null DTO primary key in tracking thread");
      }
      if (dto.getPrimaryKey().getStringKey() == null) {
        logger.warn("Null DTO primary key string in tracking thread");
      }
    }
  }

  /**
   * Has a specified DTO been processed? If the DTO is not in the history, the
   * method returns false.
   * 
   * @param key the identifier for the DTO to query
   * @return true if processed, false if not or not in history
   */
  public boolean isProcessed(String key) {
    boolean processed = false;
    DtoTrackingObject obj = history.get(key);
    if (obj != null) {
      processed = obj.isProcessed;
    }
    return processed;
  }

  /**
   * Mark a DTO in the retrieval history as processed.
   * 
   * @param key the key identifying the DTO to mark
   * @param processed true for processed, false for not processed
   */
  public void setProcessed(String key, boolean processed) {
    DtoTrackingObject obj = history.get(key);
    if (obj != null) {
      obj.setProcessed(processed);
    } else {
      throw new RuntimeException(NO_DTO_FOR_KEY_ERR + key);
    }
  }
}
