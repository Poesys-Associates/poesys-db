package com.poesys.db.col.json;

import com.poesys.db.InvalidParametersException;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertTrue;

/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
public class JsonColumnValueFactoryTest {
  private static final String NAME = "test";
  private static final String BIG_DECIMAL_TYPE = "com.poesys.db.col.BigDecimalColumnValue";
  private static final String BIG_INTEGER_TYPE = "com.poesys.db.col.BigIntegerColumnValue";
  private static final String INTEGER_TYPE = "com.poesys.db.col.IntegerColumnValue";
  private static final String LONG_TYPE = "com.poesys.db.col.LongColumnValue";
  private static final String DATE_TYPE = "com.poesys.db.col.DateColumnValue";
  private static final String TIMESTAMP_TYPE = "com.poesys.db.col.TimestampColumnValue";
  private static final String STRING_TYPE = "com.poesys.db.col.StringColumnValue";
  private static final String GUID_TYPE = "com.poesys.db.col.UuidColumnValue";

  // long test value is an integer greater than Integer.MAX_VALUE but less than Long.MAX_VALUE
  private static final Integer MAX = Integer.MAX_VALUE;
  private static final Long LONG_VALUE = 1L + MAX;
  private static final String LONG_TEST_VALUE = LONG_VALUE.toString();

  private static final String DATE_VALUE = "2018-01-28 11:17:00.000";

  private static final UUID UUID_VALUE = UUID.randomUUID();
  private static final String UUID_TEST_VALUE = UUID_VALUE.toString();

  @Test
  public void getJsonColumnValueBigDecimalColumnValue() {
    JsonColumnValue columnValue = new JsonColumnValue(NAME, BIG_DECIMAL_TYPE, "100.00");
    IJsonColumnValue concreteColumnValue = JsonColumnValueFactory.getJsonColumnValue(columnValue);
    assertTrue("name wrong for concrete column value: " + concreteColumnValue.getName(),
               concreteColumnValue.getName().equalsIgnoreCase(NAME));
    assertTrue("internal type wrong for concrete column value: " + concreteColumnValue.getType(),
               concreteColumnValue.getType().equalsIgnoreCase(BIG_DECIMAL_TYPE));
    assertTrue(
      "concrete class wrong for concrete column value: " + concreteColumnValue.getClass().getName(),
      concreteColumnValue instanceof BigDecimalJsonColumnValue);
  }

  @Test
  public void getJsonColumnValueBigIntegerColumnValue() {
    JsonColumnValue columnValue = new JsonColumnValue(NAME, BIG_INTEGER_TYPE, "100");
    IJsonColumnValue concreteColumnValue = JsonColumnValueFactory.getJsonColumnValue(columnValue);
    assertTrue("name wrong for concrete column value: " + concreteColumnValue.getName(),
               concreteColumnValue.getName().equalsIgnoreCase(NAME));
    assertTrue("internal type wrong for concrete column value: " + concreteColumnValue.getType(),
               concreteColumnValue.getType().equalsIgnoreCase(BIG_INTEGER_TYPE));
    assertTrue(
      "concrete class wrong for concrete column value: " + concreteColumnValue.getClass().getName(),
      concreteColumnValue instanceof BigIntegerJsonColumnValue);
  }

  @Test
  public void getJsonColumnValueIntegerColumnValue() {
    JsonColumnValue columnValue = new JsonColumnValue(NAME, INTEGER_TYPE, "100");
    IJsonColumnValue concreteColumnValue = JsonColumnValueFactory.getJsonColumnValue(columnValue);
    assertTrue("name wrong for concrete column value: " + concreteColumnValue.getName(),
               concreteColumnValue.getName().equalsIgnoreCase(NAME));
    assertTrue("internal type wrong for concrete column value: " + concreteColumnValue.getType(),
               concreteColumnValue.getType().equalsIgnoreCase(INTEGER_TYPE));
    assertTrue(
      "concrete class wrong for concrete column value: " + concreteColumnValue.getClass().getName(),
      concreteColumnValue instanceof IntegerJsonColumnValue);
  }

  @Test
  public void getJsonColumnValueNullIntegerColumnValue() {
    JsonColumnValue columnValue = new JsonColumnValue(NAME, INTEGER_TYPE, null);
    IJsonColumnValue concreteColumnValue = JsonColumnValueFactory.getJsonColumnValue(columnValue);
    assertTrue("name wrong for concrete column value: " + concreteColumnValue.getName(),
               concreteColumnValue.getName().equalsIgnoreCase(NAME));
    assertTrue("internal type wrong for concrete column value: " + concreteColumnValue.getType(),
               concreteColumnValue.getType().equalsIgnoreCase(INTEGER_TYPE));
    assertTrue(
      "concrete class wrong for concrete column value: " + concreteColumnValue.getClass().getName(),
      concreteColumnValue instanceof NullJsonColumnValue);
  }

