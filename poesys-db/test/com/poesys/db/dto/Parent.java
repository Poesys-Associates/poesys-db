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
import java.util.Collection;
import java.util.List;

import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.DataEvent;
import com.poesys.db.dao.IDaoFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dao.delete.DeleteSqlChild;
import com.poesys.db.dao.delete.IDeleteBatch;
import com.poesys.db.dao.insert.IInsertBatch;
import com.poesys.db.dao.insert.IInsertSql;
import com.poesys.db.dao.insert.InsertSqlChild;
import com.poesys.db.dao.query.ChildrenQuerySql;
import com.poesys.db.dao.query.IParameterizedQuerySql;
import com.poesys.db.dao.update.IUpdateBatch;
import com.poesys.db.dao.update.UpdateSqlChild;
import com.poesys.db.pk.GuidPrimaryKey;
import com.poesys.db.pk.IPrimaryKey;


/**
 * Test DTO class for Parent database table
 * 
 * @author muller
 * 
 */
public class Parent extends AbstractTestDto {
  /** Generated serial version UID for Serializable object */
  private static final long serialVersionUID = -802769967074994359L;
  /** data column for the parent */
  private String col1 = null;
  /** Ordered list of children of the parent */
  private List<Child> children = new ArrayList<Child>();
  /** Batch size for average list of children */
  private static final int CHILD_BATCH_SIZE = 3;

