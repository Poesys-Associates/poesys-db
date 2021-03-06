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

import com.poesys.db.DuplicateKeyNameException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.col.IColumnValue;
import com.poesys.db.pk.json.JsonPrimaryKey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A concrete implementation of the IPrimaryKey interface that represents an
 * association primary key. An association key is a key composed of from two to
 * any number of primary keys of associated objects, linking those objects
 * together. The primary keys must have different column names. The
 * AssociationPrimaryKey is a subclass of AbstractMultiValuedPrimaryKey because
 * you construct it with multiple primary keys, not column values.
 * <p>
 * <em>
 * Note: The association primary key does not enforce any internal constraints
 * on the multiplicity of the mapped rows (1-M-M constraints, for example). The
 * key represents only a single link, not the complete mapping between the
 * objects.
 * </em>
 * </p>
 *
 * @author Robert J. Muller
 */
public class AssociationPrimaryKey extends AbstractMultiValuedPrimaryKey {
  /** Serial version UID for Serializable class */
  private static final long serialVersionUID = -1L;
  /** The list of keys */
  private List<IPrimaryKey> keyList;
  /** message for invalid initial list with less than two keys */
  private static final String TOO_FEW_KEYS = "com.poesys.db.pk.msg.too_few_keys";
  /** message for invalid initial list with duplicate names for columns */
  private static final String DUP_NAME = "com.poesys.db.pk.msg.dup_col_name";

  /**
   * Create an AssociationPrimaryKey object. An association key must have at
   * least two internal primary keys, and all the keys must have different
   * names.
   *
   * @param list      a list of at least two primary key objects
   * @param className the name of the IDbDto class of the object that the
   *                  primary key identifies
   * @throws InvalidParametersException when the key has fewer than two nested
   *                                    primary keys
   * @throws DuplicateKeyNameException  when the key has duplicate column names
   *                                    in the nested primary keys
   */
  public AssociationPrimaryKey(List<IPrimaryKey> list, String className) throws
    DuplicateKeyNameException, InvalidParametersException {
    super(className);
    keyList = list;
    validateKeyList();

    buildColumnList(list);
  }

  /**
   * Build the multi-valued key column list.
   *
   * @param list the list of primary keys
   */
  private void buildColumnList(List<IPrimaryKey> list) {
    // Combine the key columns into a single list.
    List<IColumnValue> cols = new ArrayList<>();
    for (IPrimaryKey key : list) {
      for (IColumnValue col : key) {
        cols.add(col);
      }
    }
    // Ensure correct order and thread safety.
    setList(cols);
  }

  /**
   * Create a AssociationPrimaryKey object based on an input message primary
   * key.
   *
   * @param messageKey a message primary key
   * @param className  the name of the IDbDto class of the object that the
   *                   primary key identifies
   */
  public AssociationPrimaryKey(com.poesys.ms.pk.AssociationPrimaryKey messageKey, String
    className) {
    super(className);
    keyList = new ArrayList<>();
    if (messageKey != null) {
      for (com.poesys.ms.pk.IPrimaryKey key : messageKey.getKeys()) {
        keyList.add(MessageKeyFactory.getKey(key));
      }
      validateKeyList();
      buildColumnList(keyList);
    } else {
      throw new RuntimeException("Null message key, cannot create association primary key");
    }
  }

  /**
   * Validate a new primary key list; the list must contain at least 2 primary
   * keys and must have no duplicate names in the columns.
   */
  private void validateKeyList() {
    if (keyList == null || keyList.size() < 2) {
      throw new InvalidParametersException(TOO_FEW_KEYS);
    }
    getColumnNames();
  }

  @Override
  public boolean equals(IPrimaryKey key) {
    boolean ret = false;
    if (key instanceof AssociationPrimaryKey &&
        ((AssociationPrimaryKey)key).keyList.size() == keyList.size()) {
      // Both association keys and the lists are the same size, compare keys.
      int index = 0;
      for (IPrimaryKey k : keyList) {
        ret = k.equals(((AssociationPrimaryKey)key).keyList.get(index));
        index++;
        // Stop if any key does not compare as equal.
        if (!ret) {
          break;
        }
      }
    }
    return ret;
  }

  /**
   * Get a shallow copy of the list of primary keys that comprise the
   * association key.
   *
   * @return a List of IPrimaryKey objects
   */
  public List<IPrimaryKey> getKeyListCopy() {
    return new ArrayList<>(keyList);
  }

  @Override
  public Set<String> getColumnNames() throws DuplicateKeyNameException {
    Set<String> union = new HashSet<>();
    int setSize = 0;
    String dupName = null;
    for (IPrimaryKey key : keyList) {
      for (IColumnValue value : key) {
        if (union.contains(value.getName())) {
          dupName = value.getName();
        }
      }
      setSize += key.getColumnNames().size();
      union.addAll(key.getColumnNames());
    }

    if (setSize != union.size()) {
      List<String> parameters = new ArrayList<>();
      parameters.add(dupName);
      DuplicateKeyNameException e = new DuplicateKeyNameException(DUP_NAME);
      e.setParameters(parameters);
      throw e;
    }
    return union;
  }

  @Override
  public IPrimaryKey copy() {
    List<IPrimaryKey> newList = new ArrayList<>();
    for (IPrimaryKey key : keyList) {
      IPrimaryKey newKey = key.copy();
      newList.add(newKey);
    }
    return new AssociationPrimaryKey(newList, className);
  }

  @Override
  public com.poesys.ms.pk.IPrimaryKey getMessageObject() {
    List<com.poesys.ms.pk.IPrimaryKey> msgKeys = new ArrayList<>(keyList.size());
    for (IPrimaryKey key : keyList) {
      msgKeys.add(key.getMessageObject());
    }
    return new com.poesys.ms.pk.AssociationPrimaryKey(msgKeys, className);
  }

  @Override
  public JsonPrimaryKey getJsonPrimaryKey() {
    // Create a list of JSON primary keys from the association key list.
    List<JsonPrimaryKey> jsonKeys = new ArrayList<>(2);
    for (IPrimaryKey key : keyList) {
      jsonKeys.add(key.getJsonPrimaryKey());
    }
    // Create the JSON primary key.
    return new JsonPrimaryKey(className, jsonKeys);
  }
}
