package com.poesys.db.pk.json;/* Copyright (c) 2018 Poesys Associates. All rights reserved. */

import com.poesys.db.InvalidParametersException;
import com.poesys.db.Message;
import com.poesys.db.col.IColumnValue;
import com.poesys.db.col.StringColumnValue;
import com.poesys.db.col.json.JsonColumnValue;
import com.poesys.db.pk.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * CUT: JsonPrimaryKeyFactory
 */
public class JsonPrimaryKeyFactoryTest {
  private static final String CLASS = "com.poesys.db.ClassName";

  // messages
  private static final String INVALID_TYPE_ERROR = "com.poesys.db.pk.json.msg.invalid_pk_class";

  /**
   * Test the getJsonPrimaryKey() method with a natural key.
   */
  @Test
  public void getJsonPrimaryKeyNatural() {
    List<JsonColumnValue> list = new ArrayList<>(1);
    JsonPrimaryKey jsonKey = new JsonPrimaryKey(NaturalPrimaryKey.class.getName(), CLASS, list);
    IJsonPrimaryKey key = JsonPrimaryKeyFactory.getJsonPrimaryKey(jsonKey);
    assertTrue("wrong JSON key type: " + key.getClass().getName(),
               key instanceof NaturalJsonPrimaryKey);
  }

  /**
   * Test the getJsonPrimaryKey() method with an association key.
   */
  @Test
  public void getJsonPrimaryKeyAssociation() {
    List<JsonColumnValue> list = new ArrayList<>(1);
    JsonPrimaryKey jsonKey = new JsonPrimaryKey(AssociationPrimaryKey.class.getName(), CLASS, list);
    IJsonPrimaryKey key = JsonPrimaryKeyFactory.getJsonPrimaryKey(jsonKey);
    assertTrue("wrong JSON key type: " + key.getClass().getName(),
               key instanceof AssociationJsonPrimaryKey);
  }

  /**
   * Test the getJsonPrimaryKey() method with a composite key.
   */
  @Test
  public void getJsonPrimaryKeyComposite() {
    List<JsonColumnValue> list = new ArrayList<>(1);
    JsonPrimaryKey jsonKey = new JsonPrimaryKey(CompositePrimaryKey.class.getName(), CLASS, list);
    IJsonPrimaryKey key = JsonPrimaryKeyFactory.getJsonPrimaryKey(jsonKey);
    assertTrue("wrong JSON key type: " + key.getClass().getName(),
               key instanceof CompositeJsonPrimaryKey);
  }

  /**
   * Test the getJsonPrimaryKey() method with a GUID key.
   */
  @Test
  public void getJsonPrimaryKeyGuid() {
    List<JsonColumnValue> list = new ArrayList<>(1);
    JsonPrimaryKey jsonKey = new JsonPrimaryKey(GuidPrimaryKey.class.getName(), CLASS, list);
    IJsonPrimaryKey key = JsonPrimaryKeyFactory.getJsonPrimaryKey(jsonKey);
    assertTrue("wrong JSON key type: " + key.getClass().getName(),
               key instanceof GuidJsonPrimaryKey);
  }

  /**
   * Test the getJsonPrimaryKey() method with an identity key.
   */
  @Test
  public void getJsonPrimaryKeyIdentity() {
    List<JsonColumnValue> list = new ArrayList<>(1);
    JsonPrimaryKey jsonKey = new JsonPrimaryKey(IdentityPrimaryKey.class.getName(), CLASS, list);
    IJsonPrimaryKey key = JsonPrimaryKeyFactory.getJsonPrimaryKey(jsonKey);
    assertTrue("wrong JSON key type: " + key.getClass().getName(),
               key instanceof IdentityJsonPrimaryKey);
  }

  /**
   * Test the getJsonPrimaryKey() method with an identity key.
   */
  @Test
  public void getJsonPrimaryKeySequence() {
    List<JsonColumnValue> list = new ArrayList<>(1);
    JsonPrimaryKey jsonKey = new JsonPrimaryKey(SequencePrimaryKey.class.getName(), CLASS, list);
    IJsonPrimaryKey key = JsonPrimaryKeyFactory.getJsonPrimaryKey(jsonKey);
    assertTrue("wrong JSON key type: " + key.getClass().getName(),
               key instanceof SequenceJsonPrimaryKey);
  }

  /**
   * Test the getJsonPrimaryKey() method with an unknown type of key.
   */
  @Test
  public void getJsonPrimaryKeyInvalid() {
    List<JsonColumnValue> list = new ArrayList<>(1);
    JsonPrimaryKey jsonKey = new JsonPrimaryKey("unknown", CLASS, list);
    try {
      JsonPrimaryKeyFactory.getJsonPrimaryKey(jsonKey);
    } catch (InvalidParametersException e) {
      Object[] args = {jsonKey.getKeyType()};
      String message = Message.getMessage(INVALID_TYPE_ERROR, args);
      assertTrue("wrong invalid parameters message for unknown key type",
                 message.equals(e.getMessage()));
    } catch (Exception e) {
      fail("Wrong exception for unknown key type: " + e.getMessage());
    }
  }

  /**
   * Test the getList() method with a valid list of primary keys.
   */
  @Test
  public void getListValid() {
    List<IPrimaryKey> keyList = new ArrayList<>(2);
    List<IColumnValue> columnValueList1 = new ArrayList<>(1);
    columnValueList1.add(new StringColumnValue("name1", "value1"));
    keyList.add(new NaturalPrimaryKey(columnValueList1, "com.poesys.db.dto.TestNatural"));
    List<IColumnValue> columnValueList2 = new ArrayList<>(1);
    columnValueList2.add(new StringColumnValue("name2", "value2"));
    keyList.add(new NaturalPrimaryKey(columnValueList2, "com.poesys.db.dto.TestNatural"));
    List<JsonPrimaryKey> jsonKeyList = JsonPrimaryKeyFactory.getList(keyList);
    assertNotNull(jsonKeyList);
    assertTrue(jsonKeyList.size() == 2);
    assertTrue(jsonKeyList.get(0) instanceof JsonPrimaryKey);
    assertTrue(jsonKeyList.get(1) instanceof JsonPrimaryKey);
    assertTrue(JsonPrimaryKeyFactory.getJsonPrimaryKey(jsonKeyList.get(0)).getPrimaryKey().equals(keyList.get(0)));
    assertTrue(JsonPrimaryKeyFactory.getJsonPrimaryKey(jsonKeyList.get(1)).getPrimaryKey().equals(keyList.get(1)));
  }

  /**
   * Test the getList() method with a null input list.
   */
  @Test
  public void getListNull() {
    List<JsonPrimaryKey> list = JsonPrimaryKeyFactory.getList(null);
    assertNotNull(list);
    assertTrue("null key list resulted in valid JSON key list", list.size() == 0);
  }

  /**
   * Test the getList() method with an empty input list.
   */
  @Test
  public void getListEmpty() {
    List<JsonPrimaryKey> list = JsonPrimaryKeyFactory.getList(new ArrayList<IPrimaryKey>());
    assertNotNull(list);
    assertTrue("null key list resulted in valid JSON key list", list.size() == 0);
  }
}