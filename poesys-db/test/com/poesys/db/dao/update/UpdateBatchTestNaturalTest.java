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
import com.poesys.db.dao.insert.InsertBatch;
import com.poesys.db.dao.insert.InsertSqlTestNatural;
import com.poesys.db.dto.TestNatural;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test the update process for a collection of natural-primary-key objects using
 * batching.
 *
 * @author Robert J. Muller
 */
public class UpdateBatchTestNaturalTest extends ConnectionTest {
  private static final String QUERY = "SELECT col1 FROM TestNatural WHERE key1 = ? and key2 = ?";
  private static final int OBJECT_COUNT = 50;
  private static final int BATCH_SIZE = OBJECT_COUNT / 3;

  /**
   * Test a successful update of a batch.
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
    InsertBatch<TestNatural> inserter =
      new InsertBatch<>(new InsertSqlTestNatural(), getSubsystem());
    List<TestNatural> dtos = new CopyOnWriteArrayList<>();
    BigDecimal col1 = new BigDecimal("1234.5678");
    BigDecimal col1New = new BigDecimal("5678.5678");

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
      inserter.insert(dtos, BATCH_SIZE);

      // Update the col1 values and batch the update.
      for (TestNatural dto : dtos) {
        dto.setCol1(col1New);
      }

      UpdateBatchByKey<TestNatural> updater =
        new UpdateBatchByKey<>(new UpdateSqlTestNatural(), getSubsystem());
      updater.update(dtos, BATCH_SIZE);

      query = conn.prepareStatement(QUERY);

      // Loop through the DTOs to query and test them.
      for (TestNatural dto : dtos) {
        // Set the key values into the query as arguments.
        dto.getPrimaryKey().setParams(query, 1);

        // Query the row.
        ResultSet rs = query.executeQuery();
        BigDecimal queriedCol1 = null;
        if (rs.next()) {
          queriedCol1 = rs.getBigDecimal("col1");
        }
        assertTrue(queriedCol1 != null);
        // Must use compareTo here, not equals, because of precision difference
        assertTrue(col1New + " is not " + queriedCol1, col1New.compareTo(queriedCol1) == 0);
      }
      conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      fail("update batch process failed: " + e.getMessage());
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

  /**
   * Test a batch update with a null input.
   */
  @Test
  public void testInsertNull() {
    UpdateBatchByKey<TestNatural> cut =
      new UpdateBatchByKey<>(new UpdateSqlTestNatural(), getSubsystem());
    // Insert the test batch, which is null.
    cut.update(null, BATCH_SIZE);
  }
}
