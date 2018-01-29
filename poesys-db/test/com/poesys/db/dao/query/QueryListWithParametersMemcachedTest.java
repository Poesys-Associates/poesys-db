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

import com.poesys.db.DbErrorException;
import com.poesys.db.Message;
import com.poesys.db.NoPrimaryKeyException;
import com.poesys.db.dao.ConnectionTest;
import com.poesys.db.dao.insert.Insert;
import com.poesys.db.dao.insert.InsertSqlTestSequence;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.TestSequence;
import com.poesys.db.pk.AbstractSingleValuedPrimaryKey;
import com.poesys.db.pk.PrimaryKeyFactory;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;

/**
 * CUT: QueryListWithParametersMemcached
 *
 * @author Robert J. Muller
 */
public class QueryListWithParametersMemcachedTest extends ConnectionTest {
  private static final String CLASS_NAME = "com.poesys.test.TestSequence";

  /**
   * Test method for
   * {@link com.poesys.db.dao.query.QueryList#query()}.
   *
   * @throws IOException when can't get a property
   */
  @Test
  public void testQuery() throws IOException {
    Connection conn;
    Statement stmt = null;
    TestSequence dto1 = null;

    try {
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
      }
      finally {
        if (stmt != null) {
          stmt.close();
        }
        if (conn != null) {
          conn.close();
        }
      }

      // Create the sequence key and the objects to insert.
      Insert<TestSequence> inserter;
      TestSequence dto2 = null;
      TestSequence dto3 = null;
      inserter = new Insert<>(new InsertSqlTestSequence(), getSubsystem());
      try {
        AbstractSingleValuedPrimaryKey key1 =
          PrimaryKeyFactory.createMySqlSequenceKey("test", "key", CLASS_NAME, getSubsystem());
        String col1 = "test";
        dto1 = new TestSequence(key1, col1);
        AbstractSingleValuedPrimaryKey key2 =
          PrimaryKeyFactory.createMySqlSequenceKey("test", "key", CLASS_NAME, getSubsystem());
        dto2 = new TestSequence(key2, col1);
        AbstractSingleValuedPrimaryKey key3 =
          PrimaryKeyFactory.createMySqlSequenceKey("test", "key", CLASS_NAME, getSubsystem());
        dto3 = new TestSequence(key3, "no test");
      } catch (NoPrimaryKeyException e1) {
        fail(Message.getMessage(e1.getMessage(), e1.getParameters().toArray()));
      }

      // Insert the objects.
      inserter.insert(dto1);
      inserter.insert(dto2);
      inserter.insert(dto3);
      assertTrue(true);
    } catch (SQLException e) {
      fail("Insert test objects failed: " + e.getMessage());
    }

    IParameterizedQuerySql<TestSequence, TestSequence> sql =
      new TestSequenceQueryWithParametersSql();
    QueryListWithParameters<TestSequence, TestSequence, List<TestSequence>> dao =
      new QueryListWithParameters<>(sql, getSubsystem(), 2);
    List<TestSequence> queriedDtos = dao.query(dto1);
    assertTrue("null list queried", queriedDtos != null);
    // Should get back 2 of the 3 DTOs
    assertTrue("wrong number of DTOs: " + queriedDtos.size(), queriedDtos.size() == 2);
    for (TestSequence dto : queriedDtos) {
      assertTrue("queried dto set to new, pk:" + dto.getPrimaryKey().getValueList(),
                 dto.getStatus() != IDbDto.Status.NEW);
      assertTrue("queried dto set to changed, pk :" + dto.getPrimaryKey().getValueList(),
                 dto.getStatus() != IDbDto.Status.CHANGED);
    }
  }
}
