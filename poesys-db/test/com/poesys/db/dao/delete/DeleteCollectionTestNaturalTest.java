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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.poesys.db.BatchException;
import com.poesys.db.dao.ConnectionTest;
import com.poesys.db.dao.insert.InsertBatch;
import com.poesys.db.dao.insert.InsertSqlTestNatural;
import com.poesys.db.dto.TestNatural;


/**
 * Test the deletion of a collection of simple objects.
 * 
 * @author Robert J. Muller
 */
public class DeleteCollectionTestNaturalTest extends ConnectionTest {
  private static final String QUERY =
    "SELECT col1 FROM TestNatural WHERE key1 = ? and key2 = ?";
  private static final int OBJECT_COUNT = 50;
  private static final int BATCH_SIZE = OBJECT_COUNT / 3;

  /**
   * Test a successful update of a batch.
   * 
   * @throws IOException when can't get property
   * @throws SQLException when can't get connection
   * @throws BatchException when a problem occurs during processing
   */
  public void testDelete() throws IOException, SQLException, BatchException {
    Connection conn;
    try {
      conn = getConnection();
    } catch (SQLException e) {
      throw new RuntimeException("Connect failed: " + e.getMessage(), e);
    }
    InsertBatch<TestNatural> inserter =
      new InsertBatch<TestNatural>(new InsertSqlTestNatural());
    List<TestNatural> dtos = new CopyOnWriteArrayList<TestNatural>();
    BigDecimal col1 = new BigDecimal("1234.5678");

    for (int i = 0; i < OBJECT_COUNT; i++) {
      Integer keyValue = new Integer(i);

      // Create the DTO.
      dtos.add(new TestNatural(keyValue.toString(), keyValue.toString(), col1));
    }

    Statement stmt = null;
    PreparedStatement query = null;
    try {
      // Delete any rows in the TestNatural table.
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM TestNatural");
      stmt.close();

      // Insert the test batch.
      stmt = conn.createStatement();
      inserter.insert(conn, dtos, BATCH_SIZE);

      // Delete the batch.
      for (TestNatural dto : dtos) {
        dto.delete();
      }

      DeleteCollectionByKey<TestNatural> deleter =
        new DeleteCollectionByKey<TestNatural>(new DeleteSqlTestNatural());
      deleter.delete(conn, dtos);

      query = conn.prepareStatement(QUERY);

      // Loop through the DTOs to query and test them.
      for (TestNatural dto : dtos) {
        // Set the key values into the query as arguments.
        dto.getPrimaryKey().setParams(query, 1);

        // Query the row.
        ResultSet rs = query.executeQuery();
        if (rs.next()) {
          fail("Found supposedly deleted TestNatural object from batch");
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
      fail("delete batch process failed: " + e.getMessage());
    } finally {
      if (stmt != null) {
        stmt.close();
      }
      if (conn != null) {
        conn.commit();
        conn.close();
      }
    }
  }

  /**
   * Test a batch update with a null input.
   * 
   * @throws IOException when can't get a property
   * @throws SQLException when can't get a connection
   * @throws BatchException when a problem occurs during processing
   */
  public void testInsertNull() throws IOException, SQLException, BatchException {
    Connection conn;
    try {
      conn = getConnection();
    } catch (SQLException e) {
      throw new RuntimeException("Connect failed: " + e.getMessage(), e);
    }
    DeleteCollectionByKey<TestNatural> cut =
      new DeleteCollectionByKey<TestNatural>(new DeleteSqlTestNatural());
    Collection<TestNatural> dtos = null;
    Statement stmt = null;

    try {
      // Insert the test batch, which is null.
      stmt = conn.createStatement();
      cut.delete(conn, dtos);
    } catch (SQLException e) {
      fail("delete batch process with null input failed: " + e.getMessage());
    } finally {
      if (stmt != null) {
        stmt.close();
      }
      if (conn != null) {
        conn.commit();
        conn.close();
      }
    }
  }
}
