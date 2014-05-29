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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.poesys.db.BatchException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.Message;
import com.poesys.db.NoPrimaryKeyException;
import com.poesys.db.connection.IConnectionFactory.DBMS;
import com.poesys.db.dao.ConnectionTest;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.RootClass;
import com.poesys.db.dto.SubClass;
import com.poesys.db.dto.SubSubClass;
import com.poesys.db.pk.AbstractSingleValuedPrimaryKey;
import com.poesys.db.pk.PrimaryKeyFactory;


/**
 * Test the specialization/inheritance insertion capability.
 * 
 * @author Bob Muller (muller@computer.org)
 */
public class InsertSubSubClassTest extends ConnectionTest {
  private static final String QUERY_ROOT =
    "SELECT root_col FROM RootClass WHERE root_class_id = ?";
  private static final String QUERY_SUB =
    "SELECT sub_col FROM SubClass WHERE root_class_id = ?";
  private static final String QUERY_SUB_SUB =
    "SELECT sub_sub_col FROM SubSubClass WHERE root_class_id = ?";
  private static final String KEY_NAME = "root_class_id";
  private static final String COL_VALUE = "string";
  private static final String CLASS_NAME = "com.poesys.db.test.SubSubClass";

  /**
   * Test the basic use case for a specialized object: insert the object with no
   * errors.
   * 
   * @throws IOException when can't get a property
   * @throws SQLException when can't get a connection
   * @throws BatchException when a problem happens during processing
   */
  public void testInsert() throws IOException, SQLException, BatchException {
    Connection conn;
    try {
      conn = getConnection(DBMS.MYSQL, "com.poesys.db.poesystest.mysql");
    } catch (SQLException e) {
      throw new RuntimeException("Connect failed: " + e.getMessage(), e);
    }

    // Create the insert commands (class under test) for the three tables.
    Insert<RootClass> cut1 = new Insert<RootClass>(new InsertSqlRootClass());
    Insert<SubClass> cut2 = new Insert<SubClass>(new InsertSqlSubClass());
    Insert<SubSubClass> cut3 =
      new Insert<SubSubClass>(new InsertSqlSubSubClass());

    // Create the sequence primary key for the class.
    AbstractSingleValuedPrimaryKey key = null;
    try {
      key = PrimaryKeyFactory.createMySqlSequenceKey(conn,
                                               "root_class",
                                               KEY_NAME,
                                               CLASS_NAME);
    } catch (InvalidParametersException e1) {
     fail(e1.getMessage());
    } catch (NoPrimaryKeyException e1) {
      fail(Message.getMessage(e1.getMessage(), e1.getParameters().toArray()));
    }

    // Create the DTO with the key and the three string columns.
    SubSubClass dto = new SubSubClass(key, COL_VALUE, COL_VALUE, COL_VALUE);

    Statement stmt = null;
    PreparedStatement pstmt = null;
    try {
      // Delete any rows in the three tables.
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM SubSubClass");
      stmt.close();
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM SubClass");
      stmt.close();
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM RootClass");
      stmt.close();
      stmt = null;

      // Insert the test object. Keep the new flag set for inserting the
      // subclass parts.
      cut1.insert(conn, dto);
      dto.undoStatus();
      cut2.insert(conn, dto);
      dto.undoStatus();
      cut3.insert(conn, dto);

      // Test the flags.
      assertTrue("inserted parent not EXISTING",
                 dto.getStatus() == IDbDto.Status.EXISTING);

      // Query the root row.
      pstmt = conn.prepareStatement(QUERY_ROOT);
      key.setParams(pstmt, 1);
      ResultSet rs = pstmt.executeQuery();
      String queriedCol = null;
      if (rs.next()) {
        queriedCol = rs.getString("root_col");
      }
      pstmt.close();
      pstmt = null;
      rs = null;
      assertTrue(queriedCol != null);
      assertTrue(COL_VALUE.equals(queriedCol));

      // Query the sub-class row.
      pstmt = conn.prepareStatement(QUERY_SUB);
      key.setParams(pstmt, 1);
      rs = pstmt.executeQuery();
      queriedCol = null;
      if (rs.next()) {
        queriedCol = rs.getString("sub_col");
      }
      pstmt.close();
      pstmt = null;
      rs = null;
      assertTrue(queriedCol != null);
      assertTrue(COL_VALUE.equals(queriedCol));

      // Query the sub-sub-class row.
      pstmt = conn.prepareStatement(QUERY_SUB_SUB);
      key.setParams(pstmt, 1);
      rs = pstmt.executeQuery();
      queriedCol = null;
      if (rs.next()) {
        queriedCol = rs.getString("sub_sub_col");
      }
      pstmt.close();
      pstmt = null;
      rs = null;
      assertTrue(queriedCol != null);
      assertTrue(COL_VALUE.equals(queriedCol));
    } catch (SQLException e) {
      fail("insert method failed with SQL error: " + e.getMessage());
    } finally {
      if (stmt != null) {
        stmt.close();
      }
      if (pstmt != null) {
        pstmt.close();
      }
      if (conn != null) {
        conn.close();
      }
    }
  }
}
