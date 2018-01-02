/*
 * Copyright (c) 2018 Poesys Associates. All rights reserved.
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

import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.pk.GuidPrimaryKey;
import com.poesys.db.pk.IPrimaryKey;
import com.poesys.db.pk.NaturalPrimaryKey;

import java.util.ArrayList;
import java.util.Collection;

/** Copyright (c) 2018 Poesys Associates. All rights reserved. */
public class CompositeParent extends AbstractTestDto {
  /** Default serial version UID for the Serializable DTO */
  private static final long serialVersionUID = 1L;

  // Doesn't serialize; package access allows proxy to set on readObject()
  transient private java.util.Collection<ChildRootClass> children;
  // Ordered list of keys of the objects in the accounts list
  transient private java.util.List<com.poesys.db.pk.IPrimaryKey> childrenKeys =
    new java.util.ArrayList<>();

  private Integer parentId = null;

  /**
   * Get the parent id.
   *
   * @return an id
   */
  public Integer getParentId() {
    return parentId;
  }

  /**
   * Set the parent id.
   *
   * @param parentId the unique identifier for the parent
   */
  public void setParentId(Integer parentId) {
    this.parentId = parentId;
  }

  /**
   * Query setter for querying nested children of parent
   */
  private class QueryChildRootClassSetter extends com.poesys.db.dto
    .AbstractListSetter<ChildRootClass,
    CompositeParent, java.util.Collection<ChildRootClass>> {
    private static final long serialVersionUID = 1L;
    private static final int FETCH_SIZE = 10;

    /**
     * Create a QueryChildrenSetter object.
     */
    public QueryChildRootClassSetter() {
      super("com.poesys.db.poesystest.mysql", 2147483647);
    }

    @Override
    protected String getClassName() {
      return ChildRootClass.class.getName();
    }

    @Override
    protected int getFetchSize() {
      return FETCH_SIZE;
    }

    @Override
    protected CompositeParent getParametersDto() {
      return CompositeParent.this;
    }

    @Override
    protected com.poesys.db.dao.query.IParameterizedQuerySql<ChildRootClass, CompositeParent>
    getSql() {
      return new QueryChildrenByParent();
    }

    @Override
    protected void set(java.util.Collection<ChildRootClass> list) {
      // No status change; this is just filling in the object data.
      children = list;
      // Add the primary keys to the serialized key list if there are any.
      if (children != null) {
        if (childrenKeys != null) {
          childrenKeys.clear();
        } else {
          childrenKeys = new java.util.ArrayList<>();
        }
        for (com.poesys.db.dto.IDbDto object : children) {
          childrenKeys.add(object.getPrimaryKey());
        }
      }
    }

    @Override
    public boolean isSet() {
      // Object is set if the associated accounts list is not null
      return children != null;
    }
  }

  /**
   * Read-Object setter for de-serializing nested accounts collection
   */
  private class ReadChildrenSetter extends com.poesys.db.dto
    .AbstractCollectionReadSetter<ChildRootClass> {
    private static final long serialVersionUID = 1L;

    /**
     * Create a ReadAccountsSetter object to read the accounts collection.
     */
    public ReadChildrenSetter() {
      super("com.poesys.db.poesystest.mysql", 2147483647);
    }

    @Override
    protected String getClassName() {
      return ChildRootClass.class.getName();
    }

    @Override
    protected java.util.Collection<ChildRootClass> getObjectCollection() {
      return children;
    }

    @Override
    protected java.util.List<IPrimaryKey> getPrimaryKeys() {
      return childrenKeys;
    }

    @Override
    protected com.poesys.db.dao.query.IKeyQuerySql<ChildRootClass> getSql() {
      return new QueryChildRootClass();
    }

    @Override
    protected void set(java.util.Collection<ChildRootClass> collection) {
      children = collection;
    }
  }

  /**
   * Post-processing setter for post-processing nested to-many association accounts.
   */
  private class PostProcessChildrenSetter extends com.poesys.db.dto.AbstractPostProcessSetter {
    private static final long serialVersionUID = 1L;

    // Association accounts source: AddToManyChildCollectionProperties

    /**
     * Create an PostProcessAccountsSetter object.
     */
    public PostProcessChildrenSetter() {
      super("com.poesys.db.poesystest.mysql", 2147483647);
    }

