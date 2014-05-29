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
package com.poesys.db.pk;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.poesys.db.DuplicateKeyNameException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.col.AbstractColumnValue;


/**
 * A concrete implementation of the IPrimaryKey interface that represents a
 * composite primary key. A composite key is a key composed of a parent key and
 * an additional primary key of some kind, often an integer (natural key)
 * representing a sort order. You use a composite key when you have a parent
 * class that owns the objects of the child class in a composite aggregation
 * relationship.
 * 
 * @author Robert J. Muller
 */
public class CompositePrimaryKey extends AbstractMultiValuedPrimaryKey {
  /**
   * Serial version UID for Serializable class
   */
  private static final long serialVersionUID = 7481773653672954407L;
  /** The parent primary key */
  private IPrimaryKey parentKey = null;

  /**
   * The child sub-key that identifies the child in combination with the parent
   * key; usually a natural key
   */
  private IPrimaryKey subKey = null;

  /** No parent key message */
  private static final String NO_PARENT = "com.poesys.db.pk.msg.no_parent";
  /** No sub key message */
  private static final String NO_SUB = "com.poesys.db.pk.msg.no_sub";
  /** Duplicate name in key message */
  private static final String DUP_NAME = "com.poesys.db.pk.msg.dup_col_name";

  /**
   * Create a CompositePrimaryKey object.
   * 
   * @param parentKey the key from the owning object
   * @param subKey the key that completes the primary key
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   * @throws InvalidParametersException when there is no parent or sub key
   * @throws DuplicateKeyNameException when there are multiple columns with the
   *           same name
   */
  public CompositePrimaryKey(IPrimaryKey parentKey,
                             IPrimaryKey subKey,
                             String className)
      throws InvalidParametersException, DuplicateKeyNameException {
    super(className);
    this.parentKey = parentKey;
    this.subKey = subKey;

    validateKey();

    buildColumnList();
  }

  /**
   * Create a CompositePrimaryKey object based on an input message primary key.
   * 
   * @param messageKey a message primary key
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   */
  public CompositePrimaryKey(com.poesys.ms.pk.CompositePrimaryKey messageKey,
                             String className) {
    super(className);
    parentKey = MessageKeyFactory.getKey(messageKey.getParentKey());
    subKey = MessageKeyFactory.getKey(messageKey.getSubKey());

    validateKey();

    buildColumnList();
  }

  /**
   * Validate the key (parent and sub-keys). Both keys must not be null, and
   * there must be no duplicate column names among all the key columns.
   */
  private void validateKey() {
    if (parentKey == null) {
      throw new InvalidParametersException(NO_PARENT);
    }
    if (subKey == null) {
      throw new InvalidParametersException(NO_SUB);
    }
    // Get the column name set to validate no duplication of columns.
    getColumnNames();
  }

  /**
   * Build the superclass column list from the parent and sub keys, ensuring
   * that the columns in the list are in alphabetical order.
   */
  private void buildColumnList() {
    List<AbstractColumnValue> list = new ArrayList<AbstractColumnValue>();
    for (AbstractColumnValue value : parentKey) {
      list.add(value);
    }
    for (AbstractColumnValue value : subKey) {
      list.add(value);
    }
    // The superclass setter ensures correct order and thread safety.
    setList(list);
  }

  @Override
  public boolean equals(IPrimaryKey key) {
    boolean ret = false;
    if (key instanceof CompositePrimaryKey) {
      // Compare the parent and sub-key values.
      ret =
        parentKey.equals(((CompositePrimaryKey)key).parentKey)
            && subKey.equals(((CompositePrimaryKey)key).subKey);
    }
    return ret;
  }

  /**
   * Get the parent portion of the primary key.
   * 
   * @return the parent primary key
   */
  public IPrimaryKey getParentKey() {
    return parentKey;
  }

  /**
   * Get the sub-key of the composite primary key.
   * 
   * @return the sub-key portion of the key
   */
  public IPrimaryKey getSubKey() {
    return subKey;
  }

  @Override
  public Set<String> getColumnNames() throws DuplicateKeyNameException {
    // Get the set from the parent key.
    Set<String> set1 = parentKey.getColumnNames();
    // Get the set from the sub-key.
    Set<String> set2 = subKey.getColumnNames();
    // Union the two sets.
    int setSize = set1.size() + set2.size();
    Set<String> union = new HashSet<String>(setSize);
    union.addAll(set1);
    union.addAll(set2);
    if (setSize != union.size()) {
      String duplicateName = null;
      if (set1 != null) {
        for (String name : set1) {
          if (set2.contains(name)) {
            duplicateName = name;
            break;
          }
        }
      }
      throw new DuplicateKeyNameException(DUP_NAME
                                          + " "
                                          + duplicateName
                                          + " in composite key list");
    }

    return union;
  }

  @Override
  public IPrimaryKey copy() {
    return new CompositePrimaryKey(parentKey.copy(), subKey.copy(), className);
  }

  @Override
  public com.poesys.ms.pk.IPrimaryKey getMessageObject() {
    // Generate keys for the parent and sub-keys.
    com.poesys.ms.pk.IPrimaryKey msgParentKey = parentKey.getMessageObject();
    com.poesys.ms.pk.IPrimaryKey msgSubKey = subKey.getMessageObject();
    return new com.poesys.ms.pk.CompositePrimaryKey(msgParentKey,
                                                    msgSubKey,
                                                    className);
  }
}
