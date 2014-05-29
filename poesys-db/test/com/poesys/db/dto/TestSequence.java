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


import com.poesys.db.pk.AbstractSingleValuedPrimaryKey;
import com.poesys.db.pk.IPrimaryKey;


/**
 * A data transfer object for the TestSequence table. This table supports a test
 * of operations on a sequence-generated Oracle primary key table.
 * 
 * <pre>
 * <code>
 * CREATE TABLE TestSequence (
 *   pkey NUMBER(38) NOT NULL PRIMARY KEY,
 *   col1 varchar2(10));
 * </code>
 * </pre>
 * 
 * @author Bob Muller (muller@computer.org)
 */
public class TestSequence extends AbstractTestDto {
  /** Generated serial version UID for Serializable object */
  private static final long serialVersionUID = -8723956213271643095L;

  /** The identity primary key for the object */
  private final AbstractSingleValuedPrimaryKey key;

  /** The String column col1 */
  private String col1 = null;

  /** Message for null compare object */
  private static final String NULL_COMP_MSG =
    "Null object supplied to comparison";

  /**
   * Create a TestSequence object.
   * 
   * @param key the sequence key of the object
   * @param col1 the first non-key column of the object
   */
  public TestSequence(IPrimaryKey key, String col1) {
    // No nested objects to set or constraints to validate
    this.key = (AbstractSingleValuedPrimaryKey)key;
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
  public synchronized String getCol1() {
    return col1;
  }

  /**
   * Set the col1.
   * 
   * @param col1 The col1 to set.
   */
  public synchronized void setCol1(String col1) {
    this.col1 = col1;
  }

  @Override
  public boolean equals(Object arg0) {
    return this.compareTo((IDbDto)arg0) == 0;
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  @Override
  public void markChildrenDeleted() {
    // Do nothing, no children.
  }
}
