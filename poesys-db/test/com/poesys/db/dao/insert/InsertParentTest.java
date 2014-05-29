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
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.poesys.db.BatchException;
import com.poesys.db.connection.IConnectionFactory.DBMS;
import com.poesys.db.dao.ConnectionTest;
import com.poesys.db.dto.Child;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.Parent;
import com.poesys.db.pk.CompositePrimaryKey;
import com.poesys.db.pk.GuidPrimaryKey;
import com.poesys.db.pk.NaturalPrimaryKey;
import com.poesys.db.pk.PrimaryKeyFactory;


/**
 * Test the composite class insertion capability.
 * 
 * @author Bob Muller (muller@computer.org)
 */
public class InsertParentTest extends ConnectionTest {
  private static final String QUERY_PARENT =
    "SELECT col1 FROM Parent WHERE parent_id = ?";
  private static final String QUERY_CHILDREN =
    "SELECT child_number, col1 FROM Child WHERE parent_id = ? ORDER BY child_number";
  private static final String PARENT_KEY_NAME = "parent_id";
  private static final String CHILD_SUBKEY_NAME = "child_number";
  private static final String COL1_VALUE = "string";
  private static final String CLASS_NAME = "com.poesys.db.test.Parent";

  /**
   * Test the basic use case for a composite object: insert a parent and three
   * children with no errors.
   * 
   * @throws IOException when can't get a property
   * @throws SQLException when can't get a connection
   * @throws BatchException when a problem happens during processing
   */
  public void testInsert() throws IOException, SQLException, BatchException {
    Connection conn;
    try {
      conn = getConnection(DBMS.MYSQL, "com.poesys.db.poesystest.mysql");
    } catch (SQLException e) {
      throw new RuntimeException("Connect failed: " + e.getMessage(), e);
    }

    // Create the insert command (class under test) for the parent.
    Insert<Parent> cut = new Insert<Parent>(new InsertSqlParent());

    // Create the GUID primary key for the parent.
    GuidPrimaryKey key =
      PrimaryKeyFactory.createGuidKey(PARENT_KEY_NAME, CLASS_NAME);

    // Create the parent DTO with the key and the empty setters list.
    String col1 = new String(COL1_VALUE);
    Parent dto = new Parent(key, col1);

    // Create three children in a list and set the children into the parent.
    List<Child> children = new CopyOnWriteArrayList<Child>();
    NaturalPrimaryKey subKey1 =
      PrimaryKeyFactory.createSingleNumberKey(CHILD_SUBKEY_NAME,
                                              new BigInteger("1"),
                                              CLASS_NAME);
    CompositePrimaryKey key1 =
      new CompositePrimaryKey(key, subKey1, CLASS_NAME);
    children.add(new Child(key1, new BigInteger("1"), COL1_VALUE));
    NaturalPrimaryKey subKey2 =
      PrimaryKeyFactory.createSingleNumberKey(CHILD_SUBKEY_NAME,
                                              new BigInteger("2"),
                                              CLASS_NAME);
    CompositePrimaryKey key2 =
      new CompositePrimaryKey(key, subKey2, CLASS_NAME);
    children.add(new Child(key2, new BigInteger("2"), COL1_VALUE));
    NaturalPrimaryKey subKey3 =
      PrimaryKeyFactory.createSingleNumberKey(CHILD_SUBKEY_NAME,
                                              new BigInteger("3"),
                                              CLASS_NAME);
    CompositePrimaryKey key3 =
      new CompositePrimaryKey(key, subKey3, CLASS_NAME);
    children.add(new Child(key3, new BigInteger("3"), COL1_VALUE));
    dto.setChildren(children);

    Statement stmt = null;
    PreparedStatement pstmt = null;
    try {
      // Delete any rows in the Parent and Child tables.
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM Child");
      stmt.close();
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM Parent");
      stmt.close();
      stmt = null;

      // Insert the test row.
      cut.insert(conn, dto);

      // Test the flags.
      assertTrue("Inserted parent does not have status EXISTING",
                 dto.getStatus() == IDbDto.Status.EXISTING);

      // Query the parent row.
      pstmt = conn.prepareStatement(QUERY_PARENT);
      key.setParams(pstmt, 1);
      ResultSet rs = pstmt.executeQuery();
      String queriedCol1 = null;
      if (rs.next()) {
        queriedCol1 = rs.getString("col1");
      }
      pstmt.close();
      pstmt = null;
      assertTrue(queriedCol1 != null);
      assertTrue(COL1_VALUE.equals(queriedCol1));

      // Query the children of the parent.
      pstmt = conn.prepareStatement(QUERY_CHILDREN);
      key.setParams(pstmt, 1);
      rs = pstmt.executeQuery();
      int count = 0;
      while (rs.next()) {
        count++;
        BigInteger dbChildNumber =
          rs.getBigDecimal("child_number").toBigInteger();
        String dbCol1 = rs.getString("col1");
        BigInteger shouldBeChildNumber =
          new BigInteger(new String(new Integer(count).toString()));
        assertTrue(shouldBeChildNumber.equals(dbChildNumber));
        assertTrue(COL1_VALUE.equals(dbCol1));
      }
      assertTrue("count=" + count, count == 3);
    } catch (SQLException e) {
      fail("insert method failed: " + e.getMessage());
    } finally {
      if (stmt != null) {
        stmt.close();
      }
      if (pstmt != null) {
        pstmt.close();
      }
      if (conn != null) {
        conn.close();
      }
    }
  }

