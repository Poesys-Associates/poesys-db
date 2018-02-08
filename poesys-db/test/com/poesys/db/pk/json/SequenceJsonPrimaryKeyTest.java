package com.poesys.db.pk.json;/* Copyright (c) 2018 Poesys Associates. All rights reserved. */

import com.poesys.db.col.json.BigIntegerJsonColumnValue;
import com.poesys.db.col.json.JsonColumnValue;
import com.poesys.db.col.json.UuidJsonColumnValue;
import com.poesys.db.pk.GuidPrimaryKey;
import com.poesys.db.pk.IPrimaryKey;
import com.poesys.db.pk.IdentityPrimaryKey;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * CUT: GuidJsonPrimaryKey
 */
public class IdentityJsonPrimaryKeyTest {
  private static final String CLASS = "com.poesys.db.ClassName";

  private static final String COL_NAME_1 = "col1";
  private static final String COL_VALUE_1 = "1";
  private static final String TYPE = "com.poesys.db.col.UuidColumnValue";

  /**
   * Tests the constructor and the getPrimaryKey() method. The test is a basic unit test that
   * covers the code. See the JsonPrimaryKeyIntegrationTest for more comprehensive test cases.
   */
  @Test
  public void getPrimaryKey() {
    // Create the guid column value.
    JsonColumnValue columnValue1 = new BigIntegerJsonColumnValue(COL_NAME_1, TYPE, COL_VALUE_1);
    List<JsonColumnValue> columnValueList = new ArrayList<>(1);
    columnValueList.add(columnValue1);
    IdentityJsonPrimaryKey jsonPrimaryKey = new IdentityJsonPrimaryKey(CLASS, columnValueList);

    // Test the null values for the unused DTO fields.
    assertNull("sequence value is not null", jsonPrimaryKey.getValue());
    assertNull("parent key of composite key is not null", jsonPrimaryKey.getParentKey());
    assertNull("child key of composite key is not null", jsonPrimaryKey.getChildKey());
    assertNull("key list of association key is not null", jsonPrimaryKey.getKeyList());

    // Test getPrimaryKey().
    IPrimaryKey key = jsonPrimaryKey.getPrimaryKey();
    assertNotNull(key);
    assertTrue("wrong class for key: " + key.getClass().getName(),
               key instanceof IdentityPrimaryKey);
  }
}