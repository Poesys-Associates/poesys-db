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
package com.poesys.db.dao.query;


import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.Test;

import com.poesys.db.BatchException;
import com.poesys.db.connection.IConnectionFactory.DBMS;
import com.poesys.db.dao.ConnectionTest;
import com.poesys.db.dao.insert.Insert;
import com.poesys.db.dao.insert.InsertSqlTestSequence;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.TestSequence;
import com.poesys.db.pk.AbstractSingleValuedPrimaryKey;
import com.poesys.db.pk.PrimaryKeyFactory;


/**
 * 
 * @author Bob Muller (muller@computer.org)
 */
public class QueryListTest extends ConnectionTest {
  private static final String CLASS_NAME = "com.poesys.test.TestSequence";

  /**
   * Test method for
   * {@link com.poesys.db.dao.query.QueryList#query(java.sql.Connection)}.
   * 
   * @throws IOException when can't get a property
   * @throws SQLException when can't get a connection
   * @throws BatchException when a problem happens during processing
   */
  @Test
  public void testQuery() throws IOException, SQLException, BatchException {
    Connection conn = null;
    Statement stmt = null;
    try {
      conn = getConnection(DBMS.MYSQL, "com.poesys.db.poesystest.mysql");
    } catch (SQLException e) {
      throw new RuntimeException("Connect failed: " + e.getMessage(), e);
    }

    // Delete all the rows from TestSequence
    try {
      stmt = conn.createStatement();
      stmt.execute("DELETE FROM TestSequence");
    } catch (RuntimeException e1) {
      fail("Couldn't delete rows from TestSequence");
    } finally {
      if (stmt != null) {
        stmt.close();
      }
    }

    // Create the sequence key and the objects to insert.
    Insert<TestSequence> inserter =
      new Insert<TestSequence>(new InsertSqlTestSequence());
    AbstractSingleValuedPrimaryKey key1 =
      PrimaryKeyFactory.createMySqlSequenceKey(conn, "test", "pkey", CLASS_NAME);
    String col1 = "test";
    TestSequence dto1 = new TestSequence(key1, col1);
    AbstractSingleValuedPrimaryKey key2 =
      PrimaryKeyFactory.createMySqlSequenceKey(conn, "test", "pkey", CLASS_NAME);
    TestSequence dto2 = new TestSequence(key2, col1);
    AbstractSingleValuedPrimaryKey key3 =
      PrimaryKeyFactory.createMySqlSequenceKey(conn, "test", "pkey", CLASS_NAME);
    TestSequence dto3 = new TestSequence(key3, col1);

    // Insert the objects.
    try {
      inserter.insert(conn, dto1);
      inserter.insert(conn, dto2);
      inserter.insert(conn, dto3);
      assertTrue(true);
    } catch (SQLException e) {
      fail("Insert test objects failed: " + e.getMessage());
    }

    // Query the list of objects and test against the original.
    try {
      IQuerySql<TestSequence> sql = new TestSequenceQuerySql();
      QueryList<TestSequence> dao = new QueryList<TestSequence>(sql, 2);
      List<TestSequence> queriedDtos = dao.query(conn);
      assertTrue("null list queried", queriedDtos != null);
      assertTrue("wrong number of DTOs: " + queriedDtos.size(),
                 queriedDtos.size() == 3);
      for (IDbDto dto : queriedDtos) {
        assertTrue("queried dto set to new, pk:"
                       + dto.getPrimaryKey().getValueList(),
                   dto.getStatus() != IDbDto.Status.NEW);
        assertTrue("queried dto set to changed, pk :"
                       + dto.getPrimaryKey().getValueList(),
                   dto.getStatus() != IDbDto.Status.CHANGED);
      }
    } catch (SQLException e) {
      fail("Query List exception: " + e.getMessage());
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }

}
