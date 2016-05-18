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
package com.poesys.db.col;


import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.poesys.db.InvalidParametersException;
import com.poesys.db.dao.ConnectionTest;


/**
 * Test the DateColumnValue class.
 * 
 * @author Bob Muller (muller@computer.org)
 */
public class DateColumnValueTest extends ConnectionTest {
  String name1 = "name1";
  String name2 = "name2";
  String name3 = "name3";
  Date value1 = new Date(System.currentTimeMillis());
  Date value2 = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000); // current

  // + 1
  // day

  /**
   * Test method for {@link com.poesys.db.col.DateColumnValue#hashCode()}.
   * 
   * @throws InvalidParametersException when there is a null parameter
   */
  public void testHashCode() throws InvalidParametersException {
    DateColumnValue colValue = new DateColumnValue(name1, value1);
    assertTrue(value1.hashCode() == colValue.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.db.col.DateColumnValue#equals(com.poesys.db.col.AbstractColumnValue)}
   * .
   * 
   * @throws InvalidParametersException when there is a null parameter
   */
  public void testEqualsColumnValue() throws InvalidParametersException {
    DateColumnValue colValue1 = new DateColumnValue(name1, value1);
    DateColumnValue colValue2 = new DateColumnValue(name2, value2);
    DateColumnValue colValue3 = new DateColumnValue(name3, value1);
    DateColumnValue colValue4 = new DateColumnValue(name1, value1);

    assertTrue(colValue1.equals(colValue4)); // same name and value
    assertFalse(colValue1.equals(colValue2)); // both different
    assertFalse(colValue1.equals(colValue3)); // value different
  }

  /**
   * Test method for
   * {@link com.poesys.db.col.DateColumnValue#setParam(java.sql.PreparedStatement, int)}
   * .
   * 
   * @throws IOException when can't get property
   * @throws SQLException when can't get connection
   * @throws InvalidParametersException when there is a null parameter
   */
  public void testSetParam() throws SQLException, IOException,
      InvalidParametersException {
    Connection connection = getConnection();
    DateColumnValue colValue1 = new DateColumnValue(name1, value1);
    PreparedStatement stmt =
      connection.prepareStatement("SELECT * FROM TEST WHERE testDate = ?");
    colValue1.setParam(stmt, 1);
    assertTrue(true);
  }

  /**
   * Test method for
   * {@link com.poesys.db.col.DateColumnValue#DateColumnValue(java.lang.String, java.sql.Date)}
   * .
   * 
   * @throws InvalidParametersException when there is a null parameter
   */
  public void testDateColumnValue() throws InvalidParametersException {
    DateColumnValue colValue1 = new DateColumnValue(name1, value1);
    assertTrue(colValue1.getName().equals(name1));
    assertTrue(value1.equals(value1));

    // Supply a null column name.
    try {
      @SuppressWarnings("unused")
      DateColumnValue colValue6 = new DateColumnValue(null, value1);
      assertTrue(false);
    } catch (InvalidParametersException e) {
      assertTrue(true);
    }

    // Supply a null column value
    try {
      @SuppressWarnings("unused")
      DateColumnValue colValue6 = new DateColumnValue(name1, null);
      assertTrue(false);
    } catch (InvalidParametersException e) {
      assertTrue(true);
    }
  }

}
