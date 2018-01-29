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
import com.poesys.db.col.StringColumnValue;
import com.poesys.db.dao.ConnectionTest;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * Test the NaturalPrimaryKey class.
 *
 * @author Robert J. Muller
 */
public class NaturalPrimaryKeyTest extends ConnectionTest {
  private String col1Name = "col1";
  private String col2Name = "col2";
  private String col3Name = "col3";
  private String col4Name = "col4";

  private static final String CLASS_NAME = "com.poesys.db.dto.TestSequence";

  /**
   * Test method for single-column key
   * {@link com.poesys.db.pk.NaturalPrimaryKey#NaturalPrimaryKey(java.util.List, java.lang.String)}
   * .
   *
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException  when more than one column has the same
   *                                    name in the key
   */
  @Test
  public void testNaturalPrimaryKey1() throws InvalidParametersException, 
    DuplicateKeyNameException {
    List<IColumnValue> list = new ArrayList<>();
    list.add(new StringColumnValue(col1Name, "A"));
    NaturalPrimaryKey key = new NaturalPrimaryKey(list, CLASS_NAME);
    int i = 0;
    for (IColumnValue value : key) {
      if (i == 0) {
        assertTrue(value.getName().equalsIgnoreCase(col1Name));
        assertTrue(value.hasValue());
      } else {
        fail("More than one column in a single-column key!");
      }
      i++;
    }
  }

  /**
   * Test method for multiple-column key
   * {@link com.poesys.db.pk.NaturalPrimaryKey#NaturalPrimaryKey(java.util.List, java.lang.String)}
   * .
   *
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException  when more than one column has the same
   *                                    name in the key
   */
  @Test
  public void testNaturalPrimaryKey2() throws InvalidParametersException, 
    DuplicateKeyNameException {
    List<IColumnValue> list = new ArrayList<>();
    list.add(new StringColumnValue(col1Name, "A"));
    list.add(new BigIntegerColumnValue(col2Name, new BigInteger("1")));
    NaturalPrimaryKey key = new NaturalPrimaryKey(list, CLASS_NAME);
    int i = 0;
    for (IColumnValue value : key) {
      if (i == 0) {
        assertTrue(value.getName().equalsIgnoreCase(col1Name));
        assertTrue(value.hasValue());
      } else if (i == 1) {
        assertTrue(value.getName().equalsIgnoreCase(col2Name));
        assertTrue(value.hasValue());
      } else {
        fail("More than two columns in a two-column key!");
      }
      i++;
    }
  }

  /**
   * Test method for multiple-column key with a duplicate name
   * {@link com.poesys.db.pk.NaturalPrimaryKey#NaturalPrimaryKey(java.util.List, java.lang.String)}
   * .
   *
   * @throws InvalidParametersException when there is a null parameter
   */
  @Test
  public void testNaturalPrimaryKey3() throws InvalidParametersException {
    List<IColumnValue> list = new ArrayList<>();
    list.add(new StringColumnValue(col1Name, "A"));
    list.add(new StringColumnValue(col1Name, "B"));
    try {
      new NaturalPrimaryKey(list, CLASS_NAME);
    } catch (DuplicateKeyNameException e) {
      assertTrue(true);
    }
  }

  /**
   * Test method for attempt to create key with no key columns
   * {@link com.poesys.db.pk.NaturalPrimaryKey#NaturalPrimaryKey(java.util.List, java.lang.String)}
   * .
   *
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException  when more than one column has the same
   *                                    name in the key
   */
  @Test
  public void testNaturalPrimaryKeyNoColumns() throws DuplicateKeyNameException {
    List<IColumnValue> list = new ArrayList<>();
    try {
      new NaturalPrimaryKey(list, CLASS_NAME);
      fail("Should have thrown InvalidParametersException, did not");
    } catch (InvalidParametersException e) {
      assertTrue(true);
    }
  }

