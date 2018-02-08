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
import com.poesys.db.col.IColumnValue;
import com.poesys.db.col.json.JsonColumnValue;

import java.sql.PreparedStatement;
import java.util.*;

/**
 * Implements the IPrimaryKey interface for a key constructed from a single
 * value. Concrete subclasses create values of different single-valued object
 * types such as BigInteger or UUID. The key list can be empty if the key is an
 * identity key and has not yet been finalized.
 *
 * @author Robert J. Muller
 */
public abstract class AbstractSingleValuedPrimaryKey extends AbstractPrimaryKey {
  /** Serial version UID for Serializable class */
  private static final long serialVersionUID = 1L;

  /** List of column values; managed as list to support iterator in interface */
  protected List<IColumnValue> list = null;

  /**
   * Create a primary key value that has no list of values at all.
   *
   * @param className the name of the IDbDto class of the object that the
   *                  primary key identifies
   */
  public AbstractSingleValuedPrimaryKey(String className) {
    super(className);
  }

  /**
   * Create a primary key value that is null.
   *
   * @param list      the list of column values with no columns in it
   * @param className the name of the IDbDto class of the object that the
   *                  primary key identifies
   */
  public AbstractSingleValuedPrimaryKey(List<IColumnValue> list, String className) {
    super(className);
    this.list = list;
  }

  @Override
  public boolean equals(IPrimaryKey key) {
    boolean ret = false;

    if (key != null && key instanceof AbstractSingleValuedPrimaryKey) {
      AbstractSingleValuedPrimaryKey other = (AbstractSingleValuedPrimaryKey)key;
      if (list != null && list.size() > 0) {
        IColumnValue thisCol = list.get(0);
        IColumnValue thatCol = other.list.get(0);
        ret = thisCol.equals(thatCol);
      } else {
        if (other.list == null) {
          ret = true;
        } else if (other.list.size() == 0) {
          ret = true;
        }
      }
    } else {
      // this list empty, other list not, always false
      ret = false;
    }
    return ret;
  }

  @Override
  public int hashCode() {
    int code = 0;

    // Get the hash code of the single column value.
    if (list != null && list.size() == 1) {
      IColumnValue columnValue = list.get(0);
      code = columnValue.hashCode();
    }

    return code;
  }

  @Override
  public Iterator<IColumnValue> iterator() {
    return list.iterator();
  }

  @Override
  public String getSqlColumnList(String alias) {
    String ret = "";
    if (list.size() > 0) {
      StringBuilder str = getAlias(alias);
      str.append(list.get(0).getName());
      ret = str.toString();
    }
    return ret;
  }

  @Override
  public String getSqlWhereExpression(String alias) {
    String ret = "";
    if (list.size() > 0) {
      StringBuilder str = getAlias(alias);
      str.append(list.get(0).getName());
      str.append(COMP);
      ret = str.toString();
    }
    return ret;
  }

  @Override
  public int setParams(PreparedStatement stmt, int nextIndex) {
    // Set the single parameter with the first element in the list of column values.
    return list.get(0).setParam(stmt, nextIndex);
  }

  @Override
  public Set<String> getColumnNames() throws DuplicateKeyNameException {
    // Get the single-column name from the list.
    Set<String> set = new HashSet<>();
    if (list.size() > 0) {
      String columnName = list.get(0).getName();
      set.add(columnName);
    }
    return set;
  }

  /**
   * Copy the single-valued list and return the new list.
   *
   * @return the copy of the internal list
   */
  protected List<IColumnValue> copyList() {
    IColumnValue col = list.get(0).copy();
    List<IColumnValue> list = new ArrayList<>();
    list.add(col);

    return list;
  }

  /**
   * Build a JSON column value list from the column value list of this class. This is a
   * structured function used by subclasses that need a JsonColumnValue list.
   *
   * @return a list of JSON column values corresponding to the IColumnValues in the list in this
   * class
   */
  protected List<JsonColumnValue> getJsonColumnValueList() {
    // Build a JSON column value list from the column value list of this class.
    List<JsonColumnValue> values = new ArrayList<>(1);

    for (IColumnValue value : list) {
      values.add((JsonColumnValue)value.getJsonColumnValue());
    }
    return values;
  }
}
