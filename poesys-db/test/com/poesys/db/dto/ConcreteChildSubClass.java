/*
 * Copyright (c) 2018 Poesys Associates. All rights reserved.
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

import com.poesys.db.pk.IPrimaryKey;
import org.apache.log4j.Logger;

/**
 * Represents a concrete subclass of a composite child root class (a concrete class that inherits
 * from a parent class that is in turn a composite child of a parent class).
 *
 * @author Robert J. Muller
 */
public class ConcreteChildSubClass extends ChildRootClass {
  /** Default serial version UID for the Serializable DTO */
  private static final long serialVersionUID = 1L;

  /** Logger for this class */
  private static final Logger logger = Logger.getLogger(ConcreteChildSubClass.class);

  /** the deserializer used by the readObject method */
  private static final com.poesys.db.dto.Deserializer<ConcreteChildSubClass> deserializer =
    new com.poesys.db.dto.Deserializer<>();

  /**
   * Create an empty ConcreteChildSubClass for use in building a new object. The concrete subclass
   * must call this constructor.
   */
  public ConcreteChildSubClass() {
    abstractClass = false;
    createInserters();
  }

  /**
   * Create a ConcreteChildSubClass. The concrete subclass must call this constructor.
   *
   * @param key             the primary key of the ConcreteChildSubClass
   * @param parentId        the unique identifier for the composite parent
   * @param childId         the unique identifier for the child within the composite parent's
   *                        children
   * @param rootDataColumn  the data column belonging to the root class
   * @param childDataColumn the data column belonging to the child class
   */
  public ConcreteChildSubClass(IPrimaryKey key, Integer parentId, Integer childId, String
    rootDataColumn, String childDataColumn) {
    super(key, parentId, childId, rootDataColumn);

    this.childDataColumn = childDataColumn;

    if (this.childDataColumn == null) {
      throw new com.poesys.db.InvalidParametersException(
        "childDataColumn is required for " + key.getValueList());
    }

    abstractClass = false;
    createInserters();
  }

  /**
   * Read an object from an input stream, de-serializing it. Each generated class must have this
   * private method, which the deserialize method calls through Java reflection on the specific
   * class. The class calls a shared code method to run the readObjectSetters.
   *
   * @param in the object input stream
   * @throws ClassNotFoundException when a nested object class can't be found
   * @throws java.io.IOException    when there is an IO problem reading the stream
   */
  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException,
    ClassNotFoundException {
    logger.debug(
      "Deserializing object of class " + this.getClass().getName() + " with readObject in " +
        "ConcreteChildSubClass");
    // Do the read-object deserialization.
    deserializer.doReadObject(in, this);
  }

  /**
   * Nested property childDataColumn
   *
   * <p> a data column that belongs to the subclass </p>
   */
  private String childDataColumn;

  /**
   * Get the child data column value.
   *
   * @return a String data column value
   */

  public String getChildDataColumn() {
    return childDataColumn;
  }

  /**
   * Clear the childDataColumn data member; override in proxy if lazily loaded, otherwise this
   * method does nothing.
   */
  public void clearChildDataColumn() {
    // Override in proxy if lazily loaded; otherwise does nothing
  }

  /**
   * <p> Set the child data column value. </p>
   *
   * @param childDataColumn the value with which to set the property
   */
  public void setChildDataColumn(String childDataColumn) throws com.poesys.db
    .InvalidParametersException {
    if (childDataColumn == null) {
      throw new com.poesys.db.InvalidParametersException("receivable is required");
    }

    this.childDataColumn = childDataColumn;
    setChanged();
  }

  @Override
  public void update(com.poesys.db.dto.ISubject subject, com.poesys.db.dao.DataEvent event) {
  }

  /**
   * Create the inserters for the ConcreteChildSubClass and its superclasses.
   */
  protected void createInserters() {
    com.poesys.db.dao.IDaoManager manager =
      com.poesys.db.dao.DaoManagerFactory.getManager(getSubsystem());
    final com.poesys.db.dao.IDaoFactory<ConcreteChildSubClass> factory =
      manager.getFactory(getClass().getName(), getSubsystem(), 2147483647);
    super.createInserters();
    com.poesys.db.dao.insert.IInsertSql<ConcreteChildSubClass> sql =
      new InsertConcreteChildSubClass();
    com.poesys.db.dao.insert.IInsert<ConcreteChildSubClass> inserter = factory.getInsert(sql, true);
    inserters.add(inserter);
  }
}