    @Override
    protected String getClassName() {
      return ConcreteChildSubClass.class.getName();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected java.util.Collection<com.poesys.db.dto.IDbDto> getDtos() {
      java.util.Collection<? extends com.poesys.db.dto.IDbDto> dtos = children;
      return (java.util.Collection<com.poesys.db.dto.IDbDto>)dtos;
    }
  }

  /**
   * Insert setter for inserting nested to-many association children.
   */
  private class InsertChildrenSetter extends com.poesys.db.dto.AbstractInsertSetter {
    private static final long serialVersionUID = 1L;

    // Association accounts source: AddToManyChildCollectionProperties

    /**
     * Create an InsertAccountsSetter object.
     */
    public InsertChildrenSetter() {
      super("com.poesys.db.poesystest.mysql", 2147483647);
    }

    @Override
    protected String getClassName() {
      return ConcreteChildSubClass.class.getName();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected java.util.Collection<com.poesys.db.dto.IDbDto> getDtos() {
      java.util.Collection<? extends com.poesys.db.dto.IDbDto> dtos = children;
      return (java.util.Collection<com.poesys.db.dto.IDbDto>)dtos;
    }

    @Override
    protected boolean createKey() {
      return true;
    }

    @Override
    protected void doSet(PoesysTrackingThread thread) {
      super.doSet(thread);
    }
  }

  /**
   * Setter for processing added children and updated children. Deleted children are deleted in the
   * database cascade delete.
   */

  private class UpdateChildrenSetter extends com.poesys.db.dto
    .AbstractProcessNestedObjects<ChildRootClass, java.util.Collection<ChildRootClass>> {
    private static final long serialVersionUID = 1L;
    private static final int BATCH_SIZE = 100;

    /**
     * Create an UpdateChildrenSetter object.
     */
    public UpdateChildrenSetter() {
      super("com.poesys.db.poesystest.mysql", 2147483647);
    }

    @Override
    protected void doChanged(java.util.Collection<ChildRootClass> dtos) {
      // accounts source: AddToManyChildCollectionProperties
      // Immutable: false
      com.poesys.db.dao.IDaoManager manager =
        com.poesys.db.dao.DaoManagerFactory.getManager(subsystem);

      com.poesys.db.dao.IDaoFactory<ChildRootClass> factory =
        manager.getFactory(ChildRootClass.class.getName(), subsystem, 2147483647);
      com.poesys.db.dao.update.IUpdateBatch<ChildRootClass> updater =
        factory.getUpdateBatch(new UpdateChildRootClass());

      // Update the object of the leaf class.
      updater.update(dtos, dtos.size() / 2);
      // Complete the update by setting the DTOs to EXISTING status.
      for (com.poesys.db.dto.IDbDto dto : dtos) {
        if (dto.getStatus() == IDbDto.Status.CHANGED) {
          dto.setExisting();
        }
      }
    }

    @Override
    protected void doDeleted(Collection<ChildRootClass> dtos) {
      com.poesys.db.dao.IDaoManager manager =
        com.poesys.db.dao.DaoManagerFactory.getManager(subsystem);
      com.poesys.db.dao.IDaoFactory<ChildRootClass> factory =
        manager.getFactory(ChildRootClass.class.getName(), subsystem, 2147483647);
      com.poesys.db.dao.delete.IDeleteBatch<ChildRootClass> dao =
        factory.getDeleteBatch(new DeleteChildRootClass());
      dao.delete(dtos, BATCH_SIZE);
    }

    @Override
    protected void doNew(java.util.Collection<ChildRootClass> dtos) {
      com.poesys.db.dao.IDaoManager manager =
        com.poesys.db.dao.DaoManagerFactory.getManager(subsystem);

      com.poesys.db.dao.IDaoFactory<ChildRootClass> factory =
        manager.getFactory(ChildRootClass.class.getName(), subsystem, 2147483647);
      com.poesys.db.dao.insert.IInsertBatch<ChildRootClass> inserter =
        factory.getInsertBatch(new InsertChildRootClass());

      // Insert the object of the current class after enabling nested inserts,
      // which will allow connecting up linked objects to any of the inserted
      // classes.
      for (com.poesys.db.dto.IDbDto dto : dtos) {
        dto.setSuppressNestedInserts(false);
      }
      inserter.insert(dtos, dtos.size() / 2);
    }

    @Override
    protected java.util.Collection<ChildRootClass> getDtos() {
      return children;
    }

    @Override
    protected String getClassName() {
      return ChildRootClass.class.getName();
    }
  }

