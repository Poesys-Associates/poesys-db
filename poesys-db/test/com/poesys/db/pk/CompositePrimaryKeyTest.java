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


import com.poesys.db.DuplicateKeyNameException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.col.BigIntegerColumnValue;
import com.poesys.db.col.IColumnValue;
import com.poesys.db.dao.ConnectionTest;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * Test the CompositePrimaryKey class.
 * 
 * @author Robert J. Muller
 */
public class CompositePrimaryKeyTest extends ConnectionTest {
  private String col1Name = "col1";
  private String col2Name = "col2";
  private AbstractSingleValuedPrimaryKey key1 = null;
  private AbstractSingleValuedPrimaryKey key2 = null;
  private AbstractSingleValuedPrimaryKey key3 = null;
  private AbstractSingleValuedPrimaryKey key5 = null;

  private static final String CLASS_NAME = "com.poesys.db.dto.TestSequence";


  @Before
  public void setUp() throws Exception {
    try {
      // Define two primary keys with different column names and values.
      key1 = new SequencePrimaryKey(col1Name, new BigInteger("1"), CLASS_NAME);
      key2 =
        new SequencePrimaryKey(col2Name, new BigInteger("200"), CLASS_NAME);
      // Define primary key with key1 name and key2 value
      key3 =
        new SequencePrimaryKey(col1Name, new BigInteger("200"), CLASS_NAME);
      String col3Name = "col3";
      key5 =
        new SequencePrimaryKey(col3Name, new BigInteger("200"), CLASS_NAME);

    } catch (InvalidParametersException e) {
      throw new Exception("Invalid parameter to sequence primary key creation",
                          e);
    }
  }

  /**
   * Test method for CompositePrimaryKey constructor
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  @Test
  public void testCompositePrimaryKey() throws InvalidParametersException,
      DuplicateKeyNameException {
    NaturalPrimaryKey subKey = createSubKey(col1Name, new BigInteger("1"));
    new CompositePrimaryKey(key2, subKey, CLASS_NAME);
  }

  /**
   * Get a new sub-key to use to create a composite key. The sub-key must have a
   * BigInteger column value.
   * 
   * @param colName the name of the column comprising the sub-key
   * @param value the BigInteger value for the sub-key column
   * @return the new sub-key
   * @throws InvalidParametersException when the key has incorrect values
   * @throws DuplicateKeyNameException when the key has duplicate names
   */
  private NaturalPrimaryKey createSubKey(String colName, BigInteger value)
      throws InvalidParametersException, DuplicateKeyNameException {
    IColumnValue columnValue = new BigIntegerColumnValue(colName, value);
    List<IColumnValue> list = new ArrayList<>();
    list.add(columnValue);
    return new NaturalPrimaryKey(list, CLASS_NAME);
  }

