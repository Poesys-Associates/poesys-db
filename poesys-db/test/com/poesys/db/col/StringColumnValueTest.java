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
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.poesys.db.InvalidParametersException;
import com.poesys.db.dao.ConnectionTest;


/**
 * Test both the abstract ColumnValue and the concrete StringValue classes.
 * Requires an Oracle user test, password test, with a table Test with a
 * stringTest column defined as VARCHAR2.
 * 
 * @author Robert J. Muller
 */
public class StringColumnValueTest extends ConnectionTest {
  String name1 = "name1";
  String name2 = "name2";
  String name3 = "name3";
  String value1 = "string1";
  String value2 = "string2";
  String value3 = "String1"; // upper-case initial character
  String value4 = "string1"; // same as value1
  BigDecimal value5 = new BigDecimal("1234.5678");

  /**
   * Test method for {@link com.poesys.db.col.StringColumnValue#hashCode()}.
   * 
   * @throws InvalidParametersException when there is a constructor error
   */
  public void testHashCode() throws InvalidParametersException {
    StringColumnValue colValue = new StringColumnValue(name1, value1);
    assertTrue(value1.hashCode() == colValue.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.db.col.StringColumnValue#equals(com.poesys.db.col.AbstractColumnValue)}
   * .
   * 
   * @throws InvalidParametersException when there is a null parameter
   */
  public void testEqualsColumnValue() throws InvalidParametersException {
    StringColumnValue colValue1 = new StringColumnValue(name1, value1);
    StringColumnValue colValue2 = new StringColumnValue(name2, value2);
    StringColumnValue colValue3 = new StringColumnValue(name3, value1);
    StringColumnValue colValue4 = new StringColumnValue(name1, value1);
    StringColumnValue colValue5 = new StringColumnValue(name1, value3);

    assertTrue(colValue1.equals(colValue4)); // same name and value
    assertFalse(colValue1.equals(colValue2)); // both different
    assertFalse(colValue1.equals(colValue3)); // value different
    assertFalse(colValue1.equals(colValue5)); // upper-case letter

  }

  /**
   * Test method for
   * {@link com.poesys.db.col.StringColumnValue#setParam(java.sql.PreparedStatement, int)}
   * .
   * 
   * @throws InvalidParametersException when there is a constructor failure
   * @throws IOException when there is a DBMS connection issue
   * @throws SQLException when there is a DBMS connection issue
   */
  public void testSetParam() throws InvalidParametersException, SQLException,
      IOException {
    Connection connection = null;
    try {
      connection = getConnection();
      StringColumnValue colValue1 = new StringColumnValue(name1, value1);
      PreparedStatement stmt =
        connection.prepareStatement("SELECT * FROM TEST WHERE testString = ?");
      colValue1.setParam(stmt, 1);
      assertTrue(true);
      connection.commit();
    } finally {
      if (connection != null) {
        connection.close();
      }
    }
  }

  /**
   * Test method for
   * {@link com.poesys.db.col.StringColumnValue#StringColumnValue(java.lang.String, java.lang.String)}
   * .
   * 
   * @throws InvalidParametersException when the constructor fails
   */
  public void testStringColumnValue() throws InvalidParametersException {
    StringColumnValue colValue1 = new StringColumnValue(name1, value1);
    assertTrue(colValue1.getName().equals(name1));
    assertTrue(value1.equals(value1));

    // Supply a null column name.
    try {
      @SuppressWarnings("unused")
      StringColumnValue colValue6 = new StringColumnValue(null, value1);
      assertTrue(false);
    } catch (InvalidParametersException e) {
      assertTrue(true);
    }

    // Supply a null column value
    try {
      @SuppressWarnings("unused")
      StringColumnValue colValue6 = new StringColumnValue(name1, null);
      assertTrue(false);
    } catch (InvalidParametersException e) {
      assertTrue(true);
    }
  }

  /**
   * Test method for {@link com.poesys.db.col.AbstractColumnValue#getName()}.
   * 
   * @throws InvalidParametersException when there is a constructor failure
   */
  public void testGetName() throws InvalidParametersException {
    StringColumnValue colValue1 = new StringColumnValue(name1, value1);
    assertTrue(colValue1.getName().equals(name1));
  }

  /**
   * Test method for
   * {@link com.poesys.db.col.AbstractColumnValue#setName(java.lang.String)}.
   * 
   * @throws InvalidParametersException when there is a constructor failure
   */
  public void testSetName() throws InvalidParametersException {
    StringColumnValue colValue1 = new StringColumnValue(name1, value1);
    assertTrue(colValue1.getName().equals(name1));
    colValue1.setName(name2);
    assertTrue(colValue1.getName().equals(name2));
  }

  /**
   * Test method for valid comparisons for
   * {@link com.poesys.db.col.AbstractColumnValue#compareTo(com.poesys.db.col.AbstractColumnValue)}
   * .
   * 
   * @throws InvalidParametersException when there is a null parameter
   */
  public void testCompareToValid() throws InvalidParametersException {
    StringColumnValue colValue1 = new StringColumnValue(name1, value1);
    StringColumnValue colValue2 = new StringColumnValue(name1, value2);
    StringColumnValue colValue3 = new StringColumnValue(name1, value4);

    // Test equality
    assertTrue(colValue1.compareTo(colValue3) == 0);

    // Test less than
    assertTrue(colValue1.compareTo(colValue2) == -1);

    // Test greater than
    assertTrue(colValue2.compareTo(colValue1) == 1);
  }

  /**
   * Test method for invalid comparisons (different column names) for
   * {@link com.poesys.db.col.AbstractColumnValue#compareTo(com.poesys.db.col.AbstractColumnValue)}
   * .
   * 
   * @throws InvalidParametersException when there is a null parameter
   */
  public void testCompareToDiffCols() throws InvalidParametersException {
    StringColumnValue colValue1 = new StringColumnValue(name1, value1);
    StringColumnValue colValue2 = new StringColumnValue(name2, value2);

    try {
      int comp = colValue1.compareTo(colValue2);
      fail("Did not throw runtime exception for different column names, returned "
           + comp);
    } catch (RuntimeException e) {
      assertTrue(true);
    }
  }

  /**
   * Test method for invalid comparisons (different value types) for
   * {@link com.poesys.db.col.AbstractColumnValue#compareTo(com.poesys.db.col.AbstractColumnValue)}
   * .
   * 
   * @throws InvalidParametersException when there is a null parameter
   */
  public void testCompareToDiffTypes() throws InvalidParametersException {
    StringColumnValue colValue1 = new StringColumnValue(name1, value1);
    BigDecimalColumnValue colValue2 = new BigDecimalColumnValue(name2, value5);

    try {
      int comp = colValue1.compareTo(colValue2);
      fail("Did not throw runtime exception for different column types, returned "
           + comp);
    } catch (RuntimeException e) {
      assertTrue(true);
    }
  }
}
