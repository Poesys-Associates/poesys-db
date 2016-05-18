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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.poesys.db.BatchException;
import com.poesys.db.col.AbstractColumnValue;
import com.poesys.db.col.StringColumnValue;
import com.poesys.db.dao.ConnectionTest;
import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dao.query.IKeyQuerySql;
import com.poesys.db.dao.query.IQueryByKey;
import com.poesys.db.dto.TestNatural;
import com.poesys.db.pk.IPrimaryKey;
import com.poesys.db.pk.NaturalPrimaryKey;


/**
 * Test the insertion of an object with a natural primary key.
 * 
 * @author Robert J. Muller
 */
public class InsertTestNaturalTest extends ConnectionTest {
  private static final String QUERY =
    "SELECT col1 FROM TestNatural WHERE key1 = 'A' and key2 = 'B'";

  /**
   * Test the insert method.
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
      throw new RuntimeException("Connect failed: " + e.getMessage(), e);
    }
    Insert<TestNatural> cut =
      new Insert<TestNatural>(new InsertSqlTestNatural());

    // Create the DTO.
    BigDecimal col1 = new BigDecimal("1234.5678");
    TestNatural dto = new TestNatural("A", "B", col1);

    Statement stmt = null;
    try {
      // Delete any rows in the TestNatural table.
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM TestNatural");
      stmt.close();

      // Insert the test row.
      stmt = conn.createStatement();
      cut.insert(conn, dto);

      // Query the row.
      ResultSet rs = stmt.executeQuery(QUERY);
      BigDecimal queriedCol1 = null;
      if (rs.next()) {
        queriedCol1 = rs.getBigDecimal("col1");
      }
      assertTrue(queriedCol1 != null);
      // Must use compareTo here, not equals, because of precision difference
      assertTrue(col1.compareTo(queriedCol1) == 0);
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

  /**
   * Internal implementation of query class for testing
   * 
   * @author Robert J. Muller
   */
  public class Query implements IKeyQuerySql<TestNatural> {
    /** SQL query statement for TestX */
    private static final String SQL =
      "SELECT key1, key2, col1 FROM TestNatural WHERE ";

    public TestNatural getData(IPrimaryKey key, ResultSet rs)
        throws SQLException {
      String key1 = rs.getString("key1");
      String key2 = rs.getString("key2");
      BigDecimal col1 = rs.getBigDecimal("col1");
      return new TestNatural(key1, key2, col1);
    }

    public String getSql(IPrimaryKey key) {
      return SQL + key.getSqlWhereExpression("");
    }
  }

  /**
   * Test the insert method using a memcached cache.
   * 
   * @throws IOException when can't get a property
   * @throws SQLException when can't get a connection
   * @throws BatchException when a problem happens during processing
   */
  public void testInsertMemcached() throws IOException, SQLException,
      BatchException {
    Connection conn;
    try {
      conn = getConnection();
    } catch (SQLException e) {
      throw new RuntimeException("Connect failed: " + e.getMessage(), e);
    }

    IDaoManager manager = DaoManagerFactory.initMemcachedManager(getSubsystem());
    IDaoFactory<TestNatural> factory =
      manager.getFactory(TestNatural.class.getName(), getSubsystem(), null);

    IInsert<TestNatural> cut =
      factory.getInsert(new InsertSqlTestNatural(), true);
    IQueryByKey<TestNatural> query =
      factory.getQueryByKey(new Query(), getSubsystem());

    // Create the DTO.
    BigDecimal col1 = new BigDecimal("1234.5678");
    TestNatural dto = new TestNatural("A", "B", col1);

    Statement stmt = null;
    try {
      // Delete any rows in the TestNatural table.
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM TestNatural");
      stmt.close();

      // Insert the test row.
      stmt = conn.createStatement();
      cut.insert(conn, dto);

      // Query the row.
      List<AbstractColumnValue> keyList = new ArrayList<AbstractColumnValue>(2);
      keyList.add(new StringColumnValue("key1", "A"));
      keyList.add(new StringColumnValue("key2", "B"));
      NaturalPrimaryKey key =
        new NaturalPrimaryKey(keyList, TestNatural.class.getName());

      TestNatural queried = query.queryByKey(key);
      assertTrue(queried != null);
      // Must use compareTo here, not equals, because of precision difference
      assertTrue(queried.getCol1().compareTo(col1) == 0);
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
