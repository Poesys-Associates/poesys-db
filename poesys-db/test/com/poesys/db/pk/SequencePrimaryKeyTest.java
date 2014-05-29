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
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.poesys.db.DuplicateKeyNameException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.col.AbstractColumnValue;
import com.poesys.db.connection.IConnectionFactory.DBMS;
import com.poesys.db.dao.ConnectionTest;


/**
 * Test the sequence primary key class, the abstract single value primary key
 * class, and the abstract primary key class.
 * 
 * @author Bob Muller (muller@computer.org)
 */
public class SequencePrimaryKeyTest extends ConnectionTest {

  private static final String CLASS_NAME = "com.poesys.db.dto.TestSequence";

  /**
   * Test method for
   * {@link com.poesys.db.pk.SequencePrimaryKey#SequencePrimaryKey(java.lang.String, java.math.BigInteger,java.lang.String)}
   * .
   * 
   * @throws InvalidParametersException when there is a constructor failure
   */
  public void testSequencePrimaryKey() throws InvalidParametersException {
    String colName = "col";
    AbstractSingleValuedPrimaryKey key =
      new SequencePrimaryKey(colName, new BigInteger("1"), CLASS_NAME);
    assertTrue(key != null);
    for (AbstractColumnValue value : key) {
      assertTrue(value.getName().equalsIgnoreCase(colName));
      assertTrue(value.hasValue());
    }

    // Test the pass-through exception handling for a null column name.
    try {
      @SuppressWarnings("unused")
      AbstractSingleValuedPrimaryKey key2 =
        new SequencePrimaryKey((String)null, new BigInteger("2"), CLASS_NAME);
      assertTrue(false);
    } catch (InvalidParametersException e) {
      assertTrue(true);
    }

    // Test the pass-through exception handling for a null column value.
    try {
      @SuppressWarnings("unused")
      AbstractSingleValuedPrimaryKey key2 =
        new SequencePrimaryKey(colName, null, CLASS_NAME);
      assertTrue(false);
    } catch (InvalidParametersException e) {
      assertTrue(true);
    }
  }

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
    AbstractSingleValuedPrimaryKey key1 =
      new SequencePrimaryKey(colName1, new BigInteger("1"), CLASS_NAME);
    AbstractSingleValuedPrimaryKey key2 =
      new SequencePrimaryKey(colName1, new BigInteger("200"), CLASS_NAME);
    AbstractSingleValuedPrimaryKey key3 =
      new SequencePrimaryKey(colName1, new BigInteger("1"), CLASS_NAME);
    AbstractSingleValuedPrimaryKey key4 =
      new SequencePrimaryKey(colName2, new BigInteger("1"), CLASS_NAME);

