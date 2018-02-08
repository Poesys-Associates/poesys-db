/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db.pk.json;

import com.poesys.db.col.IColumnValue;
import com.poesys.db.col.json.JsonColumnValue;
import com.poesys.db.col.json.JsonColumnValueFactory;
import com.poesys.db.pk.GuidPrimaryKey;
import com.poesys.db.pk.IPrimaryKey;
import com.poesys.db.pk.NaturalPrimaryKey;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A JSON DTO for GUID primary keys
 */
public class GuidJsonPrimaryKey extends JsonPrimaryKey {
  /**
   * Create a GUID JSON primary key DTO with a specified DTO class name and a list of JSON column
   * values containing a single GUID string value.
   *
   * @param className       the fully qualified name of the DTO class for the objects that the
   *                        primary key identifies
   * @param columnValueList a list containing the JSON DTO for the GUID key column value
   */
  public GuidJsonPrimaryKey(String className, List<JsonColumnValue> columnValueList) {
    super(GuidPrimaryKey.class.getName(), className, columnValueList);
  }

  @Override
  public IPrimaryKey getPrimaryKey() {
    // Get the GUID and column name.
    JsonColumnValue value = getColumnValueList().get(0);
    UUID uuid = UUID.fromString(value.getValue());
    String columnName = value.getName();

    // Create the natural primary key.
    return new GuidPrimaryKey(columnName, uuid, getClassName());
  }
}
