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


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.poesys.db.dao.insert.IInsertSql;
import com.poesys.db.dao.insert.InsertSqlM2MLink;
import com.poesys.db.dao.insert.InsertSqlTernaryLink;
import com.poesys.db.pk.AbstractSingleValuedPrimaryKey;
import com.poesys.db.pk.IPrimaryKey;


/**
 * A test DTO class that serves as the second of three objects that participate
 * in associations. This class supports tests for a many-to-many association and
 * for an n-ary (ternary) association.
 * 
 * @author Bob Muller (muller@computer.org)
 */
public class Link2 extends AbstractTestDto {
  /** Generated serial version UID */
  private static final long serialVersionUID = 5439869236584295794L;
  /** test column of the link class object */
  protected String col;
  /** Many-to-many links */
  protected List<ManyToManyLink> m2mLinks;
  /** Ternary links */
  protected List<TernaryLink> ternaryLinks;

  /**
   * Private implementation of ISet for inserting the ManyToManyLink objects
   * associated with the Link2.
   * 
   * @author Bob Muller (muller@computer.org)
   */
  private class InsertManyToManyLinks extends
      AbstractBatchInsertSetter<ManyToManyLink, List<ManyToManyLink>> {
    /**  */
    private static final long serialVersionUID = 1L;

    public InsertManyToManyLinks() {
      super(getSubsystem(), Integer.MAX_VALUE);
    }

    @Override
    protected int getBatchSize() {
      return 2;
    }

    @Override
    protected String getClassName() {
      return Link2.class.getName();
    }

    @Override
    protected List<ManyToManyLink> getDtos() {
      return Link2.this.getM2mLinks();
    }

    @Override
    protected List<IInsertSql<ManyToManyLink>> getSql() {
      List<IInsertSql<ManyToManyLink>> list =
        new ArrayList<IInsertSql<ManyToManyLink>>();
      list.add(new InsertSqlM2MLink());
      return list;
    }
  }

  /**
   * Private implementation of ISet interface that inserts ternary link objects.
   * 
   * @author Bob Muller (muller@computer.org)
   */
  private class InsertTernaryLinks extends
      AbstractBatchInsertSetter<TernaryLink, List<TernaryLink>> {
    /**  */
    private static final long serialVersionUID = 1L;

    public InsertTernaryLinks() {
      super(getSubsystem(), Integer.MAX_VALUE);
    }

    @Override
    protected int getBatchSize() {
      return 2;
    }

    @Override
    protected String getClassName() {
      return Link2.class.getName();
    }

    @Override
    protected List<TernaryLink> getDtos() {
      return Link2.this.ternaryLinks;
    }

    @Override
    protected List<IInsertSql<TernaryLink>> getSql() {
      List<IInsertSql<TernaryLink>> list =
        new ArrayList<IInsertSql<TernaryLink>>();
      list.add(new InsertSqlTernaryLink());
      return list;
    }
  }

  /**
   * Create a Link2 object.
   * 
   * @param key the primary key of the object
   * @param col the data column of the object
   */
  public Link2(AbstractSingleValuedPrimaryKey key, String col) {
    this.key = key;
    this.col = col;

    // Create the setters for many-to-many and ternary links.
    insertSetters = new CopyOnWriteArrayList<ISet>();
    insertSetters.add(new InsertManyToManyLinks());
    insertSetters.add(new InsertTernaryLinks());
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dto.IDbDto#getPrimaryKey()
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
      retVal = col.compareTo(((Link2)o).col);
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
   * Get a hash code for the object based on the value of the primary key.
   */
  public int hashCode() {
    return key.hashCode();
  }

  /**
   * Get the col.
   * 
   * @return Returns the col.
   */
  public synchronized String getCol() {
    return col;
  }

  /**
   * Set the col.
   * 
   * @param col The col to set.
   */
  public synchronized void setCol(String col) {
    this.col = col;
    setChanged();
  }

  /**
   * Get the m2mLinks.
   * 
   * @return Returns the m2mLinks.
   */
  public List<ManyToManyLink> getM2mLinks() {
    return m2mLinks;
  }

  /**
   * Set the m2mLinks.
   * 
   * @param links The m2mLinks to set.
   */
  public void setM2mLinks(List<ManyToManyLink> links) {
    m2mLinks = links;
  }

  /**
   * Get the ternaryLinks.
   * 
   * @return Returns the ternaryLinks.
   */
  public List<TernaryLink> getTernaryLinks() {
    return ternaryLinks;
  }

  /**
   * Set the ternaryLinks.
   * 
   * @param ternaryLinks The ternaryLinks to set.
   */
  public void setTernaryLinks(List<TernaryLink> ternaryLinks) {
    this.ternaryLinks = ternaryLinks;
  }

  @Override
  public boolean equals(Object arg0) {
    return this.compareTo((IDbDto)arg0) == 0;
  }

  @Override
  public void markChildrenDeleted() {
    // Mark the many-to-many and ternary links deleted.
    for (IDbDto link : m2mLinks) {
      link.cascadeDelete();
    }

    for (IDbDto link : ternaryLinks) {
      link.cascadeDelete();
    }
  }
}
