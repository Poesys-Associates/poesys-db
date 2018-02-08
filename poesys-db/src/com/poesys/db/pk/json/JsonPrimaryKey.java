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
}
