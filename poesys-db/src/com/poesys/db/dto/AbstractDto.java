/*
 * Copyright (c) 2008 Poesys Associates. All rights reserved.
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


import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.poesys.db.BatchException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.Message;
import com.poesys.db.dao.CacheDaoManager;
import com.poesys.db.dao.DataEvent;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dao.insert.IInsert;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An abstract implementation of the IDbDto interface for data transfer objects.
 * The class implements a simple status tracking system with status undo but no
 * comprehensive undo strategy. It also implements all the processing for the
 * setters and validators, including the de-serialization logic.
 * </p>
 * <p>
 * If you are going to cache the DTO or otherwise use it across threads, you
 * must make the DTO thread safe. Make all non-updateable fields final, such as
 * the primary key, read-only key fields, and non-updateable data fields. For
 * Updateable fields, add the synchronized keyword to all methods that access
 * the fields. Methods that access no fields do not need to be synchronized. Use
 * a thread-safe collection class such as CopyOnWriteArrayList or
 * ConcurrentHashMap for nested collections of associated objects (or,
 * alternatively, use the thread-safe wrapper classes, although these have
 * disadvantages such as a potential for race conditions). Methods that
 * manipulate thread-safe collections don't need to be synchronized either. The
 * setter and validator collections should also be thread-safe collections. You
 * can, of course, implement fancier methods such as read-write locking if
 * performance requires such optimization.
 * </p>
 * <p>
 * You must also make the DTO serializable. You must set any non-serializable
 * nested object to be "transient", and you cannot mark such fields final, only
 * non-transient fields that aren't set by the readObject method directly. The
 * setters in the read-only setters list should handle any transient data
 * de-serialization.
 * </p>
 * 
 * @author Robert J. Muller
 */
public abstract class AbstractDto implements IDbDto {
  /** Serial version UID for serializable object */
  private static final long serialVersionUID = -2993605561312120201L;
  /** Log4j logging */
  private static final Logger logger = Logger.getLogger(AbstractDto.class);

  /** primary key */
  protected IPrimaryKey key;

  /**
   * Current status of the data transfer object; default NEW; can change only
   * through methods
   */
  private Status status;

  /** whether this DTO has been fully processed (inserted, updated, deleted) */
  private boolean processed = false;

  /** whether the DTO is an abstract class (true) or a concrete one (false) */
  protected boolean abstractClass = false;

  @Override
  public boolean isAbstractClass() {
    return abstractClass;
  }

  @Override
  public void setAbstractClass(boolean abstractClass) {
    this.abstractClass = abstractClass;
  }

  /** Previous status of the object after a status change; default null */
  private Status previousStatus = null;

  /** whether the object was queried from the database or retrieved from a cache */
  transient private boolean queried = false;

  /** List of query-related setter objects for the DTO */
  protected List<ISet> querySetters = null;

  /**
   * List of query-related setter objects to run before insert to enable
   * validation using the objects
   */
  protected List<ISet> insertQuerySetters = null;

  /** List of insert-related setter objects for the DTO */
  protected List<ISet> insertSetters = null;

  /** whether to suppress nested-object inserts */
  protected boolean suppressNestedInserts = false;

  /** whether to suppress nested-object pre-inserts */
  protected boolean suppressNestedPreInserts = false;

  /** Stack of inserter DAOs that insert the class hierarchy of the object */
  transient protected List<IInsert<? extends IDbDto>> inserters =
    new ArrayList<IInsert<? extends IDbDto>>();

  /** List of pre-operation setters for the DTO */
  protected List<ISet> preSetters = null;

  /** List of post-operation setters for the DTO */
  protected List<ISet> postSetters = null;

  /** List of de-serialization setters for the DTO */
  protected List<ISet> readObjectSetters = null;

  /** List of query-related validator objects for the DTO */
  protected Collection<IValidate> queryValidators = null;

