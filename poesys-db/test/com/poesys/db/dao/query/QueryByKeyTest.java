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
import com.poesys.db.dao.ConnectionTest;
import com.poesys.db.dao.insert.Insert;
import com.poesys.db.dao.insert.InsertSqlParent;
import com.poesys.db.dao.insert.InsertSqlTestSequence;
import com.poesys.db.dto.Child;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.Parent;
import com.poesys.db.dto.TestSequence;
import com.poesys.db.pk.*;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test the QueryByKey class.
 * 
 * @author Robert J. Muller
 */
public class QueryByKeyTest extends ConnectionTest {
  private static final String PARENT_KEY_NAME = "parent_id";
  private static final String CHILD_SUB_KEY_NAME = "child_number";
  private static final String COL1_VALUE = "string";
  private static final String CLASS_NAME = "com.poesys.test.TestSequence";

  /**
   * Test the query by key functionality using a sequence key object.
   */
  @Test
  public void testQueryByKeySequenceTest() {
    SequencePrimaryKey key;
    TestSequence dto;

    // Create the sequence key and the object to insert.
    Insert<TestSequence> inserter = new Insert<>(new InsertSqlTestSequence(), getSubsystem());
    key =
      PrimaryKeyFactory.createMySqlSequenceKey("test",
                                               "pKey",
                                               CLASS_NAME,
                                               getSubsystem());
    String col1 = "test";
    dto = new TestSequence(key, col1);

    // Insert the object.
    inserter.insert(dto);
    assertTrue(true);

    IKeyQuerySql<TestSequence> sql = new TestSequenceKeyQuerySql();
    QueryByKey<TestSequence> query = new QueryByKey<>(sql, getSubsystem());
    IDbDto queriedDto = query.queryByKey(key);
    assertTrue("no object found", queriedDto != null);
    assertTrue("data not equal", dto.compareTo(queriedDto) == 0);
    assertTrue("queried dto set to NEW",
               queriedDto.getStatus() != IDbDto.Status.NEW);
    assertTrue("queried dto set to CHANGED",
               queriedDto.getStatus() != IDbDto.Status.CHANGED);
  }

  /**
   * Test the query-by-key functionality for a Parent.
   * 
   * @throws IOException when can't get a property
   * @throws SQLException when can't get a connection
   */
  @Test
  public void testQueryByKeyParentTest() throws IOException, SQLException
       {
    Connection conn;
    try {
      conn = getConnection();
    } catch (SQLException e) {
      throw new DbErrorException("Connect failed: " + e.getMessage(), e);
    }

    // Create the insert command for the parent.
    Insert<Parent> inserter = new Insert<>(new InsertSqlParent(), getSubsystem());

    // Create the GUID primary key for the parent.
    GuidPrimaryKey key =
      PrimaryKeyFactory.createGuidKey(PARENT_KEY_NAME, CLASS_NAME);

    // Create the parent DTO with the key and the empty setters list.
         Parent dto = new Parent(key, COL1_VALUE);

    // Create three children in a list and set the children into the parent.
    List<Child> children = new ArrayList<>();
    NaturalPrimaryKey subKey1 =
      PrimaryKeyFactory.createSingleNumberKey(CHILD_SUB_KEY_NAME,
                                              new BigInteger("1"),
                                              CLASS_NAME);
    CompositePrimaryKey key1 =
      new CompositePrimaryKey(key, subKey1, CLASS_NAME);
    children.add(new Child(key1, new BigInteger("1"), COL1_VALUE));
    NaturalPrimaryKey subKey2 =
      PrimaryKeyFactory.createSingleNumberKey(CHILD_SUB_KEY_NAME,
                                              new BigInteger("2"),
                                              CLASS_NAME);
    CompositePrimaryKey key2 =
      new CompositePrimaryKey(key, subKey2, CLASS_NAME);
    children.add(new Child(key2, new BigInteger("2"), COL1_VALUE));
    NaturalPrimaryKey subKey3 =
      PrimaryKeyFactory.createSingleNumberKey(CHILD_SUB_KEY_NAME,
                                              new BigInteger("3"),
                                              CLASS_NAME);
    CompositePrimaryKey key3 =
      new CompositePrimaryKey(key, subKey3, CLASS_NAME);
    children.add(new Child(key3, new BigInteger("3"), COL1_VALUE));
    dto.setChildren(children);

    Statement stmt = null;
         try {
      // Delete any rows in the Parent and Child tables.
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM Child");
      stmt.close();
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM Parent");
      stmt.close();
      stmt = null;

      conn.commit();

      // Insert the test row.
      inserter.insert(dto);
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

    // Query the object and test against the original.
    IKeyQuerySql<Parent> sql = new ParentKeyQuerySql();
    QueryByKey<Parent> dao = new QueryByKey<>(sql, getSubsystem());
    IDbDto queriedDto = dao.queryByKey(key);
    assertTrue(queriedDto != null);
    assertTrue("data not equal", dto.compareTo(queriedDto) == 0);
    assertTrue("queried dto set to NEW",
               queriedDto.getStatus() != IDbDto.Status.NEW);
    assertTrue("queried dto set to CHANGED",
               queriedDto.getStatus() != IDbDto.Status.CHANGED);
  }
}
