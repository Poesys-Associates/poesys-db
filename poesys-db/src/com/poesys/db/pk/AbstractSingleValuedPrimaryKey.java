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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import com.poesys.db.DuplicateKeyNameException;
import com.poesys.db.col.AbstractColumnValue;


/**
 * Implements the IPrimaryKey interface for a key constructed from a single
 * value. Concrete subclasses create values of different single-valued object
 * types such as BigInteger or UUID. The key list can be empty if the key is an
 * identity key and has not yet been finalized.
 * 
 * @author Robert J. Muller
 */
public abstract class AbstractSingleValuedPrimaryKey extends AbstractPrimaryKey {
  /**
   * Serial version UID for Serializable class
   */
  private static final long serialVersionUID = -1193848306520394312L;
  /** List of column values */
  protected List<AbstractColumnValue> list = null;

  /**
   * Create a primary key value that has no list of values at all.
   * 
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   */
  public AbstractSingleValuedPrimaryKey(String className) {
    super(className);
  }

  /**
   * Create a primary key value that is null.
   * 
   * @param list the list of column values with no columns in it
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   */
  public AbstractSingleValuedPrimaryKey(List<AbstractColumnValue> list,
                                        String className) {
    super(className);
    this.list = list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dto.IPrimaryKey#equals(com.poesys.db.dto.IPrimaryKey)
   */
  public boolean equals(IPrimaryKey key) {
    boolean ret = false;
    if (key instanceof AbstractSingleValuedPrimaryKey) {
      AbstractSingleValuedPrimaryKey other =
        (AbstractSingleValuedPrimaryKey)key;
      if (list != null && list.size() > 0) {
        AbstractColumnValue thisCol = list.get(0);
        AbstractColumnValue thatCol = other.list.get(0);
        ret = thisCol.equals(thatCol);
      } else if (other.list == null | other.list.size() == 0)
        // Nothing in either list, always return true
        ret = true;
    } else {
      // this list empty, other list not, always false
      ret = false;
    }
    return ret;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dto.IPrimaryKey#getColumnValueList()
   */
  public Iterator<AbstractColumnValue> iterator() {
    return (Iterator<AbstractColumnValue>)list.iterator();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dto.IPrimaryKey#getSqlColumnList()
   */
  public String getSqlColumnList(String alias) {
    String ret = "";
    if (list.size() > 0) {
      StringBuilder str = getAlias(alias);
      str.append(list.get(0).getName());
      ret = str.toString();
    }
    return ret;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dto.IPrimaryKey#getSqlWhereExpression(java.lang.String)
   */
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

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dto.IPrimaryKey#setParams(PreparedStatement, int)
   */
  public int setParams(PreparedStatement stmt, int nextIndex) {
    int next = nextIndex;
    // Iterate through the natural key values, setting them into the statement.
    for (AbstractColumnValue col : list) {
      col.setParam(stmt, nextIndex);
      next++;
    }
    return next;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dto.IPrimaryKey#getColumnNames()
   */
  public Set<String> getColumnNames() throws DuplicateKeyNameException {
    // Get the single-column name from the list.
    Set<String> set = new HashSet<String>();
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
  protected List<AbstractColumnValue> copyList() {
    AbstractColumnValue col = list.get(0).copy();
    List<AbstractColumnValue> list = new ArrayList<AbstractColumnValue>();
    list.add(col);

    return new CopyOnWriteArrayList<AbstractColumnValue>(list);
  }
}
