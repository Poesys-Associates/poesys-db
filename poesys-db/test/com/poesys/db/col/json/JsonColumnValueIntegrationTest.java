package com.poesys.db.col.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.poesys.db.col.BigDecimalColumnValue;
import com.poesys.db.col.IColumnValue;
import com.poesys.db.col.StringColumnValue;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
public class JsonColumnValueIntegrationTest {
  private static final String NAME = "columnName";
  private static final String STRING_NAME = "stringColumnName";
  private static final String BIGDECIMAL_TYPE = BigDecimalColumnValue.class.getName();
  private static final String STRING_TYPE = StringColumnValue.class.getName();
  private static final String BIGDECIMAL_VALUE = "100.00";
  private static final String STRING_VALUE = "string column value";

  private static final String PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

  /**
   * Test the integration of GSON with the JsonColumnValue system on a single column value.
   */
  @Test
  public void testJsonInputOutputBigDecimal() {
    // Create the Gson object.
    Gson gson = new GsonBuilder().serializeNulls().setDateFormat(PATTERN).create();
    // Create the BigDecimalJsonColumnValue object.
    BigDecimalJsonColumnValue jsonColumnValue =
      new BigDecimalJsonColumnValue(NAME, BIGDECIMAL_TYPE, BIGDECIMAL_VALUE);
    // Serialize to JSON string.
    String json = gson.toJson(jsonColumnValue);
    assertTrue("wrong JSON array value " + json, json.equals(
      "{\"name\":\"" + NAME + "\",\"type\":\"" + BIGDECIMAL_TYPE + "\",\"value\":\"" +
      BIGDECIMAL_VALUE + "\"}"));
    // Serialize JSON string to new object.
    JsonColumnValue newJsonColumnValue = gson.fromJson(json, JsonColumnValue.class);
    assertTrue("wrong column name " + newJsonColumnValue.getName(),
               newJsonColumnValue.getName().equals(NAME));
    assertTrue("wrong column type " + newJsonColumnValue.getType(),
               newJsonColumnValue.getType().equals(BIGDECIMAL_TYPE));
    assertTrue("wrong column value " + newJsonColumnValue.getValue(),
               newJsonColumnValue.getValue().equals(BIGDECIMAL_VALUE));
  }

  /**
   * Test the integration of GSON with arrays of column values containing a single value. This
   * test has the soup-to-nuts code for serializing and deserializing arrays of column values
   * starting with a Poesys column value and ending with the same value.
   */
  @Test
  public void testJsonListOneColumnValue() {
    // Create the Gson object.
    Gson gson = new GsonBuilder().serializeNulls().setDateFormat(PATTERN).create();
    IColumnValue originalColumnValue =
      new BigDecimalColumnValue(NAME, new BigDecimal(BIGDECIMAL_VALUE));
    // Create the BigDecimalJsonColumnValue object.
    IJsonColumnValue jsonColumnValue = originalColumnValue.getJsonColumnValue();
    // Create a list of JSON column values, adding the single value.
    List<IJsonColumnValue> values = new ArrayList<>();
    values.add(jsonColumnValue);
    // Convert the list to a physical array.
    IJsonColumnValue[] valueArray = values.toArray(new JsonColumnValue[0]);

    // Serialize to JSON string.
    String json = gson.toJson(valueArray);
    assertTrue("wrong JSON array value " + json, json.equals(
      "[{\"name\":\"" + NAME + "\",\"type\":\"" + BIGDECIMAL_TYPE + "\",\"value\":\"" +
      BIGDECIMAL_VALUE + "\"}]"));

    // Serialize JSON string to new object.
    JsonColumnValue[] newValueArray = gson.fromJson(json, JsonColumnValue[].class);
    // Convert the array to a list.
    List<JsonColumnValue> newValues = Arrays.stream(newValueArray).collect(Collectors.toList());
    assertTrue("wrong number of columns in JSON array as list: " + newValues.size(),
               newValues.size() == 1);
    // Extract the JSON column value.
    JsonColumnValue value = newValues.get(0);
    assertTrue("wrong column name " + value.getName(), value.getName().equals(NAME));
    assertTrue("wrong column type " + value.getType(), value.getType().equals(BIGDECIMAL_TYPE));
    assertTrue("wrong column value " + value.getValue(), value.getValue().equals(BIGDECIMAL_VALUE));

    // Generate the actual JSON column value.
    IJsonColumnValue newJsonColumnValue = JsonColumnValueFactory.getJsonColumnValue(value);
    // Convert the JSON value into a Poesys column value.
    IColumnValue columnValue = newJsonColumnValue.getColumnValue();
    assertTrue("wrong column value type: " + columnValue.getClass().getName(),
               columnValue instanceof BigDecimalColumnValue);
    assertTrue("wrong column value name " + columnValue.getName(), value.getName().equals(NAME));
    assertTrue("no column value value ", columnValue.hasValue());
    assertTrue(originalColumnValue.equals(columnValue));
  }

