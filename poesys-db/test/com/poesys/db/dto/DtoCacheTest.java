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
package com.poesys.db.dto;


import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.junit.Test;

import com.poesys.db.col.AbstractColumnValue;
import com.poesys.db.col.StringColumnValue;
import com.poesys.db.pk.IPrimaryKey;
import com.poesys.db.pk.PrimaryKeyFactory;


/**
 * Test the DtoCache class.
 * 
 * @author Robert J. Muller
 */
public class DtoCacheTest {
  private static final String KEY1 = "key1";
  private static final String KEY2 = "key2";
  private static final BigDecimal COL1 = new BigDecimal("100.5");

  /**
   * Test method for
   * {@link com.poesys.db.dto.DtoCache#cache(com.poesys.db.dto.IDbDto)}. Test
   * method for
   * {@link com.poesys.db.dto.DtoCache#get(com.poesys.db.pk.IPrimaryKey)}. Tests
   * getting based on the same primary key object.
   */
  @Test
  public void testCache() {
    IDtoCache<TestNatural> cache =
      new DtoCache<TestNatural>(TestNatural.class.getName());
    TestNatural test = new TestNatural(KEY1, KEY2, COL1);
    IPrimaryKey key = test.getPrimaryKey();
    cache.cache(test);
    assertTrue(true);
    TestNatural test2 = (TestNatural)cache.get(key);
    assertTrue("Cannot get test object from cache", test2 != null);
    assertTrue("Cached object not identical to original object",
               test.equals(test2));
  }

  /**
   * Test method for
   * {@link com.poesys.db.dto.DtoCache#cache(com.poesys.db.dto.IDbDto)}. Test
   * method for
   * {@link com.poesys.db.dto.DtoCache#get(com.poesys.db.pk.IPrimaryKey)}. Tests
   * getting based on a different primary key object.
   */
  @Test
  public void testCacheDiff() {
    IDtoCache<TestNatural> cache =
      new DtoCache<TestNatural>(TestNatural.class.getName());

    // Create a Test Natural object with a primary key embedded.
    TestNatural test = new TestNatural(KEY1, KEY2, COL1);
    int hash1 = test.getPrimaryKey().hashCode();

    // Create a separate primary key using the key columns.
    ArrayList<AbstractColumnValue> list =
      new ArrayList<AbstractColumnValue>();
    list.add(new StringColumnValue(KEY1, KEY1));
    list.add(new StringColumnValue(KEY2, KEY2));
    IPrimaryKey key =
      PrimaryKeyFactory.createNaturalKey(list, "com.poesys.db.dto.TestNatural");
    int hash2 = key.hashCode();

    assertTrue("Identical key hash codes not the same: " + hash1 + ", " + hash2,
               hash1 == hash2);

    // Cache the Test Natural object with its key as the key value.
    cache.cache(test);
    assertTrue(true);

    // Use the separately created key to get the object.
    System.out.println("Getting object " + key.getValueList());
    TestNatural test2 = (TestNatural)cache.get(key);
    assertTrue("Cannot get test object from cache", test2 != null);
    assertTrue("Cached object not identical to original object",
               test.equals(test2));
  }

  /**
   * Test method for
   * {@link com.poesys.db.dto.DtoCache#remove(com.poesys.db.pk.IPrimaryKey)}.
   */
  @Test
  public void testRemove() {
    IDtoCache<TestNatural> cache =
      new DtoCache<TestNatural>(TestNatural.class.getName());
    TestNatural test = new TestNatural(KEY1, KEY2, COL1);
    IPrimaryKey key = test.getPrimaryKey();
    cache.cache(test);
    assertTrue(true);
    TestNatural test2 = (TestNatural)cache.get(key);
    assertTrue("Cannot get test object from cache", test2 != null);
    assertTrue("Cached object not identical to original object",
               test.equals(test2));
    cache.remove(key);
    test2 = (TestNatural)cache.get(key);
    assertTrue("Cached object not removed", test2 == null);
  }

}
