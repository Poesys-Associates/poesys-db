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
import java.sql.SQLException;


/**
 * An abstract implementation of the IPrimaryKey interface that is the parent of
 * all the other primary key implementations, sharing code that all or most
 * classes use.
 * 
 * @author Robert J. Muller
 */
public abstract class AbstractPrimaryKey implements IPrimaryKey {
  /**
   * Serial version UID for Serializable class
   */
  private static final long serialVersionUID = -927979208981022398L;

  /** Separator constant for multiple values in a list */
  protected static final String SEP = ", ";

  /** Constant expression fragment for key value comparison */
  protected static final String COMP = " = ?";

  /** Constant separator for the table alias */
  protected static final String ALIAS_SEP = ".";

  /** Constant logical operator that separates expressions in WHERE clause */
  protected static final String LOGICAL_OP = " AND ";

  /**
   * The string that separates components of the string key
   * 
   * @see #getStringKey()
   */
  private static final String KEY_SEP = ":";

  /** The name of the IDbDto class of the object that the primary key identifies */
  protected String className;

  /**
   * Create a AbstractPrimaryKey object.
   * 
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   */
  public AbstractPrimaryKey(String className) {
    this.className = className;
  }
  
  @Override
  public String getCacheName() {
    return className;
  }

  @Override
  public int hashCode() {
    return this.getValueList().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    IPrimaryKey other = (IPrimaryKey)o;
    return other.getValueList().equals(getValueList());
  }

  /*
   * Compare two primary keys based on column-by-column comparisons.
   */
  public int compareTo(IPrimaryKey key) {
    return getValueList().compareTo(key.getValueList());
  }

  @Override
  public String getSqlInsertColumnList() {
    // Default implementation gets the simple column list
    return getSqlColumnList("");
  }

  @Override
  public int setInsertParams(PreparedStatement stmt, int nextIndex)
      throws SQLException {
    // Default implementation sets the simple set of columns
    return setParams(stmt, nextIndex);
  }

  @Override
  public void finalizeInsert(PreparedStatement stmt) throws SQLException {
    // Do nothing as the default finalize action. Override this for identity
    // keys.
  }

  /**
   * If the input alias is not null, append an alias separator to it.
   * 
   * @param alias the alias
   * @return null or an alias and a separator
   */
  public StringBuilder getAlias(String alias) {
    StringBuilder str = new StringBuilder(alias);

    // Separate alias if there is one.
    if (str.length() > 0) {
      str.append(ALIAS_SEP);
    }
    return str;
  }

  @Override
  public String getStringKey() {
    String key = null;
    String valueList = getValueList();
    // memcached keys don't allow blanks or line-end characters.
    valueList = valueList.replace(' ', '_');
    valueList = valueList.replace('\r', '?');
    valueList = valueList.replace('\n', '~');
    // xmemcached string keys must be <= 250 characters
    key = className + KEY_SEP + valueList;
    if (key.length() > 250) {
      // Key is too long, hash it and pray for no conflicts.
      key = new Integer(key.hashCode()).toString();
    }
    return key;
  }

  @Override
  public String toString() {
    return getValueList();
  }
}
