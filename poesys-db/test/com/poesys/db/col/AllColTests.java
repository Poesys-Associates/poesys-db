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


import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test suite for the col subsystem.
 * 
 * @author Robert J. Muller
 */
public class AllColTests {

  /**
   * Run the suite.
   * 
   * @return a test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for com.poesys.db.col");
    // $JUnit-BEGIN$
    suite.addTestSuite(StringColumnValueTest.class);
    suite.addTestSuite(DateColumnValueTest.class);
    suite.addTestSuite(UuidColumnValueTest.class);
    suite.addTestSuite(BigDecimalColumnValueTest.class);
    suite.addTestSuite(TimestampColumnValueTest.class);
    suite.addTestSuite(BigIntegerColumnValueTest.class);
    suite.addTestSuite(CompareColumnVisitorTest.class);
    // $JUnit-END$
    return suite;
  }

}