  /**
   * Test method for single-column key
   * {@link com.poesys.db.pk.AbstractMultiValuedPrimaryKey#equals(com.poesys.db.pk.IPrimaryKey)}
   * .
   *
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException  when more than one column has the same
   *                                    name in the key
   */
  @Test
  public void testEqualsIPrimaryKey1() throws InvalidParametersException, 
    DuplicateKeyNameException {
    List<IColumnValue> list1 = new ArrayList<>();
    list1.add(new StringColumnValue(col1Name, "A"));
    NaturalPrimaryKey key1 = new NaturalPrimaryKey(list1, CLASS_NAME);
    List<IColumnValue> list2 = new ArrayList<>();
    list2.add(new StringColumnValue(col1Name, "B"));
    NaturalPrimaryKey key2 = new NaturalPrimaryKey(list2, CLASS_NAME);
    List<IColumnValue> list3 = new ArrayList<>();
    list3.add(new StringColumnValue(col1Name, "A"));
    NaturalPrimaryKey key3 = new NaturalPrimaryKey(list3, CLASS_NAME);
    List<IColumnValue> list4 = new ArrayList<>();
    list4.add(new StringColumnValue(col2Name, "A"));
    NaturalPrimaryKey key4 = new NaturalPrimaryKey(list4, CLASS_NAME);
    List<IColumnValue> list5 = new ArrayList<>();
    list5.add(new StringColumnValue(col2Name, "B"));
    NaturalPrimaryKey key5 = new NaturalPrimaryKey(list5, CLASS_NAME);
    assertTrue(key1.equals(key3)); // same name, same value
    assertFalse(key1.equals(key2)); // same name, different value
    assertFalse(key1.equals(key4)); // different name, same value
    assertFalse(key1.equals(key5)); // different name, different value
  }

  /**
   * Test method for multiple-column key
   * {@link com.poesys.db.pk.AbstractMultiValuedPrimaryKey#equals(com.poesys.db.pk.IPrimaryKey)}
   * .
   *
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException  when more than one column has the same
   *                                    name in the key
   */
  @Test
  public void testEqualsIPrimaryKey2() throws InvalidParametersException, 
    DuplicateKeyNameException {
    List<IColumnValue> list1 = new ArrayList<>();
    list1.add(new StringColumnValue(col1Name, "A"));
    list1.add(new BigIntegerColumnValue(col2Name, new BigInteger("1")));
    NaturalPrimaryKey key1 = new NaturalPrimaryKey(list1, CLASS_NAME);
    List<IColumnValue> list2 = new ArrayList<>();
    list2.add(new StringColumnValue(col1Name, "B"));
    list2.add(new BigIntegerColumnValue(col2Name, new BigInteger("1")));
    NaturalPrimaryKey key2 = new NaturalPrimaryKey(list2, CLASS_NAME);
    List<IColumnValue> list3 = new ArrayList<>();
    list3.add(new StringColumnValue(col1Name, "A"));
    list3.add(new BigIntegerColumnValue(col2Name, new BigInteger("1")));
    NaturalPrimaryKey key3 = new NaturalPrimaryKey(list3, CLASS_NAME);
    List<IColumnValue> list4 = new ArrayList<>();
    list4.add(new StringColumnValue(col3Name, "A"));
    list4.add(new BigIntegerColumnValue(col4Name, new BigInteger("1")));
    NaturalPrimaryKey key4 = new NaturalPrimaryKey(list4, CLASS_NAME);
    List<IColumnValue> list5 = new ArrayList<>();
    list5.add(new StringColumnValue(col1Name, "B"));
    list5.add(new BigIntegerColumnValue(col3Name, new BigInteger("2")));
    NaturalPrimaryKey key5 = new NaturalPrimaryKey(list5, CLASS_NAME);
    NaturalPrimaryKey key6 = new NaturalPrimaryKey(list1, CLASS_NAME);
    assertTrue(key1.equals(key3)); // same names, same values
    assertTrue(key1.equals(key6)); // one different name, same values
    assertFalse(key1.equals(key2)); // same names, one different value
    assertFalse(key1.equals(key4)); // two different names, same values
    assertFalse(key1.equals(key5)); // one different name, two different values
  }