  /**
   * Test composite insert where one child out of three is not new.
   * 
   * @throws IOException when can't get a property
   * @throws SQLException when can't get a connection
   * @throws BatchException when a problem happens during processing
   */
  public void testInsertNotNew() throws IOException, SQLException,
      BatchException {
    Connection conn;
    try {
      conn = getConnection(DBMS.MYSQL, "com.poesys.db.poesystest.mysql");
    } catch (SQLException e) {
      throw new RuntimeException("Connect failed: " + e.getMessage(), e);
    }

    // Create the insert command (class under test) for the parent.
    Insert<Parent> cut = new Insert<Parent>(new InsertSqlParent());

    // Create the GUID primary key for the parent.
    GuidPrimaryKey key =
      PrimaryKeyFactory.createGuidKey(PARENT_KEY_NAME, CLASS_NAME);

    // Create the parent DTO with the key and the empty setters list.
    String col1 = new String(COL1_VALUE);
    Parent dto = new Parent(key, col1);

    // Create three children in a list and set the children into the parent.
    List<Child> children = new CopyOnWriteArrayList<Child>();
    NaturalPrimaryKey subKey1 =
      PrimaryKeyFactory.createSingleNumberKey(CHILD_SUBKEY_NAME,
                                              new BigInteger("1"),
                                              CLASS_NAME);
    CompositePrimaryKey key1 =
      new CompositePrimaryKey(key, subKey1, CLASS_NAME);
    children.add(new Child(key1, new BigInteger("1"), COL1_VALUE));
    NaturalPrimaryKey subKey2 =
      PrimaryKeyFactory.createSingleNumberKey(CHILD_SUBKEY_NAME,
                                              new BigInteger("2"),
                                              CLASS_NAME);
    CompositePrimaryKey key2 =
      new CompositePrimaryKey(key, subKey2, CLASS_NAME);
    children.add(new Child(key2, new BigInteger("2"), COL1_VALUE));
    NaturalPrimaryKey subKey3 =
      PrimaryKeyFactory.createSingleNumberKey(CHILD_SUBKEY_NAME,
                                              new BigInteger("3"),
                                              CLASS_NAME);
    CompositePrimaryKey key3 =
      new CompositePrimaryKey(key, subKey3, CLASS_NAME);
    Child oldChild = new Child(key3, new BigInteger("3"), COL1_VALUE);
    children.add(oldChild);
    // Mark the child existing to exclude it from the insert.
    oldChild.setExisting();
    dto.setChildren(children);

    Statement stmt = null;
    PreparedStatement pstmt = null;
    try {
      // Delete any rows in the Parent and Child tables.
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM Child");
      stmt.close();
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM Parent");
      stmt.close();
      stmt = null;

      // Insert the test row.
      cut.insert(conn, dto);

      // Test the flags.
      assertTrue("inserted parent not EXISTING",
                 dto.getStatus() == IDbDto.Status.EXISTING);

      // Query the parent row.
      pstmt = conn.prepareStatement(QUERY_PARENT);
      key.setParams(pstmt, 1);
      ResultSet rs = pstmt.executeQuery();
      String queriedCol1 = null;
      if (rs.next()) {
        queriedCol1 = rs.getString("col1");
      }
      pstmt.close();
      pstmt = null;
      assertTrue(queriedCol1 != null);
      assertTrue(COL1_VALUE.equals(queriedCol1));

      // Query the children of the parent. Should be only 2.
      pstmt = conn.prepareStatement(QUERY_CHILDREN);
      key.setParams(pstmt, 1);
      rs = pstmt.executeQuery();
      int count = 0;
      while (rs.next()) {
        count++;
        BigInteger dbChildNumber =
          rs.getBigDecimal("child_number").toBigInteger();
        String dbCol1 = rs.getString("col1");
        BigInteger shouldBeChildNumber =
          new BigInteger(new String(new Integer(count).toString()));
        assertTrue(shouldBeChildNumber.equals(dbChildNumber));
        assertTrue(COL1_VALUE.equals(dbCol1));
      }
      assertTrue("new count=" + count, count == 2);
    } catch (SQLException e) {
      fail("insert method failed: " + e.getMessage());
    } finally {
      if (stmt != null) {
        stmt.close();
      }
      if (pstmt != null) {
        pstmt.close();
      }
      if (conn != null) {
        conn.close();
      }
    }
  }

