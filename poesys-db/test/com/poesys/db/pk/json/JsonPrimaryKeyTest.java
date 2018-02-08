package com.poesys.db.pk.json;

import com.poesys.db.Message;
import com.poesys.db.col.StringColumnValue;
import com.poesys.db.col.json.JsonColumnValue;
import com.poesys.db.pk.AssociationPrimaryKey;
import com.poesys.db.pk.CompositePrimaryKey;
import com.poesys.db.pk.NaturalPrimaryKey;
import com.poesys.db.pk.SequencePrimaryKey;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
public class JsonPrimaryKeyTest {

  private static final String CLASS = "com.poesys.db.ClassName";
  private static final String NAME_1 = "col1";
  private static final String NAME_2 = "col2";

  @Test
  public void testDefaultConstructor() {
    new JsonPrimaryKey();
  }

  /**
   * Test the column value list key constructor and the getters.
   */
  @Test
  public void testListConstructor() {
    List<JsonColumnValue> list = new ArrayList<>(2);
    JsonColumnValue value1 =
      new JsonColumnValue(NAME_1, StringColumnValue.class.getName(), "value1");
    JsonColumnValue value2 =
      new JsonColumnValue(NAME_2, StringColumnValue.class.getName(), "value2");
    list.add(value1);
    list.add(value2);
    JsonPrimaryKey key = new JsonPrimaryKey(NaturalPrimaryKey.class.getName(), CLASS, list);
    String keyType = key.getKeyType();
    assertNotNull("key type is null", keyType);
    assertTrue("wrong key type " + key.getKeyType(),
               key.getKeyType().equals(NaturalPrimaryKey.class.getName()));
    assertTrue("wrong class name " + key.getClassName(), key.getClassName().equals(CLASS));
    List<JsonColumnValue> newList = key.getColumnValueList();
    assertTrue("wrong number of column values: " + newList.size(), newList.size() == 2);
    assertTrue("wrong first column value " + newList.get(0).getName(),
               newList.get(0).getName().equals(NAME_1));
    assertTrue("wrong second column value " + newList.get(1).getName(),
               newList.get(1).getName().equals(NAME_2));
    assertNull("sequence value is not null", key.getValue());
    assertNull("parent key of composite key is not null", key.getParentKey());
    assertNull("child key of composite key is not null", key.getChildKey());
    assertNull("key list of association key is not null", key.getKeyList());
  }

  /**
   * Test the sequence key constructor and the getters.
   */
  @Test
  public void testSequenceConstructor() {
    JsonPrimaryKey key = new JsonPrimaryKey(CLASS, BigInteger.ONE);
    String keyType = key.getKeyType();
    assertNotNull("key type is null", keyType);
    assertTrue("wrong key type " + key.getKeyType(),
               key.getKeyType().equals(SequencePrimaryKey.class.getName()));
    assertTrue("wrong class name " + key.getClassName(), key.getClassName().equals(CLASS));
    assertTrue("wrong sequence number", key.getValue().equals(BigInteger.ONE));
    assertNull("column value list is not null", key.getColumnValueList());
    assertNull("parent key of composite key is not null", key.getParentKey());
    assertNull("child key of composite key is not null", key.getChildKey());
    assertNull("key list of association key is not null", key.getKeyList());
  }

  /**
   * Test the association key constructor and the getters.
   */
  @Test
  public void testAssociationConstructor() {
    // Construct a list of two keys for an association key.
    List<JsonPrimaryKey> keyList = new ArrayList<>(2);
    JsonPrimaryKey key1 = new JsonPrimaryKey();
    JsonPrimaryKey key2 = new JsonPrimaryKey();
    keyList.add(key1);
    keyList.add(key2);
    JsonPrimaryKey key = new JsonPrimaryKey(CLASS, keyList);
    String keyType = key.getKeyType();
    assertNotNull("key type is null", keyType);
    assertTrue("wrong key type " + key.getKeyType(),
               key.getKeyType().equals(AssociationPrimaryKey.class.getName()));
    assertTrue("wrong class name " + key.getClassName(), key.getClassName().equals(CLASS));
    List<JsonPrimaryKey> newList = key.getKeyList();
    assertTrue("key list is wrong size: " + newList.size(), newList.size() == 2);
    key = newList.get(0);
    assertNull("sequence value is not null", key.getValue());
    assertNull("column value list is not null", key.getColumnValueList());
    assertNull("parent key of composite key is not null", key.getParentKey());
    assertNull("child key of composite key is not null", key.getChildKey());
  }

  /**
   * Test the composite key constructor and the getters.
   */
  @Test
  public void testCompositeConstructor() {
    JsonPrimaryKey parentKey = new JsonPrimaryKey();
    JsonPrimaryKey childKey = new JsonPrimaryKey();
    JsonPrimaryKey key = new JsonPrimaryKey(CLASS, parentKey, childKey);
    String keyType = key.getKeyType();
    assertNotNull("key type is null", keyType);
    assertTrue("wrong key type " + key.getKeyType(),
               key.getKeyType().equals(CompositePrimaryKey.class.getName()));
    assertTrue("wrong class name " + key.getClassName(), key.getClassName().equals(CLASS));

    JsonPrimaryKey key1 = key.getParentKey();
    JsonPrimaryKey key2 = key.getChildKey();

    assertTrue("parent key not registered", key1 != null);
    assertTrue("child key not registered", key2 != null);

    assertNull("sequence value is not null", key.getValue());
    assertNull("column value list is not null", key.getColumnValueList());
    assertNull("key list of association key is not null", key.getKeyList());
  }

  /**
   * Test the getPrimaryKey() method, which should throw an exception.
   */
  @Test
  public void testGetPrimaryKey() {
    JsonPrimaryKey key = new JsonPrimaryKey();
    try {
      key.getPrimaryKey();
      fail("getPrimaryKey() call did not throw exception");
    } catch (RuntimeException e) {
      assertTrue("wrong exception from getPrimaryKey() call: " + e.getMessage(),
                 e.getMessage().equals(
                   Message.getMessage("com.poesys.db.pk.json.msg.json_primary_key_base_instance",
                                      null)));
    }
  }
}