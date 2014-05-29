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


import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.UUID;

import junit.framework.TestCase;

import org.junit.Test;

import com.poesys.db.InvalidParametersException;


/**
 * Test the CompareColumnVisitor class.
 * 
 * @author Robert J. Muller
 */
public class CompareColumnVisitorTest extends TestCase {

  /**
   * Test getComparison().
   * 
   * @throws InvalidParametersException when there is a null parameter
   */
  @Test
  public void testGetComparison() throws InvalidParametersException {
    // Compare two equal strings.
    StringColumnValue col1 = new StringColumnValue("col1", "val1");
    StringColumnValue col2 = new StringColumnValue("col1", "val1");
    CompareColumnVisitor visitor = new CompareColumnVisitor();
    col1.accept(visitor);
    col2.accept(visitor);
    assertTrue(visitor.getComparison() == 0);
  }

  /**
   * Test getComparison() with an error situation.
   * 
   * @throws InvalidParametersException when there is a null parameter
   */
  @Test
  public void testGetBadComparison() throws InvalidParametersException {
    // Test single accept call situation, should throw exception
    StringColumnValue col1 = new StringColumnValue("col1", "val1");
    CompareColumnVisitor visitor = new CompareColumnVisitor();
    col1.accept(visitor);
    try {
      int comp = visitor.getComparison();
      fail("Did not get runtime exception, returned " + comp);
    } catch (RuntimeException e) {
      assertTrue(true);
    }
  }

  /**
   * Test visiting a BigDecimalColumnValue.
   * 
   * @throws InvalidParametersException when there is a null parameter
   */
  @Test
  public void testVisitBigDecimalColumnValue()
      throws InvalidParametersException {
    // Compare two equal values.
    BigDecimalColumnValue col1 =
      new BigDecimalColumnValue("col1", new BigDecimal("1.2"));
    BigDecimalColumnValue col2 =
      new BigDecimalColumnValue("col1", new BigDecimal("1.2"));
    CompareColumnVisitor visitor = new CompareColumnVisitor();
    col1.accept(visitor);
    col2.accept(visitor);
    assertTrue(visitor.getComparison() == 0);
    visitor = null;

    // Less than
    BigDecimalColumnValue col0 =
      new BigDecimalColumnValue("col1", new BigDecimal("0.5"));
    visitor = new CompareColumnVisitor();
    col0.accept(visitor);
    col1.accept(visitor);
    assertTrue(visitor.getComparison() < 0);
    visitor = null;

    // Greater than
    BigDecimalColumnValue col3 =
      new BigDecimalColumnValue("col1", new BigDecimal("3.5"));
    visitor = new CompareColumnVisitor();
    col3.accept(visitor);
    col1.accept(visitor);
    assertTrue(visitor.getComparison() > 0);
  }

  /**
   * Test visiting a BigIntegerColumnValue.
   * 
   * @throws InvalidParametersException when there is a null parameter
   */
  @Test
  public void testVisitBigIntegerColumnValue()
      throws InvalidParametersException {
    // Compare two equal values.
    BigIntegerColumnValue col1 =
      new BigIntegerColumnValue("col1", new BigInteger("1"));
    BigIntegerColumnValue col2 =
      new BigIntegerColumnValue("col1", new BigInteger("1"));
    CompareColumnVisitor visitor = new CompareColumnVisitor();
    col1.accept(visitor);
    col2.accept(visitor);
    assertTrue(visitor.getComparison() == 0);
    visitor = null;

    // Less than
    BigIntegerColumnValue col0 =
      new BigIntegerColumnValue("col1", new BigInteger("0"));
    visitor = new CompareColumnVisitor();
    col0.accept(visitor);
    col1.accept(visitor);
    assertTrue(visitor.getComparison() < 0);
    visitor = null;

    // Greater than
    BigIntegerColumnValue col3 =
      new BigIntegerColumnValue("col1", new BigInteger("3"));
    visitor = new CompareColumnVisitor();
    col3.accept(visitor);
    col1.accept(visitor);
    assertTrue(visitor.getComparison() > 0);
  }

