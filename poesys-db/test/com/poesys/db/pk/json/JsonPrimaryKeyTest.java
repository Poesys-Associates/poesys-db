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
@SuppressWarnings("EqualsBetweenInconvertibleTypes")
public class JsonPrimaryKeyTest {

  private static final String CLASS = "com.poesys.db.ClassName";
  private static final String CLASS2 = "com.poesys.db.Class2Name";
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

  /**
   * Test comparison of JsonPrimaryKey object with itself
   */
  @Test
  public void testEqualsSameObject() {
    List<JsonColumnValue> list = new ArrayList<>(2);
    JsonColumnValue value1 =
      new JsonColumnValue(NAME_1, StringColumnValue.class.getName(), "value1");
    JsonColumnValue value2 =
      new JsonColumnValue(NAME_2, StringColumnValue.class.getName(), "value2");
    list.add(value1);
    list.add(value2);
    JsonPrimaryKey key = new JsonPrimaryKey(NaturalPrimaryKey.class.getName(), CLASS, list);
    assertTrue("key does not equal same key", key.equals(key));
  }

  /**
   * Test comparison of JsonPrimaryKey with object of a different class.
   */
  @Test
  public void testEqualsNotKey() {
    List<JsonColumnValue> list = new ArrayList<>(2);
    JsonColumnValue value1 =
      new JsonColumnValue(NAME_1, StringColumnValue.class.getName(), "value1");
    JsonColumnValue value2 =
      new JsonColumnValue(NAME_2, StringColumnValue.class.getName(), "value2");
    list.add(value1);
    list.add(value2);
    JsonPrimaryKey key = new JsonPrimaryKey(NaturalPrimaryKey.class.getName(), CLASS, list);
    assertTrue("key equals object of different class", !key.equals(value1));
  }

  /**
   * Test comparison of JsonPrimaryKey with non-match on key type.
   */
  @Test
  public void testNotEqualsKeyType() {
    List<JsonColumnValue> list = new ArrayList<>(2);
    JsonColumnValue value1 =
      new JsonColumnValue(NAME_1, StringColumnValue.class.getName(), "value1");
    JsonColumnValue value2 =
      new JsonColumnValue(NAME_2, StringColumnValue.class.getName(), "value2");
    list.add(value1);
    list.add(value2);
    JsonPrimaryKey key1 = new JsonPrimaryKey(NaturalPrimaryKey.class.getName(), CLASS, list);
    JsonPrimaryKey key2 = new JsonPrimaryKey(CLASS, BigInteger.ONE);
    assertFalse("key type not equals failed", key1.equals(key2));
  }

  /**
   * Test comparison of JsonPrimaryKey with non-match on class name.
   */
  @Test
  public void testNotEqualsClassName() {
    JsonPrimaryKey key1 = new JsonPrimaryKey(CLASS, BigInteger.ONE);
    JsonPrimaryKey key2 = new JsonPrimaryKey(CLASS2, BigInteger.ONE);
    assertFalse("class name not equals failed", key1.equals(key2));
  }

  /**
   * Test comparison of JsonPrimaryKey with matching column lists (1 col).
   */
  @Test
  public void testEqualsColumnList1() {
    List<JsonColumnValue> list = new ArrayList<>(1);
    JsonColumnValue value1 =
      new JsonColumnValue(NAME_1, StringColumnValue.class.getName(), "value1");
    list.add(value1);
    JsonPrimaryKey key1 = new JsonPrimaryKey(NaturalPrimaryKey.class.getName(), CLASS, list);
    List<JsonColumnValue> list2 = new ArrayList<>(1);
    JsonColumnValue value2 =
      new JsonColumnValue(NAME_1, StringColumnValue.class.getName(), "value1");
    list2.add(value2);
    JsonPrimaryKey key2 = new JsonPrimaryKey(NaturalPrimaryKey.class.getName(), CLASS, list2);
    assertTrue("matching column lists (1 col) not equal", key1.equals(key2));
  }