    assertTrue(key1.equals(key3));
    assertFalse(key1.equals(key2));
    assertFalse(key1.equals(key4));
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.AbstractSingleValuedPrimaryKey#iterator()}.
   * 
   * @throws InvalidParametersException when a parameter is null
   */
  public void testIterator() throws InvalidParametersException {
    AbstractSingleValuedPrimaryKey key1 =
      new SequencePrimaryKey("col", new BigInteger("1"), CLASS_NAME);
    int i = 0;
    for (@SuppressWarnings("unused")
    AbstractColumnValue colValue : key1) {
      i++;
    }
    assertTrue(i == 1);
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.AbstractSingleValuedPrimaryKey#getSqlColumnList(java.lang.String)}
   * .
   * 
   * @throws InvalidParametersException when a parameter is null
   */
  public void testGetSqlColumnList() throws InvalidParametersException {
    AbstractSingleValuedPrimaryKey key1 =
      new SequencePrimaryKey("col", new BigInteger("1"), CLASS_NAME);
    String colList = key1.getSqlColumnList("c");
    assertTrue("c.col".equalsIgnoreCase(colList));
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.AbstractSingleValuedPrimaryKey#getSqlInsertColumnList()}
   * .
   * 
   * @throws InvalidParametersException when a parameter is null
   */
  public void testGetSqlInsertColumnList() throws InvalidParametersException {
    AbstractSingleValuedPrimaryKey key1 =
      new SequencePrimaryKey("col", new BigInteger("1"), CLASS_NAME);
    String colList = key1.getSqlInsertColumnList();
    assertTrue("col".equalsIgnoreCase(colList));
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.AbstractSingleValuedPrimaryKey#getSqlWhereExpression(java.lang.String)}
   * .
   * 
   * @throws InvalidParametersException when a parameter is null
   */
  public void testGetSqlWhereExpression() throws InvalidParametersException {
    AbstractSingleValuedPrimaryKey key1 =
      new SequencePrimaryKey("col", new BigInteger("1"), CLASS_NAME);
    String colList = key1.getSqlWhereExpression("c");
    assertTrue("c.col = ?".equalsIgnoreCase(colList));
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.AbstractPrimaryKey#setParams(java.sql.PreparedStatement, int)}
   * .
   * 
   * @throws SQLException when can't get a connection
   * @throws InvalidParametersException when a parameter is null
   * @throws IOException when can't get a property
   */
  public void testSetParams() throws SQLException, InvalidParametersException,
      IOException {
    Connection connection =
      getConnection(DBMS.MYSQL, "com.poesys.db.poesystest.mysql");
    AbstractSingleValuedPrimaryKey key1 =
      new SequencePrimaryKey("col", new BigInteger("1"), CLASS_NAME);
    PreparedStatement stmt =
      connection.prepareStatement("SELECT * FROM TEST WHERE testInteger = ?");
    int next = key1.setParams(stmt, 1);
    assertTrue(next == 2);
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.AbstractPrimaryKey#setInsertParams(java.sql.PreparedStatement, int)}
   * .
   * 
   * @throws SQLException when can't get a connection
   * @throws InvalidParametersException when a parameter is null
   * @throws IOException when can't get a property
   */
  public void testSetInsertParams() throws SQLException,
      InvalidParametersException, IOException {
    Connection connection =
      getConnection(DBMS.MYSQL, "com.poesys.db.poesystest.mysql");
    AbstractSingleValuedPrimaryKey key1 =
      new SequencePrimaryKey("col", new BigInteger("1"), CLASS_NAME);
    PreparedStatement stmt =
      connection.prepareStatement("INSERT INTO Test (test) VALUES (?)");
    int next = key1.setInsertParams(stmt, 1);
    assertTrue(next == 2);
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.AbstractPrimaryKey#getAlias(java.lang.String)}.
   * 
   * @throws InvalidParametersException when a parameter is null
   */
  public void testGetAlias() throws InvalidParametersException {
    AbstractSingleValuedPrimaryKey key1 =
      new SequencePrimaryKey("col", new BigInteger("1"), CLASS_NAME);
    StringBuilder builder = key1.getAlias("c");
    assertTrue("c.".equalsIgnoreCase(builder.toString()));
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.SequencePrimaryKey#getColumnNames()}.
   * 
   * @throws InvalidParametersException when a parameter is null
   * @throws DuplicateKeyNameException when there is more than one column with
   *           the same name
   */
  public void testGetColumnNames() throws InvalidParametersException,
      DuplicateKeyNameException {
    AbstractSingleValuedPrimaryKey key1 =
      new SequencePrimaryKey("col", new BigInteger("1"), CLASS_NAME);
    assertTrue(key1.getColumnNames() != null);
  }

  /**
   * Test getValueList() for a single-column key.
   * 
   * @throws InvalidParametersException when a parameter is null
   * @throws DuplicateKeyNameException when there is more than one column with
   *           the same name
   */
  public void testGetValueListSingle() throws InvalidParametersException,
      DuplicateKeyNameException {
    AbstractSingleValuedPrimaryKey key1 =
      new SequencePrimaryKey("col", new BigInteger("1"), CLASS_NAME);
    assertTrue(key1 != null);
    String value = key1.getValueList();
    String shouldBe = "(col=1)";
    assertTrue(value.equals(shouldBe));
  }
}
