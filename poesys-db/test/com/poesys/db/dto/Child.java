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


import java.math.BigInteger;

import com.poesys.db.pk.CompositePrimaryKey;


/**
 * Test DTO class for Child database table. A Parent owns a child, and the id of
 * the parent is part of the Composite Primary Key. The class supports updating
 * the childNumber, the other component of the primary key, by keeping a record
 * of the original key when the key changes.
 * 
 * @author muller
 * 
 */
public class Child extends AbstractTestDto {
  /** Generated serial version UID for Serializable object */
  private static final long serialVersionUID = 8707036032449312170L;

  /**
   * Ordered subkey value; allows direct access to the value and changing the
   * value without changing the primary key.
   */
  private BigInteger childNumber = null;
  /** data column for the child */
  private String col1 = null;

  /**
   * Create a child with a composite primary key containing the parent key.
   * 
   * @param key the composite primary key
   * @param childNumber the data member representation of part of the primary
   *          key
   * @param col1 the child data value
   */
  public Child(CompositePrimaryKey key, BigInteger childNumber, String col1) {
    // No setters or constraints for this class
    this.key = key;
    // duplicate child number for display outside primary key
    this.childNumber = childNumber;
    this.col1 = col1;
  }

  @Override
  public int compareTo(IDbDto arg0) {
    int retVal = key.compareTo(arg0.getPrimaryKey());
    if (retVal == 0) {
      retVal = col1.compareTo(((Child)arg0).col1);
    }
    return retVal;
  }

  @Override
  public boolean equals(Object o) {
    return this.compareTo((IDbDto)o) == 0;
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  /**
   * Get the data column
   * 
   * @return the data column
   */
  public synchronized String getCol1() {
    return col1;
  }

  /**
   * Set the value of the data column
   * 
   * @param col1 the col1 value to set
   */
  public synchronized void setCol1(String col1) {
    this.col1 = col1;
    setChanged();
  }

  /**
   * Get the childNumber.
   * 
   * @return Returns the childNumber.
   */
  public synchronized BigInteger getChildNumber() {
    return childNumber;
  }

  /**
   * Set the childNumber. This changes the primary key when you update the
   * Child. Note that the current IPrimaryKey object does not change. Also, if
   * the input number is the same as the current value, there is no change to
   * the object: it remains in its current status. This optimization prevents
   * unnecessary updates of the primary key.
   * 
   * @param childNumber The childNumber to set.
   */
  public synchronized void setChildNumber(BigInteger childNumber) {
    if (!this.childNumber.equals(childNumber)) {
      this.childNumber = childNumber;
      setChanged();
    }
  }

  @Override
  public void markChildrenDeleted() {
    // Do nothing, no children.
  }
}
