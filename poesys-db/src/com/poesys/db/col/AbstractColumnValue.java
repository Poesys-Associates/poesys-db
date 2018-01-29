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
package com.poesys.db.col;


import com.poesys.db.DbErrorException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.Message;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * Represents a name-value pair consisting of a column name and a column value
 * corresponding to a data value from a database. The abstract class contains
 * the name; the concrete subclasses contain the value of the appropriate type.
 * The interface provides no way to access the value directly. as the design is
 * intended to be of use only in constructing SQL statements or binding JDBC
 * variables using the public methods. Add operations as required to enable the
 * ColumnValue to perform any needed function common to all column values.
 * <p>
 * <em>
 * Note: If you want to access a column value outside of a primary key, you
 * should add the value directly to the set of primitive data members in the
 * data transfer object in addition to putting it into the primary key.
 * </em>
 * </p>
 * 
 * @author Robert J. Muller
 */
public abstract class AbstractColumnValue implements
    Comparable<IColumnValue>, Serializable, IColumnValue {

  /**
   * serial version UID for Serializable object
   */
  private static final long serialVersionUID = 1L;

  /** The column name */
  protected String name;

  /** Message stating two columns are not comparable */
  private static final String NOT_COMPARABLE_ERROR =
    "com.poesys.db.col.msg.not_comparable";
  /** Message stating that the name or value is null */
  private static final String NULL_NAME_OR_VALUE_ERROR =
    "com.poesys.db.col.msg.null_name_or_value";
  /** Message stating there was an unexpected SQL exception */
  protected static final String SQL_ERROR = "com.poesys.db.dto.msg.unexpected_sql_error";
  /** Message stating there was no SQL exception supplied */
  private static final String REQUIRED_EXCEPTION_ERROR = "com.poesys.db.dto.msg.no_exception";
  
  /**
   * Create an AbstractColumnValue object with a required name.
   * 
   * @param name the required name for the column
   * @param value the value for the column, if any
   */
  public AbstractColumnValue(String name, Object value) {
    if (name == null && value != null) {
      throw getException(value.toString());
    } else if (name == null) {
      throw getException(null);
    } else {
      this.name = name;
    }
  }

  @Override
  public boolean equals(IColumnValue value) {
    return name.equals(value.getName()) && valueEquals(value);
  }

  /**
   * Are the column values equal based on a value comparison as opposed to just
   * comparing the column names? A concrete subclass must implement this method
   * to do a value comparison on the internal value. The equals operator calls
   * this method to compare values as well as comparing the column names.
   * 
   * @param value the value of the column
   * @return true if the values are equal, false if not
   */
  abstract protected boolean valueEquals(IColumnValue value);

  /**
   * Returns a hash code value for the object. This method is supported for the
   * benefit of hash tables such as those provided by java.util.Hashtable.
   * 
   * The general contract of hashCode is:
   * 
   * Whenever it is invoked on the same object more than once during an
   * execution of a Java application, the hashCode method must consistently
   * return the same integer, provided no information used in equals comparisons
   * on the object is modified. This integer need not remain consistent from one
   * execution of an application to another execution of the same application.
   * If two objects are equal according to the equals(Object) method, then
   * calling the hashCode method on each of the two objects must produce the
   * same integer result. It is not required that if two objects are unequal
   * according to the java.lang.Object.equals(java.lang.Object) method, then
   * calling the hashCode method on each of the two objects must produce
   * distinct integer results. However, the programmer should be aware that
   * producing distinct integer results for unequal objects may improve the
   * performance of hash tables. As much as is reasonably practical, the hashCode
   * method defined by class Object does return distinct integers for distinct
   * objects. (This is typically implemented by converting the internal address
   * of the object into an integer, but this implementation technique is not
   * required by the JavaTM programming language.)
   * 
   * @return a hash code value for this object
   */
  public abstract int hashCode();

  /**
   * Compare two column values using standard compareTo semantics; if the column
   * names are not the same, the strings will not be comparable.
   */
  @Override
  public int compareTo(IColumnValue col) {
    int ret;
    // Column must have the same name or its not comparable
    if (name.equalsIgnoreCase(col.getName())) {
      // Use a visitor to compare the values. The first visit stores the first
      // object, the second does the comparison.
      CompareColumnVisitor visitor = new CompareColumnVisitor();
      accept(visitor);
      col.accept(visitor);
      ret = visitor.getComparison();
    } else {
      List<String> list = new ArrayList<>();
      list.add(name);
      list.add(col.getName());
      InvalidParametersException e =
        new InvalidParametersException(Message.getMessage(NOT_COMPARABLE_ERROR,
                                                          null));
      e.setParameters(list);
      throw e;
    }
    return ret;
  }

  /**
   * Get the name.
   * 
   * @return Returns the name.
   */
  @Override
  public synchronized String getName() {
    return name;
  }

  /**
   * Accept a visit from a column comparison visitor, enabling a compareTo
   * operation that understands the types of both the objects being compared.
   * 
   * @param visitor the visitor that will compare the column values
   */
  abstract public void accept(IColumnVisitor visitor);

  /**
   * Set the name.
   * 
   * @param name The name to set.
   */
  @Override
  public synchronized void setName(String name) {
    this.name = name;
  }

  /**
   * Create an invalid-parameter exception based on a null name or value. This
   * method decouples the exception creation from the concrete instances that
   * throw the exception, factoring out the argument-creation code.
   * 
   * @param value the string representing the value
   * @return the new exception
   */
  public synchronized InvalidParametersException getException(String value) {
    List<String> list = new ArrayList<>();
    list.add(name);
    list.add(value);
    InvalidParametersException e =
      new InvalidParametersException(Message.getMessage(NULL_NAME_OR_VALUE_ERROR,
                                                        null));
    e.setParameters(list);
    return e;
  }

  @Override
  public void throwDbError(SQLException e) {
    String messageProperty = SQL_ERROR;
    if (e == null) {
      // No exception supplied, throw that message instead of the SQL exception
      messageProperty = REQUIRED_EXCEPTION_ERROR;
    }
    throw new DbErrorException(Message.getMessage(messageProperty, null), e);
  }
}
