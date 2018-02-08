/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db.col.json;

import com.poesys.db.col.IColumnValue;
import com.poesys.db.col.NullColumnValue;
import com.poesys.db.col.StringColumnValue;
import org.junit.Test;

import java.sql.Types;

import static org.junit.Assert.assertTrue;

/**
 * CUT: StringJsonColumnValue
 */
public class StringJsonColumnValueTest {
  private static final String NAME = "name";
  private static final String TYPE = "com.poesys.db.col.StringColumnValue";
  private static final String VALUE = "value";

  /**
   * Test field constructor and superclass getters. Note that there is no invalid value for a
   * string column, so there is no invalid value test.
   */
  @Test
  public void BigIntegerColumnValue() {
    IJsonColumnValue jsonColumnValue = new StringJsonColumnValue(NAME, TYPE, VALUE);
    assertTrue("string column name not set: " + jsonColumnValue.getName(),
               jsonColumnValue.getName().equalsIgnoreCase(NAME));
    assertTrue("string column type not set: " + jsonColumnValue.getType(),
               jsonColumnValue.getType().equalsIgnoreCase(TYPE));
    assertTrue("string column value not set: " + jsonColumnValue.getValue(),
               jsonColumnValue.getValue().equals(VALUE));
  }

  /**
   * Test getColumnValue() method with valid null value of NUMERIC type.
   */
  @Test
  public void getColumnValue() {
    IJsonColumnValue jsonColumnValue = new StringJsonColumnValue(NAME, TYPE, VALUE);
    IColumnValue columnValue = jsonColumnValue.getColumnValue();
    assertTrue("Wrong kind of column value: " + jsonColumnValue.getColumnValue().getClass(),
               columnValue instanceof StringColumnValue);
    IColumnValue directColumnValue = new StringColumnValue(NAME, VALUE);
    assertTrue("wrong column value: " + columnValue.getName(),
               directColumnValue.equals(columnValue));
  }
}