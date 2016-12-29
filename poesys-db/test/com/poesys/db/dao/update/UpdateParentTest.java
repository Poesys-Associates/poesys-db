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
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.poesys.db.BatchException;
import com.poesys.db.DbErrorException;
import com.poesys.db.dao.ConnectionTest;
import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.dao.insert.Insert;
import com.poesys.db.dao.insert.InsertSqlParent;
import com.poesys.db.dto.Child;
import com.poesys.db.dto.Parent;
import com.poesys.db.pk.CompositePrimaryKey;
import com.poesys.db.pk.GuidPrimaryKey;
import com.poesys.db.pk.NaturalPrimaryKey;
import com.poesys.db.pk.PrimaryKeyFactory;


/**
 * Test the update process for a parent-child complex.
 * 
 * @author Robert J. Muller
 */
public class UpdateParentTest extends ConnectionTest {
  private static final String QUERY_PARENT =
    "SELECT col1 FROM Parent WHERE parent_id = ?";
  private static final String QUERY_CHILD =
    "SELECT col1 FROM Child WHERE parent_id = ? AND child_number = 2";
  private static final String PARENT_KEY_NAME = "parent_id";
  private static final String CHILD_SUBKEY_NAME = "child_number";
  private static final String COL1_VALUE = "string";
  private static final String COL1_CHANGED = "changed";
  private static final String CLASS_NAME = "com.poesys.test.Child";

  /** timeout for the cache thread */
  private static final int TIMEOUT = 10000 * 60;

  /**
   * Test the update() method.
   * 
   * @throws IOException when can't get a property
   * @throws SQLException when can't get a connection
   * @throws BatchException when a problem happens during processing
   * @throws InterruptedException when the tracking thread gets interrupted
   *           unexpectedly
   */
  public void testUpdate() throws IOException, SQLException, BatchException,
      InterruptedException {
    Connection conn;
    try {
      conn = getConnection();
    } catch (SQLException e) {
      throw new DbErrorException("Connect failed: " + e.getMessage(), e);
    }

    // Create the insert command (class under test) for the parent.
    Insert<Parent> inserter =
      new Insert<Parent>(new InsertSqlParent(), getSubsystem());

    // Create the GUID primary key for the parent.
    GuidPrimaryKey key =
      PrimaryKeyFactory.createGuidKey(PARENT_KEY_NAME, CLASS_NAME);

    // Create the parent DTO with the key and the empty setters list.
    String col1 = new String(COL1_VALUE);
    Parent dto = new Parent(key, col1);

    // Create three children in a list and set the children into the parent.
    List<Child> children = new ArrayList<Child>();
    NaturalPrimaryKey subKey1 =
      PrimaryKeyFactory.createSingleNumberKey(CHILD_SUBKEY_NAME,
                                              new BigInteger("1"),
                                              CLASS_NAME);
    CompositePrimaryKey key1 =
      new CompositePrimaryKey(key, subKey1, CLASS_NAME);
    children.add(new Child(key1, new BigInteger("1"), COL1_VALUE));
    NaturalPrimaryKey subKey2 =
      PrimaryKeyFactory.createSingleNumberKey(CHILD_SUBKEY_NAME,
                                              new BigInteger("2"),
                                              CLASS_NAME);
    CompositePrimaryKey key2 =
      new CompositePrimaryKey(key, subKey2, CLASS_NAME);

    // Make a local variable so you can update this directly later.
    Child child2 = new Child(key2, new BigInteger("2"), COL1_VALUE);
    children.add(child2);

    NaturalPrimaryKey subKey3 =
      PrimaryKeyFactory.createSingleNumberKey(CHILD_SUBKEY_NAME,
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
    } catch (SQLException e) {
      fail("delete failed: " + e.getMessage());
    } finally {
      if (stmt != null) {
        stmt.close();
        stmt = null;
      }
    }

    // Insert the test row.
    inserter.insert(dto);

    // Create the Updater.
    UpdateByKey<Parent> updater =
      new UpdateByKey<Parent>(new UpdateSqlParent(), getSubsystem());

    PreparedStatement pstmt = null;
    try {
      // Change col1 in the parent and one of the children.
      dto.setCol1(COL1_CHANGED);
      child2.setCol1(COL1_CHANGED);

      Runnable process = new Runnable() {
        public void run() {
          PoesysTrackingThread thread =
            (PoesysTrackingThread)Thread.currentThread();
          try {
            // Update the test row.
            updater.update(dto);

            // Process nested objects. This version of Poesys/DB removes
            // post-processing from the DAO, so the client needs to run the DTO
            // method directly.
            dto.postprocessNestedObjects();
          } finally {
            thread.closeConnection();
          }
        }
      };
      PoesysTrackingThread thread =
        new PoesysTrackingThread(process, getSubsystem());
      thread.start();

      // Join the thread, blocking until the thread completes or
      // until the query times out.
      thread.join(TIMEOUT);

      // Query the column directly for comparison.
      pstmt = conn.prepareStatement(QUERY_PARENT);
      // Use key to set query parameter 1 with GUID key value
      key.setParams(pstmt, 1);
      ResultSet rs = pstmt.executeQuery();
      String queriedCol1 = null;
      if (rs.next()) {
        queriedCol1 = rs.getString("col1");
      }
      pstmt.close();
      pstmt = null;
      assertTrue("Queried parent column is null", queriedCol1 != null);
      assertTrue("Queried parent column not updated, original value "
                     + COL1_VALUE + ", queried value " + queriedCol1,
                 COL1_CHANGED.equals(queriedCol1));

      // Get the second child and see if the value has changed.
      pstmt = conn.prepareStatement(QUERY_CHILD);
      // Use key to set query parameter 1 with GUID key value, child key is
      // constant 2
      key.setParams(pstmt, 1);
      rs = pstmt.executeQuery();
      queriedCol1 = null;
      if (rs.next()) {
        queriedCol1 = rs.getString("col1");
      }
      assertTrue("Queried child column is null", queriedCol1 != null);
      assertTrue("Queried child column not updated from " + COL1_VALUE + " to "
                     + COL1_CHANGED + ": " + queriedCol1,
                 COL1_CHANGED.equals(queriedCol1));
      conn.commit();
    } catch (SQLException e) {
      fail("update method failed with SQL exception: " + e.getMessage());
    } finally {
      if (pstmt != null) {
        pstmt.close();
        pstmt = null;
      }
      if (conn != null) {
        conn.close();
      }
    }
  }
}
