/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db.pk.json;

import com.poesys.db.col.json.JsonColumnValue;
import com.poesys.db.pk.IPrimaryKey;
import com.poesys.db.pk.SequencePrimaryKey;

import java.math.BigInteger;
import java.util.List;

/**
 * A JSON DTO for sequence primary keys
 */
public class SequenceJsonPrimaryKey extends JsonPrimaryKey {
  /**
   * Create a sequence JSON primary key DTO with a specified DTO class name and list of JSON
   * column values containing the single column with its sequence value.
   *
   * @param className       the fully qualified name of the DTO class for the objects that the
   *                        primary key identifies
   * @param columnValueList a list of the JSON DTOs containing a single DTO for the sequence number
   */
  public SequenceJsonPrimaryKey(String className, List<JsonColumnValue> columnValueList) {
    super(SequencePrimaryKey.class.getName(), className, columnValueList);
  }

  @Override
  public IPrimaryKey getPrimaryKey() {
    // Create a list of IColumnValue objects from the list of JSON column values.
    JsonColumnValue value = getColumnValueList().get(0);

    // Create the sequence primary key.
    return new SequencePrimaryKey(value.getName(), new BigInteger(value.getValue()),
                                  getClassName());
  }
}
