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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.poesys.db.connection.IConnectionFactory.DBMS;
import com.poesys.db.dao.ConnectionTest;
import com.poesys.db.dto.TestMultipleParams;


/**
 * <p>
 * Test the DeleteWithParameters class.
 * </p>
 * <p>
 * This test uses the TestMultiple test table:
 * </p>
 * 
 * <pre>
 * <code>
 * CREATE TABLE TestMultiple (
 *   pkey int(12) PRIMARY KEY,
 *   col1 varchar(50) NOT NULL,
 *   colType varchar(10) NOT NULL
 * ) TYPE=InnoDb DEFAULT CHARSET=utf8;
 * </code>
 * </pre>
 * 
 * @author Bob Muller (muller@computer.org)
 */
public class DeleteWithParametersTest extends ConnectionTest {
  /** SQL statement that inserts a test row into TestMultiple */
  private static final String INSERT =
    "INSERT INTO TestMultiple (pkey, col1, colType) VALUES (?, ?, ?)";
  private static final String NEW = "new";

  /**
   * Test method for
   * {@link com.poesys.db.dao.update.UpdateWithParameters#update(java.sql.Connection, com.poesys.db.dto.IDbDto)}
   * .
   * 
   * @throws IOException when can't get a property
   * @throws SQLException when can't get a connection
   */
  public void testDelete() throws IOException, SQLException {
    Connection conn;
    try {
      conn = getConnection(DBMS.MYSQL, "com.poesys.db.poesystest.mysql");
    } catch (SQLException e) {
      throw new RuntimeException("Connect failed: " + e.getMessage(), e);
    }

    // Clear the test table.
    Statement delStmt = conn.createStatement();
    delStmt.execute("DELETE FROM TestMultiple");
    delStmt.close();

    // Insert test rows that create a set of multiple rows identified by a
    // single value of the colType column, 'b'.
    PreparedStatement stmt = conn.prepareStatement(INSERT);
    for (long i = 1; i < 10; i++) {
      // insert key, col1, colType a
      stmt.setLong(1, i);
      stmt.setString(2, "Col Value " + i);
      stmt.setString(3, "a");
      stmt.execute();
    }

    for (long i = 10; i < 15; i++) {
      // insert key, col1, colType b
      stmt.setLong(1, i);
      stmt.setString(2, "Col Value " + i);
      stmt.setString(3, "b");
      stmt.execute();
    }

    stmt.close();

    // Delete the "b" test rows.
    DeleteWithParameters<TestMultipleParams, TestMultipleParams> deleter =
      new DeleteWithParameters<TestMultipleParams, TestMultipleParams>(new DeleteSqlTestMultiple());
    TestMultipleParams parameters = new TestMultipleParams(NEW, "b");
    deleter.delete(conn, parameters);

    // Query the five "b" rows to make sure they're gone.
    Statement query = conn.createStatement();
    ResultSet rs =
      query.executeQuery("SELECT col1 FROM TestMultiple WHERE colType = 'b'");
    while (rs.next()) {
      fail("Found a 'b' row when they're supposed to have been deleted");
    }
    query.close();
  }
}