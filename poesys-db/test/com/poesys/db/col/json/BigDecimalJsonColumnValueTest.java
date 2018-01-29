/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db.col.json;

import com.poesys.db.JsonErrorException;
import com.poesys.db.col.BigDecimalColumnValue;
import com.poesys.db.col.IColumnValue;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

/**
 * CUT: BigDecimalJsonColumnValue
 */
public class BigDecimalJsonColumnValueTest {
  private static final String NAME = "name";
  private static final String TYPE = "com.poesys.db.col.BigDecimalColumnValue";
  private static final String VALUE = "100.00";
  private static final String INVALID_VALUE = "value";

  /**
   * Test field constructor and superclass getters.
   */
  @Test
  public void BigDecimalColumnValue() {
    IJsonColumnValue jsonColumnValue = new BigDecimalJsonColumnValue(NAME, TYPE, VALUE);
    assertTrue("big decimal column name not set: " + jsonColumnValue.getName(),
               jsonColumnValue.getName().equalsIgnoreCase(NAME));
    assertTrue("big decimal column type not set: " + jsonColumnValue.getType(),
               jsonColumnValue.getType().equalsIgnoreCase(TYPE));
    assertTrue("big decimal column value not set: " + jsonColumnValue.getValue(),
               jsonColumnValue.getValue().equalsIgnoreCase(VALUE));
  }

  /**
   * Test getColumnValue() method with invalid BigDecimal value.
   */
  @Test
  public void getColumnValueInvalidBigDecimal() {
    try {
      IJsonColumnValue jsonColumnValue = new BigDecimalJsonColumnValue(NAME, TYPE, INVALID_VALUE);
      jsonColumnValue.getColumnValue();
      fail("invalid BigDecimal value did not throw exception");
    } catch (JsonErrorException e) {
      String message = e.getMessage();
      // test for message from properties file
      assertTrue("wrong exception: " + message,
                 message.contains("JSON numeric data does not parse into number:"));
    }
  }

  /**
   * Test getColumnValue() method with valid BigDecimal value.
   */
  @Test
  public void getColumnValue() {
    IJsonColumnValue jsonColumnValue = new BigDecimalJsonColumnValue(NAME, TYPE, VALUE);
    IColumnValue columnValue = jsonColumnValue.getColumnValue();
    assertTrue("Wrong kind of column value: " + jsonColumnValue.getColumnValue().getClass(),
               columnValue instanceof BigDecimalColumnValue);
    IColumnValue directColumnValue = new BigDecimalColumnValue(NAME, new BigDecimal(VALUE));
    assertTrue("wrong column value: " + columnValue.getName(),
               directColumnValue.equals(columnValue));
  }
}