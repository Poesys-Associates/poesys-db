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


import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.poesys.db.DbErrorException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.Message;


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
    Comparable<AbstractColumnValue>, Serializable {

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

  /**
   * Indicates whether some other object is "equal to" this one.
   * 
   * The equals method implements an equivalence relation on non-null object
   * references:
   * 
   * It is reflexive: for any non-null reference value IUpdate,
   * IUpdate.equals(IUpdate) should return true. It is symmetric: for any
   * non-null reference values IUpdate and y, IUpdate.equals(y) should return
   * true if and only if y.equals(IUpdate) returns true. It is transitive: for
   * any non-null reference values IUpdate, y, and z, if IUpdate.equals(y)
   * returns true and y.equals(z) returns true, then IUpdate.equals(z) should
   * return true. It is consistent: for any non-null reference values IUpdate
   * and y, multiple invocations of IUpdate.equals(y) consistently return true
   * or consistently return false, provided no information used in equals
   * comparisons on the objects is modified. For any non-null reference value
   * IUpdate, IUpdate.equals(null) should return false. The equals method for
   * class Object implements the most discriminating possible equivalence
   * relation on objects; that is, for any non-null reference values IUpdate and
   * y, this method returns true if and only if IUpdate and y refer to the same
   * object (IUpdate == y has the value true).
   * 
   * Note that it is generally necessary to override the hashCode method
   * whenever this method is overridden, so as to maintain the general contract
   * for the hashCode method, which states that equal objects must have equal
   * hash codes.
   * 
   * The concrete subclass should call this method to test the equality of the
   * column names and values.
   * 
   * @param value the other column value to which to compare this one
   * @return true if the values are the same, false otherwise
   */
  public boolean equals(AbstractColumnValue value) {
    return name.equals(value.name) && valueEquals(value);
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
  abstract protected boolean valueEquals(AbstractColumnValue value);

  /**
   * Returns a hash code value for the object. This method is supported for the
   * benefit of hashtables such as those provided by java.util.Hashtable.
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
   * performance of hashtables. As much as is reasonably practical, the hashCode
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
  public int compareTo(AbstractColumnValue col) {
    int ret = 0;
    // Column must have the same name or its not comparable
    if (name.equalsIgnoreCase(col.name)) {
      // Use a visitor to compare the values. The first visit stores the first
      // object, the second does the comparison.
      CompareColumnVisitor visitor = new CompareColumnVisitor();
      accept(visitor);
      col.accept(visitor);
      ret = visitor.getComparison();
    } else {
      List<String> list = new ArrayList<String>();
      list.add(name);
      list.add(col.name);
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
  public synchronized String getName() {
    return name;
  }

  /**
   * Accept a visit from a column comparison visitor, enabling a compareTo
   * operation that understands the types of both the objects being compared.
   * 
   * @param visitor the visitor that will compare the column values
   */
  abstract protected void accept(IColumnVisitor visitor);

  /**
   * Set the name.
   * 
   * @param name The name to set.
   */
  public synchronized void setName(String name) {
    this.name = name;
  }

  /**
   * Does the column value have a value? This package method is for test classes
   * to help determine whether a constructor has worked properly.
   * 
   * @return true if the column value has a value
   */
  public abstract boolean hasValue();

  /**
   * Get a deep copy of the column value, including all the content.
   * 
   * @return a deep copy of the column value
   */
  public abstract AbstractColumnValue copy();

  /**
   * Set a JDBC parameter with the value of the column. The concrete
   * implementation of this method should call the appropriate JDBC set method
   * based on the type of the value.
   * 
   * @param stmt the JDBC statement with parameters to set
   * @param nextIndex the index of the parameter to set with the column value
   * @return the next index value after the current one set
   */
  public abstract int setParam(PreparedStatement stmt, int nextIndex);

  /**
   * Create an invalid-parameter exception based on a null name or value. This
   * method decouples the exception creation from the concrete instances that
   * throw the exception, factoring out the argument-creation code.
   * 
   * @param value the string representing the value
   * @return the new exception
   */
  protected synchronized InvalidParametersException getException(String value) {
    List<String> list = new ArrayList<String>();
    list.add(name);
    list.add(value);
    InvalidParametersException e =
      new InvalidParametersException(Message.getMessage(NULL_NAME_OR_VALUE_ERROR,
                                                        null));
    e.setParameters(list);
    return e;
  }

  /**
   * Get a Poesys/MS message object corresponding to a column value of unknown
   * type. Each concrete subclass implements the method to return a column value
   * with the appropriate data.
   * 
   * @return a column value
   */
  public abstract com.poesys.ms.col.IColumnValue<?> getMessageObject();
  
  protected void throwDbError(SQLException e) {
    throw new DbErrorException(Message.getMessage(SQL_ERROR, null));
  }
}
