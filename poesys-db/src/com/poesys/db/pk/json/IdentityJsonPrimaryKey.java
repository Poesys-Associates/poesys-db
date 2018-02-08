/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db.pk.json;

import com.poesys.db.col.IColumnValue;
import com.poesys.db.col.json.JsonColumnValue;
import com.poesys.db.col.json.JsonColumnValueFactory;
import com.poesys.db.pk.IPrimaryKey;
import com.poesys.db.pk.NaturalPrimaryKey;

import java.util.ArrayList;
import java.util.List;

/**
 * A JSON DTO for natural primary keys
 */
public class NaturalJsonPrimaryKey extends JsonPrimaryKey {
  /**
   * Create a natural JSON primary key DTO with a specified DTO class name and list of JSON
   * column values.
   *
   * @param className       the fully qualified name of the DTO class for the objects that the
   *                        primary key identifies
   * @param columnValueList a list of the JSON DTOs for the natural key column values
   */
  public NaturalJsonPrimaryKey(String className, List<JsonColumnValue> columnValueList) {
    super(NaturalPrimaryKey.class.getName(), className, columnValueList);
  }

  @Override
  public IPrimaryKey getPrimaryKey() {
    // Create a list of IColumnValue objects from the list of JSON column values.
    List<IColumnValue> list = new ArrayList<>();
    for (JsonColumnValue value : getColumnValueList()) {
      // Convert the generic column value into a concrete subclass column value, then get the
      // IColumnValue for that object and put it into the primary key list of column values.
      list.add(JsonColumnValueFactory.getJsonColumnValue(value).getColumnValue());
    }
    // Create the natural primary key.
    return new NaturalPrimaryKey(list, getClassName());
  }
}
