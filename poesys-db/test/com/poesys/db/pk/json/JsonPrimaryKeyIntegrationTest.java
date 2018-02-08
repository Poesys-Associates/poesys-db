/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db.pk.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.poesys.db.col.IColumnValue;
import com.poesys.db.col.StringColumnValue;
import com.poesys.db.pk.*;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

/**
 * CUT: IJsonPrimaryKey, IPrimaryKey
 * <p>
 * This class contains full tests of GSON-based serialization and deserialization of IPrimaryKey
 * values. Each test verifies a particular class of key. This class also exercises all of the
 * getJsonPrimaryKey factory methods in the IPrimaryKey implementation set.
 * </p>
 */
public class JsonPrimaryKeyIntegrationTest {
  private static final String CLASS = "com.poesys.db.ClassName";
  private static final String COL_NAME_1 = "col1";
  private static final String COL_VALUE_1 = "string value 1";
  private static final String COL_NAME_2 = "col2";
  private static final String COL_VALUE_2 = "string value 2";

  private static final String PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

  /**
   * Tests the serialization and deserialization of a natural primary key with a single column
   */
  @Test
  public void testNaturalKeySingleColumn() {
    IColumnValue columnValue = new StringColumnValue(COL_NAME_1, COL_VALUE_1);
    List<IColumnValue> columnValueList = new ArrayList<>(1);
    columnValueList.add(columnValue);
    IPrimaryKey key = new NaturalPrimaryKey(columnValueList, CLASS);

    // Create the Gson object.
    Gson gson = new GsonBuilder().serializeNulls().setDateFormat(PATTERN).create();
    JsonPrimaryKey jsonKey = key.getJsonPrimaryKey();
    String json = gson.toJson(jsonKey);

    assertTrue("wrong JSON generated for natural key: " + json, json.equals(
      "{\"keyType\":\"com.poesys.db.pk.NaturalPrimaryKey\",\"className\":\"com.poesys.db" +
      ".ClassName\",\"columnValueList\":[{\"name\":\"col1\",\"type\":\"com.poesys.db.col" +
      ".StringColumnValue\",\"value\":\"string value 1\"}],\"value\":null,\"keyList\":null," +
      "\"parentKey\":null,\"childKey\":null}"));

    IJsonPrimaryKey newJsonKey =
      JsonPrimaryKeyFactory.getJsonPrimaryKey(gson.fromJson(json, JsonPrimaryKey.class));
    IPrimaryKey newKey = newJsonKey.getPrimaryKey();
    assertTrue("deserialized key not equal to original key: " + newKey.getStringKey(),
               newKey.equals(key));
  }

  /**
   * Tests the serialization and deserialization of a natural primary key with multiple columns
   */
  @Test
  public void testNaturalKeyMultipleColumns() {
    IColumnValue columnValue1 = new StringColumnValue(COL_NAME_1, COL_VALUE_1);
    IColumnValue columnValue2 = new StringColumnValue(COL_NAME_2, COL_VALUE_2);
    List<IColumnValue> columnValueList = new ArrayList<>(1);
    columnValueList.add(columnValue1);
    columnValueList.add(columnValue2);
    IPrimaryKey key = new NaturalPrimaryKey(columnValueList, CLASS);

    // Create the Gson object.
    Gson gson = new GsonBuilder().serializeNulls().setDateFormat(PATTERN).create();
    JsonPrimaryKey jsonKey = key.getJsonPrimaryKey();
    String json = gson.toJson(jsonKey);

    assertTrue("wrong JSON generated for natural key: " + json, json.equals(
      "{\"keyType\":\"com" + ".poesys.db.pk" +
      ".NaturalPrimaryKey\",\"className\":\"com.poesys.db.ClassName\"," +
      "\"columnValueList\":[{\"name\":\"col1\",\"type\":\"com.poesys.db.col.StringColumnValue\"," +
      "\"value\":\"string value 1\"},{\"name\":\"col2\",\"type\":\"com.poesys.db.col" +
      ".StringColumnValue\",\"value\":\"string value 2\"}],\"value\":null,\"keyList\":null," +
      "\"parentKey\":null,\"childKey\":null}"));

    IJsonPrimaryKey newJsonKey =
      JsonPrimaryKeyFactory.getJsonPrimaryKey(gson.fromJson(json, JsonPrimaryKey.class));
    IPrimaryKey newKey = newJsonKey.getPrimaryKey();
    assertTrue("deserialized key not equal to original key: " + newKey.getStringKey(),
               newKey.equals(key));
  }

