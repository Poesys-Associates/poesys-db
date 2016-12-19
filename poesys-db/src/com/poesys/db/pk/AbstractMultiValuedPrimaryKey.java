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


import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.poesys.db.DuplicateKeyNameException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.col.AbstractColumnValue;
import com.poesys.db.col.ColumnNameComparator;


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
  private List<AbstractColumnValue> list = null;

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
  public AbstractMultiValuedPrimaryKey(List<AbstractColumnValue> list,
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
  protected void setList(List<AbstractColumnValue> newList) {
    // Ensure that the list is in alphabetical order.
    Collections.sort(newList, new ColumnNameComparator());
    // Ensure that the list is thread safe.
    list = new CopyOnWriteArrayList<AbstractColumnValue>(newList);
  }

  @Override
  public String getSqlColumnList(String alias) {
    StringBuilder columnList = new StringBuilder();
    String sep = "";
    StringBuilder prefix = getAlias(alias);

    for (AbstractColumnValue col : this) {
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

    for (AbstractColumnValue col : this) {
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
    for (AbstractColumnValue value : this) {
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
    for (AbstractColumnValue col : this) {
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
        for (AbstractColumnValue col : list) {
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
  public Iterator<AbstractColumnValue> iterator() {
    // List is always in alphabetical order
    if (list == null) {
      list = new ArrayList<AbstractColumnValue>(1);
    }
    return (Iterator<AbstractColumnValue>)list.iterator();
  }

  /**
   * Copy the internal column list, preserving list order.
   * 
   * @return the copy of the list
   */
  protected List<AbstractColumnValue> copyList() {
    List<AbstractColumnValue> newList = new ArrayList<AbstractColumnValue>();

    // Iterate using Iterable iterator, preserving order
    for (AbstractColumnValue col : this) {
      newList.add(col);
    }

    return new CopyOnWriteArrayList<AbstractColumnValue>(newList);
  }
}
