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


import com.poesys.db.DuplicateKeyNameException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.col.ColumnNameComparator;
import com.poesys.db.col.IColumnValue;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implements the IPrimaryKey interface for a key constructed from multiple
 * values. The concrete subclass must implement the appropriate structure with
 * constructors or getter/setters so a factory may create the correct structure.
 * This class implements all the methods that use a standard Iterable iterator
 * to produce a list of columns and/or values.
 * 
 * @author Robert J. Muller
 */
public abstract class AbstractMultiValuedPrimaryKey extends AbstractPrimaryKey {
  /** Serial version UID for Serializable class */
  private static final long serialVersionUID = 1L;

  /**
   * List of column values comprising the key; must always be alphabetical, so
   * private with list operations implemented in this class as protected
   */
  private List<IColumnValue> list = null;

  /** Error message for no primary keys supplied */
  private static final String NO_KEYS_MSG = "com.poesys.db.pk.msg.no_key";

  private static final String EQUALS = " = ";

  /**
   * Create a AbstractMultiValuedPrimaryKey object.
   * 
   * @param className the name of the class used for caching
   */
  public AbstractMultiValuedPrimaryKey(String className) {
    super(className);
  }

  /**
   * Create a primary key with a list of column values. The column values must
   * have different names.
   * 
   * @param list the list of column values for the key
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   * @throws DuplicateKeyNameException when there is more than one column in the
   *           list with the same name
   * @throws InvalidParametersException when there are no keys in the list
   */
  public AbstractMultiValuedPrimaryKey(List<IColumnValue> list,
                                       String className)
      throws DuplicateKeyNameException, InvalidParametersException {
    super(className);
    // There must be at least one key column.
    if (list == null || list.size() == 0) {
      throw new InvalidParametersException(NO_KEYS_MSG);
    }

    // Set the internal list, ensuring alphabetical order and thread safety.
    setList(list);
    // Test the set of column names for duplication.
    getColumnNames();
  }

  /**
   * Set the multi-valued column list, ensuring it is in alphabetical order and
   * is thread safe.
   * 
   * @param newList the list to copy into the internal list
   */
  protected void setList(List<IColumnValue> newList) {
    // Ensure that the list is in alphabetical order.
    newList.sort(new ColumnNameComparator());
    list = newList;
  }

  @Override
  public String getSqlColumnList(String alias) {
    StringBuilder columnList = new StringBuilder();
    String sep = "";
    StringBuilder prefix = getAlias(alias);

    for (IColumnValue col : this) {
      columnList.append(sep);
      columnList.append(prefix);
      columnList.append(col.getName());
      sep = SEP; // after first time, use actual separator
    }
    return columnList.toString();
  }

  @Override
  public String getSqlWhereExpression(String alias) {
    StringBuilder expr = new StringBuilder();
    // Set up the alias for concatenation.
    StringBuilder prefix = getAlias(alias);
    String logicalOp = ""; // first time, no logical operator

    for (IColumnValue col : this) {
      expr.append(logicalOp);
      expr.append(prefix);
      expr.append(col.getName());
      expr.append(COMP);
      logicalOp = LOGICAL_OP; // after the first time
    }

    return expr.toString();
  }

  @Override
  public String getValueList() {
    StringBuilder str = new StringBuilder("(");
    String sep = "";
    for (IColumnValue value : this) {
      str.append(sep);
      str.append(value.getName());
      str.append(EQUALS);
      str.append(value.toString());
      sep = SEP;
    }
    str.append(")");

    return str.toString();
  }

  @Override
  public int setParams(PreparedStatement stmt, int nextIndex) {
    int next = nextIndex;
    // Iterate through the multiple key values, setting them into the statement.
    for (IColumnValue col : this) {
      col.setParam(stmt, next);
      next++;
    }
    return next;
  }

  @Override
  public boolean equals(IPrimaryKey key) {
    boolean ret = false;
    if (key instanceof AbstractMultiValuedPrimaryKey) {
      AbstractMultiValuedPrimaryKey other = (AbstractMultiValuedPrimaryKey)key;
      // If the keys have the same number of elements, compare them.
      if (this.list.size() == other.list.size()) {
        int index = 0;
        for (IColumnValue col : list) {
          ret = col.equals(other.list.get(index));
          // Break out of the loop at the first failure to equate.
          if (!ret) {
            break;
          } else {
            index++;
          }
        }
      }
    }
    return ret;
  }

  @Override
  public Iterator<IColumnValue> iterator() {
    // List is always in alphabetical order
    if (list == null) {
      list = new ArrayList<IColumnValue>(1);
    }
    return (Iterator<IColumnValue>)list.iterator();
  }

  /**
   * Copy the internal column list, preserving list order.
   * 
   * @return the copy of the list
   */
  protected List<IColumnValue> copyList() {
    List<IColumnValue> newList = new ArrayList<IColumnValue>();

    // Iterate using Iterable iterator, preserving order
    for (IColumnValue col : this) {
      newList.add(col);
    }

    return newList;
  }
}
