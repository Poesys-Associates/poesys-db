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
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.poesys.db.InvalidParametersException;
import com.poesys.db.col.BigDecimalColumnValue;
import com.poesys.db.connection.IConnectionFactory.DBMS;
import com.poesys.db.dao.ConnectionTest;

/**
 * Test the BigDecimalColumnValue class.
 * 
 * @author Bob Muller (muller@computer.org)
 */
public class BigDecimalColumnValueTest extends ConnectionTest  {
  private static final String NAME = "name1";
  private static final String NUMBER = "2345.6789";
  private static final String NUMBER2 = "845639.95748";
  

  /**
   * Test method for {@link com.poesys.db.col.BigDecimalColumnValue#hashCode()}.
   * @throws InvalidParametersException when the BigDecimal can't be created properly
   */
  public void testHashCode() throws InvalidParametersException {
    BigDecimalColumnValue value = new BigDecimalColumnValue(NAME, new BigDecimal(NUMBER));
    BigDecimal testValue = new BigDecimal(NUMBER);
    BigDecimal testValue2 = new BigDecimal(NUMBER2);
    
    assertEquals(value.hashCode(), testValue.hashCode());
    assertTrue(value.hashCode() != testValue2.hashCode());
  }

  /**
   * Test method for {@link com.poesys.db.col.BigDecimalColumnValue#equals(com.poesys.db.col.AbstractColumnValue)}.
   * @throws InvalidParametersException when the BigDecimal can't be created properly
   */
  public void testEqualsColumnValue() throws InvalidParametersException {
    BigDecimalColumnValue value = new BigDecimalColumnValue(NAME, new BigDecimal(NUMBER));
    BigDecimalColumnValue testValue = new BigDecimalColumnValue(NAME, new BigDecimal(NUMBER));
    BigDecimal testValue2 = new BigDecimal(NUMBER2);
    
    assertTrue(value.equals(testValue));
    assertFalse(value.equals(testValue2));
  }

  /**
   * Test method for {@link com.poesys.db.col.BigDecimalColumnValue#setParam(java.sql.PreparedStatement, int)}.
   * @throws InvalidParametersException when the BigDecimal can't be created properly
   * @throws IOException when the connection factory can't get the login parameters
   * @throws SQLException when the SQL statement with parameters fails to prepare
   */
  public void testSetParam() throws InvalidParametersException, SQLException, IOException {
    Connection connection = getConnection(DBMS.MYSQL, "com.poesys.db.poesystest.mysql");
    BigDecimalColumnValue colValue1 = new BigDecimalColumnValue(NAME, new BigDecimal(NUMBER));
    PreparedStatement stmt =
        connection.prepareStatement("SELECT * FROM TEST WHERE testBigDecimal = ?");
    colValue1.setParam(stmt, 1);
    assertTrue(true);
  }

  /**
   * Test method for {@link com.poesys.db.col.BigDecimalColumnValue#BigDecimalColumnValue(java.lang.String, java.math.BigDecimal)}.
   * @throws InvalidParametersException when the BigDecimal can't be created properly
   */
  public void testBigDecimalColumnValue() throws InvalidParametersException {
    @SuppressWarnings("unused")
    BigDecimalColumnValue value = new BigDecimalColumnValue(NAME, new BigDecimal(NUMBER));
    try {
      // Bad parameter, string not number
      @SuppressWarnings("unused")
      BigDecimalColumnValue value2 = new BigDecimalColumnValue(NAME, new BigDecimal(NAME));
      // Bad parameter, null value
      @SuppressWarnings("unused")
      BigDecimalColumnValue value3 = new BigDecimalColumnValue(NAME, null);
      // Bad parameter, null column name
      @SuppressWarnings("unused")
      BigDecimalColumnValue value4 = new BigDecimalColumnValue(null, new BigDecimal(NUMBER));
      assertTrue(false);
    } catch (NumberFormatException e) {
      assertTrue(true);
    } catch (InvalidParametersException e) {
      assertTrue(true);
    }
  }

}