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
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;

import com.poesys.db.BatchException;
import com.poesys.db.DbErrorException;
import com.poesys.db.dao.ConnectionTest;
import com.poesys.db.dao.insert.Insert;
import com.poesys.db.dao.insert.InsertSqlTestSequence;
import com.poesys.db.dto.TestSequence;
import com.poesys.db.pk.AbstractSingleValuedPrimaryKey;
import com.poesys.db.pk.PrimaryKeyFactory;


/**
 * Test the QueryCount class.
 * 
 * @author Robert J. Muller
 */
public class QueryCountTest extends ConnectionTest {
  private static final String CLASS_NAME = "com.poesys.test.TestSequence";

  /**
   * Test method for
   * {@link com.poesys.db.dao.query.QueryCount#queryCount(com.poesys.db.dto.IDbDto, String)}
   * .
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
      conn = getConnection();
    } catch (SQLException e) {
      throw new DbErrorException("Connect failed: " + e.getMessage(), e);
    }

    // Delete all the rows from TestSequence
    try {
      stmt = conn.createStatement();
      stmt.execute("DELETE FROM TestSequence");
      conn.commit();
    } catch (RuntimeException e1) {
      fail("Couldn't delete rows from TestSequence");
    } finally {
      if (stmt != null) {
        stmt.close();
      }
      if (conn != null) {
        conn.close();
      }
    }

    // Create the sequence key and the objects to insert.
    Insert<TestSequence> inserter =
      new Insert<TestSequence>(new InsertSqlTestSequence(), getSubsystem());
    AbstractSingleValuedPrimaryKey key1 =
      PrimaryKeyFactory.createMySqlSequenceKey("test",
                                               "pkey",
                                               CLASS_NAME,
                                               getSubsystem());
    String col1 = "test";
    TestSequence dto1 = new TestSequence(key1, col1);
    AbstractSingleValuedPrimaryKey key2 =
      PrimaryKeyFactory.createMySqlSequenceKey("test",
                                               "pkey",
                                               CLASS_NAME,
                                               getSubsystem());
    TestSequence dto2 = new TestSequence(key2, col1);
    AbstractSingleValuedPrimaryKey key3 =
      PrimaryKeyFactory.createMySqlSequenceKey("test",
                                               "pkey",
                                               CLASS_NAME,
                                               getSubsystem());
    TestSequence dto3 = new TestSequence(key3, "no test");

    inserter.insert(dto1);
    inserter.insert(dto2);
    inserter.insert(dto3);
    assertTrue(true);

    // Query the count of objects.
    IParameterizedCountSql<TestSequence> sql =
      new TestSequenceParameterizedCountSql();
    QueryCount<TestSequence> dao = new QueryCount<TestSequence>(sql);
    BigInteger count = dao.queryCount(dto1, getSubsystem());
    assertTrue("null count queried", count != null);
    // Should get back 2 of the 3 DTOs
    assertTrue("wrong count: " + count, count.equals(new BigInteger("2")));
  }
}
