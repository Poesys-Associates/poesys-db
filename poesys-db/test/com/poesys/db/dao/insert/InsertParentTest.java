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

import com.poesys.db.DbErrorException;
import com.poesys.db.dao.ConnectionTest;
import com.poesys.db.dto.Child;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.Parent;
import com.poesys.db.pk.CompositePrimaryKey;
import com.poesys.db.pk.GuidPrimaryKey;
import com.poesys.db.pk.NaturalPrimaryKey;
import com.poesys.db.pk.PrimaryKeyFactory;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test the composite class insertion capability.
 *
 * @author Robert J. Muller
 */
public class InsertParentTest extends ConnectionTest {
  private static final String QUERY_PARENT = "SELECT col1 FROM Parent WHERE parent_id = ?";
  private static final String QUERY_CHILDREN =
    "SELECT child_number, col1 FROM Child WHERE parent_id = ? ORDER BY child_number";
  private static final String PARENT_KEY_NAME = "parent_id";
  private static final String CHILD_SUB_KEY_NAME = "child_number";
  private static final String COL1_VALUE = "string";
  private static final String CLASS_NAME = "com.poesys.db.test.Parent";

  /**
   * Test the basic use case for a composite object: insert a parent and three
   * children with no errors.
   *
   * @throws IOException  when can't get a property
   * @throws SQLException when can't get a connection
   */
  @Test
  public void testInsert() throws IOException, SQLException {
    Connection conn;
    try {
      conn = getConnection();
    } catch (SQLException e) {
      throw new DbErrorException("Connect failed: " + e.getMessage(), e);
    }

    // Create the insert command (class under test) for the parent.
    Insert<Parent> cut = new Insert<>(new InsertSqlParent(), getSubsystem());

    // Create the GUID primary key for the parent.
    GuidPrimaryKey key = PrimaryKeyFactory.createGuidKey(PARENT_KEY_NAME, CLASS_NAME);

    // Create the parent DTO with the key and the empty setters list.
    Parent dto = new Parent(key, COL1_VALUE);

    // Create three children in a list and set the children into the parent.
    List<Child> children = new ArrayList<>();
    NaturalPrimaryKey subKey1 =
      PrimaryKeyFactory.createSingleNumberKey(CHILD_SUB_KEY_NAME, new BigInteger("1"), CLASS_NAME);
    CompositePrimaryKey key1 = new CompositePrimaryKey(key, subKey1, CLASS_NAME);
    children.add(new Child(key1, new BigInteger("1"), COL1_VALUE));
    NaturalPrimaryKey subKey2 =
      PrimaryKeyFactory.createSingleNumberKey(CHILD_SUB_KEY_NAME, new BigInteger("2"), CLASS_NAME);
    CompositePrimaryKey key2 = new CompositePrimaryKey(key, subKey2, CLASS_NAME);
    children.add(new Child(key2, new BigInteger("2"), COL1_VALUE));
    NaturalPrimaryKey subKey3 =
      PrimaryKeyFactory.createSingleNumberKey(CHILD_SUB_KEY_NAME, new BigInteger("3"), CLASS_NAME);
    CompositePrimaryKey key3 = new CompositePrimaryKey(key, subKey3, CLASS_NAME);
    children.add(new Child(key3, new BigInteger("3"), COL1_VALUE));
    dto.setChildren(children);

    Statement stmt = null;
    PreparedStatement pStmt = null;
    try {
      // Delete any rows in the Parent and Child tables.
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM Child");
      stmt.close();
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM Parent");
      stmt.close();
      stmt = null;

      conn.commit();

      // Insert the test row.
      cut.insert(dto);

      // Test the flags.
      assertTrue("Inserted parent does not have status EXISTING",
                 dto.getStatus() == IDbDto.Status.EXISTING);

      // Query the parent row.
      pStmt = conn.prepareStatement(QUERY_PARENT);
      key.setParams(pStmt, 1);
      ResultSet rs = pStmt.executeQuery();
      String queriedCol1 = null;
      if (rs.next()) {
        queriedCol1 = rs.getString("col1");
      }
      pStmt.close();
      pStmt = null;
      assertTrue(queriedCol1 != null);
      assertTrue(COL1_VALUE.equals(queriedCol1));

      conn.commit();

      // Query the children of the parent.
      pStmt = conn.prepareStatement(QUERY_CHILDREN);
      key.setParams(pStmt, 1);
      rs = pStmt.executeQuery();
      int count = 0;
      while (rs.next()) {
        count++;
        BigInteger dbChildNumber = rs.getBigDecimal("child_number").toBigInteger();
        String dbCol1 = rs.getString("col1");
        BigInteger shouldBeChildNumber = new BigInteger(Integer.toString(count));
        assertTrue(shouldBeChildNumber.equals(dbChildNumber));
        assertTrue(COL1_VALUE.equals(dbCol1));
      }
      assertTrue("count=" + count, count == 3);
      conn.commit();
    } catch (SQLException e) {
      fail("insert method failed: " + e.getMessage());
    }
    finally {
      if (stmt != null) {
        stmt.close();
      }
      if (pStmt != null) {
        pStmt.close();
      }
      if (conn != null) {
        conn.close();
      }
    }
  }

