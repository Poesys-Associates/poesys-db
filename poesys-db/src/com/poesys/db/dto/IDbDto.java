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


import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.poesys.db.BatchException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.dao.insert.IInsert;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * Base interface for the database Data Transfer Objects (DTOs). A database DTO
 * holds the data queried from the database or created by the business layer
 * preparatory to inserting, updating, or deleting data from the database. The
 * basic purpose of the DTO is to transfer data from the database and data
 * access layer to the business layer. You should use these classes to get data
 * from the data access layer through the business delegates, then transform the
 * data into user interface DTOs that hold the data in a format suitable for use
 * in the user interface.
 * </p>
 * <p>
 * The IDbDto interface extends the IDto interface with operations that support
 * lifecycle processing and object status with respect to the database.
 * </p>
 * <p>
 * The database DTO supports a lifecycle that starts with either status NEW or
 * EXISTING. An in-memory factory creates a NEW object; a database query creates
 * an EXISTING object. When you store a NEW object in the database, it becomes
 * EXISTING. When you call a mutator (set method), it should change the status
 * to CHANGED. When you call update(), the CHANGED status transitions to
 * EXISTING. When you call delete(), the implementation should change the status
 * to DELETED. When you call cascadeDelete(), the implementation should changae
 * the status to CASCADE_DELETED, indicating the owning object has been marked
 * deleted. When you call undoStatus(), the implementation should restore the
 * previous status (but not necessarily the concrete state). The insert, update,
 * and delete Data Access Object (DAO) methods also transition the status to
 * EXISTING, DELETED, or FAILED. This latter status provides a way for batch
 * processing, in particular, to inform the client which objects failed to
 * process correctly. You can fix the problem, reset the status using
 * undoStatus(), and resubmit the object for processing.
 * </p>
 * <p>
 * The DTO should have collections of setters (ISet objects) that implement the
 * database-related operations on nested objects.
 * </p>
 * <ul>
 * <li><strong>query:</strong> a setter that queries nested objects</li>
 * <li><strong>insert:</strong> a setter that inserts certain kinds of nested
 * objects (children and associations)</li>
 * <li><strong>prechange:</strong> a setter that handles nested objects as
 * required before the main update or delete operation executes</li>
 * <li><strong>postchange:</strong> a setter that handles nested objects as
 * required after the main update or delete operation executes</li>
 * </ul>
 * <p>
 * The DTO should also have collections of validators for insert, update,
 * delete, and query operations. The DAO calls the validators before doing its
 * main operation.
 * </p>
 * <p>
 * You should use a database constraint option (ON DELETE CASCADE, ON UPDATE SET
 * NULL, and so on) to propagate changes along foreign keys. Not all databases
 * support all standard options, however, and you might need to propagate
 * deletes or updates using setters when working with some databases.
 * </p>
 * <p>
 * The DTO should be thread safe if you are going to cache it or otherwise use
 * it in a multi-threaded environment. See the AbstractDto for specific
 * suggestions on thread safety techniques.
 * </p>
 * <p>
 * The AbstractDto class implements a thread-safe, constructor-initialized
 * version of this interface as a reference implementation with status undo and
 * full processing for setters and validators.
 * </p>
 * <p>
 * <strong>Please see the documentation for AbstractDto for some examples of DTO
 * coding.</strong>
 * </p>
 * 
 * @see ISet
 * @see AbstractDto
 * 
 * @author Robert J. Muller
 */
