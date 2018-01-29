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

import com.poesys.db.DbErrorException;
import com.poesys.db.dao.ConnectionTest;
import com.poesys.db.dto.TestMultipleParams;
import org.junit.Test;

import static org.junit.Assert.fail;

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
 *   pKey int(12) PRIMARY KEY,
 *   col1 varchar(50) NOT NULL,
 *   colType varchar(10) NOT NULL
 * ) TYPE=InnoDb DEFAULT CHARSET=utf8;
 * </code>
 * </pre>
 * 
 * @author Robert J. Muller
 */
public class DeleteWithParametersTest extends ConnectionTest {
  /** SQL statement to clean out test table before doing test */
  private static final String DELETE_FROM_TEST = "DELETE FROM TestMultipleDelete";
  /** SQL statement that inserts a test row into TestMultiple */
  private static final String INSERT =
    "INSERT INTO TestMultipleDelete (pKey, col1, colType) VALUES (?, ?, ?)";
  private static final String NEW = "new";

  /**
   * Test method for
   * {@link com.poesys.db.dao.update.UpdateWithParameters#update(com.poesys.db.dto.IDbDto)}
   * .
   * 
   * @throws IOException when can't get a property
   * @throws SQLException when can't get a connection
   */
  @Test
  public void testDelete() throws IOException, SQLException {
    Connection conn = null;
    try {
      conn = getConnection();

      // Clear the test table. Bug: seems to encounter weird metadata locking.
      // Delete the "b" test rows.
      DeleteWithParameters<TestMultipleParams, TestMultipleParams> clearer =
        new DeleteWithParameters<>(new DeleteSqlTestMultiple(), getSubsystem());
      clearer.delete(new TestMultipleParams(NEW, "b"));

      // Delete any rows in the TestNatural table.
      Statement stmt;
      stmt = conn.createStatement();
      stmt.executeUpdate(DELETE_FROM_TEST);
      stmt.close();

      conn.commit();

      // Insert test rows that create a set of multiple rows identified by a
      // single value of the colType column, 'b'.
      PreparedStatement insertStmt = conn.prepareStatement(INSERT);
      for (long i = 1; i < 10; i++) {
        // insert key, col1, colType a
        insertStmt.setLong(1, i);
        insertStmt.setString(2, "Col Value " + i);
        insertStmt.setString(3, "a");
        insertStmt.execute();
      }

      for (long i = 10; i < 15; i++) {
        // insert key, col1, colType b
        insertStmt.setLong(1, i);
        insertStmt.setString(2, "Col Value " + i);
        insertStmt.setString(3, "b");
        insertStmt.execute();
      }

      insertStmt.close();

      conn.commit();

      // Delete the "b" test rows.
      DeleteWithParameters<TestMultipleParams, TestMultipleParams> deleter =
        new DeleteWithParameters<>(new DeleteSqlTestMultiple(), getSubsystem());
      TestMultipleParams parameters = new TestMultipleParams(NEW, "b");
      deleter.delete(parameters);

      // Query the five "b" rows to make sure they're gone.
      Statement query = conn.createStatement();
      ResultSet rs =
        query.executeQuery("SELECT col1 FROM TestMultipleDelete WHERE colType = 'b'");
      if (rs.next()) {
        fail("Found a 'b' row when they're supposed to have been deleted");
      }
      query.close();
      conn.commit();
    } catch (SQLException e) {
      throw new DbErrorException("Connect failed: " + e.getMessage(), e);
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }
}