  /**
   * Test composite insert where one child out of three is not new.
   *
   * @throws IOException  when can't get a property
   * @throws SQLException when can't get a connection
   */
  @Test
  public void testInsertNotNew() throws IOException, SQLException {
    Connection conn;
    try {
      conn = getConnection();
    } catch (SQLException e) {
      throw new DbErrorException("Connect failed: " + e.getMessage(), e);
    }

    // Create the insert command (class under test) for the parent.
    Insert<Parent> cut = new Insert<>(new InsertSqlParent(), getSubsystem());

    // Create the GUID primary key for the parent.
    GuidPrimaryKey key = PrimaryKeyFactory.createGuidKey(PARENT_KEY_NAME, CLASS_NAME);

    // Create the parent DTO with the key and the empty setters list.
    Parent dto = new Parent(key, COL1_VALUE);

    // Create three children in a list and set the children into the parent.
    List<Child> children = new ArrayList<>();
    NaturalPrimaryKey subKey1 =
      PrimaryKeyFactory.createSingleNumberKey(CHILD_SUB_KEY_NAME, new BigInteger("1"), CLASS_NAME);
    CompositePrimaryKey key1 = new CompositePrimaryKey(key, subKey1, CLASS_NAME);
    children.add(new Child(key1, new BigInteger("1"), COL1_VALUE));
    NaturalPrimaryKey subKey2 =
      PrimaryKeyFactory.createSingleNumberKey(CHILD_SUB_KEY_NAME, new BigInteger("2"), CLASS_NAME);
    CompositePrimaryKey key2 = new CompositePrimaryKey(key, subKey2, CLASS_NAME);
    children.add(new Child(key2, new BigInteger("2"), COL1_VALUE));
    NaturalPrimaryKey subKey3 =
      PrimaryKeyFactory.createSingleNumberKey(CHILD_SUB_KEY_NAME, new BigInteger("3"), CLASS_NAME);
    CompositePrimaryKey key3 = new CompositePrimaryKey(key, subKey3, CLASS_NAME);
    Child oldChild = new Child(key3, new BigInteger("3"), COL1_VALUE);
    children.add(oldChild);
    // Mark the child existing to exclude it from the insert.
    oldChild.setExisting();
    dto.setChildren(children);

    Statement stmt = null;
    PreparedStatement pStmt = null;
    try {
      // Delete any rows in the Parent and Child tables.
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM Child");
      stmt.close();
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM Parent");
      stmt.close();
      stmt = null;

      conn.commit();

      // Insert the test row.
      cut.insert(dto);

      // Test the flags.
      assertTrue("inserted parent not EXISTING", dto.getStatus() == IDbDto.Status.EXISTING);

      // Query the parent row.
      pStmt = conn.prepareStatement(QUERY_PARENT);
      key.setParams(pStmt, 1);
      ResultSet rs = pStmt.executeQuery();
      String queriedCol1 = null;
      if (rs.next()) {
        queriedCol1 = rs.getString("col1");
      }
      pStmt.close();
      pStmt = null;
      assertTrue(queriedCol1 != null);
      assertTrue(COL1_VALUE.equals(queriedCol1));

      conn.commit();

      // Query the children of the parent. Should be only 2.
      pStmt = conn.prepareStatement(QUERY_CHILDREN);
      key.setParams(pStmt, 1);
      rs = pStmt.executeQuery();
      int count = 0;
      while (rs.next()) {
        count++;
        BigInteger dbChildNumber = rs.getBigDecimal("child_number").toBigInteger();
        String dbCol1 = rs.getString("col1");
        BigInteger shouldBeChildNumber = new BigInteger(Integer.toString(count));
        assertTrue(shouldBeChildNumber.equals(dbChildNumber));
        assertTrue(COL1_VALUE.equals(dbCol1));
      }
      assertTrue("new count=" + count, count == 2);
      conn.commit();
    } catch (SQLException e) {
      fail("insert method failed: " + e.getMessage());
    }
    finally {
      if (stmt != null) {
        stmt.close();
      }
      if (pStmt != null) {
        pStmt.close();
      }
      if (conn != null) {
        conn.close();
      }
    }
  }