  /**
   * Test comparison of JsonPrimaryKey with non-match on column lists (1 col).
   */
  @Test
  public void testNotEqualsColumnList1() {
    List<JsonColumnValue> list1 = new ArrayList<>(1);
    JsonColumnValue value1 =
      new JsonColumnValue(NAME_1, StringColumnValue.class.getName(), "value1");
    list1.add(value1);
    JsonPrimaryKey key1 = new JsonPrimaryKey(NaturalPrimaryKey.class.getName(), CLASS, list1);
    List<JsonColumnValue> list2 = new ArrayList<>(1);
    JsonColumnValue value2 =
      new JsonColumnValue(NAME_1, StringColumnValue.class.getName(), "value2");
    list2.add(value2);
    JsonPrimaryKey key2 = new JsonPrimaryKey(NaturalPrimaryKey.class.getName(), CLASS, list2);
    assertFalse("differing column lists (1 col) are equal", key1.equals(key2));
  }

  /**
   * Test comparison of JsonPrimaryKey with matching column lists (2 col).
   */
  @Test
  public void testEqualsColumnList2() {
    List<JsonColumnValue> list = new ArrayList<>(2);
    JsonColumnValue value1 =
      new JsonColumnValue(NAME_1, StringColumnValue.class.getName(), "value1");
    JsonColumnValue value2 =
      new JsonColumnValue(NAME_2, StringColumnValue.class.getName(), "value2");
    list.add(value1);
    list.add(value2);
    JsonPrimaryKey key1 = new JsonPrimaryKey(NaturalPrimaryKey.class.getName(), CLASS, list);
    List<JsonColumnValue> list2 = new ArrayList<>(2);
    JsonColumnValue value21 =
      new JsonColumnValue(NAME_1, StringColumnValue.class.getName(), "value1");
    JsonColumnValue value22 =
      new JsonColumnValue(NAME_2, StringColumnValue.class.getName(), "value2");
    list2.add(value21);
    list2.add(value22);
    JsonPrimaryKey key2 = new JsonPrimaryKey(NaturalPrimaryKey.class.getName(), CLASS, list2);
    assertTrue("matching column lists (2 cols) not equal", key1.equals(key2));
  }

  /**
   * Test comparison of JsonPrimaryKey with non-match on column lists (2 col).
   */
  @Test
  public void testNotEqualsColumnList2() {
    List<JsonColumnValue> list1 = new ArrayList<>(2);
    JsonColumnValue value1 =
      new JsonColumnValue(NAME_1, StringColumnValue.class.getName(), "value1");
    JsonColumnValue value2 =
      new JsonColumnValue(NAME_2, StringColumnValue.class.getName(), "value2");
    list1.add(value1);
    list1.add(value2);
    JsonPrimaryKey key1 = new JsonPrimaryKey(NaturalPrimaryKey.class.getName(), CLASS, list1);
    List<JsonColumnValue> list2 = new ArrayList<>(1);
    JsonColumnValue value21 =
      new JsonColumnValue(NAME_1, StringColumnValue.class.getName(), "value2");
    JsonColumnValue value22 =
      new JsonColumnValue(NAME_2, StringColumnValue.class.getName(), "value1");
    list2.add(value21);
    list2.add(value22);
    JsonPrimaryKey key2 = new JsonPrimaryKey(NaturalPrimaryKey.class.getName(), CLASS, list2);
    assertFalse("differing column lists (2 col2) are equal", key1.equals(key2));

  }

  /**
   * Test comparison of JsonPrimaryKey with matching sequence values.
   */
  @Test
  public void testEqualsValue() {
    JsonPrimaryKey key1 = new JsonPrimaryKey(CLASS, BigInteger.ONE);
    JsonPrimaryKey key2 = new JsonPrimaryKey(CLASS, BigInteger.ONE);
    assertTrue("sequence keys with same value are not equal", key1.equals(key2));
  }

  /**
   * Test comparison of JsonPrimaryKey with non-matching sequence values.
   */
  @Test
  public void testNotEqualsValue() {
    JsonPrimaryKey key1 = new JsonPrimaryKey(CLASS, BigInteger.ONE);
    JsonPrimaryKey key2 = new JsonPrimaryKey(CLASS, BigInteger.TEN);
    assertFalse("sequence keys with different values are equal", key1.equals(key2));
  }

