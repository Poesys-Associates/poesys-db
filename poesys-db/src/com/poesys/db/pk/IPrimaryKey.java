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
package com.poesys.db.pk;


import java.io.Serializable;
import java.sql.PreparedStatement;
import java.util.Iterator;
import java.util.Set;

import com.poesys.db.DuplicateKeyNameException;
import com.poesys.db.col.AbstractColumnValue;


/**
 * A generic interface for primary key classes that represent a set of columns
 * and values that uniquely identify an object. You can iterate through the
 * columns using a foreach loop that refers to the interface, which implements
 * the Iterable interface. The primary key also must implement the Comparable
 * interface to allow comparisons of two keys. You use a primary key to identify
 * the object in maps and other data structures as well as to build the
 * appropriate parts of a SQL statement based on the key, such as a query of the
 * object or an update of the object. Inserts can create a primary key for
 * specific kinds of keys (sequence and identity keys). A primary key is also
 * serializable, so you should include only primitive or Serializable data in
 * primary key implementations.
 * 
 * @author Robert J. Muller
 */
public interface IPrimaryKey extends Iterable<AbstractColumnValue>,
    Comparable<IPrimaryKey>, Serializable {
  /**
   * <p>
   * Indicates whether some other object is "equal to" this one.
   * </p>
   * <p>
   * The equals method implements an equivalence relation on non-null object
   * references:
   * </p>
   * <p>
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
   * </p>
   * <p>
   * Note that it is generally necessary to override the hashCode method
   * whenever this method is overridden, so as to maintain the general contract
   * for the hashCode method, which states that equal objects must have equal
   * hash codes.
   * </p>
   * 
   * @param key an IPrimaryKey of the same concrete type
   * @return 0 if the keys are different, 1 if they are the same
   */
  boolean equals(IPrimaryKey key);

  /**
   * <p>
   * Returns a hash code value for the object. This method is supported for the
   * benefit of hashtables such as those provided by java.util.Hashtable.
   * </p>
   * <p>
   * The general contract of hashCode is:
   * </p>
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
  int hashCode();

  /**
   * Get a deep copy of the primary key, with all internal components copied.
   * This enables you to change elements of the key without affecting the
   * original, as in changing column names in association keys.
   * 
   * @return a deep copy of the primary key
   */
  IPrimaryKey copy();

  /**
   * Get a text SQL expression suitable for inclusion in a WHERE clause that
   * selects a specific row based on the value of the primary key using JDBC
   * parameters in the format col1 = ? AND col2 = ?. The method prefixes the
   * column names in the string with the specified alias, which may be null. The
   * column names are ordered in the correct order (alphabetical order).
   * 
   * @param alias the SQL alias for the table, prefixed to the column names; if
   *          null, no alias is prefixed
   * 
   * @return a string representing a SQL WHERE clause expression
   */
  String getSqlWhereExpression(String alias);

  /**
   * Get a text SQL column list suitable for inclusion in the SELECT list of a
   * SQL statement that returns the values of the primary key of the table in
   * the FROM clause. Use the input alias name as the prefix for the columns.
   * The column names are ordered in the correct order (alphabetical order).
   * 
   * @param alias the SQL alias for the table, prefixed to the column names; if
   *          null, no alias is prefixed
   * 
   * @return a string representing a SQL SELECT column list
   */
  String getSqlColumnList(String alias);

  /**
   * Get a text SQL column list suitable for inclusion in the INSERT clause of a
   * SQL statement that inserts an object into a table. This list includes all
   * the names of the primary key columns required for insert in the correct
   * order (alphabetical order).
   * 
   * @return a string representing a SQL INSERT column list
   */
  String getSqlInsertColumnList();

  /**
   * <p>
   * Get a list of values suitable for inclusion in an error message in the
   * format
   * </p>
   * 
   * <pre>
   * <code>
   * &quot; (col1=value[,col2=value]...)&quot;
   * </code>
   * </pre>
   * 
   * where col1, col2, ... are the column names of the primary key and the
   * values are the current values or "null" if there is no value. Note that
   * there should be no blanks, line returns, or null characters in the string.
   * The column names appear in the string in the correct order (alphabetical
   * order).
   * 
   * @return a list of values suitable for embedding in an error message
   */
  String getValueList();

  /**
   * Get a set of column names for the key. The set cannot contain any duplicate
   * column names, and the concrete class must implement the method to throw an
   * exception if the internal representation contains duplicate column names.
   * The Set is unordered, so an iterator over the set will return the column
   * names in an undefined order.
   * 
   * @return a Set of String column names, all of which are distinct
   * @throws DuplicateKeyNameException when there is a duplicate column name
   */
  Set<String> getColumnNames() throws DuplicateKeyNameException;

  /**
   * <p>
   * Set the key values as parameter values into a JDBC PreparedStatement. You
   * must have already prepared the statement with SQL parameterized with the
   * correct number of question marks. The method returns the next index
   * available for value setting, which you can use to set any remaining
   * parameters in the SQL statement. The parameters are set in the correct
   * order (alphabetical order).
   * </p>
   * <p>
   * The <code>setInsertParams</code> method does the same thing as this method
   * but only for the primary key of the object being inserted. Use
   * <code>setParams</code> for setting foreign keys into the INSERT statement.
   * </p>
   * 
   * @param stmt the prepared statement; you must have already prepared the
   *          statement with a parameterized SQL statement
   * @param nextIndex the parameter index at which to start setting the primary
   *          key values
   * @return the next index at which to set values
   */
  int setParams(PreparedStatement stmt, int nextIndex);

  /**
   * <p>
   * Set the key values as parameters values into a JDBC PreparedStatement that
   * contains an INSERT statement. You must have already prepared the INSERT
   * statement with the SQL parameterized with the correct number of question
   * marks--none for identity primary keys. The method returns the next index
   * available for value setting, which you can use to set any remaining
   * parameters in the SQL INSERT statement. The columns appear in the list in
   * the correct order (alphabetical order).
   * </p>
   * <p>
   * This method by default works just like the <code>setParams</code> method.
   * Concrete implementations may override the behavior for insert statements
   * that require some different behavior than just setting parameters. For
   * example, inserting an object with an identity key (autogenerated id)
   * requires that you do not have the key in the values being inserted, so this
   * method does nothing while the equivalent <code>setParams</code> method
   * still sets parameters. Use this method for setting the primary key part of
   * the INSERT statement; use setParams for setting foreign keys.
   * </p>
   * 
   * @see #setParams(PreparedStatement, int)
   * 
   * @param stmt the prepared statement; you must have already prepared the
   *          statement with a parameterized SQL INSERT statement
   * @param nextIndex the parameter index at which to start setting the primary
   *          key values
   * @return the next index at which to set values
   */
  int setInsertParams(PreparedStatement stmt, int nextIndex);

  /**
   * <p>
   * Finalize a SQL INSERT statement after executing the statement. This method
   * provides any services required to finalize the values or columns of the
   * primary key after the actual INSERT into the database. Usually, this method
   * does nothing; for identity columns, the method fetches the auto-generated
   * primary key value and puts it into the primary key column.
   * </p>
   * <p>
   * You must have just executed the PreparedStatement before calling this
   * method, and you must have supplied the Statement.RETURN_GENERATED_KEYS
   * argument to the executeUpdate() call if you expect auto-generated keys to
   * come back.
   * </p>
   * <p>
   * The caller must close the statement, which will also close the result set
   * created for the auto-generated key.
   * </p>
   * 
   * @param stmt the PreparedStatement containing the INSERT statement just
   *          executed
   */
  void finalizeInsert(PreparedStatement stmt);

  /**
   * Get the unique name to use for the cache name.
   * 
   * @return the cache name
   */
  String getCacheName();

  /**
   * Get an iterator over the column values of the primary key object. The
   * iterator lets you iterate through the column values in order. You can use
   * objects of this class in Java foreach loops. The order is the correct order
   * (alphabetical order).
   * 
   * @return a List of ColumnValue&lt;Object&gt; objects of different data types
   */
  Iterator<AbstractColumnValue> iterator();

  /**
   * Get a Poesys/DB messaging object that represents a primary key.
   * 
   * @return a Poesys/MS primary key (IPrimaryKey)
   */
  com.poesys.ms.pk.IPrimaryKey getMessageObject();

  /**
   * Get a string that serves as a globally unique identifier for the object.
   * The string key must have no blank or non-printing characters \n or \r or
   * the null character \0. The column names appear in the string in the correct
   * order (alphabetical order).
   * 
   * @return a globally unique identifier string
   */
  String getStringKey();
}
