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

import com.poesys.db.DbErrorException;
import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dao.MemcachedTest;
import com.poesys.db.dao.query.IKeyQuerySql;
import com.poesys.db.dto.TestNatural;
import com.poesys.db.pk.IPrimaryKey;
import org.apache.log4j.Logger;
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
 * Test the insertion of an object with a natural primary key.
 *
 * @author Robert J. Muller
 */
public class InsertTestNaturalTest extends MemcachedTest {
  private static final Logger logger = Logger.getLogger(InsertTestNaturalTest.class);
  /** SQL statement to query test row */
  private static final String QUERY =
    "SELECT col1 FROM TestNatural WHERE key1 = 'A' and key2 = 'B'";

  /**
   * Test the insert method.
   *
   * @throws IOException  when can't get a property
   * @throws SQLException when can't get a connection
   */
  @Test
  public void testInsert() throws IOException, SQLException {
    Connection conn;
    try {
      conn = getConnection();
    } catch (SQLException e) {
      throw new DbErrorException("Connect failed: " + e.getMessage(), e);
    }
    Insert<TestNatural> cut = new Insert<>(new InsertSqlTestNatural(), getSubsystem());

    // Create the DTO.
    BigDecimal col1 = new BigDecimal("1234.5678");
    TestNatural dto = new TestNatural("A", "B", col1);

    Statement stmt = null;
    try {
      // Delete any rows in the TestNatural table.
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM TestNatural");
      stmt.close();

      conn.commit();

      // Insert the test row.
      cut.insert(dto);

      // Query the row.
      stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(QUERY);
      BigDecimal queriedCol1 = null;
      if (rs.next()) {
        queriedCol1 = rs.getBigDecimal("col1");
      }
      assertTrue(queriedCol1 != null);
      // Must use compareTo here, not equals, because of precision difference
      assertTrue(col1.compareTo(queriedCol1) == 0);
      conn.commit();
    } catch (SQLException e) {
      fail("insert method failed: " + e.getMessage());
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
   * Internal implementation of query class for testing
   *
   * @author Robert J. Muller
   */
  public class Query implements IKeyQuerySql<TestNatural> {
    /** SQL query statement for TestX */
    private static final String SQL = "SELECT key1, key2, col1 FROM TestNatural WHERE ";

    public TestNatural getData(IPrimaryKey key, ResultSet rs) {
      String key1;
      String key2;
      BigDecimal col1;
      try {
        key1 = rs.getString("key1");
        key2 = rs.getString("key2");
        col1 = rs.getBigDecimal("col1");
      } catch (SQLException e) {
        throw new DbErrorException("SQL error", e);
      }
      return new TestNatural(key1, key2, col1);
    }

    public String getSql(IPrimaryKey key) {
      return SQL + key.getSqlWhereExpression("");
    }
  }

  /**
   * Test the insert method using a memcached cache.
   *
   * @throws IOException  when can't get a property
   * @throws SQLException when can't get a connection
   */
  @Test
  public void testInsertMemcached() throws IOException, SQLException {
    Connection conn;
    try {
      conn = getConnection();
    } catch (SQLException e) {
      throw new DbErrorException("Connect failed: " + e.getMessage(), e);
    }

    // Need to clear any previously created manager for the subsystem, as
    // the other tests in the suite create different kinds of managers for
    // the same subsystem.
    DaoManagerFactory.clearManager(getSubsystem());
    // Create a memcached manager.
    IDaoManager manager = DaoManagerFactory.initMemcachedManager(getSubsystem());
    IDaoFactory<TestNatural> factory =
      manager.getFactory(TestNatural.class.getName(), getSubsystem(), null);

    IInsert<TestNatural> cut = factory.getInsert(new InsertSqlTestNatural(), true);

    // Create the DTO.
    BigDecimal col1 = new BigDecimal("1234.5678");
    TestNatural dto = new TestNatural("A", "B", col1);

    Statement stmt = null;
    try {
      // Delete any rows in the TestNatural table.
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM TestNatural");
      stmt.close();

      conn.commit();

      // Insert the test row.
      logger.debug("Inserting dto to memcached cache: " + dto.getPrimaryKey().getStringKey());
      cut.insert(dto);
      logger.debug("Inserted dto to memcached cache: " + dto.getPrimaryKey().getStringKey());

      // Sleep for a couple of seconds to give the cache a chance to catch up.
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        logger.error("Error in sleep wait for memcached", e);
        fail("Error in sleep wait for memcached");
      }
      // Check memcached for the data.
      logger.debug("Getting object from memcached with key " + dto.getPrimaryKey().getStringKey());
      Object object = getFromMemcached(dto.getPrimaryKey());
      assertTrue("Couldn't get memcached object", object != null);

      // Query the row from the database.
      stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(QUERY);
      BigDecimal queriedCol1 = null;
      if (rs.next()) {
        queriedCol1 = rs.getBigDecimal("col1");
      }
      assertTrue(queriedCol1 != null);
      // Must use compareTo here, not equals, because of precision difference
      assertTrue(col1.compareTo(queriedCol1) == 0);
      conn.commit();
    } catch (SQLException e) {
      fail("insert method failed: " + e.getMessage());
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