  /**
   * Tests the serialization and deserialization of an association primary key
   */
  @Test
  public void testAssociationKey() {
    // Create the first primary key.
    IColumnValue columnValue1 = new StringColumnValue(COL_NAME_1, COL_VALUE_1);
    List<IColumnValue> columnValueList = new ArrayList<>(1);
    columnValueList.add(columnValue1);
    IPrimaryKey key1 = new NaturalPrimaryKey(columnValueList, CLASS);

    // Create the second primary key
    IColumnValue columnValue2 = new StringColumnValue(COL_NAME_2, COL_VALUE_2);
    columnValueList = new ArrayList<>(1);
    columnValueList.add(columnValue2);
    IPrimaryKey key2 = new NaturalPrimaryKey(columnValueList, CLASS);

    // Create the association key that associates the first and second keys.
    List<IPrimaryKey> keyList = new ArrayList<>(2);
    keyList.add(key1);
    keyList.add(key2);
    IPrimaryKey associationKey = new AssociationPrimaryKey(keyList, CLASS);

    // Create the Gson object from the association key.
    Gson gson = new GsonBuilder().serializeNulls().setDateFormat(PATTERN).create();
    JsonPrimaryKey jsonKey = associationKey.getJsonPrimaryKey();
    String json = gson.toJson(jsonKey);

    assertTrue("wrong JSON generated for association key: " + json, json.equals(
      "{\"keyType\":\"com.poesys.db.pk.AssociationPrimaryKey\",\"className\":\"com.poesys.db" +
      ".ClassName\",\"columnValueList\":null,\"value\":null,\"keyList\":[{\"keyType\":\"com" +
      ".poesys.db.pk.NaturalPrimaryKey\",\"className\":\"com.poesys.db.ClassName\"," +
      "\"columnValueList\":[{\"name\":\"col1\",\"type\":\"com.poesys.db.col.StringColumnValue\"," +
      "\"value\":\"string value 1\"}],\"value\":null,\"keyList\":null,\"parentKey\":null," +
      "\"childKey\":null},{\"keyType\":\"com.poesys.db.pk.NaturalPrimaryKey\",\"className\":\"com" +
      ".poesys.db.ClassName\",\"columnValueList\":[{\"name\":\"col2\",\"type\":\"com.poesys.db" +
      ".col.StringColumnValue\",\"value\":\"string value 2\"}],\"value\":null,\"keyList\":null," +
      "\"parentKey\":null,\"childKey\":null}],\"parentKey\":null,\"childKey\":null}"));

    IJsonPrimaryKey newJsonKey =
      JsonPrimaryKeyFactory.getJsonPrimaryKey(gson.fromJson(json, JsonPrimaryKey.class));
    IPrimaryKey newKey = newJsonKey.getPrimaryKey();
    assertTrue("deserialized key not equal to original key: " + newKey.getStringKey(),
               newKey.equals(associationKey));
  }

  /**
   * Tests the serialization and deserialization of a composite primary key
   */
  @Test
  public void testCompositeKey() {
    // Create the parent primary key.
    IColumnValue columnValue1 = new StringColumnValue(COL_NAME_1, COL_VALUE_1);
    List<IColumnValue> columnValueList = new ArrayList<>(1);
    columnValueList.add(columnValue1);
    IPrimaryKey parentKey = new NaturalPrimaryKey(columnValueList, CLASS);

    // Create the child primary key
    IColumnValue columnValue2 = new StringColumnValue(COL_NAME_2, COL_VALUE_2);
    columnValueList = new ArrayList<>(1);
    columnValueList.add(columnValue2);
    IPrimaryKey childKey = new NaturalPrimaryKey(columnValueList, CLASS);

    // Create the composite key that combines the parent and child keys.
    IPrimaryKey compositeKey = new CompositePrimaryKey(parentKey, childKey, CLASS);

    // Create the Gson object from the association key.
    Gson gson = new GsonBuilder().serializeNulls().setDateFormat(PATTERN).create();
    JsonPrimaryKey jsonKey = compositeKey.getJsonPrimaryKey();
    String json = gson.toJson(jsonKey);

    assertTrue("wrong JSON generated for composite key: " + json, json.equals(
      "{\"keyType\":\"com.poesys.db.pk.CompositePrimaryKey\",\"className\":\"com.poesys.db" +
      ".ClassName\",\"columnValueList\":null,\"value\":null,\"keyList\":null," +
      "\"parentKey\":{\"keyType\":\"com.poesys.db.pk.NaturalPrimaryKey\",\"className\":\"com" +
      ".poesys.db.ClassName\",\"columnValueList\":[{\"name\":\"col1\",\"type\":\"com.poesys.db" +
      ".col.StringColumnValue\",\"value\":\"string value 1\"}],\"value\":null,\"keyList\":null," +
      "\"parentKey\":null,\"childKey\":null},\"childKey\":{\"keyType\":\"com.poesys.db.pk" +
      ".NaturalPrimaryKey\",\"className\":\"com.poesys.db.ClassName\"," +
      "\"columnValueList\":[{\"name\":\"col2\",\"type\":\"com.poesys.db.col.StringColumnValue\"," +
      "\"value\":\"string value 2\"}],\"value\":null,\"keyList\":null,\"parentKey\":null," +
      "\"childKey\":null}}"));

    IJsonPrimaryKey newJsonKey =
      JsonPrimaryKeyFactory.getJsonPrimaryKey(gson.fromJson(json, JsonPrimaryKey.class));
    IPrimaryKey newKey = newJsonKey.getPrimaryKey();
    assertTrue("deserialized key not equal to original key: " + newKey.getStringKey(),
               newKey.equals(compositeKey));
  }

