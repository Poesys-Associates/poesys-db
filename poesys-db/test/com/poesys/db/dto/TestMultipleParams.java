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


import com.poesys.db.InvalidParametersException;
import com.poesys.db.pk.IPrimaryKey;


/**
 * A Data Transfer Object that represents the parameters for a standard update
 * of all rows in the TestMultiple class that have a particular column type
 * (colType).
 * 
 * @author Robert J. Muller
 */
public class TestMultipleParams extends AbstractTestDto {
  /** Generated serial version UID for Serializable object */
  private static final long serialVersionUID = -5122371309900812557L;
  /** TestMultiple.col1 value */
  private String col1;
  /** TestMultiple.colType value */
  private String colType;
  /** Error message for a bad column type */
  private static final String BAD_COL_TYPE = "com.poesys.db.bad_col_type";

  /**
   * Create a TestMultipleParams object with col1 and colType parameters.
   * 
   * @param col1 the col1 parameter value
   * @param colType the colType parameter value
   * @throws InvalidParametersException when the colType is not "a" or "b"
   */
  public TestMultipleParams(String col1, String colType)
      throws InvalidParametersException {
    if (!(colType.equals("a") || colType.equals("b"))) {
      throw new InvalidParametersException(BAD_COL_TYPE);
    }

    this.col1 = col1;
    this.colType = colType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dto.AbstractDto#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object arg0) {
    TestMultipleParams that = (TestMultipleParams)arg0;
    if (this.col1.equals(that.col1) && this.colType.equals(that.colType)) {
      return true;
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dto.AbstractDto#hashCode()
   */
  @Override
  public int hashCode() {
    return (col1 + colType).hashCode();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dto.IDto#getPrimaryKey()
   */
  public IPrimaryKey getPrimaryKey() {
    // No primary key
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(IDbDto arg0) {
    TestMultipleParams that = (TestMultipleParams)arg0;
    if (this.col1.compareTo(that.col1) != 0) {
      return this.col1.compareTo(that.col1);
    }
    return this.colType.compareTo(that.colType);
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

  /**
   * Get the colType.
   * 
   * @return Returns the colType.
   */
  public synchronized String getColType() {
    return colType;
  }

  /**
   * Set the colType.
   * 
   * @param colType The colType to set.
   */
  public synchronized void setColType(String colType) {
    this.colType = colType;
  }

  @Override
  public void markChildrenDeleted() {
    // Do nothing, no children.
  }
}
