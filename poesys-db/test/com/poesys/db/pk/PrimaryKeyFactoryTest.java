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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.poesys.db.DuplicateKeyNameException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.NoPrimaryKeyException;
import com.poesys.db.col.AbstractColumnValue;
import com.poesys.db.col.BigIntegerColumnValue;
import com.poesys.db.col.StringColumnValue;
import com.poesys.db.connection.IConnectionFactory.DBMS;
import com.poesys.db.dao.ConnectionTest;


/**
 * Test the PrimaryKeyFactory class.
 * 
 * @author Robert J. Muller
 */
public class PrimaryKeyFactoryTest extends ConnectionTest {

  private static final String CLASS_NAME = "com.poesys.db.dto.TestSequence";

  /**
   * Test createNaturalKey().
   * 
   * @throws InvalidParametersException when a parameter is null
   * @throws DuplicateKeyNameException when more than one column has the same
   *           name in the key
   */
  public void testCreateNaturalKey() throws InvalidParametersException,
      DuplicateKeyNameException {
    ArrayList<AbstractColumnValue> list = new ArrayList<AbstractColumnValue>();
    StringColumnValue val1 = new StringColumnValue("val1", "string_value");
    BigIntegerColumnValue val2 =
      new BigIntegerColumnValue("val2", new BigInteger("10"));
    list.add(val1);
    list.add(val2);
    NaturalPrimaryKey key = null;
    try {
      key =
        (NaturalPrimaryKey)PrimaryKeyFactory.createNaturalKey(list, CLASS_NAME);
    } catch (ClassCastException e) {
      fail("Did not create natural key");
    }
    assertTrue(key != null);
    assertTrue(key.getSqlColumnList("").equalsIgnoreCase("val1, val2"));
  }

  /**
   * Test createGuidKey().
   * 
   * @throws InvalidParametersException when a parameter is null
   */
  public void testCreateGuidKey() throws InvalidParametersException {
    GuidPrimaryKey key = null;
    try {
      key =
        (GuidPrimaryKey)PrimaryKeyFactory.createGuidKey("test_id", CLASS_NAME);
    } catch (ClassCastException e) {
      fail("Did not create guid key");
    }
    assertTrue(key != null);
    assertTrue(key.getSqlColumnList("").equalsIgnoreCase("test_id"));
  }

  /**
   * Test createIdentityKey().
   * 
   * @throws InvalidParametersException when a parameter is null
   */
  public void testCreateIdentityKey() throws InvalidParametersException {
    IdentityPrimaryKey key = null;
    try {
      key =
        (IdentityPrimaryKey)PrimaryKeyFactory.createIdentityKey("test_id",
                                                                CLASS_NAME);
    } catch (ClassCastException e) {
      fail("Did not create identity key");
    }
    assertTrue(key != null);
    assertTrue(key.getSqlColumnList("").equalsIgnoreCase("test_id"));
  }

  /*
   * public void testCreateOracleSequenceKey() throws SQLException, IOException,
   * InvalidParametersException, NoPrimaryKeyException {
   * AbstractSingleValuedPrimaryKey key = null; Connection conn =
   * getConnection(DBMS.ORACLE, "com.poesys.db.poesystest.oracle"); try { key =
   * (AbstractSingleValuedPrimaryKey)PrimaryKeyFactory.createOracleSequenceKey(
   * conn, "test_seq", "test_id"); } catch (ClassCastException e) { fail("Did
   * not create Oracle sequence key"); } assertTrue(key != null);
   * assertTrue(key.getSqlColumnList("").equalsIgnoreCase("test_id")); }
   */
  /**
   * Test createMysqlSequenceKey().
   * 
   * @throws SQLException when can't get connection
   * @throws IOException when can't get property
   * @throws InvalidParametersException when a parameter is null
   * @throws NoPrimaryKeyException when there is no primary key
   */
  public void testCreateMysqlSequenceKey() throws SQLException, IOException,
      InvalidParametersException, NoPrimaryKeyException {
    AbstractSingleValuedPrimaryKey key = null;

    // Set the sequence to start with 1.
    Connection conn = null;
    Statement stmt = null;

    try {
      conn = getConnection(DBMS.MYSQL, "com.poesys.db.poesystest.mysql");
      stmt = conn.createStatement();
      stmt.execute("UPDATE mysql_sequence set value = 0 where name = 'test'");
    } catch (SQLException e1) {
      e1.printStackTrace();
      fail("SQL exception: " + e1.getMessage());
    } finally {
      if (stmt != null) {
        stmt.close();
      }
    }

    // Create the sequence key.
    try {
      key =
        (AbstractSingleValuedPrimaryKey)PrimaryKeyFactory.createMySqlSequenceKey(conn,
                                                                                 "test",
                                                                                 "test_id",
                                                                                 CLASS_NAME);
    } catch (ClassCastException e) {
      fail("Did not create MySQL sequence key");
    } finally {
      conn.commit();
      conn.close();
    }
    assertTrue(key != null);
    assertTrue(key.getSqlColumnList("").equalsIgnoreCase("test_id"));
    String value = key.getValueList();
    String shouldBe = "(test_id=1)";
    assertTrue(value.compareTo(shouldBe) == 0);

  }

