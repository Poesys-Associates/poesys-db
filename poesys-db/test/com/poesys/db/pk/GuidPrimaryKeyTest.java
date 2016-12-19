/*
 * Copyright (c) 2008 Poesys Associates. All rights reserved.
 * 
 * This file is part of Poesys-DB.
 * 
 * Poesys-DB is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Poesys-DB is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Poesys-DB. If not, see <http://www.gnu.org/licenses/>.
 */
package com.poesys.db.pk;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.poesys.db.DuplicateKeyNameException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.col.AbstractColumnValue;
import com.poesys.db.col.UuidColumnValue;
import com.poesys.db.dao.ConnectionTest;


/**
 * 
 * @author Robert J. Muller
 */
public class GuidPrimaryKeyTest extends ConnectionTest {

  private static final String CLASS_NAME = "com.poesys.db.dto.TestSequence";

  /**
   * Test method for
   * {@link com.poesys.db.pk.AbstractSingleValuedPrimaryKey#equals(com.poesys.db.pk.IPrimaryKey)}
   * .
   * 
   * @throws InvalidParametersException when there is a constructor failure
   */
  public void testEqualsIPrimaryKey() throws InvalidParametersException {
    String colName1 = "col";
    String colName2 = "other";
    UUID guid1 = UUID.randomUUID();
    UUID guid2 = UUID.randomUUID();
    GuidPrimaryKey key1 = new GuidPrimaryKey(colName1, guid1, CLASS_NAME);
    GuidPrimaryKey key2 = new GuidPrimaryKey(colName1, guid2, CLASS_NAME);
    GuidPrimaryKey key3 = new GuidPrimaryKey(colName1, guid1, CLASS_NAME);
    GuidPrimaryKey key4 = new GuidPrimaryKey(colName2, guid1, CLASS_NAME);

    assertTrue(key1.equals(key3));
    assertFalse(key1.equals(key2));
    assertFalse(key1.equals(key4));
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.AbstractSingleValuedPrimaryKey#iterator()}.
   * 
   * @throws InvalidParametersException when there is a null parameter
   */
  public void testIterator() throws InvalidParametersException {
    String guid = UUID.randomUUID().toString();
    GuidPrimaryKey key1 = new GuidPrimaryKey("col", guid);
    int i = 0;
    for (AbstractColumnValue colValue : key1) {
      i++;
      assertTrue(colValue.hasValue());
    }
    assertTrue(i == 1);
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.AbstractSingleValuedPrimaryKey#getSqlColumnList(java.lang.String)}
   * .
   * 
   * @throws InvalidParametersException when there is a null parameter
   */
  public void testGetSqlColumnList() throws InvalidParametersException {
    String guid = UUID.randomUUID().toString();
    GuidPrimaryKey key1 = new GuidPrimaryKey("col", guid);
    String colList = key1.getSqlColumnList("c");
    assertTrue("c.col".equalsIgnoreCase(colList));
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.AbstractSingleValuedPrimaryKey#getSqlWhereExpression(java.lang.String)}
   * .
   * 
   * @throws InvalidParametersException when there is a null parameter
   */
  public void testGetSqlWhereExpression() throws InvalidParametersException {
    String guid = UUID.randomUUID().toString();
    GuidPrimaryKey key1 = new GuidPrimaryKey("col", guid);
    String colList = key1.getSqlWhereExpression("c");
    assertTrue("c.col = ?".equalsIgnoreCase(colList));
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.GuidPrimaryKey#setParams(java.sql.PreparedStatement, int)}
   * .
   * 
   * @throws IOException when can't get a property
   * @throws SQLException when can't get a connection
   * @throws InvalidParametersException when there is a null parameter
   */
  public void testSetParams() throws SQLException, IOException,
      InvalidParametersException {
    Connection connection = getConnection();
    GuidPrimaryKey key1 = new GuidPrimaryKey("col", CLASS_NAME);
    PreparedStatement stmt =
      connection.prepareStatement("SELECT * FROM TEST WHERE testGuid = ?");
    key1.setParams(stmt, 1);
    assertTrue(true);
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.GuidPrimaryKey#GuidPrimaryKey(java.lang.String, String)}
   * .
   * 
   * @throws InvalidParametersException when there is a null parameter
   */
  public void testGuidPrimaryKeyString() throws InvalidParametersException {
    final String colName = "col";
    GuidPrimaryKey key = new GuidPrimaryKey(colName, CLASS_NAME);
    assertTrue(key != null);
    for (AbstractColumnValue value : key) {
      assertTrue(value.getName().equalsIgnoreCase(colName));
      assertTrue(value.hasValue());
    }

    // Test the pass-through exception handling for a null column name.
    try {
      UUID uuid = UUID.randomUUID();
      @SuppressWarnings("unused")
      GuidPrimaryKey key2 = new GuidPrimaryKey(null, uuid, CLASS_NAME);
      assertTrue(false);
    } catch (InvalidParametersException e) {
      assertTrue(true);
    }

    // Test the pass-through exception handling for a null column value.
    try {
      @SuppressWarnings("unused")
      GuidPrimaryKey key2 = new GuidPrimaryKey(colName, null, CLASS_NAME);
      assertTrue(false);
    } catch (InvalidParametersException e) {
      assertTrue(true);
    }
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.GuidPrimaryKey#GuidPrimaryKey(java.lang.String, java.lang.String)}
   * .
   * 
   * @throws InvalidParametersException when there is a null parameter
   */
  public void testGuidPrimaryKeyStringString()
      throws InvalidParametersException {
    final String colName = "col";
    String guid = UUID.randomUUID().toString();
    GuidPrimaryKey key = new GuidPrimaryKey(colName, guid);
    assertTrue(key != null);
    for (AbstractColumnValue value : key) {
      assertTrue(value.getName().equalsIgnoreCase(colName));
      assertTrue(value.hasValue());
    }

    // Test the pass-through exception handling for a null column name.
    try {
      @SuppressWarnings("unused")
      GuidPrimaryKey key2 =
        new GuidPrimaryKey(null, UUID.randomUUID(), CLASS_NAME);
      assertTrue(false);
    } catch (InvalidParametersException e) {
      assertTrue(true);
    }

    // Test the pass-through exception handling for a null column value.
    try {
      @SuppressWarnings("unused")
      GuidPrimaryKey key2 = new GuidPrimaryKey(colName, null, CLASS_NAME);
      assertTrue(false);
    } catch (InvalidParametersException e) {
      assertTrue(true);
    }
  }

  /**
   * Test method for {@link com.poesys.db.pk.GuidPrimaryKey#getColumnNames()}.
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column has the same
   *           name in the key
   */
  public void testGetColumnNames() throws InvalidParametersException,
      DuplicateKeyNameException {
    final String colName = "col";
    GuidPrimaryKey key =
      new GuidPrimaryKey(colName, UUID.randomUUID().toString());
    assertTrue(key.getColumnNames() != null);
  }

  /**
   * Test getValueList() for a single-column key.
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column has the same
   *           name in the key
   */
  public void testGetValueListSingle() throws InvalidParametersException,
      DuplicateKeyNameException {
    List<AbstractColumnValue> list1 = new ArrayList<AbstractColumnValue>();
    UUID uuid = UUID.randomUUID();
    list1.add(new UuidColumnValue("col", uuid));
    NaturalPrimaryKey key1 = new NaturalPrimaryKey(list1, CLASS_NAME);
    assertTrue(key1 != null);
    String value = key1.getValueList();
    String shouldBe = "(col = " + uuid.toString() + ")";
    assertTrue(value.equals(shouldBe));
  }
}
