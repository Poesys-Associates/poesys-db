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
 * 
 */
package com.poesys.db.col;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import com.poesys.db.InvalidParametersException;
import com.poesys.db.col.UuidColumnValue;
import com.poesys.db.connection.IConnectionFactory.DBMS;
import com.poesys.db.dao.ConnectionTest;


/**
 * 
 * @author Bob Muller (muller@computer.org)
 */
public class UuidColumnValueTest extends ConnectionTest {
  String name1 = "name1";
  String name2 = "name2";
  String name3 = "name3";
  UUID uuid1 = UUID.randomUUID();
  UUID uuid2 = UUID.randomUUID();

  /**
   * Test method for {@link com.poesys.db.col.UuidColumnValue#hashCode()}.
   * 
   * @throws InvalidParametersException when there is a null parameter
   */
  public void testHashCode() throws InvalidParametersException {
    UuidColumnValue colValue = new UuidColumnValue(name1, uuid1);
    assertTrue(uuid1.hashCode() == colValue.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.db.col.UuidColumnValue#equals(com.poesys.db.col.AbstractColumnValue)}.
   * 
   * @throws InvalidParametersException when there is a null parameter
   */
  public void testEqualsColumnValue() throws InvalidParametersException {
    UuidColumnValue colValue1 = new UuidColumnValue(name1, uuid1);
    UuidColumnValue colValue2 = new UuidColumnValue(name2, uuid2);
    UuidColumnValue colValue3 = new UuidColumnValue(name3, uuid2);
    UuidColumnValue colValue4 = new UuidColumnValue(name1, uuid1);

    assertTrue(colValue1.equals(colValue4)); // same name and value
    assertFalse(colValue1.equals(colValue2)); // both different
    assertFalse(colValue1.equals(colValue3)); // value different
  }

  /**
   * Test method for
   * {@link com.poesys.db.col.UuidColumnValue#setParam(java.sql.PreparedStatement, int)}.
   * 
   * @throws IOException when can't get property
   * @throws SQLException when can't get connection
   * @throws InvalidParametersException when there is a null parameter
   */
  public void testSetParam() throws SQLException, IOException,
      InvalidParametersException {
    Connection connection = getConnection(DBMS.MYSQL, "com.poesys.db.poesystest.mysql");
    UuidColumnValue colValue1 = new UuidColumnValue(name1, uuid1);
    PreparedStatement stmt =
        connection.prepareStatement("SELECT * FROM TEST WHERE testUuid = ?");
    colValue1.setParam(stmt, 1);
    assertTrue(true);
  }

  /**
   * Test method for
   * {@link com.poesys.db.col.UuidColumnValue#UuidColumnValue(String, UUID)}.
   * 
   * @throws InvalidParametersException when there is a null parameter
   */
  public void testUuidColumnValue() throws InvalidParametersException {
    UuidColumnValue colValue1 = new UuidColumnValue(name1, uuid1);
    assertTrue(colValue1.getName().equals(name1));
    assertTrue(uuid1.equals(uuid1));

    // Supply a null column name.
    try {
      @SuppressWarnings("unused")
      UuidColumnValue colValue6 = new UuidColumnValue(null, uuid1);
      assertTrue(false);
    } catch (InvalidParametersException e) {
      assertTrue(true);
    }

    // Supply a null column value
    try {
      @SuppressWarnings("unused")
      UuidColumnValue colValue6 = new UuidColumnValue(name1, null);
      assertTrue(false);
    } catch (InvalidParametersException e) {
      assertTrue(true);
    }
  }

}
