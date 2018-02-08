/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db.col.json;

import com.poesys.db.col.IColumnValue;
import com.poesys.db.col.NullColumnValue;
import org.junit.Test;

import java.sql.Types;

import static org.junit.Assert.assertTrue;

/**
 * CUT: NullJsonColumnValue
 */
public class NullJsonColumnValueTest {
  private static final String NAME = "name";
  private static final String TYPE = "com.poesys.db.col.NullColumnValue";
  private static final int JDBC_TYPE = Types.NUMERIC;

  /**
   * Test field constructor and superclass getters. Note that there is no value parameter, so
   * there is no test for "invalid" value.
   */
  @Test
  public void BigIntegerColumnValue() {
    IJsonColumnValue jsonColumnValue = new NullJsonColumnValue(NAME, TYPE, JDBC_TYPE);
    assertTrue("null column name not set: " + jsonColumnValue.getName(),
               jsonColumnValue.getName().equalsIgnoreCase(NAME));
    assertTrue("null column type not set: " + jsonColumnValue.getType(),
               jsonColumnValue.getType().equalsIgnoreCase(TYPE));
    assertTrue("null column value not set: " + jsonColumnValue.getValue(),
               jsonColumnValue.getValue() == null);
  }

  /**
   * Test getColumnValue() method with valid null value of NUMERIC type.
   */
  @Test
  public void getColumnValue() {
    NullJsonColumnValue jsonColumnValue = new NullJsonColumnValue(NAME, TYPE, JDBC_TYPE);
    IColumnValue columnValue = jsonColumnValue.getColumnValue();
    assertTrue("Wrong kind of column value: " + jsonColumnValue.getColumnValue().getClass(),
               columnValue instanceof NullColumnValue);
    IColumnValue directColumnValue = new NullColumnValue(NAME, JDBC_TYPE);
    // null value comparisons are always false
    assertTrue("wrong column value: " + columnValue.getName(),
               !directColumnValue.equals(columnValue));
  }
}