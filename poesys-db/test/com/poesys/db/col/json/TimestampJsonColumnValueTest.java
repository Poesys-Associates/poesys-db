/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db.col.json;

import com.poesys.db.col.IColumnValue;
import com.poesys.db.col.TimestampColumnValue;
import org.junit.Test;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * CUT: TimestampJsonColumnValue
 */
public class TimestampJsonColumnValueTest {
  private static final String NAME = "name";
  private static final String TYPE = "com.poesys.db.col.TimestampColumnValue";
  private static final String VALUE = "2018-01-28 11:17:00.000";
  private static final String INVALID_VALUE = "value";

  private static final String PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
  private static final SimpleDateFormat format = new SimpleDateFormat(PATTERN);

  /**
   * Test field constructor and superclass getters.
   */
  @Test
  public void DateColumnValue() {
    IJsonColumnValue jsonColumnValue = new TimestampJsonColumnValue(NAME, TYPE, VALUE);
    assertTrue("Timestamp column name not set: " + jsonColumnValue.getName(),
               jsonColumnValue.getName().equalsIgnoreCase(NAME));
    assertTrue("Timestamp column type not set: " + jsonColumnValue.getType(),
               jsonColumnValue.getType().equalsIgnoreCase(TYPE));
    assertTrue("Timestamp column value not set: " + jsonColumnValue.getValue(),
               jsonColumnValue.getValue().equalsIgnoreCase(VALUE));
  }

  /**
   * Test getColumnValue() method with invalid Date value.
   */
  @Test
  public void getColumnValueInvalidDate() {
    try {
      IJsonColumnValue jsonColumnValue = new TimestampJsonColumnValue(NAME, TYPE, INVALID_VALUE);
      jsonColumnValue.getColumnValue();
      fail("invalid Timestamp value did not throw exception");
    } catch (RuntimeException e) {
      // test for message from properties file
      String message = e.getMessage();
      assertTrue("wrong exception: " + message,
                 message.contains("JSON date data not in correct format (yyyy-MM-dd HH:mm:ss.SSS):"));
    }
  }

  /**
   * Test getColumnValue() method with valid Date value.
   */
  @Test
  public void getColumnValue() {
    IJsonColumnValue jsonColumnValue = new TimestampJsonColumnValue(NAME, TYPE, VALUE);
    IColumnValue columnValue = jsonColumnValue.getColumnValue();
    assertTrue("Wrong kind of column value: " + jsonColumnValue.getColumnValue().getClass(),
               columnValue instanceof TimestampColumnValue);
    // Get a Java date from the date value.
    java.util.Date date = null;
    try {
      date = format.parse(VALUE);
    } catch (ParseException e) {
      fail("could not parse valid date string " + VALUE);
    }
    // Create the direct column value using the Java date to create a SQL timestamp.
    IColumnValue directColumnValue = new TimestampColumnValue(NAME, new Timestamp(date.getTime()));
    assertTrue("wrong column value: " + columnValue.getName(),
               directColumnValue.equals(columnValue));
  }
}