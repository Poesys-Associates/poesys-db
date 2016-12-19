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
package com.poesys.db.pk;


import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import com.poesys.db.DuplicateKeyNameException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.col.AbstractColumnValue;
import com.poesys.db.dao.ConnectionTest;


/**
 * Test the AssociationPrimaryKey class.
 * 
 * @author Robert J. Muller
 */
public class AssociationPrimaryKeyTest extends ConnectionTest {
  private String col1Name = "col1";
  private String col2Name = "col2";
  private String col3Name = "col3";
  private AbstractSingleValuedPrimaryKey key1 = null;
  private AbstractSingleValuedPrimaryKey key2 = null;
  private AbstractSingleValuedPrimaryKey key3 = null;
  private AbstractSingleValuedPrimaryKey key4 = null;
  private AbstractSingleValuedPrimaryKey key5 = null;
  private AbstractSingleValuedPrimaryKey key6 = null;

  private static final String CLASS_NAME = "com.poesys.db.dto.TestSequence";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    try {
      // Define two primary keys with different column names and values.
      key1 = new SequencePrimaryKey(col1Name, new BigInteger("1"), CLASS_NAME);
      key2 =
        new SequencePrimaryKey(col2Name, new BigInteger("200"), CLASS_NAME);
      // Define primary key with key1 name and key2 value
      key3 =
        new SequencePrimaryKey(col1Name, new BigInteger("200"), CLASS_NAME);
      key4 =
        new SequencePrimaryKey(col2Name, new BigInteger("300"), CLASS_NAME);
      key5 =
        new SequencePrimaryKey(col3Name, new BigInteger("200"), CLASS_NAME);
      key6 =
        new SequencePrimaryKey(col3Name, new BigInteger("300"), CLASS_NAME);
    } catch (InvalidParametersException e) {
      throw new Exception("Invalid parameter to sequence primary key creation",
                          e);
    }
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.AssociationPrimaryKey#AssociationPrimaryKey(java.util.List,String)}
   * .
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  public void testAssociationPrimaryKey() throws InvalidParametersException,
      DuplicateKeyNameException {
    List<IPrimaryKey> list = new CopyOnWriteArrayList<IPrimaryKey>();
    list.add(key1);
    list.add(key2);
    AssociationPrimaryKey key = new AssociationPrimaryKey(list, CLASS_NAME);
    assertTrue(key != null);
    int i = 0;
    for (AbstractColumnValue value : key) {
      if (i == 0) {
        String name = value.getName();
        assertTrue(name.equalsIgnoreCase(col1Name));
        assertTrue(value.hasValue());
      } else if (i == 1) {
        String name = value.getName();
        assertTrue(name.equalsIgnoreCase(col2Name));
        assertTrue(value.hasValue());
      } else {
        fail("More than two columns in a two-column key!");
      }
      i++;
    }
  }

  /**
   * Test method for single-column key
   * {@link com.poesys.db.pk.AssociationPrimaryKey#AssociationPrimaryKey(java.util.List,String)}
   * .
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  public void testAssociationPrimaryKey2() throws InvalidParametersException,
      DuplicateKeyNameException {
    List<IPrimaryKey> list = new CopyOnWriteArrayList<IPrimaryKey>();
    list.add(key1);
    try {
      AssociationPrimaryKey key = new AssociationPrimaryKey(list, CLASS_NAME);
      assertTrue(key == null);
      fail("Should have gotten InvalidParametersException");
    } catch (InvalidParametersException e) {
      assertTrue(true);
    }
  }

  /**
   * Test method for multiple-column key with duplicate column names
   * {@link com.poesys.db.pk.AssociationPrimaryKey#AssociationPrimaryKey(java.util.List,String)}
   * .
   * 
   * @throws InvalidParametersException when there is a null parameter
   */
  public void testAssociationPrimaryKey3() throws InvalidParametersException {
    List<IPrimaryKey> list = new CopyOnWriteArrayList<IPrimaryKey>();
    list.add(key1);
    list.add(key1);
    try {
      AssociationPrimaryKey key = new AssociationPrimaryKey(list, CLASS_NAME);
      assertTrue(key != null);
    } catch (DuplicateKeyNameException e) {
      assertTrue(true);
    }
  }

