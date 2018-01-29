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
package com.poesys.db.dao.update;

import com.poesys.db.DbErrorException;
import com.poesys.db.dao.ConnectionTest;
import com.poesys.db.dao.insert.Insert;
import com.poesys.db.dao.insert.InsertSqlTestNatural;
import com.poesys.db.dto.TestNatural;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test the update process for a natural-primary-key object.
 *
 * @author Robert J. Muller
 */
public class UpdateTestNaturalTest extends ConnectionTest {
  private static final String QUERY =
    "SELECT col1 FROM TestNatural WHERE key1 = 'A' and key2 = 'B'";

  /**
   * Test the update() method.
   *
   * @throws IOException  when can't get a property
   * @throws SQLException when can't get a connection
   */
  @Test
  public void testUpdate() throws IOException, SQLException {
    Connection conn;
    try {
      conn = getConnection();
    } catch (SQLException e) {
      throw new DbErrorException("Connect failed: " + e.getMessage(), e);
    }

    // Create an Inserter to add the row to update
    Insert<TestNatural> inserter = new Insert<>(new InsertSqlTestNatural(), getSubsystem());

    // Create the DTO.
    BigDecimal col1 = new BigDecimal("1234.5678");
    TestNatural dto = new TestNatural("A", "B", col1);

    // Create the Updater.
    UpdateByKey<TestNatural> updater =
      new UpdateByKey<>(new UpdateSqlTestNatural(), getSubsystem());

    Statement stmt = null;
    try {
      // Delete any rows in the TestNatural table.
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM TestNatural");
      stmt.close();

      conn.commit();

      // Insert the row to update
      inserter.insert(dto);

      // Query the row.
      stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(QUERY);
      BigDecimal queriedCol1 = null;
      if (rs.next()) {
        queriedCol1 = rs.getBigDecimal("col1");
      }
      stmt.close();

      conn.commit();

      // Change col1.
      BigDecimal col1Changed = new BigDecimal("1234.5678");
      dto.setCol1(col1Changed);

      // Update the test row.
      updater.update(dto);

      // Query the row again for comparison.
      stmt = conn.createStatement();
      rs = stmt.executeQuery(QUERY);
      BigDecimal queriedCol2 = null;
      if (rs.next()) {
        queriedCol2 = rs.getBigDecimal("col1");
      }
      assertTrue("col1 is null", queriedCol1 != null);
      assertTrue("col2 is null", queriedCol2 != null);
      // Must use compareTo here, not equals, because of precision difference
      // col2 should be the changed value
      assertTrue(queriedCol2.compareTo(col1Changed) == 0);
      conn.commit();
    } catch (SQLException e) {
      fail("update method failed: " + e.getMessage());
    }
    finally {
      if (stmt != null) {
        stmt.close();
      }
      if (conn != null) {
        conn.close();
      }
    }
  }
}
