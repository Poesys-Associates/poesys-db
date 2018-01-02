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

import com.poesys.db.pk.IPrimaryKey;
import org.apache.log4j.Logger;

/**
 * A test DTO class that serves as the root of a class generalization hierarchy to enable testing of
 * hierarchical operations for a root that is also the child of a composite parent class. The root
 * key is a CompositePrimaryKey comprising the parent id and the child id, both integer values, with
 * the nested keys in the composite key being natural keys to keep things simple.
 *
 * @author Robert J. Muller
 */
public class ChildRootClass extends AbstractTestDto {
  /** Default serial version UID for the Serializable DTO */
  private static final long serialVersionUID = 1L;

  /** Logger for this class */
  private static final Logger logger = Logger.getLogger(ChildRootClass.class);

  /** the deserializer used by the readObject method */
  private static final com.poesys.db.dto.Deserializer<ChildRootClass> deserializer =
    new com.poesys.db.dto.Deserializer<>();

  // Setter strategy nested classes for single-object associations

  /**
   * Nested class that manages the compositeParent association data
   */
  private class QueryCompositeParentSetter extends com.poesys.db.dto
    .AbstractObjectSetter<CompositeParent> {
    private static final long serialVersionUID = 1L;

    QueryCompositeParentSetter() {
      super("com.poesys.db.poesystest.mysql", 2147483647);
    }

    @Override
    protected String getClassName() {
      return CompositeParent.class.getName();
    }

    @Override
    protected IPrimaryKey getKey() {
      return parentKey;
    }

    @Override
    protected com.poesys.db.dao.query.IKeyQuerySql<CompositeParent> getSql() {
      return new QueryCompositeParent();
    }

    @Override
    protected void set(CompositeParent dto) {
      // No status change, this is just filling in the object data.
      compositeParent = dto;
    }

    @Override
    public boolean isSet() {
      // Object is set if the associated compositeParent is not null
      return compositeParent != null;
    }
  }

