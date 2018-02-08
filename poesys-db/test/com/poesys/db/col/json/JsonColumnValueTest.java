/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db.col.json;

import com.poesys.db.Message;
import com.poesys.db.col.IColumnValue;
import org.junit.Test;

import static org.junit.Assert.*;

public class JsonColumnValueTest {
  private static final String NAME = "test";
  private static final String TYPE = "com.poesys.db.col.StringColumnValue";
  private static final String VALUE = "test value";

  /** Test default constructor */
  @Test
  public void testJsonColumnValueDefault() {
    JsonColumnValue columnValue = new JsonColumnValue();
  }

  /** Test field constructor and getters */
  @Test
  public void testJsonColumnValueField() {
    JsonColumnValue columnValue = new JsonColumnValue(NAME, TYPE, VALUE);
    assertTrue("name getter got wrong name: " + columnValue.getName(),
               NAME.equals(columnValue.getName()));
    assertTrue("type getter got wrong type: " + columnValue.getType(),
               TYPE.equals(columnValue.getType()));
    assertTrue("value getter got wrong value: " + columnValue.getValue(),
               VALUE.equals(columnValue.getValue()));
  }

  @Test
  public void testJsonColumnValueGetColumnValue() {
    JsonColumnValue columnValue = new JsonColumnValue();
    try {
      columnValue.getColumnValue();
      // should have thrown exception
      fail("no exception from getColumnValue()");
    } catch (RuntimeException e) {
      assertTrue("wrong message for exception",
                 Message.getMessage("com.poesys.db.col.json.msg.json_column_value_base_instance",
                                    null).equals(e.getMessage()));
    }
  }
}