  /**
   * Inner class that implements ISet for querying children.
   * 
   * @author Bob Muller (bob@poesys.com)
   */
  private class QueryChildren extends
      AbstractListSetter<Child, Parent, List<Child>> {
    /**  */
    private static final long serialVersionUID = 1L;

    public QueryChildren() {
      super(getSubsystem(), Integer.MAX_VALUE);
    }

    @Override
    protected String getClassName() {
      return Parent.class.getName();
    }

    @Override
    protected int getFetchSize() {
      return 10;
    }

    @Override
    protected Parent getParametersDto() {
      return Parent.this;
    }

    @Override
    protected IParameterizedQuerySql<Child, Parent> getSql() {
      return new ChildrenQuerySql();
    }

    @Override
    protected void set(List<Child> list) {
      // Setting children marks the DTO changed, catch problem with that
      try {
        setChildren(list);
      } catch (DtoStatusException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }

    @Override
    public boolean isSet() {
      return getChildren() != null;
    }
  }

  /**
   * Inner class that implements ISet for inserting children.
   * 
   * @author Bob Muller (bob@poesys.com)
   */
  private class InsertChildren extends
      AbstractBatchInsertSetter<Child, List<Child>> {
    /**  */
    private static final long serialVersionUID = 1L;

    public InsertChildren() {
      super(getSubsystem(), Integer.MAX_VALUE);
    }

    @Override
    protected int getBatchSize() {
      return 2;
    }

    @Override
    protected String getClassName() {
      return Parent.class.getName();
    }

    @Override
    protected List<Child> getDtos() {
      return getChildren();
    }

    @Override
    protected List<IInsertSql<Child>> getSql() {
      List<IInsertSql<Child>> list = new ArrayList<IInsertSql<Child>>();
      list.add(new InsertSqlChild());
      return list;
    }
  }

  /**
   * Inner class that implements ISet for pre-processing children.
   * 
   * @author Bob Muller (bob@poesys.com)
   */
  private class PreprocessChildren extends
      AbstractProcessNestedObjects<Child, List<Child>> {

    /** version UID for Serializable object */
    private static final long serialVersionUID = 1L;

    public PreprocessChildren() {
      super(getSubsystem(), Integer.MAX_VALUE);
    }

    @Override
    protected void doNew(List<Child> dtos) {
      // Do nothing.
    }

    @Override
    protected void doChanged(List<Child> dtos) {
      // Do nothing.
    }

    @Override
    protected void doDeleted(List<Child> dtos) {
      IDaoManager manager = DaoManagerFactory.getManager(getSubsystem());
      IDaoFactory<Child> factory =
        manager.getFactory(getClassName(), getSubsystem(), expiration);
      IDeleteBatch<Child> dao = factory.getDeleteBatch(new DeleteSqlChild());
      dao.delete(dtos, 2);
    }

    @Override
    protected String getClassName() {
      return "com.poesys.db.dto.Child";
    }

    @Override
    protected List<Child> getDtos() {
      return getChildren();
    }
  }

  /**
   * Inner class that implements ISet for pre-processing children.
   * 
   * @author Bob Muller (bob@poesys.com)
   */
  private class PostprocessChildren extends
      AbstractProcessNestedObjects<Child, List<Child>> {

    /**  */
    private static final long serialVersionUID = 1L;

    public PostprocessChildren() {
      super(getSubsystem(), Integer.MAX_VALUE);
    }

    @Override
    protected void doDeleted(List<Child> dtos) {
      // Do nothing, children already deleted
    }

    @Override
    protected List<Child> getDtos() {
      return getChildren();
    }

    @Override
    protected void doNew(List<Child> dtos) {
      // Insert the children.
      IDaoManager manager = DaoManagerFactory.getManager(getSubsystem());
      IDaoFactory<Child> factory =
        manager.getFactory(getClassName(), subsystem, expiration);
      IInsertBatch<Child> dao = factory.getInsertBatch(new InsertSqlChild());
      dao.insert(dtos, CHILD_BATCH_SIZE);
    }

    @Override
    protected void doChanged(List<Child> dtos) {
      IDaoManager manager = DaoManagerFactory.getManager(getSubsystem());
      IDaoFactory<Child> factory =
        manager.getFactory(getClassName(), subsystem, expiration);
      // Update the children.
      IUpdateBatch<Child> dao = factory.getUpdateBatch(new UpdateSqlChild());
      dao.update(dtos, CHILD_BATCH_SIZE);
    }

    @Override
    protected String getClassName() {
      return "com.poesys.db.dto.Child";
    }
  }

  /**
   * Post-process setter for post-processing nested object property community.
   */
  private class PostProcessChildrenSetter 
      extends com.poesys.db.dto.AbstractPostProcessSetter {
    private static final long serialVersionUID = 1L;

    /**
     * Create a PostProcessChildrenSetter object.
     */
    public PostProcessChildrenSetter() {
      super("org.tair.db.community", 2147483647);
    }

    @Override
    protected String getClassName() {
      return Parent.class.getName();
    }

    @Override
    protected Collection<IDbDto> getDtos() {
      Collection<IDbDto> collection = new ArrayList<IDbDto>(children);
      return collection;
    }
  }

  /**
   * Create a Parent with a GUID primary key value and a list of setters for
   * setting the composite child collection field.
   * 
   * @param key the GUID primary key for the parent
   * @param col1 the value for col1
   */
  public Parent(IPrimaryKey key, String col1) {
    super();

    // Create the setters and validators.
    this.insertSetters = new ArrayList<ISet>();
    insertSetters.add(new InsertChildren());
    this.preSetters = new ArrayList<ISet>();
    preSetters.add(new PreprocessChildren());
    this.postSetters = new ArrayList<ISet>();
    postSetters.add(new PostprocessChildren());
    this.postProcessSetters = new ArrayList<ISet>();
    postProcessSetters.add(new PostProcessChildrenSetter());
    this.querySetters = new ArrayList<ISet>();
    querySetters.add(new QueryChildren());

    this.key = (GuidPrimaryKey)key;
    this.col1 = col1;
  }

  @Override
  public int compareTo(IDbDto arg0) {
    int retVal = key.compareTo(arg0.getPrimaryKey());
    if (retVal == 0 && col1 != null && ((Parent)arg0).col1 != null) {
      retVal = col1.compareTo(((Parent)arg0).col1);
    } else if (col1 == null && ((Parent)arg0).col1 == null) {
      retVal = 0;
    } else {
      // one col1 is not null, the other isn't
      if (col1 == null) {
        // null ordered first, this greater than that
        retVal = 1;
      } else
        // null ordered first, that greater than this
        retVal = -1;
    }
    return retVal;
  }

  @Override
  public boolean equals(Object arg0) {
    return this.compareTo((IDbDto)arg0) == 0;
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
   * Get the children of the parent in an ordered list
   * 
   * @return the children
   */
  public List<Child> getChildren() {
    return children;
  }

  /**
   * Set the ordered list of children for the parent
   * 
   * @param children the children to set
   */
  public void setChildren(List<Child> children) {
    this.children = children;
    setChanged();
  }

  @Override
  public void markChildrenDeleted() {
    // Mark the Child objects deleted.
    for (AbstractTestDto child : children) {
      child.cascadeDelete();
    }
  }

  @Override
  public void update(ISubject subject, DataEvent event) {
    // Remove any deleted children.
    if (subject instanceof Child && event == DataEvent.DELETE) {
      children.remove(subject);
    }
  }
}
