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
import com.poesys.db.InvalidParametersException;
import com.poesys.db.Message;
import com.poesys.db.NoPrimaryKeyException;
import com.poesys.db.dao.ConnectionTest;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.Link1;
import com.poesys.db.dto.Link2;
import com.poesys.db.dto.ManyToManyLink;
import com.poesys.db.pk.AbstractSingleValuedPrimaryKey;
import com.poesys.db.pk.AssociationPrimaryKey;
import com.poesys.db.pk.IPrimaryKey;
import com.poesys.db.pk.PrimaryKeyFactory;


/**
 * Test the specialization/inheritance insertion capability.
 * 
 * @author Bob Muller (muller@computer.org)
 */
public class InsertManyToManyLink1Test extends ConnectionTest {
  private static final String QUERY_LINK1 =
    "SELECT col FROM Link1 WHERE link1_id = ?";
  private static final String QUERY_LINK2 =
    "SELECT col FROM Link2 WHERE link2_id = ?";
  private static final String QUERY_M2M_LINK =
    "SELECT col FROM ManyToManyLink WHERE link1_id = ?";
  private static final String KEY1_NAME = "link1_id";
  private static final String KEY2_NAME = "link2_id";
  private static final String COL_VALUE = "string";
  private static final String CLASS_NAME = "com.poesys.db.test.ManyToManyLink1";

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
      throw new RuntimeException("Connect failed: " + e.getMessage(), e);
    }

    // Create the insert commands (class under test) for the two linked tables.
    Insert<Link1> cut1 = new Insert<Link1>(new InsertSqlLink1());
    Insert<Link2> cut2 = new Insert<Link2>(new InsertSqlLink2());

    // Create the sequence primary keys for the objects.
    AbstractSingleValuedPrimaryKey key1 = null;
    AbstractSingleValuedPrimaryKey key21 = null;
    AbstractSingleValuedPrimaryKey key22 = null;
    AbstractSingleValuedPrimaryKey key23 = null;
    try {
      key1 =
        PrimaryKeyFactory.createMySqlSequenceKey(conn,
                                                 "link1",
                                                 KEY1_NAME,
                                                 CLASS_NAME);
      key21 =
        PrimaryKeyFactory.createMySqlSequenceKey(conn,
                                                 "link2",
                                                 KEY2_NAME,
                                                 CLASS_NAME);
      key22 =
        PrimaryKeyFactory.createMySqlSequenceKey(conn,
                                                 "link2",
                                                 KEY2_NAME,
                                                 CLASS_NAME);
      key23 =
        PrimaryKeyFactory.createMySqlSequenceKey(conn,
                                                 "link2",
                                                 KEY2_NAME,
                                                 CLASS_NAME);
    } catch (InvalidParametersException e1) {
      fail(e1.getMessage());
    } catch (NoPrimaryKeyException e1) {
      fail(Message.getMessage(e1.getMessage(), e1.getParameters().toArray()));
    }

    // Create the Link1 and Link2 DTOs with the key and the string column.
    Link1 link1Dto = new Link1(key1, COL_VALUE);
    Link2 link21Dto = new Link2(key21, COL_VALUE);
    Link2 link22Dto = new Link2(key22, COL_VALUE);
    Link2 link23Dto = new Link2(key23, COL_VALUE);

    // Create the links and add them to the Link1 object.
    List<IPrimaryKey> keylist1_2_1 = new CopyOnWriteArrayList<IPrimaryKey>();
    keylist1_2_1.add(key1); // Link1 object
    keylist1_2_1.add(key21); // first link2 object
    AssociationPrimaryKey key1_2_1 =
      new AssociationPrimaryKey(keylist1_2_1, CLASS_NAME);
    ManyToManyLink link1_2_1 = new ManyToManyLink(key1_2_1, COL_VALUE);
    link1_2_1.setLink1(link1Dto);
    link1_2_1.setLink2(link21Dto);

    List<IPrimaryKey> keylist1_2_2 = new CopyOnWriteArrayList<IPrimaryKey>();
    keylist1_2_2.add(key1); // Link1 object
    keylist1_2_2.add(key22); // second link2 object
    AssociationPrimaryKey key1_2_2 =
      new AssociationPrimaryKey(keylist1_2_2, CLASS_NAME);
    ManyToManyLink link1_2_2 = new ManyToManyLink(key1_2_2, COL_VALUE);
    link1_2_2.setLink1(link1Dto);
    link1_2_2.setLink2(link22Dto);

    List<IPrimaryKey> keylist1_2_3 = new CopyOnWriteArrayList<IPrimaryKey>();
    keylist1_2_3.add(key1); // Link1 object
    keylist1_2_3.add(key23); // third link2 object
    AssociationPrimaryKey key1_2_3 =
      new AssociationPrimaryKey(keylist1_2_3, CLASS_NAME);
    ManyToManyLink link1_2_3 = new ManyToManyLink(key1_2_3, COL_VALUE);
    link1_2_3.setLink1(link1Dto);
    link1_2_3.setLink2(link23Dto);

    List<ManyToManyLink> links1 = new CopyOnWriteArrayList<ManyToManyLink>();
    links1.add(link1_2_1);
    links1.add(link1_2_2);
    links1.add(link1_2_3);

    List<ManyToManyLink> links21 = new CopyOnWriteArrayList<ManyToManyLink>();
    links1.add(link1_2_1);

    List<ManyToManyLink> links22 = new CopyOnWriteArrayList<ManyToManyLink>();
    links1.add(link1_2_2);

    List<ManyToManyLink> links23 = new CopyOnWriteArrayList<ManyToManyLink>();
    links1.add(link1_2_3);

    // Add the links to the link objects.
    link1Dto.setM2mLinks(links1);
    link21Dto.setM2mLinks(links21);
    link22Dto.setM2mLinks(links22);
    link23Dto.setM2mLinks(links23);

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
      stmt.executeUpdate("DELETE FROM ManyToManyLink");
      stmt.close();
      stmt = null;

      // Insert the Link2 objects. There should be no exceptions from attempts
      // to
      // insert links.
      cut2.insert(conn, link21Dto);
      cut2.insert(conn, link22Dto);
      cut2.insert(conn, link23Dto);

      // Insert the Link1 object. The links should be inserted.
      cut1.insert(conn, link1Dto);

      // Test the flags.
      assertTrue("status not EXISTING for inserted Link1: "
                     + link1Dto.getStatus(),
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

      // Query the many-to-many linking table rows.
      pstmt = conn.prepareStatement(QUERY_M2M_LINK);
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