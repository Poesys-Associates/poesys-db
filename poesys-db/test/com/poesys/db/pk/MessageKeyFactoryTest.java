/*
 * Copyright (c) 2011 Poesys Associates. All rights reserved.
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

package com.poesys.db.pk;


import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;


/**
 * Test the message key factory.
 * 
 * @author Robert J. Muller
 */
public class MessageKeyFactoryTest {

  /**
   * Test method for
   * {@link com.poesys.db.pk.MessageKeyFactory#getKey(com.poesys.ms.pk.IPrimaryKey)}
   * .
   */
  @Test
  public void testGetKeyGuidPrimaryKey() {
    // Create a GUID message DTO.
    UUID uuid = UUID.randomUUID();
    String name = "col";
    com.poesys.ms.pk.IPrimaryKey messageKey =
      new com.poesys.ms.pk.GuidPrimaryKey(name,
                                          uuid,
                                          "com.poesys.db.dto.TestSequence");
    // Test creation of IPrimaryKey object.
    IPrimaryKey key = MessageKeyFactory.getKey(messageKey);
    assertTrue(key != null);
  }

}
