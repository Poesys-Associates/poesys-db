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
package com.poesys.db.dao.delete;


import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.poesys.db.BatchException;
import com.poesys.db.dao.ConnectionTest;
import com.poesys.db.dao.insert.Insert;
import com.poesys.db.dao.insert.InsertSqlTestNatural;
import com.poesys.db.dto.TestNatural;


/**
 * Test the delete process for a simple object.
 * 
 * @author Robert J. Muller
 */
public class DeleteTestNaturalTest extends ConnectionTest {
  private static final String QUERY =
    "SELECT col1 FROM TestNatural WHERE key1 = 'A' and key2 = 'B'";

  /**
   * Test the delete method.
   * 
   * @throws IOException when can't get a property
   * @throws SQLException when can't get a connection
   * @throws BatchException when a problem happens during processing
   */
  public void testDelete() throws IOException, SQLException, BatchException {
    Connection conn;
    try {
      conn = getConnection();
    } catch (SQLException e) {
      throw new RuntimeException("Connect failed: " + e.getMessage(), e);
    }

    // Create an Inserter to add the row to update
    Insert<TestNatural> inserter =
      new Insert<TestNatural>(new InsertSqlTestNatural());

    // Create the DTO.
    BigDecimal col1 = new BigDecimal("1234.5678");
    TestNatural dto = new TestNatural("A", "B", col1);

    // Create the Deleter.
    DeleteByKey<TestNatural> deleter =
      new DeleteByKey<TestNatural>(new DeleteSqlTestNatural());

    Statement stmt = null;
    try {
      // Delete any rows in the TestNatural table.
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM TestNatural");
      stmt.close();

      // Insert the row to delete with the DAO class under test.
      inserter.insert(conn, dto);

      // Query the row.
      stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(QUERY);
      if (!rs.next()) {
        fail("Failed to insert row to delete");
      }
      stmt.close();

      // Delete the test row.
      dto.delete();
      deleter.delete(conn, dto);

      // Query the row again for comparison.
      stmt = conn.createStatement();
      rs = stmt.executeQuery(QUERY);
      if (rs.next()) {
        fail("Found row ostensibly deleted");
      } else {
        assertTrue(true);
      }
    } catch (SQLException e) {
      fail("delete test failed: " + e.getMessage());
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