public interface IDbDto extends Serializable, Comparable<IDbDto>, ISubject,
    IObserver {
  /** Enumerated type with the four possible states of the object */
  public enum Status {
    /** A newly created object */
    NEW,
    /** An object queried from the database */
    EXISTING,
    /** An existing object modified since query */
    CHANGED,
    /** An existing object marked for direct deletion */
    DELETED,
    /** An existing object marked for deletion by cascade along a foreign key */
    CASCADE_DELETED,
    /** An object in an invalid state after an operation such as nested query */
    FAILED
  };

  /**
   * Get the current status of the Data Transfer Object
   * 
   * @return Status of the object
   */
  Status getStatus();

  /**
   * Is the DTO an abstract class or a concrete class?
   * 
   * @return true if abstract, false if concrete
   */
  boolean isAbstractClass();

  /**
   * Set the abstract class flag.
   * 
   * @param isAbstract the value to which to set the flag
   */
  void setAbstractClass(boolean isAbstract);

  /**
   * Set the status of the DTO to EXISTING if it is currently NEW.
   */
  void setExisting();

  /**
   * Set the status of the DTO to CHANGED if it is currently EXISTING or FAILED.
   * If the DTO is NEW, the status remains unchanged, as all changes to state
   * are still part of the NEW object.
   * 
   */
  void setChanged();

  /**
   * Set the status of the DTO to FAILED.
   */
  void setFailed();

  /**
   * Restore the previous status of the object.
   */
  void undoStatus();

  /**
   * Has the status changed? Is the previous status different from the current
   * status? The undoStatus() method will switch to a different status if this
   * method returns true.
   * 
   * @return true if the status has changed, false otherwise
   */
  boolean hasStatusChanged();

  /**
   * Finalize the current status by setting the previous status to be the same
   * as the current status. The undoStatus() method will then never switch to a
   * different status. Call this method after committing an operation that
   * changes status, indicating that the status is final in the persistent
   * store.
   */
  void finalizeStatus();

  /**
   * Was the object queried from the database (true) or retrieved from a cache
   * (false)?
   * 
   * @return true if queried, false if retrieved from cache
   */
  boolean isQueried();

  /**
   * Has the object been fully processed (inserted, updated, deleted)? If so,
   * don't process any further.
   * 
   * @return true if processed, false if not
   */
  boolean isProcessed();

  /**
   * Set the DTO to processed (true) or unprocessed (false).
   * 
   * @param processed true or false
   */
  void setProcessed(boolean processed);

  /**
   * Set the flag indicating whether the object was queried from the database.
   * 
   * @param queried true if queried, false if retrieved from cache
   */
  void setQueried(boolean queried);

  /**
   * Query all the nested objects registered with the DTO. If the factory has
   * added a setter (ISet) for the nested object or collection, calling this
   * method executes the setter to instantiate the nested object(s).
   * 
   * @see ISet
   * @throws SQLException when there is a problem with one of the queries
   * @throws BatchException when there is a problem with one or more updates in
   *           a batch of updates
   */
  void queryNestedObjects() throws SQLException, BatchException;

  /**
   * Query nested objects before inserting the parent object. Setting objects
   * such as linked objects in a many-to-many link enables the insert method to
   * validate using information from those objects if required. You should put
   * setters into the insertQuerySetters list only if such objects are always
   * pre-existing in the database, and you should add code to the set() method
   * to check whether an object is already present before setting the DTO data
   * member.
   * 
   * @throws SQLException when there is a problem with one of the queries
   * @throws BatchException when there is a problem with one or more batches
   */
  void queryNestedObjectsForValidation() throws SQLException, BatchException;

  /**
   * Insert all the nested objects registered with the DTO. If the factory has
   * added a setter (ISet) for the nested object or collection, calling this
   * method executes the setter to insert the nested object(s). The nested
   * objects are either composite children of the DTO parent or association
   * objects with all the associated objects already stored in the database.
   * 
   * @param connection the database connection for the transaction
   * 
   * @see ISet
   * 
   * @throws SQLException when there is a problem with one of the inserts
   * @throws BatchException when there is a problem with one or more updates in
   *           a batch of updates
   */
  void insertNestedObjects(Connection connection) throws SQLException,
      BatchException;

  /**
   * Get the list of inserters for the various objects in the class hierarchy of
   * the DTO.
   * 
   * @return a list of inserter DAOs
   */
  List<IInsert<? extends IDbDto>> getInserters();

  /**
   * Preprocess all the nested objects registered with the DTO. If the factory
   * has added a setter (ISet) for the nested object or collection, calling this
   * method executes the setter to handle the nested object(s) with processing
   * appropriate to their current status. Call this method before executing the
   * main SQL operation.
   * 
   * @param connection the database connection for the transaction
   * 
   * @see ISet
   * 
   * @throws SQLException when there is a problem with one of the operations
   * @throws BatchException when there is a problem with one or more operations
   *           in a batch of operations
   */
  void preprocessNestedObjects(Connection connection) throws SQLException,
      BatchException;

  /**
   * Process the read-object setters to deserialize all nested objects.
   */
  void deserializeNestedObjects();
  
  /**
   * Post-process all the nested objects registered with the DTO. If the factory
   * has added a setter (ISet) for the nested object or collection, calling this
   * method executes the setter to handle the nested object(s) with processing
   * appropriate to their current status. Call this method after executing the
   * main SQL operation.
   * 
   * @param connection the database connection for the transaction
   * 
   * @see ISet
   * 
   * @throws SQLException when there is a problem with one of the operations
   * @throws BatchException when there is a problem with one or more operations
   *           in a batch of operations
   */
  void postprocessNestedObjects(Connection connection) throws SQLException,
      BatchException;

  /**
   * Validate the DTO for inserting.
   * 
   * @throws SQLException when there is a problem accessing the database
   * @throws InvalidParametersException when the DTO has no primary key or has
   *           invalid parameters for insert
   */
  void validateForInsert() throws SQLException;

  /**
   * Validate the DTO for updating.
   * 
   * @throws SQLException when there is a problem accessing the database
   */
  void validateForUpdate() throws SQLException;

  /**
   * Validate the DTO for deleting.
   * 
   * @throws SQLException when there is a problem accessing the database
   */
  void validateForDelete() throws SQLException;

  /**
   * Validate the DTO for querying.
   * 
   * @throws SQLException when there is a problem accessing the database
   */
  void validateForQuery() throws SQLException;

  /**
   * Get the primary key of the object. The primary key uniquely identifies the
   * object in a collection of objects.
   * 
   * @return an IPrimaryKey primary key object
   */
  IPrimaryKey getPrimaryKey();

  /**
   * Finalize the insert of an object by updating the DTO as appropriate. The
   * code has the active, executed statement with a possible generated-key
   * result set.
   * 
   * @param stmt the executed JDBC prepared statement for the INSERT
   * @throws SQLException when the generated key result set is not found
   */
  void finalizeInsert(PreparedStatement stmt) throws SQLException;

  /**
   * Mark the object as deleted if it is EXISTING, CHANGED, or FAILED.
   */
  void delete();

  /**
   * Call this method when you want to cascade a delete along a foreign key
   * association (a composite aggregation or a many-to-many-link). The
   * implementation should set the status for the relevant children to
   * Status.CASCADE_DELETED.
   */
  void markChildrenDeleted();

  /**
   * Mark the object as cascade-deleted if it is EXISTING, CHANGED, or DELETED.
   * The result will be to remove the object from its cache without taking any
   * action in the database, because the database performs the delete directly.
   */
  void cascadeDelete();

  /**
   * Get a SQL connection to the persistent subsystem that contains the DTO.
   * 
   * @return a SQL connection
   * @throws java.sql.SQLException when there is a problem getting the
   *           connection
   */
  java.sql.Connection getConnection() throws java.sql.SQLException;

  /**
   * Get the fully qualified name of the subsystem of the concrete DTO.
   * 
   * @return a fully qualified subsystem name
   */
  String getSubsystem();

  /**
   * Is suppression of nested inserts turned on? This would be true when the
   * system is processing a series of superclass constructors; the post-insert
   * setters should not run until the constructor in the concrete class.
   * 
   * @return true if suppressed, false if not
   */
  boolean isSuppressNestedInserts();

  /**
   * Set suppression of nested-object post-insert setters on or off.
   * 
   * @param suppressNestedInserts true to set suppression on, false to set
   *          suppression off
   * @see #isSuppressNestedInserts()
   */
  void setSuppressNestedInserts(boolean suppressNestedInserts);

  /**
   * Is suppression of nested inserts in pre-insert setters turned on? This
   * would be true when the system is processing a series of superclass
   * constructors other than the root class, which should execute all pre-insert
   * setters; the rest of the hierarchy should not execute such setters, so
   * should turn off this flag.
   * 
   * @return true if suppressed, false if not
   */
  boolean isSuppressNestedPreInserts();

  /**
   * Set suppression of nested-object pre-insert setters on or off.
   * 
   * @param suppressNestedPreInserts true to set suppression on, false to set
   *          suppression off
   * @see #isSuppressNestedPreInserts()
   */
  void setSuppressNestedPreInserts(boolean suppressNestedPreInserts);
}