  /** List of insert-related validator objects for the DTO */
  protected Collection<IValidate> insertValidators = null;

  /** List of update-related validator objects for the DTO */
  protected Collection<IValidate> updateValidators = null;

  /** List of delete-related validator objects for the DTO */
  protected Collection<IValidate> deleteValidators = null;

  // Observer lists for IObserver methods
  /** Observer list for delete event observers */
  protected Collection<IObserver> deleteObservers = null;
  /** Observer list for detached-delete event observers */
  protected List<IObserver> detachedDeleteObservers = null;
  /** Observer list for marked-as-deleted event observers */
  protected Collection<IObserver> markedDeletedObservers = null;
  /** Observer list for detached-and-marked-as-deleted event observers */
  protected List<IObserver> detachedMarkedDeletedObservers = null;

  /** Message string when attempting to mark a non-NEW object EXISTING */
  private static final String NOT_NEW_MSG = "com.poesys.db.dto.msg.not_new";

  /** Message string when attempting to DELETE an object with an invalid status */
  private static final String CANNOT_DELETE_MSG =
    "com.poesys.db.dto.msg.cannot_delete";

  /**
   * Message string when attempting to de-serialize a cached object and there is
   * some kind of exception
   */
  private static final String READ_OBJECT_MSG =
    "com.poesys.db.dto.msg.read_object";
  private static final String NULL_KEY_MSG =
    "com.poesys.db.dto.msg.no_object_key";

  /**
   * Create an AbstractDto object. This constructor takes no arguments (the
   * default constructor) and sets the setter, validator, and observer lists to
   * null, so you must set these in the subclass if you need to set or validate.
   * The constructor also sets status to NEW; override this in the subclass if
   * you want the value to be something else. You should also set the subsystem
   * in the subclass.
   */
  public AbstractDto() {
    status = Status.NEW;
  }

  public List<IInsert<? extends IDbDto>> getInserters() {
    return inserters;
  }

  @Override
  abstract public int hashCode();

  @Override
  abstract public boolean equals(Object arg0);

  @Override
  public synchronized Status getStatus() {
    return status;
  }

  @Override
  public synchronized void setExisting() {
    if (status == Status.NEW || status == Status.CHANGED) {
      // Allow undo to NEW or CHANGED for subclass inserts
      previousStatus = status;
      status = Status.EXISTING;
      // Note that undo NEW or CHANGED should always set processed to FALSE
      // because undo is always done before processing.
      processed = false;
    } else if (status == Status.EXISTING) {
      // do nothing; it's already EXISTING
    } else {
      DtoStatusException e = new DtoStatusException(NOT_NEW_MSG);
      List<String> parameters = new ArrayList<String>();
      parameters.add(status.toString());
      e.setParameters(parameters);
    }
  }

  @Override
  public synchronized void setChanged() {
    if (status == Status.EXISTING || status == Status.FAILED
        || status == Status.CHANGED) {
      // Allow undo to EXISTING, FAILED, or CHANGED
      previousStatus = status;
      status = Status.CHANGED;
      processed = false; // reset when Changed
    } else if (status == Status.NEW) {
      // do nothing, just ignore the attempt to set to CHANGED
    } else {
      DtoStatusException e = new DtoStatusException(NOT_NEW_MSG);
      List<String> parameters = new ArrayList<String>();
      parameters.add(status.toString());
      e.setParameters(parameters);
    }
  }

  @Override
  public synchronized void setFailed() {
    previousStatus = status;
    status = Status.FAILED;
  }

  @Override
  public synchronized void delete() {
    if (status == Status.EXISTING || status == Status.CHANGED
        || status == Status.FAILED) {
      status = Status.DELETED;
      // Mark composite aggregates and links deleted.
      markChildrenDeleted();
      // Notify the parent of the marking.
      notify(DataEvent.MARKED_DELETED);
    } else if (status == Status.DELETED || status == Status.CASCADE_DELETED) {
      // do nothing, it's already DELETED
    } else if (status == Status.NEW) {
      // new object, just mark deleted, no notification required
      status = Status.DELETED;
      markChildrenDeleted();
    } else {
      logger.warn(Message.getMessage(CANNOT_DELETE_MSG, null) + ": status "
                  + status + " for " + getPrimaryKey().getStringKey());
    }
  }

