/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db.col.json;

import com.poesys.db.JsonErrorException;
import com.poesys.db.col.UuidColumnValue;
import com.poesys.db.col.IColumnValue;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * CUT: UuidJsonColumnValue
 */
public class UuidJsonColumnValueTest {
  private static final String NAME = "name";
  private static final String TYPE = "com.poesys.db.col.UuidColumnValue";
  private static final UUID UUID_VALUE = UUID.randomUUID();
  private static final String VALUE = UUID_VALUE.toString();
  private static final String INVALID_VALUE = "value";

  /**
   * Test field constructor and superclass getters.
   */
  @Test
  public void UuidColumnValue() {
    IJsonColumnValue jsonColumnValue = new UuidJsonColumnValue(NAME, TYPE, VALUE);
    assertTrue("guid column name not set: " + jsonColumnValue.getName(),
               jsonColumnValue.getName().equalsIgnoreCase(NAME));
    assertTrue("guid column type not set: " + jsonColumnValue.getType(),
               jsonColumnValue.getType().equalsIgnoreCase(TYPE));
    assertTrue("guid column value not set: " + jsonColumnValue.getValue(),
               jsonColumnValue.getValue().equalsIgnoreCase(VALUE));
  }

  /**
   * Test getColumnValue() method with invalid guid value.
   */
  @Test
  public void getColumnValueInvalidUuid() {
    try {
      IJsonColumnValue jsonColumnValue = new UuidJsonColumnValue(NAME, TYPE, INVALID_VALUE);
      jsonColumnValue.getColumnValue();
      fail("invalid guid value did not throw exception");
    } catch (JsonErrorException e) {
      String message = e.getMessage();
      // test for message from properties file
      assertTrue("wrong exception: " + message,
                 message.contains("JSON GUID data does not parse into GUID:"));
    }
  }

  /**
   * Test getColumnValue() method with valid guid value.
   */
  @Test
  public void getColumnValue() {
    IJsonColumnValue jsonColumnValue = new UuidJsonColumnValue(NAME, TYPE, VALUE);
    IColumnValue columnValue = jsonColumnValue.getColumnValue();
    assertTrue("Wrong kind of column value: " + jsonColumnValue.getColumnValue().getClass(),
               columnValue instanceof UuidColumnValue);
    IColumnValue directColumnValue = new UuidColumnValue(NAME, UUID_VALUE);
    assertTrue("wrong column value: " + columnValue.getName(),
               directColumnValue.equals(columnValue));
  }
}