  /**
   * Test method for CompositePrimaryKey constructor with invalid sub-key
   * parameter
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  @Test
  public void testCompositePrimaryKeyInvalidParametersSub()
      throws DuplicateKeyNameException {
    @SuppressWarnings("unused")
    CompositePrimaryKey key;
    try {
      new CompositePrimaryKey(key1, null, CLASS_NAME);
      fail();
    } catch (InvalidParametersException e) {
      assertTrue(true);
    }
  }

  /**
   * Test method for CompositePrimaryKey constructor with invalid parameters
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  @Test
  public void testCompositePrimaryKeyInvalidParametersPrimary()
      throws DuplicateKeyNameException {
    try {
      @SuppressWarnings("unused")
      CompositePrimaryKey key =
        new CompositePrimaryKey(null,
                                createSubKey(col1Name, new BigInteger("1")),
                                CLASS_NAME);
      fail();
    } catch (InvalidParametersException e) {
      assertTrue(true);
    }
  }

  /**
   * Test method for CompositePrimaryKey constructor with duplicate name error
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  @Test
  public void testCompositePrimaryKeyDupName()
      throws InvalidParametersException {
    try {
      @SuppressWarnings("unused")
      CompositePrimaryKey key =
        new CompositePrimaryKey(key1,
                                createSubKey(col1Name, new BigInteger("1")),
                                CLASS_NAME);
      fail("Duplicate key name not detected");
    } catch (DuplicateKeyNameException e) {
      assertTrue(true);
    }
  }

  /**
   * Test method for equal keys (same column names, same values)
   * {@link com.poesys.db.pk.CompositePrimaryKey#equals(com.poesys.db.pk.IPrimaryKey)}
   * .
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  @Test
  public void testEqualsIPrimaryKey() throws InvalidParametersException,
      DuplicateKeyNameException {
    // same key columns and values
    CompositePrimaryKey firstKey =
      new CompositePrimaryKey(key1,
                              createSubKey(col2Name, new BigInteger("1")),
                              CLASS_NAME);
    CompositePrimaryKey secondKey =
      new CompositePrimaryKey(key1,
                              createSubKey(col2Name, new BigInteger("1")),
                              CLASS_NAME);
    assertTrue(firstKey.equals(secondKey));
  }

  /**
   * Test method for unequal keys (different column names, same values)
   * {@link com.poesys.db.pk.CompositePrimaryKey#equals(com.poesys.db.pk.IPrimaryKey)}
   * .
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  @Test
  public void testNotEqualsIPrimaryKey() throws InvalidParametersException,
      DuplicateKeyNameException {
    // same key columns and values
    CompositePrimaryKey firstKey =
      new CompositePrimaryKey(key1,
                              createSubKey(col2Name, new BigInteger("1")),
                              CLASS_NAME);
    CompositePrimaryKey secondKey =
      new CompositePrimaryKey(key2,
                              createSubKey(col1Name, new BigInteger("1")),
                              CLASS_NAME);
    assertFalse(firstKey.equals(secondKey));
  }

  /**
   * Test method for unequal keys (same column names, different values)
   * {@link com.poesys.db.pk.CompositePrimaryKey#equals(com.poesys.db.pk.IPrimaryKey)}
   * .
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  @Test
  public void testNotEqualsIPrimaryKey2() throws InvalidParametersException,
      DuplicateKeyNameException {
    // same key columns and values
    CompositePrimaryKey firstKey =
      new CompositePrimaryKey(key1,
                              createSubKey(col2Name, new BigInteger("1")),
                              CLASS_NAME);
    CompositePrimaryKey secondKey =
      new CompositePrimaryKey(key3,
                              createSubKey(col2Name, new BigInteger("1")),
                              CLASS_NAME);
    assertFalse(firstKey.equals(secondKey));
  }

  /**
   * Test method for unequal keys (different column names, different values)
   * {@link com.poesys.db.pk.CompositePrimaryKey#equals(com.poesys.db.pk.IPrimaryKey)}
   * .
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  @Test
  public void testNotEqualsIPrimaryKey3() throws InvalidParametersException,
      DuplicateKeyNameException {
    // same key columns and values
    CompositePrimaryKey firstKey =
      new CompositePrimaryKey(key3,
                              createSubKey(col2Name, new BigInteger("1")),
                              CLASS_NAME);
    CompositePrimaryKey secondKey =
      new CompositePrimaryKey(key5,
                              createSubKey(col1Name, new BigInteger("1")),
                              CLASS_NAME);
    assertFalse(firstKey.equals(secondKey));
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.CompositePrimaryKey#getSqlColumnList(java.lang.String)}
   * .
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  @Test
  public void testGetSqlColumnList() throws InvalidParametersException,
      DuplicateKeyNameException {
    NaturalPrimaryKey subKey = createSubKey(col1Name, new BigInteger("1"));
    CompositePrimaryKey key = new CompositePrimaryKey(key2, subKey, CLASS_NAME);
    String colList = key.getSqlColumnList("c");
    assertTrue(colList, "c.col1, c.col2".equalsIgnoreCase(colList));
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.CompositePrimaryKey#getSqlWhereExpression(java.lang.String)}
   * .
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  @Test
  public void testGetSqlWhereExpression() throws InvalidParametersException,
      DuplicateKeyNameException {
    NaturalPrimaryKey subKey = createSubKey(col1Name, new BigInteger("1"));
    CompositePrimaryKey key = new CompositePrimaryKey(key2, subKey, CLASS_NAME);
    String colList = key.getSqlWhereExpression("c");
    assertTrue("c.col1 = ? AND c.col2 = ?".equalsIgnoreCase(colList));
  }

  /**
   * Test method for {@link com.poesys.db.pk.CompositePrimaryKey#iterator()}.
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  @Test
  public void testIterator() throws InvalidParametersException,
      DuplicateKeyNameException {
    NaturalPrimaryKey subKey = createSubKey(col1Name, new BigInteger("1"));
    CompositePrimaryKey key = new CompositePrimaryKey(key2, subKey, CLASS_NAME);
    int i = 0;
    for (IColumnValue colValue : key) {
      assertTrue(colValue != null);
      i++;
    }
    assertTrue(i == 2);
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.CompositePrimaryKey#setParams(java.sql.PreparedStatement, int)}
   * .
   * 
   * @throws IOException when can't get a property
   * @throws SQLException when can't get a connection
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  @Test
  public void testSetParams() throws SQLException, IOException,
      InvalidParametersException, DuplicateKeyNameException {
    Connection connection = getConnection();
    NaturalPrimaryKey subKey = createSubKey(col1Name, new BigInteger("1"));
    CompositePrimaryKey key = new CompositePrimaryKey(key2, subKey, CLASS_NAME);
    PreparedStatement stmt =
      connection.prepareStatement("SELECT * FROM TEST WHERE testKey1 = ? AND testKey2 = ?");
    key.setParams(stmt, 1);
    assertTrue(true);
  }

  /**
   * Test method for {@link com.poesys.db.pk.CompositePrimaryKey#getParentKey()}
   * .
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  @Test
  public void testGetParentKey() throws InvalidParametersException,
      DuplicateKeyNameException {
    NaturalPrimaryKey subKey = createSubKey(col1Name, new BigInteger("1"));
    CompositePrimaryKey key = new CompositePrimaryKey(key2, subKey, CLASS_NAME);
    IPrimaryKey parentKey = key.getParentKey();
    for (IColumnValue c : parentKey) {
      String name = c.getName();
      assertTrue(name.equalsIgnoreCase(col2Name));
    }
  }

  /**
   * Test method for {@link com.poesys.db.pk.CompositePrimaryKey#getSubKey()}.
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  @Test
  public void testGetSubKey() throws InvalidParametersException,
      DuplicateKeyNameException {
    NaturalPrimaryKey subKey = createSubKey(col1Name, new BigInteger("1"));
    CompositePrimaryKey key = new CompositePrimaryKey(key2, subKey, CLASS_NAME);
    IPrimaryKey subKey1 = key.getSubKey();
    for (IColumnValue c : subKey1) {
      String name = c.getName();
      assertTrue(name.equalsIgnoreCase(col1Name));
    }
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.CompositePrimaryKey#getColumnNames()}.
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  @Test
  public void testGetColumnNames() throws InvalidParametersException,
      DuplicateKeyNameException {
    NaturalPrimaryKey subKey = createSubKey(col1Name, new BigInteger("1"));
    CompositePrimaryKey key = new CompositePrimaryKey(key2, subKey, CLASS_NAME);
    Set<String> names = key.getColumnNames();
    assertTrue("size != 2: " + names.size(), names.size() == 2);
    assertTrue(names.contains(col1Name));
    assertTrue(names.contains(col2Name));
  }

  /**
   * Test getValueList() for a single-column parent and child key. There are
   * more complicated possibilities here, but the code for producing them comes
   * from the nested key classes; the composite key has only two keys so this
   * test covers all the real possibilities.
   * 
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException when more than one column in the key has
   *           the same name
   */
  @Test
  public void testGetValueListSingle() throws InvalidParametersException,
      DuplicateKeyNameException {
    NaturalPrimaryKey subKey = createSubKey(col2Name, new BigInteger("1"));
    CompositePrimaryKey key = new CompositePrimaryKey(key1, subKey, CLASS_NAME);
    String value = key.getValueList();
    String shouldBe = "(" + col1Name + " = 1, " + col2Name + " = 1)";
    assertTrue(value + " should be " + shouldBe, value.equals(shouldBe));
  }
}
