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
import com.poesys.db.DbErrorException;
import com.poesys.db.dao.ConnectionTest;
import com.poesys.db.dto.TestSequence;
import com.poesys.db.pk.IPrimaryKey;
import com.poesys.db.pk.PrimaryKeyFactory;


/**
 * Test the insert of an object with a sequence primary key.
 * 
 * @author Robert J. Muller
 */
public class InsertTestSequenceTest extends ConnectionTest {
  private static final String QUERY =
    "SELECT col1 FROM TestSequence WHERE pkey = ?";
  private static final String CLASS_NAME = "com.poesys.db.test.TestSequence";

  /**
   * Test the insert method using a MySQL sequence.
   * 
   * @throws IOException when can't get a property
   * @throws SQLException when can't get a connection
   * @throws BatchException when a problem happens during processing
   */
  public void testInsertMySql() throws IOException, SQLException,
      BatchException {
    Connection conn;

    // Set the sequence to start with 1.
    Statement stmt = null;

    try {
      conn = getConnection();
    } catch (SQLException e) {
      throw new DbErrorException("Connect failed: " + e.getMessage(), e);
    }

    try {
      stmt = conn.createStatement();
      stmt.execute("UPDATE mysql_sequence set value = 0 where name = 'test'");
    } catch (SQLException e1) {
      fail("Problem updating the sequence");
    } finally {
      if (stmt != null) {
        stmt.close();
      }
    }
    
    conn.commit();

    Insert<TestSequence> cut =
      new Insert<TestSequence>(new InsertSqlTestSequence(), getSubsystem());

    // Create the primary key.
    IPrimaryKey key =
      PrimaryKeyFactory.createMySqlSequenceKey("test", "pkey", CLASS_NAME, getSubsystem());

    // Create the DTO.
    String col1 = "test";
    TestSequence dto = new TestSequence(key, col1);

    stmt = null;
    PreparedStatement query = null;

    try {
      // Delete from the TestSequence table.
      Statement del = conn.createStatement();
      del.execute("DELETE FROM TestSequence");
      
      conn.commit();

      // Insert the test row.
      cut.insert(dto);

      // Set the key value into the query as an argument.
      query = conn.prepareStatement(QUERY);
      key.setParams(query, 1);

      // Query the row.
      ResultSet rs = query.executeQuery();
      String queriedCol1 = null;
      if (rs.next()) {
        queriedCol1 = rs.getString("col1");
      }
      assertTrue(queriedCol1 != null);
      assertTrue(col1.equals(queriedCol1));
      conn.commit();
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