  /**
   * Post-process setter for post-processing nested object property compositeParent.
   */
  private class PostProcessCompositeParentSetter extends com.poesys.db.dto
    .AbstractPostProcessSetter {
    // Property compositeParent source: AddToOneAssociationRequiredObjectProperties
    private static final long serialVersionUID = 1L;

    /**
     * Create a PostProcessCompositeParentSetter object.
     */
    PostProcessCompositeParentSetter() {
      super("com.poesys.db.poesystest.mysql", 2147483647);
    }

    @Override
    protected String getClassName() {
      return CompositeParent.class.getName();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected java.util.Collection<com.poesys.db.dto.IDbDto> getDtos() {
      java.util.ArrayList<com.poesys.db.dto.IDbDto> array = new java.util.ArrayList<>(1);
      if (compositeParent != null) {
        array.add(compositeParent);
      }
      return array;
    }
  }

  /**
   * Insert setter for inserting nested object property compositeParent.
   */
  private class InsertCompositeParentSetter extends com.poesys.db.dto.AbstractInsertSetter {
    // Property compositeParent source: AddToOneAssociationRequiredObjectProperties
    private static final long serialVersionUID = 1L;

    /**
     * Create an InsertEntitySetter object.
     */
    InsertCompositeParentSetter() {
      super("com.poesys.db.poesystest.mysql", 2147483647);
    }

    @Override
    protected String getClassName() {
      return CompositeParent.class.getName();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected java.util.Collection<com.poesys.db.dto.IDbDto> getDtos() {
      java.util.ArrayList<com.poesys.db.dto.IDbDto> array = new java.util.ArrayList<>(1);
      array.add(compositeParent);
      return array;
    }

    @Override
    protected boolean createKey() {
      // Key type: NaturalKey
      return true;
    }
  }

  /**
   * Setter for processing added compositeParent, updated compositeParent, and deleted
   * compositeParent.
   */

  private class UpdateCompositeParentSetter extends com.poesys.db.dto
    .AbstractProcessNestedObject<CompositeParent> {
    private static final long serialVersionUID = 1L;

    /**
     * Create an UpdateEntitySetter object.
     */
    UpdateCompositeParentSetter() {
      super("com.poesys.db.poesystest.mysql", 2147483647);
    }

    @Override
    protected void doChanged(CompositeParent dto) {
      // compositeParent source: AddToOneAssociationRequiredObjectProperties
      // Immutable: false
      com.poesys.db.dao.IDaoManager manager =
        com.poesys.db.dao.DaoManagerFactory.getManager(subsystem);
      com.poesys.db.dao.IDaoFactory<CompositeParent> factory =
        manager.getFactory(CompositeParent.class.getName(), subsystem, 2147483647);
      com.poesys.db.dao.update.IUpdate<CompositeParent> updater =
        factory.getUpdate(new UpdateCompositeParent());

      updater.update(dto);
      // Complete the update by setting the DTO to EXISTING status.
      dto.setExisting();
    }

    @Override
    protected void doDeleted(CompositeParent dto) {
      com.poesys.db.dao.IDaoManager manager =
        com.poesys.db.dao.DaoManagerFactory.getManager(subsystem);
      com.poesys.db.dao.IDaoFactory<CompositeParent> factory =
        manager.getFactory(CompositeParent.class.getName(), subsystem, 2147483647);
      com.poesys.db.dao.delete.IDelete<CompositeParent> dao =
        factory.getDelete(new DeleteCompositeParent());
      dao.delete(dto);
    }

    @Override
    protected void doNew(CompositeParent dto) {
      com.poesys.db.dao.IDaoManager manager =
        com.poesys.db.dao.DaoManagerFactory.getManager(subsystem);
      com.poesys.db.dao.IDaoFactory<CompositeParent> factory =
        manager.getFactory(CompositeParent.class.getName(), subsystem, 2147483647);
      com.poesys.db.dao.insert.IInsert<CompositeParent> inserter =
        factory.getInsert(new InsertCompositeParent(), createKey());

      // Insert the superclass objects from the root down. Suppress nested
      // inserts for the superclasses, wait until the concrete class. Also set
      // pre-insert suppression off to have the root insert linked, to-one class
      // objects.
      dto.setSuppressNestedInserts(true);
      dto.setSuppressNestedPreInserts(false);

      // Suppress inserts in concrete class.
      dto.setSuppressNestedPreInserts(true);

      // Insert the object of the current class after enabling nested inserts,
      // which will allow connecting up linked objects to any of the inserted
      // classes.
      dto.setSuppressNestedInserts(false);
      inserter.insert(dto);
    }

    @Override
    protected CompositeParent getDto() {
      return compositeParent;
    }

    @Override
    protected String getClassName() {
      return CompositeParent.class.getName();
    }

    @Override
    protected boolean createKey() {
      return true;
    }
  }

  /**
   * Foreign key object used by QueryEntitySetter nested class to query object
   */
  private IPrimaryKey parentKey;

  /**
   * Set the foreign key parentKey. This has package access to enable the subsystem factory getData
   * method to call this method to set the key by creating it from the queried result set.
   *
   * @param parentKey the foreign key
   */
  void setParentKey(IPrimaryKey parentKey) {
    this.parentKey = parentKey;
  }

  /**
   * Create an empty ChildRootClass for use in building a new object. The concrete subclass must
   * call this constructor.
   */
  public ChildRootClass() {
    abstractClass = false;
    createInserters();

    // Setter arrays (create if null)
    if (querySetters == null) {
      querySetters = new java.util.ArrayList<>();
    }
    if (insertSetters == null) {
      insertSetters = new java.util.ArrayList<>();
    }
    if (preSetters == null) {
      preSetters = new java.util.ArrayList<>();
    }
    if (postSetters == null) {
      postSetters = new java.util.ArrayList<>();
    }
    if (postProcessSetters == null) {
      postProcessSetters = new java.util.ArrayList<>();
    }
    if (readObjectSetters == null) {
      readObjectSetters = new java.util.ArrayList<>();
    }

    // Add the setters for the compositeParent property.
    querySetters.add(new QueryCompositeParentSetter());
    preSetters.add(new InsertCompositeParentSetter());
    postSetters.add(new UpdateCompositeParentSetter());
    postProcessSetters.add(new PostProcessCompositeParentSetter());
  }

  /**
   * Create a ChildRootClass. The concrete subclass must call this constructor.
   *
   * @param key            the primary key of the ChildRootClass
   * @param parentId       composite super-key attribute that uniquely identifies child combined
   *                       with child sub-key and any other parent super-keys
   * @param childId        the account name; unique within the compositeParent
   * @param rootDataColumn text rootDataColumn of the nature of the account
   */
  public ChildRootClass(IPrimaryKey key, Integer parentId, Integer childId, String rootDataColumn) {
    this.key = key;

    this.parentId = parentId;

    if (this.parentId == null) {
      throw new com.poesys.db.InvalidParametersException(
        "parentId is required for " + key.getValueList());
    }

    this.childId = childId;

    if (this.childId == null) {
      throw new com.poesys.db.InvalidParametersException(
        "childId is required for " + key.getValueList());
    }

    this.rootDataColumn = rootDataColumn;

    if (this.rootDataColumn == null) {
      throw new com.poesys.db.InvalidParametersException(
        "rootDataColumn is required for " + key.getValueList());
    }

    // Setter arrays (create if null)
    if (querySetters == null) {
      querySetters = new java.util.ArrayList<>();
    }
    if (insertQuerySetters == null) {
      insertQuerySetters = new java.util.ArrayList<>();
    }
    if (insertSetters == null) {
      insertSetters = new java.util.ArrayList<>();
    }
    if (preSetters == null) {
      preSetters = new java.util.ArrayList<>();
    }
    if (postSetters == null) {
      postSetters = new java.util.ArrayList<>();
    }
    if (postProcessSetters == null) {
      postProcessSetters = new java.util.ArrayList<>();
    }
    if (readObjectSetters == null) {
      readObjectSetters = new java.util.ArrayList<>();
    }

    // Add the setters for the compositeParent property.
    querySetters.add(new QueryCompositeParentSetter());
    // Set the object property primary key with a factory method.
    parentKey = TestFactory.getCompositeParentPrimaryKey(parentId);
    insertSetters.add(new InsertCompositeParentSetter());
    preSetters.add(new InsertCompositeParentSetter());
    postSetters.add(new UpdateCompositeParentSetter());
    postProcessSetters.add(new PostProcessCompositeParentSetter());

    // Add a setter to instantiate the required compositeParent object before insert.
    insertQuerySetters.add(new QueryCompositeParentSetter());
    abstractClass = false;
    // Superclass should not call createInserters() in constructor, leaf class calls.
    // createInserters();
  }

  /**
   * Read an object from an input stream, de-serializing it. Each generated class must have this
   * private method, which the deserialize method calls through Java reflection on the specific
   * class. The class calls a shared code method to run the readObjectSetters.
   *
   * @param in the object input stream
   * @throws ClassNotFoundException when a nested object class can't be found
   * @throws java.io.IOException    when there is an IO problem reading the stream
   */
  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException,
    ClassNotFoundException {
    logger.debug(
      "Deserializing object of class " + this.getClass().getName() + " with readObject in " +
        "ChildRootClass");
    // Do the read-object deserialization.
    deserializer.doReadObject(in, this);
  }

  @Override
  public boolean equals(Object arg0) {
    if (!(arg0 instanceof ChildRootClass)) {
      return false;
    }
    ChildRootClass other = (ChildRootClass)arg0;
    return other.getPrimaryKey().equals(key);
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  @Override
  public int compareTo(com.poesys.db.dto.IDbDto o) {
    ChildRootClass other = (ChildRootClass)o;
    // Sort on the key. Same semantics as equals and hashCode().
    return other.getPrimaryKey().compareTo(key);
  }

  @Override
  public String getSubsystem() {
    return "com.poesys.accounting.db.account";
  }

  @Override
  public void markChildrenDeleted() {
  }

  @Override
  public IPrimaryKey getPrimaryKey() {
    return key;
  }

  /**
   * Nested property parentId
   *
   * <p> Composite super-key attribute that uniquely identifies child combined with child sub-key
   * and any other parent super-keys </p>
   *
   * Added by AddNaturalKeyProperties + AddParentKeyAttributes Class is read/write: true Class is
   * immutable: false Property is read/write: false Property is lazy: false
   */
  private Integer parentId;

  /**
   * Get an object of java.lang.String.
   *
   * Source: AddNaturalKeyProperties + AddParentKeyAttributes
   *
   * @return a java.lang.String
   */

  public Integer getParentId() {
    return parentId;
  }

  /**
   * Clear the parentId data member; override in proxy if lazily loaded, otherwise this method does
   * nothing.
   */
  public void clearParentId() {
    // Override in proxy if lazily loaded; otherwise does nothing
  }

  /**
   * <p> Set the parentId. </p> <ul> <li>Read/Write DTO: true</li> <li>Immutable DTO: false</li>
   * <li>Read/Write property: false</li> <li>Immutable property: false</li> <li>Lazy property: false
   * (if true, proxy calls this method)</li> </ul> <p> Composite super-key attribute that uniquely
   * identifies child combined with child sub-key and any other parent super-keys </p>
   *
   * @param parentId the value with which to set the property
   */
  void setParentId(Integer parentId) throws com.poesys.db.InvalidParametersException {
    if (parentId == null) {
      throw new com.poesys.db.InvalidParametersException("parentId is required");
    }

    this.parentId = parentId;
    setChanged();
  }

  /**
   * Nested property childId
   *
   * <p> the child id; unique within the compositeParent </p>
   */
  private Integer childId;

  /**
   * Get the child id.
   *
   * @return a child id
   */

  public Integer getChildId() {
    return childId;
  }

  /**
   * Clear the childId data member; override in proxy if lazily loaded, otherwise this method does
   * nothing.
   */
  public void clearChildId() {
    // Override in proxy if lazily loaded; otherwise does nothing
  }

  /**
   * <p> Set the childId. </p>
   *
   * @param childId the value with which to set the property
   */
  void setChildId(Integer childId) throws com.poesys.db.InvalidParametersException {
    if (childId == null) {
      throw new com.poesys.db.InvalidParametersException("childId is required");
    }

    this.childId = childId;
    setChanged();
  }

  /**
   * Nested property rootDataColumn
   *
   * <p> text rootDataColumn </p>
   */
  private java.lang.String rootDataColumn;

  /**
   * Get an object of java.lang.String.
   *
   * Source: AddLocalAttributeProperties
   *
   * @return a java.lang.String
   */

  public java.lang.String getRootDataColumn() {
    return rootDataColumn;
  }

  /**
   * <p> Set the rootDataColumn. </p>
   *
   * @param rootDataColumn the value with which to set the property
   */
  public void setRootDataColumn(java.lang.String rootDataColumn) throws com.poesys.db
    .InvalidParametersException {
    if (rootDataColumn == null) {
      throw new com.poesys.db.InvalidParametersException("rootDataColumn is required");
    }

    this.rootDataColumn = rootDataColumn;
    setChanged();
  }

  /**
   * Nested property compositeParent
   */
  private CompositeParent compositeParent;

  /**
   * Get an object of com.poesys.accounting.db.account.IEntity.
   *
   * Source: AddToOneAssociationRequiredObjectProperties
   *
   * @return a com.poesys.accounting.db.account.IEntity
   */

  public CompositeParent getCompositeParent() {
    return compositeParent;
  }

  /**
   * Clear the compositeParent data member; override in proxy if lazily loaded, otherwise this
   * method does nothing.
   */
  public void clearCompositeParent() {
    // Override in proxy if lazily loaded; otherwise does nothing
  }

  /**
   * <p> Set the compositeParent. </p> <ul> <li>Read/Write DTO: true</li> <li>Immutable DTO:
   * false</li> <li>Read/Write property: true</li> <li>Immutable property: false</li> <li>Lazy
   * property: false (if true, proxy calls this method)</li> </ul>
   *
   * @param compositeParent the value with which to set the property
   */
  public void setCompositeParent(CompositeParent compositeParent) throws com.poesys.db
    .InvalidParametersException {
    if (compositeParent == null) {
      throw new com.poesys.db.InvalidParametersException("compositeParent is required");
    }

    this.compositeParent = compositeParent;
    setChanged();
  }

  @Override
  public void update(com.poesys.db.dto.ISubject subject, com.poesys.db.dao.DataEvent event) {
  }

  /**
   * Create the inserters for the ChildRootClass and its superclasses.
   */
  protected void createInserters() {
    com.poesys.db.dao.IDaoManager manager =
      com.poesys.db.dao.DaoManagerFactory.getManager(getSubsystem());
    final com.poesys.db.dao.IDaoFactory<ChildRootClass> factory =
      manager.getFactory(this.getClass().getName(), getSubsystem(), 2147483647);
    com.poesys.db.dao.insert.IInsertSql<ChildRootClass> sql = new InsertChildRootClass();
    com.poesys.db.dao.insert.IInsert<ChildRootClass> inserter = factory.getInsert(sql, true);
    inserters.add(inserter);
  }
}