  /**
   * Test method for single-valued key
   * {@link com.poesys.db.pk.AbstractMultiValuedPrimaryKey#iterator()}.
   *
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException  when more than one column has the same
   *                                    name in the key
   */
  @Test
  public void testIterator1() throws InvalidParametersException, DuplicateKeyNameException {
    List<IColumnValue> list = new ArrayList<>();
    list.add(new StringColumnValue(col3Name, "A"));
    NaturalPrimaryKey key1 = new NaturalPrimaryKey(list, CLASS_NAME);
    int i = 0;
    for (IColumnValue ignored : key1) {
      i++;
    }
    assertTrue(i == 1);
  }

  /**
   * Test method for multiple-valued key
   * {@link com.poesys.db.pk.AbstractMultiValuedPrimaryKey#iterator()}.
   *
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException  when more than one column has the same
   *                                    name in the key
   */
  @Test
  public void testIterator2() throws InvalidParametersException, DuplicateKeyNameException {
    List<IColumnValue> list = new ArrayList<>();
    list.add(new StringColumnValue(col3Name, "A"));
    list.add(new BigIntegerColumnValue(col2Name, new BigInteger("1")));
    NaturalPrimaryKey key1 = new NaturalPrimaryKey(list, CLASS_NAME);
    int i = 0;
    for (@SuppressWarnings("unused") IColumnValue colValue : key1) {
      i++;
    }
    assertTrue(i == 2);
  }

  /**
   * Test method for single-valued key
   * {@link com.poesys.db.pk.AbstractMultiValuedPrimaryKey#getSqlColumnList(java.lang.String)}
   * .
   *
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException  when more than one column has the same
   *                                    name in the key
   */
  @Test
  public void testGetSqlColumnList1() throws InvalidParametersException, DuplicateKeyNameException {
    List<IColumnValue> list = new ArrayList<>();
    list.add(new StringColumnValue(col1Name, "A"));
    NaturalPrimaryKey key1 = new NaturalPrimaryKey(list, CLASS_NAME);
    String colList = key1.getSqlColumnList("c");
    assertTrue("c.col1".equalsIgnoreCase(colList));
  }

  /**
   * Test method for multiple-valued key
   * {@link com.poesys.db.pk.AbstractMultiValuedPrimaryKey#getSqlColumnList(java.lang.String)}
   * .
   *
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException  when more than one column has the same
   *                                    name in the key
   */
  @Test
  public void testGetSqlColumnList2() throws InvalidParametersException, DuplicateKeyNameException {
    List<IColumnValue> list = new ArrayList<>();
    list.add(new StringColumnValue(col1Name, "A"));
    list.add(new BigIntegerColumnValue(col2Name, new BigInteger("1")));
    NaturalPrimaryKey key1 = new NaturalPrimaryKey(list, CLASS_NAME);
    String colList = key1.getSqlColumnList("c");
    assertTrue("c.col1, c.col2".equalsIgnoreCase(colList));
  }

  /**
   * Test method for single-valued key
   * {@link com.poesys.db.pk.AbstractMultiValuedPrimaryKey#getSqlWhereExpression(java.lang.String)}
   * .
   *
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException  when more than one column has the same
   *                                    name in the key
   */
  @Test
  public void testGetSqlWhereExpression1() throws InvalidParametersException, 
    DuplicateKeyNameException {
    List<IColumnValue> list = new ArrayList<>();
    list.add(new StringColumnValue(col1Name, "A"));
    NaturalPrimaryKey key1 = new NaturalPrimaryKey(list, CLASS_NAME);
    String colList = key1.getSqlWhereExpression("c");
    assertTrue("c.col1 = ?".equalsIgnoreCase(colList));
  }