  @Test
  public void getJsonColumnValueLongColumnValue() {
    JsonColumnValue columnValue = new JsonColumnValue(NAME, LONG_TYPE, LONG_TEST_VALUE);
    IJsonColumnValue concreteColumnValue = JsonColumnValueFactory.getJsonColumnValue(columnValue);
    assertTrue("name wrong for concrete column value: " + concreteColumnValue.getName(),
               concreteColumnValue.getName().equalsIgnoreCase(NAME));
    assertTrue("internal type wrong for concrete column value: " + concreteColumnValue.getType(),
               concreteColumnValue.getType().equalsIgnoreCase(LONG_TYPE));
    assertTrue(
      "concrete class wrong for concrete column value: " + concreteColumnValue.getClass().getName(),
      concreteColumnValue instanceof LongJsonColumnValue);
  }

  @Test
  public void getJsonColumnValueNullLongColumnValue() {
    JsonColumnValue columnValue = new JsonColumnValue(NAME, LONG_TYPE, null);
    IJsonColumnValue concreteColumnValue = JsonColumnValueFactory.getJsonColumnValue(columnValue);
    assertTrue("name wrong for concrete column value: " + concreteColumnValue.getName(),
               concreteColumnValue.getName().equalsIgnoreCase(NAME));
    assertTrue("internal type wrong for concrete column value: " + concreteColumnValue.getType(),
               concreteColumnValue.getType().equalsIgnoreCase(LONG_TYPE));
    assertTrue(
      "concrete class wrong for concrete column value: " + concreteColumnValue.getClass().getName(),
      concreteColumnValue instanceof NullJsonColumnValue);
  }

  @Test
  public void getJsonColumnValueDateColumnValue() {
    JsonColumnValue columnValue = new JsonColumnValue(NAME, DATE_TYPE, DATE_VALUE);
    IJsonColumnValue concreteColumnValue = JsonColumnValueFactory.getJsonColumnValue(columnValue);
    assertTrue("name wrong for concrete column value: " + concreteColumnValue.getName(),
               concreteColumnValue.getName().equalsIgnoreCase(NAME));
    assertTrue("internal type wrong for concrete column value: " + concreteColumnValue.getType(),
               concreteColumnValue.getType().equalsIgnoreCase(DATE_TYPE));
    assertTrue(
      "concrete class wrong for concrete column value: " + concreteColumnValue.getClass().getName(),
      concreteColumnValue instanceof DateJsonColumnValue);
  }

  @Test
  public void getJsonColumnValueTimestampColumnValue() {
    JsonColumnValue columnValue = new JsonColumnValue(NAME, TIMESTAMP_TYPE, DATE_VALUE);
    IJsonColumnValue concreteColumnValue = JsonColumnValueFactory.getJsonColumnValue(columnValue);
    assertTrue("name wrong for concrete column value: " + concreteColumnValue.getName(),
               concreteColumnValue.getName().equalsIgnoreCase(NAME));
    assertTrue("internal type wrong for concrete column value: " + concreteColumnValue.getType(),
               concreteColumnValue.getType().equalsIgnoreCase(TIMESTAMP_TYPE));
    assertTrue(
      "concrete class wrong for concrete column value: " + concreteColumnValue.getClass().getName(),
      concreteColumnValue instanceof TimestampJsonColumnValue);
  }

  @Test
  public void getJsonColumnValueStringColumnValue() {
    JsonColumnValue columnValue = new JsonColumnValue(NAME, STRING_TYPE, "string value");
    IJsonColumnValue concreteColumnValue = JsonColumnValueFactory.getJsonColumnValue(columnValue);
    assertTrue("name wrong for concrete column value: " + concreteColumnValue.getName(),
               concreteColumnValue.getName().equalsIgnoreCase(NAME));
    assertTrue("internal type wrong for concrete column value: " + concreteColumnValue.getType(),
               concreteColumnValue.getType().equalsIgnoreCase(STRING_TYPE));
    assertTrue(
      "concrete class wrong for concrete column value: " + concreteColumnValue.getClass().getName(),
      concreteColumnValue instanceof StringJsonColumnValue);
  }

  @Test
  public void getJsonColumnValueUuidColumnValue() {
    JsonColumnValue columnValue = new JsonColumnValue(NAME, GUID_TYPE, UUID_TEST_VALUE);
    IJsonColumnValue concreteColumnValue = JsonColumnValueFactory.getJsonColumnValue(columnValue);
    assertTrue("name wrong for concrete column value: " + concreteColumnValue.getName(),
               concreteColumnValue.getName().equalsIgnoreCase(NAME));
    assertTrue("internal type wrong for concrete column value: " + concreteColumnValue.getType(),
               concreteColumnValue.getType().equalsIgnoreCase(GUID_TYPE));
    assertTrue(
      "concrete class wrong for concrete column value: " + concreteColumnValue.getClass().getName(),
      concreteColumnValue instanceof UuidJsonColumnValue);
  }

  @Test
  public void getJsonColumnValueUnknownColumnValue() {
    JsonColumnValue columnValue = new JsonColumnValue(NAME, "unknown", "unknown test value");
    try {
      JsonColumnValueFactory.getJsonColumnValue(columnValue);
    } catch (InvalidParametersException e) {
      // test message from properties file
      assertTrue("wrong exception: " + e.getMessage(),
                 e.getMessage().contains("JSON type for database data is not known:"));
    }
  }
}