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


/**
 * <p>
 * A Strategy-pattern interface for strategies that set data members of a Data
 * Transfer Object. Setting can be either query-oriented or
 * data-manipulation-oriented, either querying nested objects based on foreign
 * keys or manipulating nested objects in the database as appropriate to a
 * specific operation (insert, update, delete). Each concrete implementation of
 * the interface sets one "attribute" in the DTO. The AbstractDto contains four
 * lists of setters:
 * </p>
 * <ul>
 * <li><strong>query:</strong> queries a nested object or set of objects within
 * the DTO</li>
 * <li><strong>insert:</strong> inserts NEW nested objects within the DTO</li>
 * <li><strong>pre:</strong> processes nested objects for insert, update, or
 * delete before the main operation</li>
 * <li><strong>post:</strong> processes nested objects for insert, update, or
 * delete after the main operation</li>
 * </ul>
 * <p>
 * The DAO calls the appropriate method on the DTO to execute the list of
 * setters. If the implementation requires access to the members of the object,
 * you should implement the interface as a nested, internal class within the DTO
 * class.
 * </p>
 * <p>
 * For queries, the set strategy class queries the nested object or collection
 * from the data source, then calls the parent set method to set it into the
 * appropriate member of the IDto.
 * </p>
 * <p>
 * For inserts, updates, and deletes, the set strategy class calls the parent
 * getter method to get the nested object or collection, then processes it by
 * inserting, updating, or deleting the objects as required. Usually, this
 * processing is done by an internal class that extends the
 * AbstractProcessNestedObjects class, an implementation of ISet, with the
 * appropriate operations. See the AbstractDto class for an example of an
 * internal processor (Parent class).
 * </p>
 * <p>
 * The strategy pattern decouples the logic of DTO containment hierarchy
 * building from the DTO and DAO while providing a mechanism for the DAO to
 * build the nested elements of the DTO. Reuse is somewhat limited by the
 * association of the strategy to a specific DTO class, which it needs to know
 * because of the need to manipulate the getters and setters of that class. You
 * can use the structure of the setter list to control how the containment
 * hierarchy is built in different contexts by supplying or not supplying a
 * particular strategy to the DTO. For example, a lazy-load approach could
 * remove the setter from the DTO and put it into a lazy-loading proxy so that a
 * set of linked objects is built only when you need it.
 * </p>
 * 
 * @author Robert J. Muller
 * @see AbstractDto
 */
public interface ISet extends Serializable {
  /**
   * <p>
   * Set a field in a Data Transfer Object (DTO). Setting a field can mean
   * either querying data and setting it into the DTO or taking data from the
   * DTO field and setting it into the database. The set can use batch
   * operations, so the caller must be able to handle the BatchException as well
   * as SQLException.
   * </p>
   */
  void set();

  /**
   * Is the field already set? A client may use this method to test the object
   * of the setter to see whether to run the set method.
   * 
   * @return true if set, false if not
   */
  boolean isSet();
}