  /**
   * Add ChildRootClass object to accounts collection.
   *
   * @param object the ChildRootClass object
   */
  public void addChildren(ChildRootClass object) {
    if (children == null) {
      // Association not yet created, create it.
      children = new java.util.ArrayList<>();
    }
    children.add(object);
    // Add the primary key to the primary key array.
    if (childrenKeys != null) {
      childrenKeys.clear();
    } else {
      childrenKeys = new java.util.ArrayList<>();
    }
    childrenKeys.add(object.getPrimaryKey());
  }

  /**
   * Create a Parent with a primary key value, a data column value, and a list of setters for
   * setting the composite child collection field.
   *
   * @param key              the primary key for the parent
   * @param parentId         the primary key value
   * @param parentDataColumn the value for dataColumn
   */
  public CompositeParent(IPrimaryKey key, Integer parentId, String parentDataColumn) {
    super();

    // Create the setters and validators.
    this.insertSetters = new ArrayList<ISet>();
    insertSetters.add(new CompositeParent.InsertChildrenSetter());
    this.preSetters = new ArrayList<ISet>();
    // no presetters for this class
    this.postSetters = new ArrayList<ISet>();
    postSetters.add(new CompositeParent.PostProcessChildrenSetter());
    this.postProcessSetters = new ArrayList<ISet>();
    postProcessSetters.add(new CompositeParent.PostProcessChildrenSetter());
    this.querySetters = new ArrayList<ISet>();
    querySetters.add(new CompositeParent.QueryChildRootClassSetter());

    this.key = (NaturalPrimaryKey)key;
    this.parentId = parentId;
    this.parentDataColumn = parentDataColumn;
  }

  @Override
  public boolean equals(Object arg0) {
    if (!(arg0 instanceof CompositeParent)) {
      return false;
    }
    CompositeParent other = (CompositeParent)arg0;
    return other.getPrimaryKey().equals(key);
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  @Override
  public int compareTo(com.poesys.db.dto.IDbDto o) {
    CompositeParent other = (CompositeParent)o;
    // Sort on the key. Same semantics as equals and hashCode().
    return other.getPrimaryKey().compareTo(key);
  }

  private String parentDataColumn;

  /**
   * Get the data column
   *
   * @return the data column
   */
  public synchronized String getParentDataColumn() {
    return parentDataColumn;
  }

  /**
   * Set the value of the data column
   *
   * @param parentDataColumn the value to set
   */
  public synchronized void setParentDataColumn(String parentDataColumn) {
    this.parentDataColumn = parentDataColumn;
    setChanged();
  }

  /**
   * Get the children of the parent in an ordered list
   *
   * @return the children
   */
  public Collection<ChildRootClass> getChildren() {
    return children;
  }

  /**
   * Set the ordered list of children for the parent
   *
   * @param children the children to set
   */
  public void setChildren(Collection<ChildRootClass> children) {
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
  public void update(com.poesys.db.dto.ISubject subject, com.poesys.db.dao.DataEvent event) {

    // Clean up accounts.
    if (subject != null && subject instanceof ChildRootClass && event == com.poesys.db.dao.DataEvent.DELETE && children != null) {
      // Delete to-many accounts child from collection
      children.remove(subject);
      subject.detach(this, com.poesys.db.dao.DataEvent.MARKED_DELETED);
      subject.detach(this, com.poesys.db.dao.DataEvent.DELETE);
    }
  }

    /**
     * Create the inserters for the Entity and its superclasses.
     */

  private void createInserters() {
    com.poesys.db.dao.IDaoManager manager =
      com.poesys.db.dao.DaoManagerFactory.getManager(getSubsystem());
    final com.poesys.db.dao.IDaoFactory<CompositeParent> entityFactory =
      manager.getFactory(this.getClass().getName(), getSubsystem(), 2147483647);
    com.poesys.db.dao.insert.IInsertSql<CompositeParent> sql =
      new InsertCompositeParent();
    com.poesys.db.dao.insert.IInsert<CompositeParent> inserter = entityFactory.getInsert(sql, true);
    inserters.add(inserter);
  }
}

