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
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.poesys.db.InvalidParametersException;
import com.poesys.db.dao.ConnectionTest;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * Test the BigIntegerColumnValue class.
 * 
 * @author Robert J. Muller
 */
public class BigIntegerColumnValueTest extends ConnectionTest {
  private static final String NAME = "name1";
  private static final String NUMBER = "2345";
  private static final String NUMBER2 = "845639";

  /**
   * Test method for {@link com.poesys.db.col.BigIntegerColumnValue#hashCode()}.
   * 
   * @throws InvalidParametersException when the BigInteger can't be created
   *           properly
   */
  @Test
  public void testHashCode() throws InvalidParametersException {
    BigIntegerColumnValue value =
      new BigIntegerColumnValue(NAME, new BigInteger(NUMBER));
    BigInteger testValue = new BigInteger(NUMBER);
    BigInteger testValue2 = new BigInteger(NUMBER2);

    assertEquals(value.hashCode(), testValue.hashCode());
    assertTrue(value.hashCode() != testValue2.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.db.col.BigIntegerColumnValue#equals(com.poesys.db.col.AbstractColumnValue)}
   * .
   * 
   * @throws InvalidParametersException when the BigInteger can't be created
   *           properly
   */
  @Test
  public void testEqualsColumnValue() throws InvalidParametersException {
    BigIntegerColumnValue value =
      new BigIntegerColumnValue(NAME, new BigInteger(NUMBER));
    BigIntegerColumnValue testValue =
      new BigIntegerColumnValue(NAME, new BigInteger(NUMBER));
    BigDecimal testValue2 = new BigDecimal(NUMBER2);

    assertTrue(value.equals(testValue));
    //noinspection EqualsBetweenInconvertibleTypes
    assertFalse(value.equals(testValue2));
  }

  /**
   * Test method for
   * {@link com.poesys.db.col.BigIntegerColumnValue#setParam(java.sql.PreparedStatement, int)}
   * .
   * 
   * @throws IOException when the connection factory can't read the connection
   *           settings
   * @throws SQLException then the connection fails or the SQL statement
   *           parameter setting fails
   * @throws InvalidParametersException when the BigInteger can't be created
   *           properly
   */
  @Test
  public void testSetParam() throws SQLException, IOException,
      InvalidParametersException {
    Connection connection = null;
    try {
      connection = getConnection();
      BigIntegerColumnValue colValue1 =
        new BigIntegerColumnValue(NAME, new BigInteger(NUMBER));
      PreparedStatement stmt =
        connection.prepareStatement("SELECT * FROM TEST WHERE testLong = ?");
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
   * {@link com.poesys.db.col.BigIntegerColumnValue#BigIntegerColumnValue(java.lang.String, java.math.BigInteger)}
   * .
   * 
   * @throws InvalidParametersException when the BigInteger can't be created
   *           properly
   */
  @Test
  public void testBigIntegerColumnValue() throws InvalidParametersException {
    @SuppressWarnings("unused")
    BigDecimalColumnValue value =
      new BigDecimalColumnValue(NAME, new BigDecimal(NUMBER));
    try {
      // Bad parameter, string not number
      @SuppressWarnings("unused")
      BigDecimalColumnValue value2 =
        new BigDecimalColumnValue(NAME, new BigDecimal(NAME));
      // Bad parameter, null value
      @SuppressWarnings("unused")
      BigDecimalColumnValue value3 = new BigDecimalColumnValue(NAME, null);
      // Bad parameter, null column name
      @SuppressWarnings("unused")
      BigDecimalColumnValue value4 =
        new BigDecimalColumnValue(null, new BigDecimal(NUMBER));
      fail("No exception from bad parameters");
    } catch (NumberFormatException | InvalidParametersException e) {
      // success
    }
  }

}
