package com.poesys.db.pk.json;/* Copyright (c) 2018 Poesys Associates. All rights reserved. */

import com.poesys.db.col.IColumnValue;
import com.poesys.db.col.StringColumnValue;
import com.poesys.db.col.json.IJsonColumnValue;
import com.poesys.db.col.json.JsonColumnValue;
import com.poesys.db.col.json.StringJsonColumnValue;
import com.poesys.db.pk.AssociationPrimaryKey;
import com.poesys.db.pk.IPrimaryKey;
import com.poesys.db.pk.NaturalPrimaryKey;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class AssociationJsonPrimaryKeyTest {
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
    // Create the first primary key.
    JsonColumnValue columnValue1 = new StringJsonColumnValue(COL_NAME_1, TYPE, COL_VALUE_1);
    List<JsonColumnValue> columnValueList = new ArrayList<>(1);
    columnValueList.add(columnValue1);
    JsonPrimaryKey key1 = new NaturalJsonPrimaryKey(CLASS, columnValueList);

    // Create the second primary key
    JsonColumnValue columnValue2 = new StringJsonColumnValue(COL_NAME_2, TYPE, COL_VALUE_2);
    columnValueList = new ArrayList<>(1);
    columnValueList.add(columnValue2);
    JsonPrimaryKey key2 = new NaturalJsonPrimaryKey(CLASS, columnValueList);

    // Create the association key that associates the first and second keys.
    List<JsonPrimaryKey> primaryKeyList = new ArrayList<>(2);
    primaryKeyList.add(key1);
    primaryKeyList.add(key2);

    AssociationJsonPrimaryKey jsonPrimaryKey = new AssociationJsonPrimaryKey(CLASS, primaryKeyList);

    // Test the null values for the unused DTO fields.
    assertNull("sequence value is not null", jsonPrimaryKey.getValue());
    assertNull("column value list is not null", jsonPrimaryKey.getColumnValueList());
    assertNull("parent key of composite key is not null", jsonPrimaryKey.getParentKey());
    assertNull("child key of composite key is not null", jsonPrimaryKey.getChildKey());
    // Test getPrimaryKey().
    IPrimaryKey key = jsonPrimaryKey.getPrimaryKey();
    assertNotNull(key);
    assertTrue("wrong class for key: " + key.getClass().getName(),
               key instanceof AssociationPrimaryKey);
  }
}