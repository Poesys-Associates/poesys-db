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


import com.poesys.db.col.IColumnValue;
import com.poesys.db.col.StringColumnValue;
import com.poesys.db.pk.IPrimaryKey;
import com.poesys.db.pk.NaturalPrimaryKey;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * A data transfer object for the TestNatural table.
 * 
 * <pre>
 * <code>
 * CREATE TABLE TestNatural (
 *   key1 varchar2(10) NOT NULL,
 *   key2 varchar2(10) NOT NULL,
 *   col1 number,
 *   PRIMARY KEY (key1, key2));
 * </code>
 * </pre>
 * 
 * @author Robert J. Muller
 */
public class TestNatural extends AbstractTestDto {
  /** Generated serial version UID for Serializable object */
  private static final long serialVersionUID = -8655519924326979904L;

  /** The first primary key natural value for display only */
  private final String key1;

  /** The second primary key natural value for display only */
  private final String key2;

  /** The BigDecimal/NUMBER column col1 */
  private BigDecimal col1;

  /** Message for null compare object */
  private static final String NULL_COMP_MSG =
    "Null object supplied to comparison";

  /**
   * Create a TestNatural object.
   * 
   * @param key1 the first part of the natural key of the object
   * @param key2 the second part of the natural key of the object
   * @param col1 the first non-key column of the object
   */
  public TestNatural(String key1, String key2, BigDecimal col1) {
    // No nested objects to set or constraints to validate
    List<IColumnValue> list = new ArrayList<>();
    list.add(new StringColumnValue("key1", key1));
    list.add(new StringColumnValue("key2", key2));
    this.key = new NaturalPrimaryKey(list, "com.poesys.db.dto.TestNatural");

    this.key1 = key1;
    this.key2 = key2;
    this.col1 = col1;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dto.IDto#getPrimaryKey()
   */
  public IPrimaryKey getPrimaryKey() {
    return key;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(IDbDto o) {
    if (o == null) {
      throw new RuntimeException(NULL_COMP_MSG);
    }
    return this.key.compareTo(o.getPrimaryKey());
  }

  /**
   * Get the col1.
   * 
   * @return Returns the col1.
   */
  public synchronized BigDecimal getCol1() {
    return col1;
  }

  /**
   * Set the col1.
   * 
   * @param col1 The col1 to set.
   */
  public synchronized void setCol1(BigDecimal col1) {
    this.col1 = col1;
    setChanged();
  }

  @Override
  public boolean equals(Object arg0) {
    boolean equals = false;
    if (arg0 != null && arg0 instanceof TestNatural) {
      equals = this.compareTo((IDbDto)arg0) == 0;
    }

    return equals;
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  @Override
  public void markChildrenDeleted() {
    // Do nothing, no children.
  }

  /**
   * Get the key1 value.
   * 
   * @return Returns the key1 value.
   */
  public String getKey1() {
    return key1;
  }

  /**
   * Get the key2 value.
   * 
   * @return Returns the key2 value.
   */
  public String getKey2() {
    return key2;
  }
}
