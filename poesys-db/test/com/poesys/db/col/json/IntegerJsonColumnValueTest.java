/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db.col.json;

import com.poesys.db.JsonErrorException;
import com.poesys.db.col.IColumnValue;
import com.poesys.db.col.IntegerColumnValue;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * CUT: IntegerJsonColumnValue
 */
public class IntegerJsonColumnValueTest {
  private static final String NAME = "name";
  private static final String TYPE = "com.poesys.db.col.IntegerColumnValue";
  private static final String VALUE = "100";
  private static final String INVALID_VALUE = "value";

  /**
   * Test field constructor and superclass getters.
   */
  @Test
  public void IntegerColumnValue() {
    IJsonColumnValue jsonColumnValue = new IntegerJsonColumnValue(NAME, TYPE, VALUE);
    assertTrue("Integer column name not set: " + jsonColumnValue.getName(),
               jsonColumnValue.getName().equalsIgnoreCase(NAME));
    assertTrue("Integer column type not set: " + jsonColumnValue.getType(),
               jsonColumnValue.getType().equalsIgnoreCase(TYPE));
    assertTrue("Integer column value not set: " + jsonColumnValue.getValue(),
               jsonColumnValue.getValue().equalsIgnoreCase(VALUE));
  }

  /**
   * Test getColumnValue() method with invalid Integer value.
   */
  @Test
  public void getColumnValueInvalidInteger() {
    try {
      IJsonColumnValue jsonColumnValue = new IntegerJsonColumnValue(NAME, TYPE, INVALID_VALUE);
      jsonColumnValue.getColumnValue();
      fail("invalid Integer value did not throw exception");
    } catch (JsonErrorException e) {
      String message = e.getMessage();
      // test for message from properties file
      assertTrue("wrong exception: " + message,
                 message.contains("JSON numeric data does not parse into number:"));
    }
  }

  /**
   * Test getColumnValue() method with valid Integer value.
   */
  @Test
  public void getColumnValue() {
    IJsonColumnValue jsonColumnValue = new IntegerJsonColumnValue(NAME, TYPE, VALUE);
    IColumnValue columnValue = jsonColumnValue.getColumnValue();
    assertTrue("Wrong kind of column value: " + jsonColumnValue.getColumnValue().getClass(),
               columnValue instanceof IntegerColumnValue);
    IColumnValue directColumnValue = new IntegerColumnValue(NAME, new Integer(VALUE));
    assertTrue("wrong column value: " + columnValue.getName(),
               directColumnValue.equals(columnValue));
  }
}