  /**
   * Test method for multiple-column key with duplicate column names
   * {@link com.poesys.db.pk.AssociationPrimaryKey#AssociationPrimaryKey(java.util.List,String)}
   * .
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  public void testDuplicateNameException() throws DuplicateKeyNameException,
      InvalidParametersException {
    List<IPrimaryKey> list2 = new CopyOnWriteArrayList<IPrimaryKey>();
    list2.add(key1);
    list2.add(key3);
    try {
      AssociationPrimaryKey akey2 =
        new AssociationPrimaryKey(list2, CLASS_NAME);
      assertTrue(akey2 != null);
      fail("Should have gotten DuplicateKeyNameException");
    } catch (DuplicateKeyNameException e) {
      assertTrue(true);
    }
  }

  /**
   * Test method for association keys with two primary keys (binary association)
   * {@link com.poesys.db.pk.AssociationPrimaryKey#equals(com.poesys.db.pk.IPrimaryKey)}
   * .
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  public void testEqualsIPrimaryKey() throws InvalidParametersException,
      DuplicateKeyNameException {
    List<IPrimaryKey> list1 = new CopyOnWriteArrayList<IPrimaryKey>();
    list1.add(key1);
    list1.add(key2);
    AssociationPrimaryKey akey1 = new AssociationPrimaryKey(list1, CLASS_NAME);

    List<IPrimaryKey> list3 = new CopyOnWriteArrayList<IPrimaryKey>();
    list3.add(key1);
    list3.add(key2);
    AssociationPrimaryKey akey3 = new AssociationPrimaryKey(list3, CLASS_NAME);

    List<IPrimaryKey> list4 = new CopyOnWriteArrayList<IPrimaryKey>();
    list4.add(key1);
    list4.add(key4);
    AssociationPrimaryKey akey4 = new AssociationPrimaryKey(list4, CLASS_NAME);

    List<IPrimaryKey> list5 = new CopyOnWriteArrayList<IPrimaryKey>();
    list5.add(key1);
    list5.add(key5);
    AssociationPrimaryKey akey5 = new AssociationPrimaryKey(list5, CLASS_NAME);

    List<IPrimaryKey> list6 = new CopyOnWriteArrayList<IPrimaryKey>();
    list6.add(key1);
    list6.add(key6);
    AssociationPrimaryKey akey6 = new AssociationPrimaryKey(list6, CLASS_NAME);

    assertTrue(akey1.equals(akey3)); // same names, same values
    assertFalse(akey1.equals(akey4)); // same names, different values in one
    // primary key
    assertFalse(akey1.equals(akey5)); // different name, same value
    assertFalse(akey1.equals(akey6)); // different name, different value
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.AssociationPrimaryKey#getSqlColumnList(java.lang.String)}
   * .
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  public void testGetSqlColumnList() throws DuplicateKeyNameException,
      InvalidParametersException {
    List<IPrimaryKey> list = new CopyOnWriteArrayList<IPrimaryKey>();
    list.add(key1);
    list.add(key2);
    AssociationPrimaryKey key1 = new AssociationPrimaryKey(list, CLASS_NAME);
    String colList = key1.getSqlColumnList("c");
    assertTrue("c.col1, c.col2".equalsIgnoreCase(colList));
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.AssociationPrimaryKey#getSqlWhereExpression(java.lang.String)}
   * .
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  public void testGetSqlWhereExpression() throws DuplicateKeyNameException,
      InvalidParametersException {
    List<IPrimaryKey> list = new CopyOnWriteArrayList<IPrimaryKey>();
    list.add(key1);
    list.add(key2);
    AssociationPrimaryKey key1 = new AssociationPrimaryKey(list, CLASS_NAME);
    String colList = key1.getSqlWhereExpression("c");
    assertTrue("c.col1 = ? AND c.col2 = ?".equalsIgnoreCase(colList));
  }

  /**
   * Test method for single-valued key Test method for
   * {@link com.poesys.db.pk.AssociationPrimaryKey#iterator()}.
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  public void testIterator() throws DuplicateKeyNameException,
      InvalidParametersException {
    List<IPrimaryKey> list = new CopyOnWriteArrayList<IPrimaryKey>();
    list.add(key1);
    list.add(key2);
    AssociationPrimaryKey key1 = new AssociationPrimaryKey(list, CLASS_NAME);
    int i = 0;
    for (AbstractColumnValue colValue : key1) {
      assertTrue(colValue != null);
      i++;
    }
    assertTrue(i == 2);
  }

  /**
   * Test method for AssociationPrimaryKey setParams
   * 
   * @throws IOException when can't get a property
   * @throws SQLException when can't get a connection
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  public void testSetParams() throws SQLException, IOException,
      DuplicateKeyNameException, InvalidParametersException {
    Connection connection = getConnection();
    List<IPrimaryKey> list = new CopyOnWriteArrayList<IPrimaryKey>();
    list.add(key1);
    list.add(key2);
    AssociationPrimaryKey key1 = new AssociationPrimaryKey(list, CLASS_NAME);
    PreparedStatement stmt =
      connection.prepareStatement("SELECT * FROM TEST WHERE testKey1 = ? AND testKey2 = ?");
    key1.setParams(stmt, 1);
    assertTrue(true);
  }

  /**
   * Test method for single-valued key
   * {@link com.poesys.db.pk.AssociationPrimaryKey#getKeyListCopy()}.
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  public void testGetKeyList() throws DuplicateKeyNameException,
      InvalidParametersException {
    List<IPrimaryKey> list = new CopyOnWriteArrayList<IPrimaryKey>();
    list.add(key1);
    list.add(key2);
    AssociationPrimaryKey key1 = new AssociationPrimaryKey(list, CLASS_NAME);
    List<IPrimaryKey> keyList = key1.getKeyListCopy();
    assertTrue(keyList.size() == 2);
    assertTrue(keyList.get(0).getColumnNames().size() == 1);
    Set<String> set1 = keyList.get(0).getColumnNames();
    for (String name : set1) {
      assertTrue(name.equalsIgnoreCase(col1Name));
    }
    assertTrue(keyList.get(1).getColumnNames().size() == 1);
    Set<String> set2 = keyList.get(1).getColumnNames();
    for (String name : set2) {
      assertTrue(name.equalsIgnoreCase(col2Name));
    }
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.AssociationPrimaryKey#getColumnNames()}.
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  public void testGetColumnNames() throws DuplicateKeyNameException,
      InvalidParametersException {
    List<IPrimaryKey> list = new CopyOnWriteArrayList<IPrimaryKey>();
    list.add(key1);
    list.add(key2);
    AssociationPrimaryKey key = new AssociationPrimaryKey(list, CLASS_NAME);
    assertTrue(key.getColumnNames().size() == 2);
    Set<String> set = key.getColumnNames();
    if (set.contains(col1Name) && set.contains(col2Name)) {
      assertTrue(true);
    } else {
      fail("Column name not in set");
    }
  }

  /**
   * Test getValueList() for two single-column keys.
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  public void testGetValueList2() throws InvalidParametersException,
      DuplicateKeyNameException {
    List<IPrimaryKey> list = new CopyOnWriteArrayList<IPrimaryKey>();
    list.add(key1);
    list.add(key2);
    AssociationPrimaryKey key = new AssociationPrimaryKey(list, CLASS_NAME);
    assertTrue(key != null);
    String value = key.getValueList();
    String shouldBe = "(" + col1Name + " = 1, " + col2Name + " = 200)";
    assertTrue(value.equals(shouldBe));
  }

  /**
   * Test getValueList() for three single-column keys.
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  public void testGetValueList3() throws InvalidParametersException,
      DuplicateKeyNameException {
    List<IPrimaryKey> list = new CopyOnWriteArrayList<IPrimaryKey>();
    list.add(key1);
    list.add(key2);
    list.add(key5);
    AssociationPrimaryKey key = new AssociationPrimaryKey(list, CLASS_NAME);
    assertTrue(key != null);
    String value = key.getValueList();
    String shouldBe =
      "(" + col1Name + " = 1, " + col2Name + " = 200, " + col3Name + " = 200)";
    assertTrue(value + " should be " + shouldBe, value.equals(shouldBe));
  }
}
