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


import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CopyOnWriteArrayList;

import com.poesys.db.ConstraintViolationException;
import com.poesys.db.pk.AssociationPrimaryKey;
import com.poesys.db.pk.IPrimaryKey;


/**
 * A test DTO class that serves as an association class for testing n-ary
 * linking.
 * 
 * @author Bob Muller (muller@computer.org)
 */
public class TernaryLink extends AbstractTestDto {
  /** Generated serial version UID for Serializable object */
  private static final long serialVersionUID = -7445049985437811625L;
  /** Primary key of the link class object */
  protected final AssociationPrimaryKey key;
  /** test column of the link class object */
  protected String col;
  /** Associated Link1 object */
  protected Link1 link1;
  /** Associated link2 object */
  protected Link2 link2;
  /** Associated link3 object */
  protected Link3 link3;

  /**
   * Validate a TernaryLink about to be inserted. All linked objects must have
   * primary keys to insert the link, or the insert will get a foreign key
   * violation. Another way to say this: you must have already inserted all
   * linked objects.
   * 
   * @author Bob Muller (muller@computer.org)
   */
  private class LinkTargetIsNotNew implements IValidate {
    /**  */
    private static final long serialVersionUID = 1L;
    private static final String ERROR_MSG = "com.poesys.db.ternary_link_is_new";

    /*
     * (non-Javadoc)
     * 
     * @see com.poesys.db.dto.IValidate#validate(java.sql.Connection)
     */
    public synchronized void validate(Connection connection)
        throws SQLException {
      Link1 link1 = TernaryLink.this.getLink1();
      Link2 link2 = TernaryLink.this.getLink2();
      Link3 link3 = TernaryLink.this.getLink3();
      if (link1.getStatus() == Status.NEW || link2.getStatus() == Status.NEW
          || link3.getStatus() == Status.NEW) {
        throw new ConstraintViolationException(ERROR_MSG);
      }
    }
  }

  /**
   * 
   * Create a TernaryLink object.
   * 
   * @param key the association primary key for the link
   * @param col the column for the link object
   */
  public TernaryLink(AssociationPrimaryKey key, String col) {
    this.key = key;
    this.col = col;

    // Create the insert validator.
    insertValidators = new CopyOnWriteArrayList<IValidate>();
    insertValidators.add(new LinkTargetIsNotNew());
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
      retVal = col.compareTo(((TernaryLink)o).col);
    }
    return retVal;
  }

  /**
   * Is the object equal to another object? This result is the same as
   * compareTo(o) == 0.
   * 
   * @param o the other root class
   * @return true if the objects are equal, false otherwise
   */
  public boolean equals(IDbDto o) {
    return compareTo(o) == 0;
  }

  /**
   * Get the col.
   * 
   * @return Returns the col.
   */
  public String getCol() {
    return col;
  }

  /**
   * Set the col.
   * 
   * @param col The col to set.
   */
  public void setCol(String col) {
    this.col = col;
    setChanged();
  }

  /**
   * Get the link1.
   * 
   * @return Returns the link1.
   */
  public Link1 getLink1() {
    return link1;
  }

  /**
   * Set the link1.
   * 
   * @param link1 The link1 to set. S
   */
  public void setLink1(Link1 link1) {
    this.link1 = link1;
    setChanged();
  }

  /**
   * Get the link2.
   * 
   * @return Returns the link2.
   */
  public Link2 getLink2() {
    return link2;
  }

  /**
   * Set the link2.
   * 
   * @param link2 The link2 to set.
   */
  public void setLink2(Link2 link2) {
    this.link2 = link2;
    setChanged();
  }

  /**
   * Get the link3.
   * 
   * @return Returns the link3.
   */
  public Link3 getLink3() {
    return link3;
  }

  /**
   * Set the link3.
   * 
   * @param link3 The link3 to set.
   */
  public void setLink3(Link3 link3) {
    this.link3 = link3;
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