  /**
   * Test method for multiple-valued key
   * {@link com.poesys.db.pk.AbstractMultiValuedPrimaryKey#getSqlWhereExpression(java.lang.String)}
   * .
   *
   * @throws InvalidParametersException when there is a null parameter
   * @throws DuplicateKeyNameException  when more than one column has the same
   *                                    name in the key
   */
  @Test
  public void testGetSqlWhereExpression2() throws InvalidParametersException, 
    DuplicateKeyNameException {
    List<IColumnValue> list = new ArrayList<>();
    list.add(new StringColumnValue(col1Name, "A"));
    list.add(new BigIntegerColumnValue(col2Name, new BigInteger("1")));
    NaturalPrimaryKey key1 = new NaturalPrimaryKey(list, CLASS_NAME);
    String colList = key1.getSqlWhereExpression("c");
    assertTrue("c.col1 = ? AND c.col2 = ?".equalsIgnoreCase(colList));
  }

  /**
   * Test method for single-valued key
   * {@link com.poesys.db.pk.NaturalPrimaryKey#setParams(java.sql.PreparedStatement, int)}
   * .
   *
   * @throws SQLException               when can't get a connection
   * @throws InvalidParametersException when a parameter is null
   * @throws IOException                when can't get a property
   * @throws DuplicateKeyNameException  when more than one column has the same
   *                                    name in the key
   */
  @Test
  public void testSetParams1() throws SQLException, InvalidParametersException, IOException, 
    DuplicateKeyNameException {
    Connection connection = getConnection();
    List<IColumnValue> list = new ArrayList<>();
    list.add(new StringColumnValue(col1Name, "A"));
    NaturalPrimaryKey key1 = new NaturalPrimaryKey(list, CLASS_NAME);
    PreparedStatement stmt = connection.prepareStatement("SELECT * FROM TEST WHERE testString = ?");
    key1.setParams(stmt, 1);
    assertTrue(true);
  }

  /**
   * Test method for multiple-valued key
   * {@link com.poesys.db.pk.NaturalPrimaryKey#setParams(java.sql.PreparedStatement, int)}
   * .
   *
   * @throws SQLException               when can't get a connection
   * @throws InvalidParametersException when a parameter is null
   * @throws IOException                when can't get a property
   * @throws DuplicateKeyNameException  when more than one column has the same
   *                                    name in the key
   */
  @Test
  public void testSetParams2() throws SQLException, InvalidParametersException, IOException, 
    DuplicateKeyNameException {
    Connection connection = getConnection();
    List<IColumnValue> list = new ArrayList<>();
    list.add(new StringColumnValue(col1Name, "A"));
    list.add(new BigIntegerColumnValue(col2Name, new BigInteger("1")));
    NaturalPrimaryKey key1 = new NaturalPrimaryKey(list, CLASS_NAME);
    PreparedStatement stmt =
      connection.prepareStatement("SELECT * FROM TEST WHERE testString = ? AND testInteger = ?");
    key1.setParams(stmt, 1);
    assertTrue(true);
  }

