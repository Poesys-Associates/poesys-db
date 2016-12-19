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


/**
 * A sub class of the sub class of the root class; provides a test object for
 * testing operations on an inheritance hierarchy; the sub-sub class specializes
 * the sub-class, which specializes the root class.
 * 
 * @author Robert J. Muller
 */
public class SubSubClass extends SubClass {
  /** Generated serial version UID for Serializable object */
  private static final long serialVersionUID = 7260056456601736283L;
  /** The value column of the sub class */
  protected String subSubCol = null;

  /**
   * Create a SubSubClass object.
   * 
   * @param key the primary key of the class object
   * @param rootCol the root column value of the class object
   * @param subCol the sub class column value of the class object
   * @param subSubCol the sub-sub class column value of the class object
   */
  public SubSubClass(AbstractSingleValuedPrimaryKey key,
                     String rootCol,
                     String subCol,
                     String subSubCol) {
    super(key, rootCol, subCol);
    this.subSubCol = subSubCol;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(IDbDto o) {
    int retVal = super.compareTo(o);
    if (retVal == 0) {
      retVal = subSubCol.compareTo(((SubSubClass)o).subSubCol);
    }
    return retVal;
  }

  /**
   * Get the subSubCol.
   * 
   * @return Returns the subSubCol.
   */
  public synchronized String getSubSubCol() {
    return subSubCol;
  }

  /**
   * Set the subSubCol.
   * 
   * @param subSubCol The subSubCol to set.
   */
  public synchronized void setSubCol(String subSubCol) {
    this.subSubCol = subSubCol;
    setChanged();
  }
}