  /**
   * Test the use case for a composite object: insert a parent and no children.
   *
   * @throws IOException  when can't get a property
   * @throws SQLException when can't get a connection
   */
  @Test
  public void testInsertNoChildren() throws IOException, SQLException {
    Connection conn;
    try {
      conn = getConnection();
    } catch (SQLException e) {
      throw new DbErrorException("Connect failed: " + e.getMessage(), e);
    }

    // Create the insert command (class under test) for the parent.
    Insert<Parent> cut = new Insert<>(new InsertSqlParent(), getSubsystem());

    // Create the GUID primary key for the parent.
    GuidPrimaryKey key = PrimaryKeyFactory.createGuidKey(PARENT_KEY_NAME, CLASS_NAME);

    // Create the parent DTO with the key and the empty setters list.
    Parent dto = new Parent(key, COL1_VALUE);

    Statement stmt = null;
    PreparedStatement pStmt = null;
    try {
      // Delete any rows in the Parent and Child tables.
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM Child");
      stmt.close();
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM Parent");
      stmt.close();
      stmt = null;

      conn.commit();

      // Insert the test row.
      cut.insert(dto);

      // Test the flags.
      assertTrue("inserted parent not EXISTING", dto.getStatus() == IDbDto.Status.EXISTING);

      // Query the parent row.
      pStmt = conn.prepareStatement(QUERY_PARENT);
      key.setParams(pStmt, 1);
      ResultSet rs = pStmt.executeQuery();
      String queriedCol1 = null;
      if (rs.next()) {
        queriedCol1 = rs.getString("col1");
      }
      pStmt.close();
      pStmt = null;
      assertTrue(queriedCol1 != null);
      assertTrue(COL1_VALUE.equals(queriedCol1));

      conn.commit();

      // Query the children of the parent.
      pStmt = conn.prepareStatement(QUERY_CHILDREN);
      key.setParams(pStmt, 1);
      rs = pStmt.executeQuery();
      int count = 0;
      while (rs.next()) {
        count++;
        BigInteger dbChildNumber = rs.getBigDecimal("child_number").toBigInteger();
        String dbCol1 = rs.getString("col1");
        BigInteger shouldBeChildNumber = new BigInteger(Integer.toString(count));
        assertTrue(shouldBeChildNumber.equals(dbChildNumber));
        assertTrue(COL1_VALUE.equals(dbCol1));
      }
      assertTrue("count=" + count, count == 0);
      conn.commit();
    } catch (SQLException e) {
      fail("insert method failed: " + e.getMessage());
    }
    finally {
      if (stmt != null) {
        stmt.close();
      }
      if (pStmt != null) {
        pStmt.close();
      }
      if (conn != null) {
        conn.close();
      }
    }
  }
}
