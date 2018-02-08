/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db.pk.json;

import com.poesys.db.InvalidParametersException;
import com.poesys.db.Message;
import com.poesys.db.col.IColumnValue;
import com.poesys.db.col.json.IJsonColumnValue;
import com.poesys.db.col.json.JsonColumnValue;
import com.poesys.db.pk.NaturalPrimaryKey;

import java.util.ArrayList;
import java.util.List;

/**
 * A factory method pattern class that generates objects of a specific concrete implementation of
 * IJsonPrimaryKey based on a generic JsonPrimaryKey object
 */
public class JsonPrimaryKeyFactory {
  // messages
  private static final String INVALID_TYPE_ERROR = "com.poesys.db.pk.json.msg.invalid_pk_class";

  /**
   * Get the appropriate concrete JSON primary key type for a specified generic JSON primary key.
   * This factory method translates a generic key generated from JSON data into a specific
   * concrete key class which you can then use to produce an IPrimaryKey object.
   *
   * @param jsonKey the generic JsonPrimaryKey object
   * @return the concrete subclass IJsonPrimaryKey object
   */
  public static IJsonPrimaryKey getJsonPrimaryKey(JsonPrimaryKey jsonKey) {
    IJsonPrimaryKey key = null;

    switch (jsonKey.getKeyType()) {
      case "com.poesys.db.pk.NaturalPrimaryKey":
        key = new NaturalJsonPrimaryKey(jsonKey.getClassName(), jsonKey.getColumnValueList());
        break;
      case "com.poesys.db.pk.AssociationPrimaryKey":
        key = new AssociationJsonPrimaryKey(jsonKey.getClassName(), jsonKey.getKeyList());
        break;
      case "com.poesys.db.pk.CompositePrimaryKey":
        key = new CompositeJsonPrimaryKey(jsonKey.getClassName(), jsonKey.getParentKey(), jsonKey.getChildKey());
        break;
      case "com.poesys.db.pk.GuidPrimaryKey":
        key = new GuidJsonPrimaryKey(jsonKey.getClassName(), jsonKey.getColumnValueList());
        break;
      case "com.poesys.db.pk.IdentityPrimaryKey":
        key = new IdentityJsonPrimaryKey(jsonKey.getClassName(), jsonKey.getColumnValueList());
        break;
      case "com.poesys.db.pk.SequencePrimaryKey":
        key = new SequenceJsonPrimaryKey(jsonKey.getClassName(), jsonKey.getColumnValueList());
        break;
      default:
        Object[] args = {jsonKey.getKeyType()};
        String message = Message.getMessage(INVALID_TYPE_ERROR, args);
        throw new InvalidParametersException(message);
    }
    return key;
  }
}
