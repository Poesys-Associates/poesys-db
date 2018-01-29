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
import com.poesys.db.dto.TestNatural;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test the insert process for a collection using batching.
 * 
 * @author Robert J. Muller
 */
public class InsertBatchTestNaturalTest extends ConnectionTest {
  private static final String QUERY =
    "SELECT col1 FROM TestNatural WHERE key1 = ? and key2 = ?";
  private static final int OBJECT_COUNT = 50;
  private static final int BATCH_SIZE = OBJECT_COUNT / 3;

  /**
   * Test a successful insert of a batch.
   * 
   * @throws IOException when can't get a property
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
    InsertBatch<TestNatural> cut = new InsertBatch<>(new InsertSqlTestNatural(), getSubsystem());
    List<TestNatural> dtos = new ArrayList<>();
    BigDecimal col1 = new BigDecimal("1234.5678");

    for (int i = 0; i < OBJECT_COUNT; i++) {
      Integer keyValue = i;

      // Create the DTO.
      dtos.add(new TestNatural(keyValue.toString(), keyValue.toString(), col1));
    }

    Statement stmt = null;
    PreparedStatement query;
    try {
      // Delete any rows in the TestNatural table.
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM TestNatural");
      stmt.close();
      
      conn.commit();

      // Insert the test batch.
      stmt = conn.createStatement();
      cut.insert(dtos, BATCH_SIZE);

      query = conn.prepareStatement(QUERY);

      // Loop through the DTOs to query them and test.
      for (TestNatural dto : dtos) {
        // Set the key values into the query as arguments.
        dto.getPrimaryKey().setParams(query, 1);

        // Query the row.
        ResultSet rs = query.executeQuery();
        BigDecimal queriedCol1 = null;
        if (rs.next()) {
          queriedCol1 = rs.getBigDecimal("col1");
        }
        assertTrue("Couldn't query object", queriedCol1 != null);
        // Must use compareTo here, not equals, because of precision difference
        assertTrue("Wrong object found", col1.compareTo(queriedCol1) == 0);
      }
      conn.commit();
    } catch (SQLException e) {
      fail("insert batch method failed: " + e.getMessage());
    } finally {
      if (stmt != null) {
        stmt.close();
      }
      if (conn != null) {
        conn.close();
      }
    }
  }

  /**
   * Test a batch insert with primary key errors.
   * 
   * @throws IOException when can't get a property
   * @throws SQLException when can't get a connection
   */
  @Test
  public void testInsertError() throws IOException, SQLException {
    Connection conn;
    try {
      conn = getConnection();
    } catch (SQLException e) {
      throw new DbErrorException("Connect failed: " + e.getMessage(), e);
    }
    InsertBatch<TestNatural> cut = new InsertBatch<>(new InsertSqlTestNatural(), getSubsystem());
    List<TestNatural> errorDtos = new ArrayList<>();
    Collection<TestNatural> goodDtos = new ArrayList<>();
    BigDecimal col1 = new BigDecimal("1234.5678");

    for (int i = 0; i < OBJECT_COUNT; i++) {
      // Create the primary key.
      String keyValue;
      if (i > 0 && i % 3 == 0) {
        // Put an invalid string (TOO LONG) key in for a few rows.
        keyValue = "a b c d" + (new Integer(i)).toString();
      } else {
        keyValue = (new Integer(i)).toString();
      }

      // Create the DTO.
      errorDtos.add(new TestNatural(keyValue, keyValue, col1));
    }

    for (int i = 0; i < OBJECT_COUNT; i++) {
      Integer keyValue = i;

      // Create the DTO.
      goodDtos.add(new TestNatural(keyValue.toString(),
                                   keyValue.toString(),
                                   col1));
    }

    Statement stmt = null;
    PreparedStatement query;
    try {
      // Delete any rows in the TestNatural table.
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM TestNatural");
      stmt.close();
      
      conn.commit();

      // Insert the test batch.
      stmt = conn.createStatement();
      try {
        cut.insert(errorDtos, BATCH_SIZE);
        fail();
      } catch (Throwable e) {
        assertTrue(true);
        System.out.println(e.getMessage());
      }

      query = conn.prepareStatement(QUERY);

      // Loop through the good list of DTOs to query them and test.
      int i = 0;
      for (TestNatural dto : goodDtos) {
        // Set the key values into the query as arguments.
        dto.getPrimaryKey().setParams(query, 1);

        // Query the row.
        ResultSet rs = query.executeQuery();
        BigDecimal queriedCol1;
        if (rs.next()) {
          // Got the object, compare the value.
          queriedCol1 = rs.getBigDecimal("col1");
          assertTrue("Couldn't query object from database", queriedCol1 != null);
          // Must use compareTo here, not equals, because of precision
          // difference
          assertTrue("Wrong object", col1.compareTo(queriedCol1) == 0);
        } else {
          // No object in DB, make sure it's one of the "bad" ones.
          assertTrue("Bad object found", i % 3 == 0);
        }
        i++;
      }
      conn.commit();
    } catch (SQLException e) {
      fail("insert batch method failed: " + e.getMessage());
    } finally {
      if (stmt != null) {
        stmt.close();
      }
      if (conn != null) {
        conn.close();
      }
    }
  }

  /**
   * Test a batch insert with a null input.
   * 
   * @throws IOException when can't get a property
   * @throws SQLException when can't get a connection
   */
  @Test
  public void testInsertNull() throws IOException, SQLException {
    Connection conn;
    try {
      conn = getConnection();
    } catch (SQLException e) {
      throw new DbErrorException("Connect failed: " + e.getMessage(), e);
    }
    InsertBatch<TestNatural> cut = new InsertBatch<>(new InsertSqlTestNatural(), getSubsystem());
    Statement stmt = null;

    try {
      // Delete any rows in the TestNatural table.
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM TestNatural");
      stmt.close();
      
      conn.commit();

      // Insert the test batch, which is null.
      cut.insert(null, BATCH_SIZE);
      conn.commit();
    } catch (SQLException e) {
      fail("insert batch method with null input failed: " + e.getMessage());
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