  /**
   * Test the use case for a composite object: insert a parent and no children.
   * 
   * @throws IOException when can't get a property
   * @throws SQLException when can't get a connection
   * @throws BatchException when a problem happens during processing
   */
  public void testInsertNoChildren() throws IOException, SQLException,
      BatchException {
    Connection conn;
    try {
      conn = getConnection(DBMS.MYSQL, "com.poesys.db.poesystest.mysql");
    } catch (SQLException e) {
      throw new RuntimeException("Connect failed: " + e.getMessage(), e);
    }

    // Create the insert command (class under test) for the parent.
    Insert<Parent> cut = new Insert<Parent>(new InsertSqlParent());

    // Create the GUID primary key for the parent.
    GuidPrimaryKey key =
      PrimaryKeyFactory.createGuidKey(PARENT_KEY_NAME, CLASS_NAME);

    // Create the parent DTO with the key and the empty setters list.
    String col1 = new String(COL1_VALUE);
    Parent dto = new Parent(key, col1);

    Statement stmt = null;
    PreparedStatement pstmt = null;
    try {
      // Delete any rows in the Parent and Child tables.
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM Child");
      stmt.close();
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM Parent");
      stmt.close();
      stmt = null;

      // Insert the test row.
      cut.insert(conn, dto);

      // Test the flags.
      assertTrue("inserted parent not EXISTING",
                 dto.getStatus() == IDbDto.Status.EXISTING);

      // Query the parent row.
      pstmt = conn.prepareStatement(QUERY_PARENT);
      key.setParams(pstmt, 1);
      ResultSet rs = pstmt.executeQuery();
      String queriedCol1 = null;
      if (rs.next()) {
        queriedCol1 = rs.getString("col1");
      }
      pstmt.close();
      pstmt = null;
      assertTrue(queriedCol1 != null);
      assertTrue(COL1_VALUE.equals(queriedCol1));

      // Query the children of the parent.
      pstmt = conn.prepareStatement(QUERY_CHILDREN);
      key.setParams(pstmt, 1);
      rs = pstmt.executeQuery();
      int count = 0;
      while (rs.next()) {
        count++;
        BigInteger dbChildNumber =
          rs.getBigDecimal("child_number").toBigInteger();
        String dbCol1 = rs.getString("col1");
        BigInteger shouldBeChildNumber =
          new BigInteger(new String(new Integer(count).toString()));
        assertTrue(shouldBeChildNumber.equals(dbChildNumber));
        assertTrue(COL1_VALUE.equals(dbCol1));
      }
      assertTrue("count=" + count, count == 0);
    } catch (SQLException e) {
      fail("insert method failed: " + e.getMessage());
    } finally {
      if (stmt != null) {
        stmt.close();
      }
      if (pstmt != null) {
        pstmt.close();
      }
      if (conn != null) {
        conn.close();
      }
    }
  }
}
