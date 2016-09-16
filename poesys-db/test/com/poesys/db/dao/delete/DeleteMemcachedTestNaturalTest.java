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

import org.apache.log4j.Logger;

import com.poesys.db.BatchException;
import com.poesys.db.dao.ConnectionTest;
import com.poesys.db.dao.insert.InsertMemcached;
import com.poesys.db.dao.insert.InsertSqlTestNatural;
import com.poesys.db.dao.query.IKeyQuerySql;
import com.poesys.db.dao.query.QueryByKey;
import com.poesys.db.dao.query.QueryMemcachedByKey;
import com.poesys.db.dao.query.TestNaturalKeyQuerySql;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.TestNatural;
import com.poesys.db.pk.IPrimaryKey;


/**
 * Test the memcached delete process for a simple object. Debugging TAIR-2733.
 * 
 * @author Robert J. Muller
 */
public class DeleteMemcachedTestNaturalTest extends ConnectionTest {
  private static final Logger logger = Logger.getLogger(DeleteMemcachedTestNaturalTest.class);
  
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
    InsertMemcached<TestNatural> inserter =
      new InsertMemcached<TestNatural>(new InsertSqlTestNatural(),
                                       getSubsystem(),
                                       Integer.MAX_VALUE);

    // Create the DTO.
    BigDecimal col1 = new BigDecimal("1234.5678");
    TestNatural dto = new TestNatural("A", "B", col1);
    IPrimaryKey key = dto.getPrimaryKey();

    // Create the Deleter.
    DeleteMemcachedByKey<TestNatural> deleter =
      new DeleteMemcachedByKey<TestNatural>(new DeleteSqlTestNatural(),
                                            getSubsystem());

    Statement stmt = null;
    try {
      // Delete any rows in the TestNatural table using JDBC.
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM TestNatural");
      stmt.close();

      // Insert the row to delete with the DAO class under test.
      inserter.insert(conn, dto);

      // Query the row using JDBC, no cache.
      stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(QUERY);
      if (!rs.next()) {
        fail("Failed to insert row to delete");
      }
      stmt.close();
      stmt = null;

      // Delete the test row.
      dto.delete();
      deleter.delete(conn, dto);

      // Query the row from the database to verify database delete.
      stmt = conn.createStatement();
      rs = stmt.executeQuery(QUERY);
      if (rs.next()) {
        fail("Found row ostensibly deleted");
      }
      stmt.close();
      stmt = null;
      logger.info("Verified row removed from database");

      // Get the row using a standard memcached query to see if it's deleted in
      // the cache.
      IKeyQuerySql<TestNatural> sql = new TestNaturalKeyQuerySql();
      QueryByKey<TestNatural> query =
        new QueryMemcachedByKey<TestNatural>(sql,
                                             getSubsystem(),
                                             Integer.MAX_VALUE);
      IDbDto queriedDto = query.queryByKey(key);
      assertTrue("TestNatural object not deleted", queriedDto == null);
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
