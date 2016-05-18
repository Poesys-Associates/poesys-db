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
package com.poesys.db.dao.insert;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.poesys.db.BatchException;
import com.poesys.db.dao.ConnectionTest;
import com.poesys.db.dto.TestIdentity;
import com.poesys.db.pk.IdentityPrimaryKey;
import com.poesys.db.pk.PrimaryKeyFactory;


/**
 * Test inserting an object with an identity primary key.
 * 
 * @author Robert J. Muller
 */
public class InsertTestIdentityTest extends ConnectionTest {
  private static final String QUERY =
    "SELECT col1 FROM TestIdentity WHERE pkey = ?";
  private static final String CLASS_NAME = "com.poesys.db.test.TestIdentity";

  /**
   * Test the insert method using the Insert DAO.
   * 
   * @throws IOException when can't get a property
   * @throws SQLException when can't get a connection
   * @throws BatchException when a problem happens during processing
   */
  public void testInsert() throws IOException, SQLException, BatchException {
    Connection conn;
    try {
      conn = getConnection();
    } catch (SQLException e) {
      throw new RuntimeException("Connect failed: " + e.getMessage(), e);
    }

    Insert<TestIdentity> cut =
      new Insert<TestIdentity>(new InsertSqlTestIdentity());

    // Create the primary key.
    IdentityPrimaryKey key =
      PrimaryKeyFactory.createIdentityKey("pkey", CLASS_NAME);

    // Create the DTO.
    String col1 = "test";
    TestIdentity dto = new TestIdentity(key, null, col1);

    PreparedStatement query = null;

    try {
      // Insert the test row.
      cut.insert(conn, dto);
      conn.commit();

      // Set the key value into the query as an argument.
      query = conn.prepareStatement(QUERY, Statement.RETURN_GENERATED_KEYS);
      key.setParams(query, 1);

      // Query the row.
      ResultSet rs = query.executeQuery();
      String queriedCol1 = null;
      if (rs.next()) {
        queriedCol1 = rs.getString("col1");
      }
      assertTrue("Test row not found", queriedCol1 != null);
      assertTrue("Queried test row does not match insert",
                 col1.equals(queriedCol1));
    } catch (SQLException e) {
      fail("insert method failed: " + e.getMessage());
    } finally {
      if (query != null) {
        query.close();
      }
      if (conn != null) {
        conn.close();
      }
    }
  }

  /**
   * Test the insert method using the InsertNoKey DAO.
   * 
   * @throws IOException when can't get a property
   * @throws SQLException when can't get a connection
   * @throws BatchException when a problem happens during processing
   */
  public void testInsertNoKey() throws IOException, SQLException,
      BatchException {
    Connection conn;
    try {
      conn = getConnection();
    } catch (SQLException e) {
      throw new RuntimeException("Connect failed: " + e.getMessage(), e);
    }

    InsertNoKey<TestIdentity> cut =
      new InsertNoKey<TestIdentity>(new InsertSqlTestIdentity());

    // Create the primary key.
    IdentityPrimaryKey key =
      PrimaryKeyFactory.createIdentityKey("pkey", CLASS_NAME);

    // Create the DTO.
    String col1 = "test";
    TestIdentity dto = new TestIdentity(key, null, col1);

    Statement stmt = null;
    PreparedStatement query = null;

    try {
      // Insert the test row.
      stmt = conn.createStatement();
      cut.insert(conn, dto);
      conn.commit();

      // Set the key value into the query as an argument.
      query = conn.prepareStatement(QUERY);
      key.setParams(query, 1);

      // Query the row.
      ResultSet rs = query.executeQuery();
      String queriedCol1 = null;
      if (rs.next()) {
        queriedCol1 = rs.getString("col1");
      }
      assertTrue("Test row not found", queriedCol1 != null);
      assertTrue("Queried test row does not match insert",
                 col1.equals(queriedCol1));
    } catch (SQLException e) {
      fail("insert method failed: " + e.getMessage());
    } finally {
      if (stmt != null) {
        stmt.close();
      }
      if (conn != null) {
        conn.close();
      }
    }
  }

}