  /**
   * Test comparison of JsonPrimaryKey with matching key lists (1 col).
   */
  @Test
  public void testEqualsKeyList1() {
    List<JsonPrimaryKey> keyList = new ArrayList<>(2);
    JsonPrimaryKey key1 = new JsonPrimaryKey(CLASS, BigInteger.ONE);
    keyList.add(key1);
    JsonPrimaryKey primaryKey1 = new JsonPrimaryKey(CLASS, keyList);
    List<JsonPrimaryKey> keyList2 = new ArrayList<>(2);
    JsonPrimaryKey key2 = new JsonPrimaryKey(CLASS, BigInteger.ONE);
    keyList2.add(key2);
    JsonPrimaryKey primaryKey2 = new JsonPrimaryKey(CLASS, keyList2);
    assertTrue("association keys (1) with same key list are not equal", primaryKey1.equals(primaryKey2));
  }

  /**
   * Test comparison of JsonPrimaryKey with matching key lists (2 cols).
   */
  @Test
  public void testEqualsKeyList2() {
    List<JsonPrimaryKey> keyList = new ArrayList<>(2);
    JsonPrimaryKey key1 = new JsonPrimaryKey(CLASS, BigInteger.ONE);
    JsonPrimaryKey key2 = new JsonPrimaryKey(CLASS, BigInteger.ONE);
    keyList.add(key1);
    keyList.add(key2);
    JsonPrimaryKey primaryKey1 = new JsonPrimaryKey(CLASS, keyList);
    List<JsonPrimaryKey> keyList2 = new ArrayList<>(2);
    JsonPrimaryKey key21 = new JsonPrimaryKey(CLASS, BigInteger.ONE);
    JsonPrimaryKey key22 = new JsonPrimaryKey(CLASS, BigInteger.ONE);
    keyList2.add(key21);
    keyList2.add(key22);
    JsonPrimaryKey primaryKey2 = new JsonPrimaryKey(CLASS, keyList2);
    assertTrue("association keys (2) with same key list are not equal", primaryKey1.equals(primaryKey2));
  }

  /**
   * Test comparison of JsonPrimaryKey with non-match on key lists (1 col).
   */
  @Test
  public void testNotEqualsKeyList1() {
    List<JsonPrimaryKey> keyList1 = new ArrayList<>(2);
    JsonPrimaryKey key11 = new JsonPrimaryKey(CLASS, BigInteger.ONE);
    keyList1.add(key11);
    JsonPrimaryKey primaryKey1 = new JsonPrimaryKey(CLASS, keyList1);
    List<JsonPrimaryKey> keyList2 = new ArrayList<>(2);
    JsonPrimaryKey key21 = new JsonPrimaryKey(CLASS, BigInteger.TEN);
    keyList2.add(key21);
    JsonPrimaryKey primaryKey2 = new JsonPrimaryKey(CLASS, keyList2);
    assertFalse("association keys (1) with different key lists are not equal", primaryKey1.equals(primaryKey2));
  }

  /**
   * Test comparison of JsonPrimaryKey with non-match on key lists (2 cols).
   */
  @Test
  public void testNotEqualsKeyList2() {
    List<JsonPrimaryKey> keyList1 = new ArrayList<>(2);
    JsonPrimaryKey key11 = new JsonPrimaryKey(CLASS, BigInteger.ONE);
    JsonPrimaryKey key12 = new JsonPrimaryKey(CLASS, BigInteger.ONE);
    keyList1.add(key11);
    keyList1.add(key12);
    JsonPrimaryKey primaryKey1 = new JsonPrimaryKey(CLASS, keyList1);
    List<JsonPrimaryKey> keyList2 = new ArrayList<>(2);
    JsonPrimaryKey key21 = new JsonPrimaryKey(CLASS, BigInteger.TEN);
    JsonPrimaryKey key22 = new JsonPrimaryKey(CLASS, BigInteger.TEN);
    keyList2.add(key21);
    keyList2.add(key22);
    JsonPrimaryKey primaryKey2 = new JsonPrimaryKey(CLASS, keyList2);
    assertFalse("association keys (2) with different key lists are not equal", primaryKey1.equals(primaryKey2));
  }