  @Override
  public synchronized void cascadeDelete() {
    if (status == Status.EXISTING || status == Status.CHANGED
        || status == Status.DELETED) {
      status = Status.CASCADE_DELETED;
      // Cascade the delete along the chain.
      markChildrenDeleted();
    } else if (status == Status.CASCADE_DELETED) {
      // do nothing, it's already CASCADE_DELETED
    } else if (status == Status.NEW) {
      // new object, just mark deleted, no notification required
      status = Status.DELETED;
      markChildrenDeleted();
    } else {
      logger.warn(Message.getMessage(CANNOT_DELETE_MSG, null) + " status "
                  + status + " for " + getPrimaryKey().getStringKey());
      // throw new DtoStatusException(CANNOT_DELETE_MSG + " status " + status);
    }
  }

  @Override
  public synchronized void undoStatus() {
    if (previousStatus != null) {
      status = previousStatus;
    }
  }

  @Override
  public boolean hasStatusChanged() {
    boolean changed = false;
    if (previousStatus != null) {
      changed = (status != previousStatus);
    }
    return changed;
  }

  @Override
  public void finalizeStatus() {
    previousStatus = status;
  }

  @Override
  public boolean isQueried() {
    return queried;
  }

  @Override
  public void setQueried(boolean queried) {
    this.queried = queried;
  }

  @Override
  public void queryNestedObjects(Connection connection) throws SQLException,
      BatchException {
    if (querySetters != null) {
      for (ISet set : querySetters) {
        // Only set if not already set
        if (!set.isSet()) {
          set.set(connection);
        }
      }
    }
  }

  @Override
  public void queryNestedObjectsForValidation(Connection connection)
      throws SQLException, BatchException {
    if (insertQuerySetters != null) {
      for (ISet set : insertQuerySetters) {
        // Only set the object if not already set
        if (!set.isSet()) {
          set.set(connection);
        }
      }
    }
  }

  @Override
  public void insertNestedObjects(Connection connection) throws SQLException,
      BatchException {
    if (insertSetters != null && !suppressNestedInserts) {
      // As this method runs only for the last concrete class in a class
      // hierarchy, this is the point at which the DTO gets set to EXISTING.
      // This suppresses any further processing of the DTO in the nested
      // objects, cutting any recursive or infinite loop.
      setExisting();
      for (ISet set : insertSetters) {
        set.set(connection);
      }
    } else {
      // no setters, or setters suppressed, set main object to EXISTING
      setExisting();
    }
  }

  @Override
  public boolean isSuppressNestedInserts() {
    return suppressNestedInserts;
  }

  @Override
  public void setSuppressNestedInserts(boolean suppressNestedInserts) {
    this.suppressNestedInserts = suppressNestedInserts;
  }

  @Override
  public boolean isSuppressNestedPreInserts() {
    return suppressNestedPreInserts;
  }

  @Override
  public void setSuppressNestedPreInserts(boolean suppressNestedPreInserts) {
    this.suppressNestedPreInserts = suppressNestedPreInserts;
  }

  /**
   * Convert an input collection of business-layer DTOs of type T to an output
   * collection of type R. The implementation uses a thread-safe
   * CopyOnWriteArrayList. This requires an unchecked up-cast to the type, so
   * you should be sure the R type is a superclass of S.
   * 
   * @param <T> T is an IDbDto that is a
   * @param <R> R is an IDbDto that is a superclass of T or T itself, which
   *          permits you to convert a list of T objects to a list of T objects
   *          or to a list of superclass of T objects
   * @param list the input DTO list
   * @return a collection of type R (data-access transfer objects)
   */
  @SuppressWarnings("unchecked")
  protected <T extends IDbDto, R extends IDbDto> Collection<R> convertDtoList(Collection<T> list) {
    Collection<R> dbList = new CopyOnWriteArrayList<R>();
    for (T dto : list) {
      // Extract data-access DTO from business DTO.
      dbList.add((R)dto);
    }
    return dbList;
  }

