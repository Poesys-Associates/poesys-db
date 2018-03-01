/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db.pk.json;

import com.poesys.db.Message;
import com.poesys.db.col.json.JsonColumnValue;
import com.poesys.db.pk.AssociationPrimaryKey;
import com.poesys.db.pk.CompositePrimaryKey;
import com.poesys.db.pk.IPrimaryKey;
import com.poesys.db.pk.SequencePrimaryKey;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * A serialized version of a Poesys primary key value; collapses all the different data
 * structures for the various primary keys into a single class to allow serialization and
 * deserialization from JSON strings; each concrete subclass of this class implements a factory
 * method that generates the appropriate type of IPrimaryKey object, and each IPrimaryKey
 * implementation contains a factory method that generates an IJsonPrimaryKey object
 */
public class JsonPrimaryKey implements IJsonPrimaryKey {
  /** the fully qualified Java class name for the kind of primary key this object represents */
  private String keyType;
  /** the name of the IDbDto class of the object that the primary key identifies */
  private String className;
  /** a list of column values that comprise the key */
  private List<JsonColumnValue> columnValueList;
  /** a single-valued sequence number key value */
  private BigInteger value;
  /** a list of primary keys that form an association key */
  private List<JsonPrimaryKey> keyList;
  /** a parent primary key for a composite primary key */
  private JsonPrimaryKey parentKey;
  /** a child sub-key for a composite primary key */
  private JsonPrimaryKey childKey;

  // messages
  private static final String INSTANTIATION_ERROR =
    "com.poesys.db.pk.json.msg.json_primary_key_base_instance";

  /**
   * Create an empty JSON primary key object. This constructor enables GSON to build the object
   * using reflection.
   */
  public JsonPrimaryKey() {
  }

  /**
   * Create a JSON primary key for one of the three column-list key types GuidPrimaryKey,
   * IdentityPrimaryKey, or NaturalPrimaryKey.
   *
   * @param keyType         the fully qualified Java class name for the kind of primary key this
   *                        object represents
   * @param className       the name of the IDbDto class of the object that the primary key
   *                        identifies
   * @param columnValueList a list of column values that comprise the key
   */
  public JsonPrimaryKey(String keyType, String className, List<JsonColumnValue> columnValueList) {
    this.keyType = keyType;
    this.className = className;
    this.columnValueList = columnValueList;
  }

  /**
   * Create a JSON primary key for a sequence key with a BigInteger value.
   *
   * @param className the name of the IDbDto class of the object that the primary key
   *                  identifies
   * @param value     the sequence value
   */
  public JsonPrimaryKey(String className, BigInteger value) {
    this.keyType = SequencePrimaryKey.class.getName();
    this.className = className;
    this.value = value;
  }

  /**
   * Create a JSON primary key for an association key
   *
   * @param className the name of the IDbDto class of the object that the primary key
   *                  identifies
   * @param keyList   a list of primary keys that form the association key
   */
  public JsonPrimaryKey(String className, List<JsonPrimaryKey> keyList) {
    this.keyType = AssociationPrimaryKey.class.getName();
    this.className = className;
    this.keyList = keyList;
  }

  /**
   * Create a JSON primary key for a composite key.
   *
   * @param className the name of the IDbDto class of the object that the primary key
   *                  identifies
   * @param parentKey a parent primary key
   * @param childKey  a child primary key
   */
  public JsonPrimaryKey(String className, JsonPrimaryKey parentKey, JsonPrimaryKey childKey) {
    this.keyType = CompositePrimaryKey.class.getName();
    this.className = className;
    this.parentKey = parentKey;
    this.childKey = childKey;
  }

  @Override
  public String getKeyType() {
    return keyType;
  }

  @Override
  public String getClassName() {
    return className;
  }

  @Override
  public List<JsonColumnValue> getColumnValueList() {
    return columnValueList;
  }

  @Override
  public BigInteger getValue() {
    return value;
  }

  @Override
  public List<JsonPrimaryKey> getKeyList() {
    return keyList;
  }

  @Override
  public JsonPrimaryKey getParentKey() {
    return parentKey;
  }

  @Override
  public JsonPrimaryKey getChildKey() {
    return childKey;
  }

  @Override
  public IPrimaryKey getPrimaryKey() {
    throw new RuntimeException(Message.getMessage(INSTANTIATION_ERROR, null));
  }

  // Code special equals() logic to ensure proper null handling.
  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof JsonPrimaryKey)) {
      return false;
    }

    JsonPrimaryKey that = (JsonPrimaryKey)object;

    // First compare key type and class name, which are required fields. Even if an object is an
    // instance of the parent concrete class JsonPrimaryKey, it will have a keyType that
    // identifies the targeted concrete subclass, and this must be the same for both keys.
    boolean isEqual = java.util.Objects.equals(keyType, that.keyType) &&
                      java.util.Objects.equals(className, that.className);

    if (isEqual) {

      // Compute the various concrete field combinations. This enables comparing keys of different
      // concrete types to the concrete supertype, which is what comes out of GSON.

      if (columnValueList != null) {
        // Comparing natural keys, GUID keys, or identity keys; compare the value list.
        isEqual = isEqualList(that.getColumnValueList());
      } else if (value != null) {
        // Comparing sequence keys
        isEqual = java.util.Objects.equals(value, that.value);
      } else if (keyList != null) {
        // Comparing association keys
        isEqual = isEqualKeyList(that.keyList);
      } else {
        // Comparing composite keys; if any of the four keys is null, sets flag to false
        boolean parentEqual =
          parentKey != null && that.parentKey != null && parentKey.equals(that.parentKey);
        boolean childEqual =
          childKey != null && that.childKey != null && childKey.equals(that.childKey);
        isEqual = parentEqual && childEqual;
      }
    }

    return isEqual;
  }

  @Override
  public int hashCode() {
    int result = keyType.hashCode();
    result = 31 * result + className.hashCode();
    result = 31 * result + (columnValueList != null ? columnValueList.hashCode() : 0);
    result = 31 * result + (value != null ? value.hashCode() : 0);
    result = 31 * result + (keyList != null ? keyList.hashCode() : 0);
    result = 31 * result + (parentKey != null ? parentKey.hashCode() : 0);
    result = 31 * result + (childKey != null ? childKey.hashCode() : 0);
    return result;
  }

  /**
   * Is the column-value list of this object equal to another column value list?
   *
   * @param otherColumnList the other list
   * @return true if the lists contain equal objects in the same order
   */
  private boolean isEqualList(List<JsonColumnValue> otherColumnList) {
    boolean equal = false;
    if (columnValueList.size() == otherColumnList.size()) {
      equal = true;
      for (int i = 0; i < columnValueList.size(); i++) {
        equal = columnValueList.get(i).equals(otherColumnList.get(i));
        if (!equal) {
          break;
        }
      }
    }
    return equal;
  }

  /**
   * Is the key list of this object equal to another key list?
   *
   * @param otherKeyList the other list
   * @return true if the lists contain equal keys in the same order
   */
  private boolean isEqualKeyList(List<JsonPrimaryKey> otherKeyList) {
    boolean equal = false;
    if (keyList.size() == otherKeyList.size()) {
      equal = true;
      for (int i = 0; i < keyList.size(); i++) {
        equal = keyList.get(i).equals(otherKeyList.get(i));
        if (!equal) {
          break;
        }
      }
    }
    return equal;
  }
}
