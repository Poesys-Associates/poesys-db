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
 * A test DTO class that serves as the root of a class generalization hierarchy
 * to enable testing of hierarchical operations. The root contains the sequence
 * primary key, and each class has a string attribute. Only the root string
 * attribute is not null (required).
 * 
 * @author Robert J. Muller
 */
public class RootClass extends AbstractTestDto {
  /** Generated serial version UID for Serializable object */
  private static final long serialVersionUID = 1427047128267368268L;
  /** Root column of the root class object */
  protected String rootCol;

  /**
   * Create a RootClass object.
   * 
   * @param key the primary key that identifies the root class object
   * @param rootCol the value of the root column
   */
  public RootClass(AbstractSingleValuedPrimaryKey key, String rootCol) {
    // No nested objects or constraints
    this.key = key;
    this.rootCol = rootCol;
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
    int retVal = key.compareTo(o.getPrimaryKey());
    if (retVal == 0) {
      retVal = rootCol.compareTo(((RootClass)o).rootCol);
    }
    return retVal;
  }

  /**
   * Get the root column value.
   * 
   * @return Returns the rootCol.
   */
  public synchronized String getRootCol() {
    return rootCol;
  }

  /**
   * Set the root column value.
   * 
   * @param rootCol The value to set.
   */
  public synchronized void setRootCol(String rootCol) {
    this.rootCol = rootCol;
    setChanged();
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
