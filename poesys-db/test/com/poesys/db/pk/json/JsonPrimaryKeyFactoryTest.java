package com.poesys.db.pk.json;/* Copyright (c) 2018 Poesys Associates. All rights reserved. */

import com.poesys.db.InvalidParametersException;
import com.poesys.db.Message;
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
}