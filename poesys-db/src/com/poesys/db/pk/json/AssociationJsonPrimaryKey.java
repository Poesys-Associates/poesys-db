/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db.pk.json;

import com.poesys.db.col.IColumnValue;
import com.poesys.db.col.json.JsonColumnValue;
import com.poesys.db.col.json.JsonColumnValueFactory;
import com.poesys.db.pk.AssociationPrimaryKey;
import com.poesys.db.pk.IPrimaryKey;
import com.poesys.db.pk.NaturalPrimaryKey;

import java.util.ArrayList;
import java.util.List;

/**
 * A JSON DTO for association primary keys
 */
public class AssociationJsonPrimaryKey extends JsonPrimaryKey {
  /**
   * Create an association JSON primary key DTO with a specified DTO class name and a list of JSON
   * primary keys.
   *
   * @param className       the fully qualified name of the DTO class for the objects that the
   *                        primary key identifies
   * @param primaryKeyList a list of the JSON DTOs for the natural key column values
   */
  public AssociationJsonPrimaryKey(String className, List<JsonPrimaryKey> primaryKeyList) {
    super(className, primaryKeyList);
  }

  @Override
  public IPrimaryKey getPrimaryKey() {
    // Create a list of IColumnValue objects from the list of JSON column values.
    List<IPrimaryKey> list = new ArrayList<>();
    for (JsonPrimaryKey jsonKey : getKeyList()) {
      list.add(JsonPrimaryKeyFactory.getJsonPrimaryKey(jsonKey).getPrimaryKey());
    }
    // Create the association primary key.
    return new AssociationPrimaryKey(list, getClassName());
  }
}
