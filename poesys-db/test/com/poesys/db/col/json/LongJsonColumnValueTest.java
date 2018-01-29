/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db.col.json;

import com.poesys.db.JsonErrorException;
import com.poesys.db.col.IColumnValue;
import com.poesys.db.col.LongColumnValue;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * CUT: LongJsonColumnValue
 */
public class LongJsonColumnValueTest {
  private static final String NAME = "name";
  private static final String TYPE = "com.poesys.db.col.LongColumnValue";
  // value is an integer greater than MAXINT but less than MAXLONG
  private static final Integer MAX = Integer.MAX_VALUE;
  private static final Long LONG_VALUE = 1L+MAX;
  private static final String VALUE = LONG_VALUE.toString();
  private static final String INVALID_VALUE = "value";

  /**
   * Test field constructor and superclass getters.
   */
  @Test
  public void LongColumnValue() {
    IJsonColumnValue jsonColumnValue = new LongJsonColumnValue(NAME, TYPE, VALUE);
    assertTrue("Long column name not set: " + jsonColumnValue.getName(),
               jsonColumnValue.getName().equalsIgnoreCase(NAME));
    assertTrue("Long column type not set: " + jsonColumnValue.getType(),
               jsonColumnValue.getType().equalsIgnoreCase(TYPE));
    assertTrue("Long column value not set: " + jsonColumnValue.getValue(),
               jsonColumnValue.getValue().equalsIgnoreCase(VALUE));
  }

  /**
   * Test getColumnValue() method with invalid Long value.
   */
  @Test
  public void getColumnValueInvalidLong() {
    try {
      IJsonColumnValue jsonColumnValue = new LongJsonColumnValue(NAME, TYPE, INVALID_VALUE);
      jsonColumnValue.getColumnValue();
      fail("invalid Long value did not throw exception");
    } catch (JsonErrorException e) {
      String message = e.getMessage();
      // test for message from properties file
      assertTrue("wrong exception: " + message,
                 message.contains("JSON numeric data does not parse into number:"));
    }
  }

  /**
   * Test getColumnValue() method with valid Long value.
   */
  @Test
  public void getColumnValue() {
    IJsonColumnValue jsonColumnValue = new LongJsonColumnValue(NAME, TYPE, VALUE);
    IColumnValue columnValue = jsonColumnValue.getColumnValue();
    assertTrue("Wrong kind of column value: " + jsonColumnValue.getColumnValue().getClass(),
               columnValue instanceof LongColumnValue);
    IColumnValue directColumnValue = new LongColumnValue(NAME, new Long(VALUE));
    assertTrue("wrong column value: " + columnValue.getName(),
               directColumnValue.equals(columnValue));
  }
}