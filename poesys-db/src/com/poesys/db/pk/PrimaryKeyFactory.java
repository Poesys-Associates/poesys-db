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


import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import com.poesys.db.DuplicateKeyNameException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.NoPrimaryKeyException;
import com.poesys.db.col.AbstractColumnValue;
import com.poesys.db.col.BigIntegerColumnValue;


/**
 * A Factory Method pattern class containing factory methods for all the
 * different kinds of primary keys. Use this factory to create a primary key in
 * a data access object (DAO) or anywhere else you must create a key. Add
 * methods here if you add new types of primary key.
 * 
 * @author Robert J. Muller
 */
public class PrimaryKeyFactory {
  /** The Oracle sequence query before the sequence name */
  private static final String ORA_SEQ1 = "SELECT ";
  /** The Oracle sequence query after the sequence name */
  private static final String ORA_SEQ2 = ".NEXTVAL AS VALUE FROM DUAL";
  /** The MySQL sequence update */
  private static final String MYSQL_SEQ_UPDATE =
    "UPDATE mysql_sequence SET value = value + 1 WHERE name = ?";
  /** The MySQL sequence query */
  private static final String MYSQL_SEQ_QUERY =
    "SELECT value FROM mysql_sequence WHERE name = ?";

  /** Error message for no sequence generation */
  private static final String NO_SEQ_MSG = "com.poesys.db.pk.msg.no_sequence";
  /** Error message for null value key name or value */
  private static final String NULL_VALUE_MSG =
    "com.poesys.db.pk.msg.null_value_parameters";

  /**
   * Create a natural primary key from a name and an integer. This is a shortcut
   * factory method for a common kind of natural primary key, a single integer
   * value. A common use for this is for the second part of a composite primary
   * key.
   * 
   * @param name the column name for the primary key
   * @param value the key value
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   * @return the primary key
   * @throws DuplicateKeyNameException when the list contains more than one
   *           column with the same name
   * @throws InvalidParametersException when the list has no key columns
   */
  public static NaturalPrimaryKey createSingleNumberKey(String name,
                                                        BigInteger value,
                                                        String className)
      throws DuplicateKeyNameException, InvalidParametersException {
    List<AbstractColumnValue> list = new ArrayList<AbstractColumnValue>();
    list.add(new BigIntegerColumnValue(name, value));
    list = new ArrayList<AbstractColumnValue>(list);
    NaturalPrimaryKey key = new NaturalPrimaryKey(list, className);
    return key;
  }

  /**
   * Create a primary key from a list of column values. The list comprises the
   * columns of a natural key, a key built out of one or more columns supplied
   * by the user.
   * 
   * @param list the list of column values
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   * @return the primary key
   * @throws DuplicateKeyNameException when the list contains more than one
   *           column with the same name
   * @throws InvalidParametersException when the list has no key columns
   */
  public static NaturalPrimaryKey createNaturalKey(List<AbstractColumnValue> list,
                                                   String className)
      throws DuplicateKeyNameException, InvalidParametersException {
    NaturalPrimaryKey key = new NaturalPrimaryKey(list, className);
    return key;
  }

  /**
   * Create a GUID primary key. The GUID is a generated globally unique
   * identifier.
   * 
   * @param name the name of the single key column
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   * @return the GUID primary key
   * @throws InvalidParametersException when the column name is null or GUID
   *           generation fails
   */
  public static GuidPrimaryKey createGuidKey(String name, String className)
      throws InvalidParametersException {
    GuidPrimaryKey key = new GuidPrimaryKey(name, className);
    return key;
  }

  /**
   * Create a GUID primary key using a queried GUID string converted to a Java
   * UUID value. The GUID is a generated globally unique identifier.
   * 
   * @param name the name of the single key column
   * @param uuid the Java UUID
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   * @return the GUID primary key
   * @throws InvalidParametersException when the column name is null or GUID
   *           generation fails
   */
  public static GuidPrimaryKey createGuidKey(String name, UUID uuid,
                                             String className)
      throws InvalidParametersException {
    GuidPrimaryKey key = new GuidPrimaryKey(name, uuid, className);
    return key;
  }