  /**
   * Test comparison of composite JsonPrimaryKey with matching parent-child key values.
   */
  @Test
  public void testEqualsComposite() {
    JsonPrimaryKey parentKey1 = new JsonPrimaryKey(CLASS, BigInteger.ONE);
    JsonPrimaryKey childKey1 = new JsonPrimaryKey(CLASS, BigInteger.ONE);
    JsonPrimaryKey key1 = new JsonPrimaryKey(CLASS, parentKey1, childKey1);
    JsonPrimaryKey parentKey2 = new JsonPrimaryKey(CLASS, BigInteger.ONE);
    JsonPrimaryKey childKey2 = new JsonPrimaryKey(CLASS, BigInteger.ONE);
    JsonPrimaryKey key2 = new JsonPrimaryKey(CLASS, parentKey2, childKey2);
    assertTrue("composite key with same keys not equal", key1.equals(key2));
  }

  /**
   * Test comparison of composite JsonPrimaryKey with non-matching parent key values.
   */
  @Test
  public void testNotEqualsParent() {
    JsonPrimaryKey parentKey1 = new JsonPrimaryKey();
    JsonPrimaryKey childKey = new JsonPrimaryKey(CLASS, BigInteger.ONE);
    JsonPrimaryKey key1 = new JsonPrimaryKey(CLASS, parentKey1, childKey);
    JsonPrimaryKey parentKey2 = new JsonPrimaryKey(CLASS, BigInteger.TEN);
    JsonPrimaryKey key2 = new JsonPrimaryKey(CLASS, parentKey2, childKey);
    assertFalse("composite key with different parent keys not equal", key1.equals(key2));
  }

  /**
   * Test comparison of composite JsonPrimaryKey with non-matching child key values.
   */
  @Test
  public void testNotEqualsChild() {
    JsonPrimaryKey parentKey = new JsonPrimaryKey();
    JsonPrimaryKey childKey1 = new JsonPrimaryKey(CLASS, BigInteger.ONE);
    JsonPrimaryKey key1 = new JsonPrimaryKey(CLASS, parentKey, childKey1);
    JsonPrimaryKey childKey2 = new JsonPrimaryKey(CLASS, BigInteger.TEN);
    JsonPrimaryKey key2 = new JsonPrimaryKey(CLASS, parentKey, childKey2);
    assertFalse("composite key with different parent keys not equal", key1.equals(key2));
  }

  /**
   * Test hash codes from same object (single-valued natural key)
   */
  @Test
  public void testHashCodeSame() {
    List<JsonColumnValue> list = new ArrayList<>(2);
    JsonColumnValue value1 =
      new JsonColumnValue(NAME_1, StringColumnValue.class.getName(), "value1");
    list.add(value1);
    JsonPrimaryKey key1 = new JsonPrimaryKey(NaturalPrimaryKey.class.getName(), CLASS, list);
    JsonPrimaryKey key2 = new JsonPrimaryKey(NaturalPrimaryKey.class.getName(), CLASS, list);
    assertTrue("same keys have different hash codes", key1.hashCode() == key2.hashCode());
  }

  /**
   * Test hash codes from different object (single-valued natural key)
   */
  @Test
  public void testHashCodeDifferent() {
    List<JsonColumnValue> list1 = new ArrayList<>(1);
    JsonColumnValue value1 =
      new JsonColumnValue(NAME_1, StringColumnValue.class.getName(), "value1");
    list1.add(value1);
    List<JsonColumnValue> list2 = new ArrayList<>(1);
    JsonColumnValue value2 =
      new JsonColumnValue(NAME_1, StringColumnValue.class.getName(), "value2");
    list2.add(value2);
    JsonPrimaryKey key1 = new JsonPrimaryKey(NaturalPrimaryKey.class.getName(), CLASS, list1);
    JsonPrimaryKey key2 = new JsonPrimaryKey(NaturalPrimaryKey.class.getName(), CLASS, list2);
    assertFalse("different keys have same hash codes", key1.hashCode() == key2.hashCode());
  }
}