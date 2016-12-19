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
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import com.poesys.db.BatchException;
import com.poesys.db.DbErrorException;
import com.poesys.db.dao.ConnectionTest;
import com.poesys.db.dto.TestNatural;


/**
 * Test the insert process for a collection.
 * 
 * @author Robert J. Muller
 */
public class InsertCollectionTestNaturalTest extends ConnectionTest {
  private static final String QUERY =
    "SELECT col1 FROM TestNatural WHERE key1 = ? and key2 = ?";
  private static final int OBJECT_COUNT = 50;

  /**
   * Test the insert() method.
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
      throw new DbErrorException("Connect failed: " + e.getMessage(), e);
    }
    InsertCollection<TestNatural> cut =
      new InsertCollection<TestNatural>(new InsertSqlTestNatural(), getSubsystem());
    Collection<TestNatural> dtos = new CopyOnWriteArrayList<TestNatural>();
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

      conn.commit();
      
      // Insert the test collection.
      stmt = conn.createStatement();
      cut.insert(dtos);

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
        assertTrue(queriedCol1 != null);
        // Must use compareTo here, not equals, because of precision difference
        assertTrue(col1.compareTo(queriedCol1) == 0);
      }
      conn.commit();
    } catch (SQLException e) {
      fail("insert collection method failed: " + e.getMessage());
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
