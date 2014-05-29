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
package com.poesys.db.pk;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.poesys.db.InvalidParametersException;


/**
 * 
 * @author Robert J. Muller
 */
public class AssociationKeyMappingTest {

  /**
   * Test method for
   * {@link com.poesys.db.pk.AssociationKeyMapping#map(java.lang.Integer, java.lang.String, java.lang.String)}.
   */
  @Test
  public void testMap() {
    AssociationKeyMapping mapping = new AssociationKeyMapping(2);
    assertTrue("null mapping", mapping != null);
    mapping.map(0, "A1", "A1_1");
    mapping.map(0, "A2", "A2_1");
    mapping.map(1, "A1", "A1_2");
    mapping.map(1, "A2", "A2_2");
    assertTrue("Mapping not done", mapping.lookUp(1, "A1").equals("A1_2"));
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.AssociationKeyMapping#map(java.lang.Integer, java.lang.String, java.lang.String)}.
   */
  @Test
  public void testMapDupName() {
    AssociationKeyMapping mapping = new AssociationKeyMapping(2);
    assertTrue("null mapping", mapping != null);
    mapping.map(0, "A1", "A1_1");
    mapping.map(0, "A2", "A2_1");
    try {
      mapping.map(0, "A1", "A1_2");
    } catch (InvalidParametersException e) {
      assertTrue(true);
      return;
    }
    fail("Did not throw exception for duplicate name");
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.AssociationKeyMapping#map(java.lang.Integer, java.lang.String, java.lang.String)}.
   */
  @Test
  public void testMapNumberOfKeysTooSmall() {
    try {
      @SuppressWarnings("unused")
      AssociationKeyMapping mapping = new AssociationKeyMapping(1);
    } catch (InvalidParametersException e) {
      assertTrue(true);
      return;
    }
    fail("Didn't throw exception for not enough keys");
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.AssociationKeyMapping#lookUp(java.lang.Integer, java.lang.String)}.
   */
  @Test
  public void testLookUp() {
    AssociationKeyMapping mapping = new AssociationKeyMapping(2);
    assertTrue("null mapping", mapping != null);
    mapping.map(0, "A1", "A1_1");
    mapping.map(0, "A2", "A2_1");
    mapping.map(1, "A1", "A1_2");
    mapping.map(1, "A2", "A2_2");
    assertTrue("Mapping not done", mapping.lookUp(1, "A1").equals("A1_2"));
  }

}