  /**
   * Test visiting a DateColumnValue.
   * 
   * @throws InvalidParametersException when there is a null parameter
   */
  @Test
  public void testVisitDateColumnValue() throws InvalidParametersException {
    // Compare two equal values.
    Date date1 = new Date(System.currentTimeMillis());
    DateColumnValue col1 = new DateColumnValue("col1", date1);
    DateColumnValue col2 = new DateColumnValue("col1", date1);
    CompareColumnVisitor visitor = new CompareColumnVisitor();
    col1.accept(visitor);
    col2.accept(visitor);
    assertTrue(visitor.getComparison() == 0);
    visitor = null;

    // Less than
    Date date0 = new Date(date1.getTime() - 3000);
    DateColumnValue col0 = new DateColumnValue("col1", date0);
    visitor = new CompareColumnVisitor();
    col0.accept(visitor);
    col1.accept(visitor);
    assertTrue(visitor.getComparison() < 0);
    visitor = null;

    // Greater than
    Date date3 = new Date(date1.getTime() + 3000);
    DateColumnValue col3 = new DateColumnValue("col1", date3);
    visitor = new CompareColumnVisitor();
    col3.accept(visitor);
    col1.accept(visitor);
    assertTrue(visitor.getComparison() > 0);
  }

  /**
   * Test visiting a StringColumnValue.
   * 
   * @throws InvalidParametersException when there is a null parameter
   */
  @Test
  public void testVisitStringColumnValue() throws InvalidParametersException {
    // Compare two equal strings.
    StringColumnValue col1 = new StringColumnValue("col1", "val1");
    StringColumnValue col2 = new StringColumnValue("col1", "val1");
    CompareColumnVisitor visitor = new CompareColumnVisitor();
    col1.accept(visitor);
    col2.accept(visitor);
    assertTrue(visitor.getComparison() == 0);
    visitor = null;

    // Less than
    StringColumnValue col0 = new StringColumnValue("col1", "val0");
    visitor = new CompareColumnVisitor();
    col0.accept(visitor);
    col1.accept(visitor);
    assertTrue(visitor.getComparison() < 0);
    visitor = null;

    // Greater than
    StringColumnValue col3 = new StringColumnValue("col1", "val3");
    visitor = new CompareColumnVisitor();
    col3.accept(visitor);
    col1.accept(visitor);
    assertTrue(visitor.getComparison() > 0);
  }

  /**
   * Test visiting a TimestampColumnValue.
   * 
   * @throws InvalidParametersException when there is a null parameter
   */
  @Test
  public void testVisitTimestampColumnValue() throws InvalidParametersException {
    // Compare two equal values.
    Timestamp timestamp1 = new Timestamp(System.currentTimeMillis());
    TimestampColumnValue col1 = new TimestampColumnValue("col1", timestamp1);
    TimestampColumnValue col2 = new TimestampColumnValue("col1", timestamp1);
    CompareColumnVisitor visitor = new CompareColumnVisitor();
    col1.accept(visitor);
    col2.accept(visitor);
    assertTrue(visitor.getComparison() == 0);
    visitor = null;

    // Less than
    Timestamp timestamp0 = new Timestamp(timestamp1.getTime() - 3000);
    TimestampColumnValue col0 = new TimestampColumnValue("col1", timestamp0);
    visitor = new CompareColumnVisitor();
    col0.accept(visitor);
    col1.accept(visitor);
    assertTrue(visitor.getComparison() < 0);
    visitor = null;

    // Greater than
    Timestamp timestamp3 = new Timestamp(timestamp1.getTime() + 3000);
    TimestampColumnValue col3 = new TimestampColumnValue("col1", timestamp3);
    visitor = new CompareColumnVisitor();
    col3.accept(visitor);
    col1.accept(visitor);
    assertTrue(visitor.getComparison() > 0);
  }

  /**
   * Test visiting a UuidColumnValue.
   * 
   * @throws InvalidParametersException when there is a null parameter
   */
  @Test
  public void testVisitUuidColumnValue() throws InvalidParametersException {
    // Compare two equal values.
    UuidColumnValue col1 = new UuidColumnValue("col1", UUID.randomUUID());
    UuidColumnValue col2 = new UuidColumnValue("col1", col1.getValue());
    CompareColumnVisitor visitor = new CompareColumnVisitor();
    col1.accept(visitor);
    col2.accept(visitor);
    assertTrue(visitor.getComparison() == 0);
    visitor = null;

    // Different (less or greater)
    UuidColumnValue col0 = new UuidColumnValue("col1", UUID.randomUUID());
    visitor = new CompareColumnVisitor();
    col0.accept(visitor);
    col1.accept(visitor);
    assertTrue(visitor.getComparison() != 0);
  }
}
