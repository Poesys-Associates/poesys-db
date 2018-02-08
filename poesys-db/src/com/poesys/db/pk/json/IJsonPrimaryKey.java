package com.poesys.db.pk.json;/* Copyright (c) 2018 Poesys Associates. All rights reserved. */

import com.poesys.db.col.json.JsonColumnValue;
import com.poesys.db.pk.IPrimaryKey;

import java.math.BigInteger;
import java.util.List;

public interface IJsonPrimaryKey {
  /**
   * Get the fully qualified Java class name for the IPrimaryKey implementation class.
   *
   * @return a fully qualified Java class name
   */
  String getKeyType();

  /**
   * Get the fully qualified Java class name for the IDbDto implementation class. This is the
   * class of the DTO object for which the key is a primary key. This name also serves as the
   * unique cache name for objects identified by the key.
   *
   * @return a fully qualified Java class name
   */
  String getClassName();

  /**
   * Get the column value list for a list-valued key (Guid, Identity, or Natural keys).
   *
   * @return a JSON column value list
   */
  List<JsonColumnValue> getColumnValueList();

  /**
   * Get the integer sequence value for a Sequence key.
   *
   * @return a BigInteger value
   */
  BigInteger getValue();

  /**
   * Get the key list for an Association key.
   *
   * @return a list of JSON primary key objects
   */
  List<JsonPrimaryKey> getKeyList();

  /**
   * Get the parent key for a Composite key.
   *
   * @return a JSON primary key
   */
  JsonPrimaryKey getParentKey();

  /**
   * Get the child key for a Composite key.
   *
   * @return a JSON primary key
   */
  JsonPrimaryKey getChildKey();

  /**
   * Get a primary key based on the content of the object. The primary key should be an object of
   * the class specified by getKeyType().
   *
   * @return an IPrimaryKey object
   */
  IPrimaryKey getPrimaryKey();
}
