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


import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.spy.memcached.MemcachedClient;

import org.apache.log4j.Logger;

import com.poesys.db.BatchException;
import com.poesys.db.DbErrorException;
import com.poesys.db.dao.MemcachedTest;
import com.poesys.db.dao.insert.Insert;
import com.poesys.db.dao.insert.InsertMemcached;
import com.poesys.db.dao.insert.InsertSqlTestNatural;
import com.poesys.db.dto.IDbDto.Status;
import com.poesys.db.dto.TestNatural;
import com.poesys.db.pk.IPrimaryKey;


/**
 * Test the memcached update process for a simple object, including cache
 * deletion. Debugging TAIR-2733.
 * 
 * @author Robert J. Muller
 */
public class UpdateMemcachedTestNaturalTest extends MemcachedTest {
  private static final Logger logger =
    Logger.getLogger(UpdateMemcachedTestNaturalTest.class);

  private static final String QUERY =
    "SELECT col1 FROM TestNatural WHERE key1 = 'A' and key2 = 'B'";

  /**
   * Test the update method, verifying correct memcached operations.
   * 
   * @throws IOException when can't get a property
   * @throws SQLException when can't get a connection
   * @throws BatchException when a problem happens during processing
   */
  public void testUpdate() throws IOException, SQLException, BatchException {
    Connection conn;
    try {
      conn = getConnection();
    } catch (SQLException e) {
      throw new DbErrorException("Connect failed: " + e.getMessage(), e);
    }

    Statement stmt = null;
    try {
      // Delete any rows in the TestNatural table using JDBC.
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM TestNatural");
      stmt.close();

      conn.commit();

      // Create an Inserter to add the row to update
      Insert<TestNatural> inserter =
        new InsertMemcached<TestNatural>(new InsertSqlTestNatural(),
                                         getSubsystem(),
                                         Integer.MAX_VALUE);

      // Create the DTO.
      BigDecimal col1 = new BigDecimal("1234.5678");
      TestNatural dto = new TestNatural("A", "B", col1);
      IPrimaryKey key = dto.getPrimaryKey();

      // Insert the row to update with the DAO class under test.
      inserter.insert(dto);

      // Check status flag.
      assertTrue("Inserted DTO does not have status EXISTING: "
                     + dto.getStatus(),
                 dto.getStatus() == Status.EXISTING);

      // Query the row using JDBC, no cache.
      stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(QUERY);
      if (!rs.next()) {
        fail("Failed to insert row to update");
      }
      stmt.close();
      stmt = null;

      conn.commit();

      // Update the test object.
      // Create the Updater.
      UpdateMemcachedByKey<TestNatural> updater =
        new UpdateMemcachedByKey<TestNatural>(new UpdateSqlTestNatural(),
                                              getSubsystem());
      BigDecimal col1Updated = new BigDecimal("100.3");
      // Update the object.
      dto.setCol1(col1Updated);
      updater.update(dto);

      // Query the object directly using JDBC to test update in DB.
      stmt = conn.createStatement();
      rs = stmt.executeQuery(QUERY);
      if (!rs.next()) {
        fail("Failed to find updated row");
      } else {
        // Test the update
        BigDecimal dbCol1 = rs.getBigDecimal("col1");
        assertTrue("Col 1 not updated from " + col1 + " to " + col1Updated
                   + ": " + dbCol1, dbCol1.compareTo(col1Updated) == 0);
        logger.info("Successfully updated database TestNatural object");
      }
      stmt.close();
      stmt = null;

      conn.commit();

      // Get the memcached client for direct lookup of the object in the cache.
      MemcachedClient client = clients.getObject();

      // Check the memcached cache for the object.
      TestNatural object = (TestNatural)client.get(key.getStringKey());
      assertTrue("TestNatural object not removed from memcached cache",
                 object == null);

      logger.info("Updated object successfully removed from cache");
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