  /**
   * Convert an input list of business-layer DTOs of type T to an output
   * collection of type R. The implementation uses a thread-safe
   * CopyOnWriteArrayList. This requires an unchecked up-cast to the type, so
   * you should be sure the R type is a superclass of S.
   * 
   * @param <T> T is an IDbDto that is a
   * @param <R> R is an IDbDto that is a superclass of T or T itself, which
   *          permits you to convert a list of T objects to a list of T objects
   *          or to a list of superclass of T objects
   * @param list the input DTO list
   * @return a collection of type R (data-access transfer objects)
   */
  @SuppressWarnings("unchecked")
  protected <T extends IDbDto, R extends IDbDto> Collection<R> convertDtoList(List<T> list) {
    Collection<R> dbList = new CopyOnWriteArrayList<R>();
    for (T dto : list) {
      // Extract data-access DTO from business DTO.
      dbList.add((R)dto);
    }
    return dbList;
  }

  @Override
  public void postprocessNestedObjects(Connection connection)
      throws SQLException, BatchException {
    if (postSetters != null) {
      for (ISet set : postSetters) {
        set.set(connection);
      }
    }
  }

  @Override
  public void preprocessNestedObjects(Connection connection)
      throws SQLException, BatchException {
    // Run the setters unless suppression is turned on for subclasses.
    if (preSetters != null && !suppressNestedPreInserts) {
      for (ISet set : preSetters) {
        set.set(connection);
      }
    }
  }

  /**
   * Read an object from an input stream, de-serializing it. This custom
   * de-serialization method calls the default read-object method to read in all
   * non-transient fields then runs a series of setters in the readObjectSetters
   * list to de-serialize any transient elements.
   * 
   * @param in the object input stream
   * @throws ClassNotFoundException when a nested object class can't be found
   * @throws IOException when there is an IO problem reading the stream
   */
  private void readObject(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    Connection connection = null;

    // Check the stream input.
    if (in == null) {
      throw new RuntimeException(READ_OBJECT_MSG);
    }
    
    // First de-serialize the non-transient data using the default process.
    // Note: THIS MUST COME BEFORE ANY STREAM ACCESS.
    in.defaultReadObject();

    // Check for the primary key.
    if (key == null) {
      throw new RuntimeException(NULL_KEY_MSG);
    }

    // Cache the object in memory before getting nested objects.
    IDaoManager manager = CacheDaoManager.getInstance();
    manager.putObjectInCache(key.getCacheName(), 0, this);

    // Finally, iterate through the setters to process nested objects.
    if (readObjectSetters != null) {
      try {
        connection = getConnection();
        for (ISet set : readObjectSetters) {
          set.set(connection);
        }
      } catch (SQLException e) {
        // Should never happen, log and throw RuntimeException
        logger.error(READ_OBJECT_MSG, e);
        throw new RuntimeException(READ_OBJECT_MSG, e);
      } finally {
        try {
          if (connection != null && !connection.isClosed()) {
            connection.close();
          }
        } catch (SQLException e) {
          logger.error(READ_OBJECT_MSG, e);
          throw new RuntimeException(READ_OBJECT_MSG, e);
        }
      }
    }
  }

  /**
   * Get a SQL connection to the subsystem that owns the DTO. The implementing
   * DTO must use the subsystem resource bundle and database properties file to
   * acquire the right connection. The method is public to permit Proxy classes
   * to use it to load DTOs lazily.
   * 
   * @return a SQL connection
   * @throws java.sql.SQLException when there is a problem getting the
   *           connection
   */
  abstract public java.sql.Connection getConnection()
      throws java.sql.SQLException;

