package com.poesys.db.pk.json;/* Copyright (c) 2018 Poesys Associates. All rights reserved. */

import com.poesys.db.col.json.JsonColumnValue;
import com.poesys.db.col.json.StringJsonColumnValue;
import com.poesys.db.pk.AssociationPrimaryKey;
import com.poesys.db.pk.CompositePrimaryKey;
import com.poesys.db.pk.IPrimaryKey;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CompositeJsonPrimaryKeyTest {
  private static final String CLASS = "com.poesys.db.ClassName";

  private static final String COL_NAME_1 = "col1";
  private static final String COL_VALUE_1 = "string value 1";
  private static final String COL_NAME_2 = "col2";
  private static final String COL_VALUE_2 = "string value 2";
  private static final String TYPE = "com.poesys.db.col.StringColumnValue";

  /**
   * Tests the constructor and the getPrimaryKey() method. The test is a basic unit test that
   * covers the code. See the JsonPrimaryKeyIntegrationTest for more comprehensive test cases.
   */
  @Test
  public void getPrimaryKey() {
    // Create the parent primary key.
    JsonColumnValue columnValue1 = new StringJsonColumnValue(COL_NAME_1, TYPE, COL_VALUE_1);
    List<JsonColumnValue> columnValueList = new ArrayList<>(1);
    columnValueList.add(columnValue1);
    JsonPrimaryKey parentKey = new NaturalJsonPrimaryKey(CLASS, columnValueList);

    // Create the child primary key
    JsonColumnValue columnValue2 = new StringJsonColumnValue(COL_NAME_2, TYPE, COL_VALUE_2);
    columnValueList = new ArrayList<>(1);
    columnValueList.add(columnValue2);
    JsonPrimaryKey childKey = new NaturalJsonPrimaryKey(CLASS, columnValueList);

    CompositeJsonPrimaryKey jsonPrimaryKey =
      new CompositeJsonPrimaryKey(CLASS, parentKey, childKey);

    // Test the null values for the unused DTO fields.
    assertNull("sequence value is not null", jsonPrimaryKey.getValue());
    assertNull("column value list is not null", jsonPrimaryKey.getColumnValueList());
    assertNull("key list of association key is not null", jsonPrimaryKey.getKeyList());

    // Test getPrimaryKey().
    IPrimaryKey key = jsonPrimaryKey.getPrimaryKey();
    assertNotNull(key);
    assertTrue("wrong class for key: " + key.getClass().getName(),
               key instanceof CompositePrimaryKey);
  }
}