  /**
   * Test method for {@link com.poesys.db.pk.NaturalPrimaryKey#getColumnNames()}
   * .
   *
   * @throws InvalidParametersException when a parameter is null
   * @throws DuplicateKeyNameException  when more than one column has the same
   *                                    name in the key
   */
  @Test
  public void testGetColumnNames() throws InvalidParametersException, DuplicateKeyNameException {
    List<IColumnValue> list = new ArrayList<>();
    list.add(new StringColumnValue(col1Name, "A"));
    list.add(new BigIntegerColumnValue(col2Name, new BigInteger("1")));
    NaturalPrimaryKey key1 = new NaturalPrimaryKey(list, CLASS_NAME);
    assertTrue(key1.getColumnNames() != null);
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.AbstractPrimaryKey#compareTo(IPrimaryKey)}. Tests
   * equality of two single-valued keys.
   *
   * @throws InvalidParametersException when a parameter is null
   * @throws DuplicateKeyNameException  when more than one column has the same
   *                                    name in the key
   */
  @Test
  public void testCompareToEqual1() throws InvalidParametersException, DuplicateKeyNameException {
    List<IColumnValue> list1 = new ArrayList<>();
    list1.add(new StringColumnValue(col1Name, "A"));
    NaturalPrimaryKey key1 = new NaturalPrimaryKey(list1, CLASS_NAME);
    List<IColumnValue> list2 = new ArrayList<>();
    list2.add(new StringColumnValue(col1Name, "A"));
    NaturalPrimaryKey key2 = new NaturalPrimaryKey(list2, CLASS_NAME);
    assertTrue(key1.compareTo(key2) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.AbstractPrimaryKey#compareTo(IPrimaryKey)}. Tests
   * equality of two multi-valued keys.
   *
   * @throws InvalidParametersException when a parameter is null
   * @throws DuplicateKeyNameException  when more than one column has the same
   *                                    name in the key
   */
  @Test
  public void testCompareToEqual2() throws InvalidParametersException, DuplicateKeyNameException {
    List<IColumnValue> list1 = new ArrayList<>();
    list1.add(new StringColumnValue(col1Name, "A"));
    list1.add(new StringColumnValue(col2Name, "B"));
    NaturalPrimaryKey key1 = new NaturalPrimaryKey(list1, CLASS_NAME);
    List<IColumnValue> list2 = new ArrayList<>();
    list2.add(new StringColumnValue(col1Name, "A"));
    list2.add(new StringColumnValue(col2Name, "B"));
    NaturalPrimaryKey key2 = new NaturalPrimaryKey(list2, CLASS_NAME);
    assertTrue(key1.compareTo(key2) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.AbstractPrimaryKey#compareTo(IPrimaryKey)}. Tests
   * greater-than relation of two single-valued keys.
   *
   * @throws InvalidParametersException when a parameter is null
   * @throws DuplicateKeyNameException  when more than one column has the same
   *                                    name in the key
   */
  @Test
  public void testCompareToGreaterThan1() throws InvalidParametersException, 
    DuplicateKeyNameException {
    assertTrue("B is not greater than A", "(col1=B)".compareTo("(col1=A)") > 0);
    List<IColumnValue> list1 = new ArrayList<>();
    list1.add(new StringColumnValue(col1Name, "B"));
    NaturalPrimaryKey key1 = new NaturalPrimaryKey(list1, CLASS_NAME);
    List<IColumnValue> list2 = new ArrayList<>();
    list2.add(new StringColumnValue(col1Name, "A"));
    NaturalPrimaryKey key2 = new NaturalPrimaryKey(list2, CLASS_NAME);
    assertTrue(key1.getValueList() + " is not greater than " + key2.getValueList(),
               key1.compareTo(key2) > 0);
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.AbstractPrimaryKey#compareTo(IPrimaryKey)}. Tests
   * greater-than relation of two multi-valued keys.
   *
   * @throws InvalidParametersException when a parameter is null
   * @throws DuplicateKeyNameException  when more than one column has the same
   *                                    name in the key
   */
  @Test
  public void testCompareToGreaterThan2() throws InvalidParametersException, 
    DuplicateKeyNameException {
    List<IColumnValue> list1 = new ArrayList<>();
    list1.add(new StringColumnValue(col1Name, "A"));
    list1.add(new StringColumnValue(col2Name, "C"));
    NaturalPrimaryKey key1 = new NaturalPrimaryKey(list1, CLASS_NAME);
    List<IColumnValue> list2 = new ArrayList<>();
    list2.add(new StringColumnValue(col1Name, "A"));
    list2.add(new StringColumnValue(col2Name, "B"));
    NaturalPrimaryKey key2 = new NaturalPrimaryKey(list2, CLASS_NAME);
    assertTrue(key1.compareTo(key2) > 0);
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.AbstractPrimaryKey#compareTo(IPrimaryKey)}. Tests
   * less-than relation of two single-valued keys.
   *
   * @throws InvalidParametersException when a parameter is null
   * @throws DuplicateKeyNameException  when more than one column has the same
   *                                    name in the key
   */
  @Test
  public void testCompareToLessThan1() throws InvalidParametersException, 
    DuplicateKeyNameException {
    List<IColumnValue> list1 = new ArrayList<>();
    list1.add(new StringColumnValue(col1Name, "A"));
    NaturalPrimaryKey key1 = new NaturalPrimaryKey(list1, CLASS_NAME);
    List<IColumnValue> list2 = new ArrayList<>();
    list2.add(new StringColumnValue(col1Name, "B"));
    NaturalPrimaryKey key2 = new NaturalPrimaryKey(list2, CLASS_NAME);
    assertTrue(key1.compareTo(key2) < 0);
  }

  /**
   * Test method for
   * {@link com.poesys.db.pk.AbstractPrimaryKey#compareTo(IPrimaryKey)}. Tests
   * less-than relation of two multi-valued keys.
   *
   * @throws InvalidParametersException when a parameter is null
   * @throws DuplicateKeyNameException  when more than one column has the same
   *                                    name in the key
   */
  @Test
  public void testCompareToLessThan2() throws InvalidParametersException, 
    DuplicateKeyNameException {
    List<IColumnValue> list1 = new ArrayList<>();
    list1.add(new StringColumnValue(col1Name, "A"));
    list1.add(new StringColumnValue(col2Name, "B"));
    NaturalPrimaryKey key1 = new NaturalPrimaryKey(list1, CLASS_NAME);
    List<IColumnValue> list2 = new ArrayList<>();
    list2.add(new StringColumnValue(col1Name, "A"));
    list2.add(new StringColumnValue(col2Name, "C"));
    NaturalPrimaryKey key2 = new NaturalPrimaryKey(list2, CLASS_NAME);
    assertTrue(key1.compareTo(key2) < 0);
  }

  /**
   * Test getValueList() for a single-column key.
   *
   * @throws InvalidParametersException when a parameter is null
   * @throws DuplicateKeyNameException  when more than one column has the same
   *                                    name in the key
   */
  @Test
  public void testGetValueListSingle() throws InvalidParametersException, 
    DuplicateKeyNameException {
    List<IColumnValue> list1 = new ArrayList<>();
    list1.add(new StringColumnValue(col1Name, "A"));
    NaturalPrimaryKey key1 = new NaturalPrimaryKey(list1, CLASS_NAME);
    String value = key1.getValueList();
    String shouldBe = "(" + col1Name + " = A)";
    assertTrue(value + " should be " + shouldBe, value.equals(shouldBe));
  }

  /**
   * Test getValueList() for a two-column key.
   *
   * @throws InvalidParametersException when a parameter is null
   * @throws DuplicateKeyNameException  when more than one column has the same
   *                                    name in the key
   */
  @Test
  public void testGetValueListDouble() throws InvalidParametersException, 
    DuplicateKeyNameException {
    List<IColumnValue> list1 = new ArrayList<>();
    list1.add(new StringColumnValue(col1Name, "A"));
    list1.add(new StringColumnValue(col2Name, "B"));
    NaturalPrimaryKey key1 = new NaturalPrimaryKey(list1, CLASS_NAME);
   String value = key1.getValueList();
    String shouldBe = "(" + col1Name + " = A, " + col2Name + " = B)";
    assertTrue(value + " should be " + shouldBe, value.equals(shouldBe));
  }

  /**
   * Test getValueList() for a multiple-column key with more than 2 columns.
   *
   * @throws InvalidParametersException when a parameter is null
   * @throws DuplicateKeyNameException  when more than one column has the same
   *                                    name in the key
   */
  @Test
  public void testGetValueListMany() throws InvalidParametersException, DuplicateKeyNameException {
    List<IColumnValue> list1 = new ArrayList<>();
    list1.add(new StringColumnValue(col1Name, "A"));
    list1.add(new StringColumnValue(col2Name, "B"));
    list1.add(new StringColumnValue(col3Name, "C"));
    list1.add(new StringColumnValue(col4Name, "D"));
    NaturalPrimaryKey key1 = new NaturalPrimaryKey(list1, CLASS_NAME);
    String value = key1.getValueList();
    String shouldBe =
      "(" + col1Name + " = A, " + col2Name + " = B, " + col3Name + " = C, " + col4Name + " = D)";
    assertTrue(value + " should be " + shouldBe, value.equals(shouldBe));
  }
}