  @Override
  public void validateForQuery(Connection connection) throws SQLException,
      InvalidParametersException {
    if (queryValidators != null) {
      for (IValidate validator : queryValidators) {
        validator.validate(connection);
      }
    }
  }

  @Override
  public void validateForInsert(Connection connection) throws SQLException {
    if (insertValidators != null) {
      for (IValidate validator : insertValidators) {
        validator.validate(connection);
      }
    }
  }

  @Override
  public void validateForUpdate(Connection connection) throws SQLException {
    if (updateValidators != null) {
      for (IValidate validator : updateValidators) {
        validator.validate(connection);
      }
    }
  }

  @Override
  public void validateForDelete(Connection connection) throws SQLException {
    if (deleteValidators != null) {
      for (IValidate validator : deleteValidators) {
        validator.validate(connection);
      }
    }
  }

  @Override
  public void finalizeInsert(PreparedStatement stmt) throws SQLException {
    // No action required--default implementation
  }

  public void attach(IObserver observer, DataEvent event) {
    // Only DELETE is supported at this time
    if (event == DataEvent.DELETE) {
      if (deleteObservers == null) {
        // Allocate the list of delete observers
        deleteObservers = new ArrayList<IObserver>();
        detachedDeleteObservers = new ArrayList<IObserver>();
      }
      deleteObservers.add(observer);
    } else if (event == DataEvent.MARKED_DELETED) {
      if (markedDeletedObservers == null) {
        markedDeletedObservers = new ArrayList<IObserver>();
        detachedMarkedDeletedObservers = new ArrayList<IObserver>();
      }
      markedDeletedObservers.add(observer);
    }
  }

  @Override
  public void detach(IObserver observer, DataEvent event) {
    // Adds observer to appropriate list of detached observers; the notify
    // method will actually remove these observers, permitting the notify
    // iterator to finish iterating before modifying the list, which prevents
    // the iterator concurrent-modification exception.
    if (event == DataEvent.DELETE && detachedDeleteObservers != null) {
      detachedDeleteObservers.add(observer);
    } else if (event == DataEvent.MARKED_DELETED
               && this.detachedMarkedDeletedObservers != null) {
      detachedMarkedDeletedObservers.add(observer);
    }
  }

  @Override
  public void notify(DataEvent event) {
    // Only updates on DELETE and MARKED_DELETED events
    if (event == DataEvent.DELETE && deleteObservers != null) {
      for (IObserver observer : deleteObservers) {
        observer.update(this, event);
      }
    } else if (event == DataEvent.MARKED_DELETED
               && markedDeletedObservers != null) {
      for (IObserver observer : markedDeletedObservers) {
        observer.update(this, event);
      }
    }

    // Clean up the DELETE observers detached by the update, if any.
    if (deleteObservers != null && detachedDeleteObservers != null) {
      for (IObserver observer : detachedDeleteObservers) {
        deleteObservers.remove(observer);
      }
    }

    // Clean up the MARKED_DELETE observers detached by the update, if any.
    if (markedDeletedObservers != null
        && detachedMarkedDeletedObservers != null) {
      for (IObserver observer : detachedMarkedDeletedObservers) {
        markedDeletedObservers.remove(observer);
      }
    }
  }

  @Override
  public void update(ISubject subject, DataEvent event) {
    // Default action is to do nothing; concrete DTOs must implement the
    // appropriate method that handles actions for updating nested children.
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dto.IDbDto#getPrimaryKey()
   */
  @Override
  public IPrimaryKey getPrimaryKey() {
    return key;
  }

  @Override
  public boolean isProcessed() {
    return processed;
  }

  @Override
  public void setProcessed(boolean processed) {
    this.processed = processed;
  }
}