  /**
   * Test createCompositeKey().
   * 
   * @throws InvalidParametersException when a parameter is null
   * @throws DuplicateKeyNameException when more than one column has the same
   *           name in the key
   */
  public void testCreateCompositeKey() throws InvalidParametersException,
      DuplicateKeyNameException {
    ArrayList<AbstractColumnValue> list = new ArrayList<AbstractColumnValue>();
    StringColumnValue val1 = new StringColumnValue("val1", "string_value");
    list.add(val1);
    IPrimaryKey parent = PrimaryKeyFactory.createNaturalKey(list, CLASS_NAME);
    ArrayList<AbstractColumnValue> list2 = new ArrayList<AbstractColumnValue>();
    BigIntegerColumnValue val2 =
      new BigIntegerColumnValue("val2", new BigInteger("10"));
    list2.add(val2);
    NaturalPrimaryKey child =
      (NaturalPrimaryKey)PrimaryKeyFactory.createNaturalKey(list2, CLASS_NAME);
    CompositePrimaryKey key = null;
    try {
      key =
        (CompositePrimaryKey)PrimaryKeyFactory.createCompositeKey(parent,
                                                                  child,
                                                                  CLASS_NAME);
    } catch (ClassCastException e) {
      fail("Did not create composite key");
    }
    assertTrue(key != null);
    assertTrue(key.getSqlColumnList("").equalsIgnoreCase("val1, val2"));
  }

  /**
   * Test createAssociationKey().
   * 
   * @throws InvalidParametersException when a parameter is null
   * @throws DuplicateKeyNameException when more than one column has the same
   *           name in the key
   */
  public void testCreateAssociationKey() throws InvalidParametersException,
      DuplicateKeyNameException {
    ArrayList<AbstractColumnValue> collist1 =
      new ArrayList<AbstractColumnValue>();
    StringColumnValue val1 = new StringColumnValue("val1", "string_value");
    collist1.add(val1);
    IPrimaryKey key1 = PrimaryKeyFactory.createNaturalKey(collist1, CLASS_NAME);
    BigIntegerColumnValue val2 =
      new BigIntegerColumnValue("val2", new BigInteger("10"));
    ArrayList<AbstractColumnValue> collist2 =
      new ArrayList<AbstractColumnValue>();
    collist2.add(val2);
    NaturalPrimaryKey key2 =
      PrimaryKeyFactory.createNaturalKey(collist2, CLASS_NAME);
    AssociationPrimaryKey key = null;
    ArrayList<IPrimaryKey> keys = new ArrayList<IPrimaryKey>();
    keys.add(key1);
    keys.add(key2);

    try {
      key = PrimaryKeyFactory.createAssociationKey(keys, CLASS_NAME);
    } catch (ClassCastException e) {
      fail("Did not create association key");
    }
    assertTrue(key != null);
    assertTrue(key.getSqlColumnList("").equalsIgnoreCase("val1, val2"));
  }

  /**
   * Test createAssociationKey() with an AssociationKeyMapping object.
   * 
   * @throws InvalidParametersException when a parameter is null
   * @throws DuplicateKeyNameException when more than one column has the same
   *           name in the key
   */
  public void testCreateAssociationKeyWithMapping()
      throws InvalidParametersException, DuplicateKeyNameException {
    AssociationPrimaryKey key = null;

    IPrimaryKey key1 =
      PrimaryKeyFactory.createIdentityKey("id", new BigInteger("1"), CLASS_NAME);
    IPrimaryKey key2 =
      PrimaryKeyFactory.createIdentityKey("id", new BigInteger("2"), CLASS_NAME);

    List<IPrimaryKey> keys = new CopyOnWriteArrayList<IPrimaryKey>();
    keys.add(key1);
    keys.add(key2);

    // Build a column-name mapping from the Term PK to the association keys.
    AssociationKeyMapping mapping = new AssociationKeyMapping(2);
    mapping.map(0, "id", "root_term_id");
    mapping.map(1, "id", "term_id");

    try {
      key = PrimaryKeyFactory.createAssociationKey(keys, mapping, CLASS_NAME);
    } catch (ClassCastException e) {
      fail("Did not create association key with mapping");
    }
    assertTrue("No association key created with mapping", key != null);
    assertTrue(key.getSqlColumnList("").equalsIgnoreCase("root_term_id, term_id"));
  }

  /**
   * Test a variation on using the AssociationKeyMapping() object.
   * 
   * @throws InvalidParametersException when a parameter is null
   * @throws DuplicateKeyNameException when more than one column has the same
   *           name in the key
   */
  public void testCreateAssociationKeyWithMapping2()
      throws InvalidParametersException, DuplicateKeyNameException {
    // Build a list of the primary keys to associate.
    List<IPrimaryKey> keys = new CopyOnWriteArrayList<IPrimaryKey>();
    keys.add(PrimaryKeyFactory.createIdentityKey("id",
                                                 new BigInteger("1"),
                                                 CLASS_NAME));
    keys.add(PrimaryKeyFactory.createIdentityKey("id",
                                                 new BigInteger("2"),
                                                 CLASS_NAME));

    // Build a column-name mapping from the Term PK to the association keys.
    AssociationKeyMapping mapping = new AssociationKeyMapping(2);
    mapping.map(0, "id", "root_term_id");
    mapping.map(1, "id", "term_id");

    // Create the new key.
    AssociationPrimaryKey key =
      PrimaryKeyFactory.createAssociationKey(keys, mapping, CLASS_NAME);
    assertTrue("No association key created with mapping", key != null);
    assertTrue(key.getSqlColumnList("").equalsIgnoreCase("root_term_id, term_id"));
  }

}
