/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db.col.json;

import com.poesys.db.JsonErrorException;
import com.poesys.db.col.BigIntegerColumnValue;
import com.poesys.db.col.IColumnValue;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * CUT: BigIntegerJsonColumnValue
 */
public class BigIntegerJsonColumnValueTest {
  private static final String NAME = "name";
  private static final String TYPE = "com.poesys.db.col.BigIntegerColumnValue";
  private static final String VALUE = "100";
  private static final String INVALID_VALUE = "value";

  /**
   * Test field constructor and superclass getters.
   */
  @Test
  public void BigIntegerColumnValue() {
    IJsonColumnValue jsonColumnValue = new BigIntegerJsonColumnValue(NAME, TYPE, VALUE);
    assertTrue("BigInteger column name not set: " + jsonColumnValue.getName(),
               jsonColumnValue.getName().equalsIgnoreCase(NAME));
    assertTrue("BigInteger column type not set: " + jsonColumnValue.getType(),
               jsonColumnValue.getType().equalsIgnoreCase(TYPE));
    assertTrue("BigInteger column value not set: " + jsonColumnValue.getValue(),
               jsonColumnValue.getValue().equalsIgnoreCase(VALUE));
  }

  /**
   * Test getColumnValue() method with invalid BigInteger value.
   */
  @Test
  public void getColumnValueInvalidBigInteger() {
    try {
      IJsonColumnValue jsonColumnValue = new BigIntegerJsonColumnValue(NAME, TYPE, INVALID_VALUE);
      jsonColumnValue.getColumnValue();
      fail("invalid BigInteger value did not throw exception");
    } catch (JsonErrorException e) {
      String message = e.getMessage();
      // test for message from properties file
      assertTrue("wrong exception: " + message,
                 message.contains("JSON numeric data does not parse into number:"));
    }
  }

  /**
   * Test getColumnValue() method with valid BigInteger value.
   */
  @Test
  public void getColumnValue() {
    IJsonColumnValue jsonColumnValue = new BigIntegerJsonColumnValue(NAME, TYPE, VALUE);
    IColumnValue columnValue = jsonColumnValue.getColumnValue();
    assertTrue("Wrong kind of column value: " + jsonColumnValue.getColumnValue().getClass(),
               columnValue instanceof BigIntegerColumnValue);
    IColumnValue directColumnValue = new BigIntegerColumnValue(NAME, new BigInteger(VALUE));
    assertTrue("wrong column value: " + columnValue.getName(),
               directColumnValue.equals(columnValue));
  }
}