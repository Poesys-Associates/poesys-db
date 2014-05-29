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
 * A sub class of the root class; provides a test object for testing operations
 * on an inheritance hierarchy; the sub class is a specialization of the root
 * class.
 * 
 * @author Bob Muller (muller@computer.org)
 */
public class SubClass extends RootClass {
  /** Generated serial version UID for Serializable object */
  private static final long serialVersionUID = 2412636381610687376L;
  /** The value column of the sub class */
  protected String subCol = null;

  /**
   * Create a SubClass object.
   * 
   * @param key the primary key of the class object
   * @param rootCol the root column value of the class object
   * @param subCol the sub class column value of the class object
   */
  public SubClass(AbstractSingleValuedPrimaryKey key,
                  String rootCol,
                  String subCol) {
    super(key, rootCol);
    this.subCol = subCol;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(IDbDto o) {
    int retVal = super.compareTo(o);
    if (retVal == 0) {
      retVal = subCol.compareTo(((SubClass)o).subCol);
    }
    return retVal;
  }

  /**
   * Get the subCol.
   * 
   * @return Returns the subCol.
   */
  public synchronized String getSubCol() {
    return subCol;
  }

  /**
   * Set the subCol.
   * 
   * @param subCol The subCol to set.
   */
  public synchronized void setSubCol(String subCol) {
    this.subCol = subCol;
    setChanged();
  }
}