  /**
   * Create a primary key suitable for later value generation using an identity
   * column, a column that auto-generates a unique integer key when you insert a
   * row. The DAO must finalize the insert by calling that method on the
   * Identity Primary Key.
   * 
   * @param name the column name of the identity column
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   * @return the primary key with a zero (0) value
   * @throws InvalidParametersException when the name is null
   */
  public static IdentityPrimaryKey createIdentityKey(String name,
                                                     String className)
      throws InvalidParametersException {
    IdentityPrimaryKey key = new IdentityPrimaryKey(name, className);
    return key;
  }

  /**
   * Create a primary key from previously generated data, such as a key queried
   * from the database, for an identity column, a column that auto-generates a
   * unique integer key when you insert a row.
   * 
   * @param name the column name of the identity column
   * @param value the previously generated identity value
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   * @return the primary key with the specified value
   * @throws InvalidParametersException when the name is null
   */
  public static IdentityPrimaryKey createIdentityKey(String name,
                                                     BigInteger value,
                                                     String className)
      throws InvalidParametersException {
    IdentityPrimaryKey key = new IdentityPrimaryKey(name, value, className);
    return key;
  }

  /**
   * Create a primary key containing a value generated from a named Oracle
   * SEQUENCE object. You must create the sequence in the target Oracle database
   * and have it start with an appropriate value. The sequence name, if null,
   * defaults to the key column name.
   * 
   * @param connection the connection to the Oracle database
   * @param sequenceName the name of the Oracle SEQUENCE object
   * @param name the name of the single primary key column
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   * @return the sequence primary key
   * @throws InvalidParametersException when the name or sequence-generated
   *           value is null
   * @throws NoPrimaryKeyException when the sequence-generated value can't be
   *           generated, possibly because of a SQL error such as the wrong
   *           sequence name or because the sequence query failed to return a
   *           value, which should never happen
   * @throws SQLException when there is a problem closing the SQL statement
   */
  public static SequencePrimaryKey createOracleSequenceKey(Connection connection,
                                                           String sequenceName,
                                                           String name,
                                                           String className)
      throws InvalidParametersException, NoPrimaryKeyException, SQLException {
    PreparedStatement stmt = null;
    SequencePrimaryKey key = null;
    // Default the sequence name to the column name.
    if (sequenceName == null) {
      sequenceName = name;
    }
    try {
      // Get the sequence value and set it into the primary key.
      StringBuilder seq = new StringBuilder(ORA_SEQ1);
      seq.append(sequenceName);
      seq.append(ORA_SEQ2);
      stmt = connection.prepareStatement(seq.toString());
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        BigDecimal seqValue = rs.getBigDecimal("value");
        key = new SequencePrimaryKey(name, seqValue.toBigInteger(), className);
      } else {
        List<String> list = new ArrayList<String>();
        NoPrimaryKeyException d = new NoPrimaryKeyException(NO_SEQ_MSG);
        list.add(sequenceName);
        list.add("no value found");
        d.setParameters(list);
        throw d;
      }
    } catch (SQLException e) {
      List<String> list = new ArrayList<String>();
      NoPrimaryKeyException d = new NoPrimaryKeyException(NO_SEQ_MSG);
      list.add(sequenceName);
      list.add(e.getMessage());
      d.setParameters(list);
      throw d;
    } finally {
      // Close the statement if it is open.
      if (stmt != null) {
        stmt.close();
      }
    }
    return key;
  }

  /**
   * <p>
   * Create a primary key containing a value generated from a table called
   * mysql_sequence that simulates the Oracle SEQUENCE. The sequence name, if
   * null, defaults to the key column name. You must use the value in the same
   * transaction or the value is not guaranteed to be unique. Here is some JDBC
   * code that creates the sequence table and initializes the sequence to 0. The
   * ? parameter is the name of the sequence.
   * </p>
   * 
   * <pre>
   * CREATE TABLE mysql_sequence (
   *   name varchar(30) NOT NULL PRIMARY KEY,
   *   value decimal(12) NOT NULL
   * ) type=InnoDB;
   * 
   * INSERT INTO mysql_sequence values (?, 0);
   * 
   * UPDATE mysql_sequence
   *    SET value = value+1
   *  WHERE name = ?;
   *  
   * SELECT value
   *   FROM mysql_sequence
   *  WHERE name = ?;
   * </pre>
   * 
   * @param connection the connection to the MySQL database
   * @param sequenceName the name of the MySQL sequence
   * @param name the name of the single primary key column
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   * @return the sequence primary key
   * @throws InvalidParametersException when the name or sequence-generated
   *           value is null
   * @throws NoPrimaryKeyException when the sequence-generated value can't be
   *           generated, possibly because of a SQL error such as the wrong
   *           sequence name
   * @throws SQLException when there is a problem closing the SQL statement
   */
  public static SequencePrimaryKey createMySqlSequenceKey(Connection connection,
                                                          String sequenceName,
                                                          String name,
                                                          String className)
      throws InvalidParametersException, NoPrimaryKeyException, SQLException {
    PreparedStatement stmt = null;
    SequencePrimaryKey key = null;
    // Default the sequence name to the column name.
    if (sequenceName == null) {
      sequenceName = name;
    }
    try {
      // Get the sequence value and set it into the primary key.
      stmt = connection.prepareStatement(MYSQL_SEQ_UPDATE);
      stmt.setString(1, sequenceName);
      stmt.execute();
      stmt.close();
      stmt = connection.prepareStatement(MYSQL_SEQ_QUERY);
      stmt.setString(1, sequenceName);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        BigDecimal seqValue = rs.getBigDecimal("value");
        key = new SequencePrimaryKey(name, seqValue.toBigInteger(), className);
      } else {
        List<String> list = new ArrayList<String>();
        NoPrimaryKeyException x = new NoPrimaryKeyException(NO_SEQ_MSG);
        list.add(sequenceName);
        list.add("No row for sequence in mysql_sequence table");
        x.setParameters(list);
        throw x;
      }
    } catch (SQLException e) {
      List<String> list = new ArrayList<String>();
      NoPrimaryKeyException x = new NoPrimaryKeyException(NO_SEQ_MSG);
      list.add(sequenceName);
      list.add(e.getMessage());
      x.setParameters(list);
      throw x;
    } finally {
      // Close the statement if it is open.
      if (stmt != null) {
        stmt.close();
      }
      // Commit the sequence generation.
      if (connection != null) {
        connection.commit();
      }
    }
    return key;
  }

  /**
   * Create a sequence key from a value. This method just creates the key using
   * the name and value, it does not generate a new sequence value.
   * 
   * @param name the key column name
   * @param value the sequence value
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   * @return a new sequence primary key
   * @throws InvalidParametersException when the key name or value is null
   */
  public static SequencePrimaryKey createSequenceKey(String name,
                                                     BigInteger value,
                                                     String className)
      throws InvalidParametersException {
    // Check that the key name and value are valid.
    if (name == null || value == null) {
      throw new InvalidParametersException(NULL_VALUE_MSG);
    }
    return new SequencePrimaryKey(name, value, className);
  }

  /**
   * Create a composite primary key, a key comprising the key of the parent
   * object that owns the identified object plus an additional key that
   * identifies the object within a set of children of the parent object.
   * 
   * @param parentKey the key of the parent (owning) object
   * @param subKey the identifying value within the parent key
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   * @return the composite key
   * @throws InvalidParametersException when there is no parent or sub-key
   * @throws DuplicateKeyNameException when there are multiple columns with the
   *           same name
   */
  public static CompositePrimaryKey createCompositeKey(IPrimaryKey parentKey,
                                                       IPrimaryKey subKey,
                                                       String className)
      throws InvalidParametersException, DuplicateKeyNameException {
    CompositePrimaryKey key =
      new CompositePrimaryKey(parentKey, subKey, className);
    return key;
  }

  /**
   * Create a composite primary key in the special but common case of a parent
   * with a sequence key and a child with a number that identifies the child
   * within the parent and provides an ordering of the children of the parent.
   * 
   * @param parentColumnName the name of the parent key column
   * @param parentId the parent key value
   * @param childColumnName the name of the child number column
   * @param childNumber the child number value
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   * @return a composite primary key with an embedded sequence key for the
   *         parent and a natural key for the child.
   */
  public static CompositePrimaryKey createCompositeKey(String parentColumnName,
                                                       BigInteger parentId,
                                                       String childColumnName,
                                                       BigInteger childNumber,
                                                       String className) {
    IPrimaryKey parentKey =
      new SequencePrimaryKey(parentColumnName, parentId, className);
    BigIntegerColumnValue col =
      new BigIntegerColumnValue(childColumnName, childNumber);
    List<AbstractColumnValue> list = new ArrayList<AbstractColumnValue>();
    list.add(col);
    list = new CopyOnWriteArrayList<AbstractColumnValue>(list);
    NaturalPrimaryKey childKey = new NaturalPrimaryKey(list, className);
    CompositePrimaryKey key =
      new CompositePrimaryKey(parentKey, childKey, className);
    return key;
  }

  /**
   * Create a composite primary key in the special but common case of a parent
   * with a GUID key and a child with a number that identifies the child within
   * the parent and provides an ordering of the children of the parent.
   * 
   * @param parentColumnName the name of the parent key column
   * @param parentId the parent key value, a GUID as a string
   * @param childColumnName the name of the child number column
   * @param childNumber the child number value
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   * @return a composite primary key with an embedded sequence key for the
   *         parent and a natural key for the child.
   */
  public static CompositePrimaryKey createCompositeKey(String parentColumnName,
                                                       String parentId,
                                                       String childColumnName,
                                                       BigInteger childNumber,
                                                       String className) {
    IPrimaryKey parentKey = new GuidPrimaryKey(parentColumnName, parentId);
    BigIntegerColumnValue col =
      new BigIntegerColumnValue(childColumnName, childNumber);
    List<AbstractColumnValue> list = new ArrayList<AbstractColumnValue>();
    list.add(col);
    list = new CopyOnWriteArrayList<AbstractColumnValue>(list);
    NaturalPrimaryKey childKey = new NaturalPrimaryKey(list, className);
    CompositePrimaryKey key =
      new CompositePrimaryKey(parentKey, childKey, className);
    return key;
  }

  /**
   * Create an association key, a key comprising two or more keys of associated
   * objects.
   * 
   * @param keys a list of the primary keys of associated objects
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   * @return the association primary key
   * @throws DuplicateKeyNameException when there are multiple columns in the
   *           primary keys with the same name
   * @throws InvalidParametersException when the key has fewer than two nested
   *           primary keys
   */
  public static AssociationPrimaryKey createAssociationKey(List<IPrimaryKey> keys,
                                                           String className)
      throws DuplicateKeyNameException, InvalidParametersException {
    AssociationPrimaryKey key = new AssociationPrimaryKey(keys, className);
    return key;
  }

  /**
   * Create an association key, a key comprising two or more keys of associated
   * objects, using input keys for the objects and a mapping to transform the
   * column names from the objects into the column names of the association.
   * 
   * @param keys the keys that comprise the association key
   * @param mapping the mapping of column names
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   * @return a new association primary key
   */
  public static AssociationPrimaryKey createAssociationKey(List<IPrimaryKey> keys,
                                                           AssociationKeyMapping mapping,
                                                           String className) {
    List<IPrimaryKey> list = new ArrayList<IPrimaryKey>();

    // Copy the keys into a new Association key, setting the names by mapping.
    int i = 0; // index for key
    for (IPrimaryKey oldKey : keys) {
      IPrimaryKey newKey = oldKey.copy();
      for (AbstractColumnValue col : newKey) {
        col.setName(mapping.lookUp(i, col.getName()));
      }
      // Add the key to the new list.
      list.add(newKey);
      i++;
    }
    return new AssociationPrimaryKey(list, className);
  }
}