  /**
   * Test the integration of GSON with arrays of column values containing multiple values of
   * different types. This
   * test has the soup-to-nuts code for serializing and deserializing arrays of column values
   * starting with the Poesys column values and ending with the same values.
   */
  @Test
  public void testJsonListMultipleColumnValues() {
    // Create the Gson object.
    Gson gson = new GsonBuilder().serializeNulls().setDateFormat(PATTERN).create();

    // Create the original BigDecimalColumnValue object.
    IColumnValue originalBigDecimalColumnValue =
      new BigDecimalColumnValue(NAME, new BigDecimal(BIGDECIMAL_VALUE));
    // Create the original StringColumnValue object.
    IColumnValue originalStringColumnValue = new StringColumnValue(STRING_NAME, STRING_VALUE);

    // Create the BigDecimalJsonColumnValue object.
    IJsonColumnValue bigDecimalJsonColumnValue = originalBigDecimalColumnValue.getJsonColumnValue();
    // Create the StringJsonColumnValue object.
    IJsonColumnValue stringJsonColumnValue = originalStringColumnValue.getJsonColumnValue();

    // Create a list of JSON column values, adding the multiple values.
    List<IJsonColumnValue> values = new ArrayList<>();
    values.add(bigDecimalJsonColumnValue);
    values.add(stringJsonColumnValue);
    // Convert the list to a physical array.
    IJsonColumnValue[] valueArray = values.toArray(new JsonColumnValue[0]);

    // Serialize to JSON string.
    String json = gson.toJson(valueArray);
    assertTrue("wrong JSON array value " + json, json.equals(
      "[{\"name\":\"" + NAME + "\",\"type\":\"" + BIGDECIMAL_TYPE + "\"," + "\"value\":\"" +
      BIGDECIMAL_VALUE + "\"},{\"name\":\"" + STRING_NAME + "\",\"type\":\"" + STRING_TYPE +
      "\",\"value\":\"" + STRING_VALUE + "\"}]"));

    // Serialize JSON string to new object.
    JsonColumnValue[] newValueArray = gson.fromJson(json, JsonColumnValue[].class);
    // Convert the array to a list.
    List<JsonColumnValue> newValues = Arrays.stream(newValueArray).collect(Collectors.toList());
    assertTrue("wrong number of columns in JSON array as list: " + newValues.size(),
               newValues.size() == 2);
    // Extract the BigDecimal JSON column value.
    JsonColumnValue value = newValues.get(0);
    assertTrue("wrong column name " + value.getName(), value.getName().equals(NAME));
    assertTrue("wrong column type " + value.getType(), value.getType().equals(BIGDECIMAL_TYPE));
    assertTrue("wrong column value " + value.getValue(), value.getValue().equals(BIGDECIMAL_VALUE));

    // Generate the actual JSON column value.
    IJsonColumnValue newJsonColumnValue = JsonColumnValueFactory.getJsonColumnValue(value);
    // Convert the JSON value into a Poesys column value.
    IColumnValue columnValue = newJsonColumnValue.getColumnValue();
    assertTrue("wrong column value type: " + columnValue.getClass().getName(),
               columnValue instanceof BigDecimalColumnValue);
    assertTrue("wrong column value name " + columnValue.getName(), value.getName().equals(NAME));
    assertTrue("no column value value ", columnValue.hasValue());
    assertTrue(originalBigDecimalColumnValue.equals(columnValue));

    // Extract the String JSON column value.
    value = newValues.get(1);
    assertTrue("wrong column name " + value.getName(), value.getName().equals(STRING_NAME));
    assertTrue("wrong column type " + value.getType(), value.getType().equals(STRING_TYPE));
    assertTrue("wrong column value " + value.getValue(), value.getValue().equals(STRING_VALUE));

    // Generate the actual JSON column value.
    newJsonColumnValue = JsonColumnValueFactory.getJsonColumnValue(value);
    // Convert the JSON value into a Poesys column value.
    columnValue = newJsonColumnValue.getColumnValue();
    assertTrue("wrong column value type: " + columnValue.getClass().getName(),
               columnValue instanceof StringColumnValue);
    assertTrue("wrong column value name " + columnValue.getName(),
               value.getName().equals(STRING_NAME));
    assertTrue("no column value value ", columnValue.hasValue());
    assertTrue(originalStringColumnValue.equals(columnValue));
  }
}