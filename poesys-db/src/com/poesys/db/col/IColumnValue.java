/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db.col;

import com.poesys.db.InvalidParametersException;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Interface for column values representing the columns that make up a primary key; used by
 * implementations of the IPrimaryKey interface
 */
public interface IColumnValue extends Comparable<IColumnValue>, Serializable {
  /**
   * Implements the Comparable interface method compareTo for the generic interface
   *
   * @param col the column to which to compare the current column value; the names must be the
   *            same for the columns to be comparable
   * @return -1, 0, or 1 for values that are less than, equal to, or greater than the current column
   */
  int compareTo(IColumnValue col);

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
  boolean equals(IColumnValue value);

  /**
   * Get the column name.
   *
   * @return a column name string
   */
  String getName();

  /**
   * Set the column name for the value.
   *
   * @param name the column name
   */
  void setName(String name);

  /**
   * Does the column value have a value? This package method is for test classes to help
   * determine whether a constructor has worked properly.
   *
   * @return true if the column value has a value
   */
  boolean hasValue();

  /**
   * Get a deep copy of the column value, including all the content.
   *
   * @return a deep copy of the column value
   */
  IColumnValue copy();

  /**
   * Set a JDBC parameter with the value of the column. The concrete implementation of this
   * method should call the appropriate JDBC set method based on the type of the value.
   *
   * @param stmt      the JDBC statement with parameters to set
   * @param nextIndex the index of the parameter to set with the column value
   * @return the next index value after the current one set
   */
  int setParam(PreparedStatement stmt, int nextIndex);

  /**
   * Get a Poesys/MS message object corresponding to a column value of unknown type. Each
   * concrete subclass implements the method to return a column value with the appropriate data.
   *
   * @return a column value
   */
  com.poesys.ms.col.IColumnValue<?> getMessageObject();

  /**
   * Accept a visit from a column comparison visitor, enabling a compareTo operation that
   * understands the types of both the objects being compared.
   *
   * @param visitor the visitor that will compare the column values
   */
  void accept(IColumnVisitor visitor);

  /**
   * Create an invalid-parameter exception based on a null name or value. This method decouples
   * the exception creation from the concrete instances that throw the exception, factoring out
   * the argument-creation code.
   *
   * @param value the string representing the value
   * @return the new exception
   */
  InvalidParametersException getException(String value);

  /**
   * Throw a DbErrorException that wraps a SQL exception and sets the SQL exception as the cause
   * of the DB error.
   *
   * @param e a SQL exception (required)
   */
  void throwDbError(SQLException e);
}
