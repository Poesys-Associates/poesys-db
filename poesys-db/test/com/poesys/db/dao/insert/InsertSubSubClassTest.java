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
import com.poesys.db.InvalidParametersException;
import com.poesys.db.Message;
import com.poesys.db.NoPrimaryKeyException;
import com.poesys.db.dao.ConnectionTest;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.RootClass;
import com.poesys.db.dto.SubClass;
import com.poesys.db.dto.SubSubClass;
import com.poesys.db.pk.AbstractSingleValuedPrimaryKey;
import com.poesys.db.pk.PrimaryKeyFactory;
import org.junit.Test;

import java.io.IOException;
import java.sql.*;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test the specialization/inheritance insertion capability.
 *
 * @author Robert J. Muller
 */
public class InsertSubSubClassTest extends ConnectionTest {
  private static final String QUERY_ROOT = "SELECT root_col FROM RootClass WHERE root_class_id = ?";
  private static final String QUERY_SUB = "SELECT sub_col FROM SubClass WHERE root_class_id = ?";
  private static final String QUERY_SUB_SUB =
    "SELECT sub_sub_col FROM SubSubClass WHERE root_class_id = ?";
  private static final String KEY_NAME = "root_class_id";
  private static final String COL_VALUE = "string";
  private static final String CLASS_NAME = "com.poesys.db.test.SubSubClass";

  /**
   * Test the basic use case for a specialized object: insert the object with no
   * errors.
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

    // Create the insert commands (class under test) for the three tables.
    Insert<RootClass> cut1 = new Insert<>(new InsertSqlRootClass(), getSubsystem());
    Insert<SubClass> cut2 = new Insert<>(new InsertSqlSubClass(), getSubsystem());
    Insert<SubSubClass> cut3 = new Insert<>(new InsertSqlSubSubClass(), getSubsystem());

    // Create the sequence primary key for the class.
    AbstractSingleValuedPrimaryKey key = null;
    try {
      key = PrimaryKeyFactory.createMySqlSequenceKey("root_class", KEY_NAME, CLASS_NAME,
                                                     getSubsystem());
    } catch (InvalidParametersException e1) {
      fail(e1.getMessage());
    } catch (NoPrimaryKeyException e1) {
      fail(Message.getMessage(e1.getMessage(), e1.getParameters().toArray()));
    }

    // Create the DTO with the key and the three string columns.
    SubSubClass dto = new SubSubClass(key, COL_VALUE, COL_VALUE, COL_VALUE);

    Statement stmt = null;
    PreparedStatement pStmt = null;
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

      conn.commit();

      // Insert the test object. Keep the new flag set for inserting the
      // subclass parts.
      cut1.insert(dto);
      dto.undoStatus();
      cut2.insert(dto);
      dto.undoStatus();
      cut3.insert(dto);

      // Test the flags.
      assertTrue("inserted parent not EXISTING", dto.getStatus() == IDbDto.Status.EXISTING);

      // Query the root row.
      pStmt = conn.prepareStatement(QUERY_ROOT);
      key.setParams(pStmt, 1);
      ResultSet rs = pStmt.executeQuery();
      String queriedCol = null;
      if (rs.next()) {
        queriedCol = rs.getString("root_col");
      }
      pStmt.close();
      pStmt = null;
      assertTrue(queriedCol != null);
      assertTrue(COL_VALUE.equals(queriedCol));

      conn.commit();

      // Query the sub-class row.
      pStmt = conn.prepareStatement(QUERY_SUB);
      key.setParams(pStmt, 1);
      rs = pStmt.executeQuery();
      queriedCol = null;
      if (rs.next()) {
        queriedCol = rs.getString("sub_col");
      }
      pStmt.close();
      pStmt = null;
      assertTrue(queriedCol != null);
      assertTrue(COL_VALUE.equals(queriedCol));

      conn.commit();

      // Query the sub-sub-class row.
      pStmt = conn.prepareStatement(QUERY_SUB_SUB);
      key.setParams(pStmt, 1);
      rs = pStmt.executeQuery();
      queriedCol = null;
      if (rs.next()) {
        queriedCol = rs.getString("sub_sub_col");
      }
      pStmt.close();
      pStmt = null;
      assertTrue(queriedCol != null);
      assertTrue(COL_VALUE.equals(queriedCol));

      conn.commit();
    } catch (SQLException e) {
      fail("insert method failed with SQL error: " + e.getMessage());
    }
    finally {
      if (stmt != null) {
        stmt.close();
      }
      if (pStmt != null) {
        pStmt.close();
      }
      if (conn != null) {
        conn.close();
      }
    }
  }
}
