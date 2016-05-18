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
import java.sql.Statement;

import org.junit.Test;

import com.poesys.db.DuplicateKeyNameException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.col.AbstractColumnValue;
import com.poesys.db.dao.ConnectionTest;


/**
 * 
 * @author Bob Muller (muller@computer.org)
 */
public class IdentityPrimaryKeyTest extends ConnectionTest {

  private static final String CLASS_NAME = "com.poesys.db.dto.TestSequence";

  /**
   * Test method for
   * {@link com.poesys.db.pk.IdentityPrimaryKey#IdentityPrimaryKey(java.lang.String,java.lang.String)}
   * .
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column has the same
   *           name in the key
   */
  @Test
  public void testIdentityPrimaryKey() throws InvalidParametersException,
      DuplicateKeyNameException {
    IdentityPrimaryKey key1 = new IdentityPrimaryKey("col", CLASS_NAME);
    // Should get back the column name from getColumnNames
    assertTrue(key1.getColumnNames().size() == 1);
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.IdentityPrimaryKey#getSqlInsertColumnList()}.
   * 
   * @throws InvalidParametersException when there is a null parameter
   */
  @Test
  public void testGetSqlInsertColumnList() throws InvalidParametersException {
    IdentityPrimaryKey key1 = new IdentityPrimaryKey("col", CLASS_NAME);
    String colList = key1.getSqlInsertColumnList();
    assertTrue("".equalsIgnoreCase(colList));
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.IdentityPrimaryKey#setInsertParams(java.sql.PreparedStatement, int)}
   * .
   * 
   * @throws SQLException when can't get a connection
   * @throws InvalidParametersException when a parameter is null
   * @throws IOException when can't get a property
   */
  @Test
  public void testSetInsertParams() throws SQLException,
      InvalidParametersException, IOException {
    Connection connection = getConnection();
    IdentityPrimaryKey key1 = new IdentityPrimaryKey("col", CLASS_NAME);
    PreparedStatement stmt =
      connection.prepareStatement("INSERT INTO Test () VALUES ()");
    int next = key1.setInsertParams(stmt, 1);
    assertTrue(next == 1);

  }

  /**
   * Test method for Oracle version of
   * {@link com.poesys.db.pk.IdentityPrimaryKey#finalizeInsert(java.sql.PreparedStatement)}
   * .
   * 
   * @throws IOException when can't get a property
   * @throws SQLException when can't get a connection
   * @throws InvalidParametersException when a parameter is null
   */
  /*
   * @Test public void testFinalizeInsertOracle() throws IOException,
   * InvalidParametersException, SQLException { Connection connection =
   * getConnection(DBMS.ORACLE, "com.poesys.db.poesystest.oracle");
   * IdentityPrimaryKey key1 = new IdentityPrimaryKey("col"); PreparedStatement
   * stmt = connection.prepareStatement("INSERT INTO Test (testString) VALUES
   * (?)"); int next = key1.setInsertParams(stmt, 1); stmt.setString(next, "test
   * string"); stmt.execute(); try { key1.finalizeInsert(stmt); fail("Oracle
   * finalize succeeded but should have thrown SQL Exception"); } catch
   * (SQLException e) { assertTrue(true); } }
   */

  /**
   * Test method for MySQL version of
   * {@link com.poesys.db.pk.IdentityPrimaryKey#finalizeInsert(java.sql.PreparedStatement)}
   * .
   * 
   * @throws IOException when can't get a property
   * @throws SQLException when can't get a connection
   * @throws InvalidParametersException when a parameter is null
   */
  @Test
  public void testFinalizeInsertMySql() throws SQLException, IOException,
      InvalidParametersException {
    Connection connection = getConnection();
    IdentityPrimaryKey key1 = new IdentityPrimaryKey("col", CLASS_NAME);
    PreparedStatement stmt =
      connection.prepareStatement("INSERT INTO TestIdentity () VALUES ()",
                                  Statement.RETURN_GENERATED_KEYS);
    int next = key1.setInsertParams(stmt, 1);
    assertTrue(next == 1);
    stmt.execute();
    key1.finalizeInsert(stmt);
    for (AbstractColumnValue col : key1) {
      assertTrue(col.hasValue());
    }
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
    IdentityPrimaryKey key1 = new IdentityPrimaryKey("col", CLASS_NAME);
    assertTrue(key1 != null);
    String value = key1.getValueList();
    // Default value is null before auto-generated value set
    String shouldBe = "(col=null)";
    assertTrue(value.equals(shouldBe));
  }
}
