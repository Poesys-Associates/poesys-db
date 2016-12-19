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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.poesys.db.BatchException;
import com.poesys.db.DbErrorException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.Message;
import com.poesys.db.NoPrimaryKeyException;
import com.poesys.db.dao.ConnectionTest;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.Link1;
import com.poesys.db.dto.Link2;
import com.poesys.db.dto.Link3;
import com.poesys.db.dto.TernaryLink;
import com.poesys.db.pk.AbstractSingleValuedPrimaryKey;
import com.poesys.db.pk.AssociationPrimaryKey;
import com.poesys.db.pk.IPrimaryKey;
import com.poesys.db.pk.PrimaryKeyFactory;


/**
 * Test the specialization/inheritance insertion capability.
 * 
 * @author Robert J. Muller
 */
public class InsertTernaryLink1Test extends ConnectionTest {
  private static final String QUERY_LINK1 =
    "SELECT col FROM Link1 WHERE link1_id = ?";
  private static final String QUERY_LINK2 =
    "SELECT col FROM Link2 WHERE link2_id = ?";
  private static final String QUERY_LINK3 =
    "SELECT col FROM Link3 WHERE link3_id = ?";
  private static final String QUERY_TERNARY_LINK =
    "SELECT col FROM TernaryLink WHERE link1_id = ?";
  private static final String KEY1_NAME = "link1_id";
  private static final String KEY2_NAME = "link2_id";
  private static final String KEY3_NAME = "link3_id";
  private static final String COL_VALUE = "string";
  private static final String CLASS_NAME = "com.poesys.db.test.TernaryLink1";

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
      conn = getConnection();
    } catch (SQLException e) {
      throw new DbErrorException("Connect failed: " + e.getMessage(), e);
    }

    // Create the insert commands (class under test) for the two linked tables.
    Insert<Link1> cut1 =
      new Insert<Link1>(new InsertSqlLink1(), getSubsystem());
    Insert<Link2> cut2 =
      new Insert<Link2>(new InsertSqlLink2(), getSubsystem());
    Insert<Link3> cut3 =
      new Insert<Link3>(new InsertSqlLink3(), getSubsystem());

    // Create the sequence primary keys for the objects.
    AbstractSingleValuedPrimaryKey key1 = null;
    AbstractSingleValuedPrimaryKey key21 = null;
    AbstractSingleValuedPrimaryKey key22 = null;
    AbstractSingleValuedPrimaryKey key23 = null;
    AbstractSingleValuedPrimaryKey key3 = null;
    try {
      key1 =
        PrimaryKeyFactory.createMySqlSequenceKey("link1",
                                                 KEY1_NAME,
                                                 CLASS_NAME,
                                                 getSubsystem());
      key21 =
        PrimaryKeyFactory.createMySqlSequenceKey("link2",
                                                 KEY2_NAME,
                                                 CLASS_NAME,
                                                 getSubsystem());
      key22 =
        PrimaryKeyFactory.createMySqlSequenceKey("link2",
                                                 KEY2_NAME,
                                                 CLASS_NAME,
                                                 getSubsystem());
      key23 =
        PrimaryKeyFactory.createMySqlSequenceKey("link2",
                                                 KEY2_NAME,
                                                 CLASS_NAME,
                                                 getSubsystem());
      key3 =
        PrimaryKeyFactory.createMySqlSequenceKey("link3",
                                                 KEY3_NAME,
                                                 CLASS_NAME,
                                                 getSubsystem());
    } catch (InvalidParametersException e1) {
      fail(e1.getMessage());
    } catch (NoPrimaryKeyException e1) {
      fail(Message.getMessage(e1.getMessage(), e1.getParameters().toArray()));
    }

    // Create the Link1, Link2, and Link3 DTOs with the key and the string
    // column.
    Link1 link1Dto = new Link1(key1, COL_VALUE);
    Link2 link21Dto = new Link2(key21, COL_VALUE);
    Link2 link22Dto = new Link2(key22, COL_VALUE);
    Link2 link23Dto = new Link2(key23, COL_VALUE);
    Link3 link3Dto = new Link3(key3, COL_VALUE);

    // Create the links and add them to the Link1 object.
    List<IPrimaryKey> keylist1_2_1_3 = new CopyOnWriteArrayList<IPrimaryKey>();
    keylist1_2_1_3.add(key1); // Link1 object
    keylist1_2_1_3.add(key21); // first link2 object
    keylist1_2_1_3.add(key3); // Link3 object
    AssociationPrimaryKey key1_2_1_3 =
      new AssociationPrimaryKey(keylist1_2_1_3, CLASS_NAME);
    TernaryLink link1 = new TernaryLink(key1_2_1_3, COL_VALUE);
    link1.setLink1(link1Dto);
    link1.setLink2(link21Dto);
    link1.setLink3(link3Dto);

    List<IPrimaryKey> keylist1_2_2_3 = new CopyOnWriteArrayList<IPrimaryKey>();
    keylist1_2_2_3.add(key1); // Link1 object
    keylist1_2_2_3.add(key22); // second link2 object
    keylist1_2_2_3.add(key3); // Link3 object
    AssociationPrimaryKey key1_2_2_3 =
      new AssociationPrimaryKey(keylist1_2_2_3, CLASS_NAME);
    TernaryLink link2 = new TernaryLink(key1_2_2_3, COL_VALUE);
    link2.setLink1(link1Dto);
    link2.setLink2(link22Dto);
    link2.setLink3(link3Dto);

    List<IPrimaryKey> keylist1_2_3_3 = new CopyOnWriteArrayList<IPrimaryKey>();
    keylist1_2_3_3.add(key1); // Link1 object
    keylist1_2_3_3.add(key23); // third link2 object
    keylist1_2_3_3.add(key3); // Link3 object
    AssociationPrimaryKey key1_2_3_3 =
      new AssociationPrimaryKey(keylist1_2_3_3, CLASS_NAME);
    TernaryLink link3 = new TernaryLink(key1_2_3_3, COL_VALUE);
    link3.setLink1(link1Dto);
    link3.setLink2(link23Dto);
    link3.setLink3(link3Dto);

    List<TernaryLink> links1 = new CopyOnWriteArrayList<TernaryLink>();
    links1.add(link1);
    links1.add(link2);
    links1.add(link3);

    List<TernaryLink> links21 = new CopyOnWriteArrayList<TernaryLink>();
    links1.add(link1);

    List<TernaryLink> links22 = new CopyOnWriteArrayList<TernaryLink>();
    links1.add(link2);

    List<TernaryLink> links23 = new CopyOnWriteArrayList<TernaryLink>();
    links1.add(link3);

    // Add the links to the link objects.
    link1Dto.setTernaryLinks(links1);
    link21Dto.setTernaryLinks(links21);
    link22Dto.setTernaryLinks(links22);
    link23Dto.setTernaryLinks(links23);

    Statement stmt = null;
    PreparedStatement pstmt = null;
    try {
      // Delete any rows in the tables.
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM Link1");
      stmt.close();
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM Link2");
      stmt.close();
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM Link3");
      stmt.close();
      stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM TernaryLink");
      stmt.close();
      stmt = null;

      conn.commit();

      // Insert the Link2 objects. There should be no exceptions from attempts
      // to insert links.
      cut2.insert(link21Dto);
      cut2.insert(link22Dto);
      cut2.insert(link23Dto);
      cut3.insert(link3Dto);

      // Insert the Link1 object. The links should be inserted.
      cut1.insert(link1Dto);

      // Test the flags.
      assertTrue("inserted link not EXISTING",
                 link1Dto.getStatus() == IDbDto.Status.EXISTING);

      // Commit for debugging.
      conn.commit();

      // Query the Link1 row.
      pstmt = conn.prepareStatement(QUERY_LINK1);
      key1.setParams(pstmt, 1);
      ResultSet rs = pstmt.executeQuery();
      String queriedCol = null;
      if (rs.next()) {
        queriedCol = rs.getString("col");
      }
      pstmt.close();
      pstmt = null;
      rs = null;
      assertTrue(queriedCol != null);
      assertTrue(COL_VALUE.equals(queriedCol));

      conn.commit();

      // Query the Link2 rows.
      pstmt = conn.prepareStatement(QUERY_LINK2);
      for (int counter = 0; counter < 3; counter++) {
        key21.setParams(pstmt, 1);
        rs = pstmt.executeQuery();
        queriedCol = null;
        if (rs.next()) {
          queriedCol = rs.getString("col");
          assertTrue(queriedCol != null);
          assertTrue(COL_VALUE.equals(queriedCol));
        } else {
          fail("Not enough Link2 rows inserted--no row at index " + counter);
        }
      }
      pstmt.close();
      pstmt = null;
      rs = null;

      conn.commit();

      // Query the Link3 rows.
      pstmt = conn.prepareStatement(QUERY_LINK3);
      for (int counter = 0; counter < 3; counter++) {
        key3.setParams(pstmt, 1);
        rs = pstmt.executeQuery();
        queriedCol = null;
        if (rs.next()) {
          queriedCol = rs.getString("col");
          assertTrue(queriedCol != null);
          assertTrue(COL_VALUE.equals(queriedCol));
        } else {
          fail("Not enough Link3 rows inserted--no row at index " + counter);
        }
      }
      pstmt.close();
      pstmt = null;
      rs = null;

      conn.commit();

      // Query the many-to-many linking table rows.
      pstmt = conn.prepareStatement(QUERY_TERNARY_LINK);
      key1.setParams(pstmt, 1); // use Link1 key value for query
      rs = pstmt.executeQuery();
      queriedCol = null;
      int counter = 0;
      while (rs.next()) {
        counter++;
        queriedCol = rs.getString("col");
        assertTrue(queriedCol != null);
        assertTrue(COL_VALUE.equals(queriedCol));
      }
      pstmt.close();
      pstmt = null;
      rs = null;
      assertTrue(counter == 3);
      conn.commit();
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