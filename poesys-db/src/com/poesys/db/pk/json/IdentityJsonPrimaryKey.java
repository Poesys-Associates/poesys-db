/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db.pk.json;

import com.poesys.db.col.IColumnValue;
import com.poesys.db.col.json.JsonColumnValue;
import com.poesys.db.col.json.JsonColumnValueFactory;
import com.poesys.db.pk.IPrimaryKey;
import com.poesys.db.pk.IdentityPrimaryKey;
import com.poesys.db.pk.NaturalPrimaryKey;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * A JSON DTO for identity primary keys
 */
public class IdentityJsonPrimaryKey extends JsonPrimaryKey {
  /**
   * Create an identity JSON primary key DTO with a specified DTO class name and list of JSON
   * column values containing the single column with its identity value.
   *
   * @param className       the fully qualified name of the DTO class for the objects that the
   *                        primary key identifies
   * @param columnValueList a list of the JSON DTOs containing a single DTO for the identity number
   */
  public IdentityJsonPrimaryKey(String className, List<JsonColumnValue> columnValueList) {
    super(IdentityPrimaryKey.class.getName(), className, columnValueList);
  }

  @Override
  public IPrimaryKey getPrimaryKey() {
    // Create a list of IColumnValue objects from the list of JSON column values.
    JsonColumnValue value = getColumnValueList().get(0);

    // Create the identity primary key.
    return new IdentityPrimaryKey(value.getName(), new BigInteger(value.getValue()), getClassName());
  }
}