  /**
   * Tests the serialization and deserialization of a GUID primary key
   */
  @Test
  public void testGuidKey() {
    UUID uuid = UUID.randomUUID();
    String uuidString = uuid.toString();
    IPrimaryKey key = new GuidPrimaryKey(COL_NAME_1, uuid, CLASS);

    // Create the Gson object.
    Gson gson = new GsonBuilder().serializeNulls().setDateFormat(PATTERN).create();
    JsonPrimaryKey jsonKey = key.getJsonPrimaryKey();
    String json = gson.toJson(jsonKey);

    assertTrue("wrong JSON generated for GUID key: " + json, json.equals(
      "{\"keyType\":\"com.poesys.db.pk.GuidPrimaryKey\",\"className\":\"com.poesys.db" +
      ".ClassName\",\"columnValueList\":[{\"name\":\"col1\",\"type\":\"com.poesys.db.col" +
      ".UuidColumnValue\",\"value\":\"" + uuidString + "\"}],\"value\":null," +
      "\"keyList\":null,\"parentKey\":null,\"childKey\":null}"));

    IJsonPrimaryKey newJsonKey =
      JsonPrimaryKeyFactory.getJsonPrimaryKey(gson.fromJson(json, JsonPrimaryKey.class));
    IPrimaryKey newKey = newJsonKey.getPrimaryKey();
    assertTrue("deserialized key not equal to original key: " + newKey.getStringKey(),
               newKey.equals(key));
  }

  /**
   * Tests the serialization and deserialization of an identity primary key
   */
  @Test
  public void testIdentityKey() {
    BigInteger value = BigInteger.ONE;
    IPrimaryKey key = new IdentityPrimaryKey(COL_NAME_1, value, CLASS);

    // Create the Gson object.
    Gson gson = new GsonBuilder().serializeNulls().setDateFormat(PATTERN).create();
    JsonPrimaryKey jsonKey = key.getJsonPrimaryKey();
    String json = gson.toJson(jsonKey);

    assertTrue("wrong JSON generated for identity key: " + json, json.equals(
      "{\"keyType\":\"com.poesys.db.pk.IdentityPrimaryKey\",\"className\":\"com.poesys.db" +
      ".ClassName\",\"columnValueList\":[{\"name\":\"col1\",\"type\":\"com.poesys.db.col" +
      ".BigIntegerColumnValue\",\"value\":\"1\"}],\"value\":null,\"keyList\":null," +
      "\"parentKey\":null,\"childKey\":null}"));

    IJsonPrimaryKey newJsonKey =
      JsonPrimaryKeyFactory.getJsonPrimaryKey(gson.fromJson(json, JsonPrimaryKey.class));
    IPrimaryKey newKey = newJsonKey.getPrimaryKey();
    assertTrue("deserialized key not equal to original key: " + newKey.getStringKey() + " != " +
               key.getStringKey(), newKey.equals(key));
  }

  /**
   * Tests the serialization and deserialization of a sequence primary key
   */
  @Test
  public void testSequenceKey() {
    BigInteger value = BigInteger.ONE;
    IPrimaryKey key = new SequencePrimaryKey(COL_NAME_1, value, CLASS);

    // Create the Gson object.
    Gson gson = new GsonBuilder().serializeNulls().setDateFormat(PATTERN).create();
    JsonPrimaryKey jsonKey = key.getJsonPrimaryKey();
    String json = gson.toJson(jsonKey);

    assertTrue("wrong JSON generated for sequence key: " + json, json.equals(
      "{\"keyType\":\"com.poesys.db.pk.SequencePrimaryKey\",\"className\":\"com.poesys.db" +
      ".ClassName\",\"columnValueList\":[{\"name\":\"col1\",\"type\":\"com.poesys.db.col" +
      ".BigIntegerColumnValue\",\"value\":\"1\"}],\"value\":null,\"keyList\":null," +
      "\"parentKey\":null,\"childKey\":null}"));

    IJsonPrimaryKey newJsonKey =
      JsonPrimaryKeyFactory.getJsonPrimaryKey(gson.fromJson(json, JsonPrimaryKey.class));
    IPrimaryKey newKey = newJsonKey.getPrimaryKey();
    assertTrue("deserialized key not equal to original key: " + newKey.getStringKey() + " != " +
               key.getStringKey(), newKey.equals(key));
  }
}