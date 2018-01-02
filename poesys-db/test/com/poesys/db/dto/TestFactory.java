/*
 * Copyright (c) 2018 Poesys Associates. All rights reserved.
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

import com.poesys.db.InvalidParametersException;
import com.poesys.db.pk.IPrimaryKey;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <p> A separate, sharable set of factory methods for all the account classes, including JDBC
 * data-setting, parameter-setting, and primary-key-generation methods. This class is abstract and
 * has a single concrete subclass, AccountFactory, that you can modify to override the default
 * behavior in the abstract class or implement an as-yet unimplemented method. </p>
 *
 * @author Poesys/DB Cartridge
 */
public class TestFactory {
  /**
   * Retrieve the CompositeParent data from a result set's current row and create a CompositeParent
   * object.
   *
   * @param key the primary key for the CompositeParent
   * @param rs  the query result set
   * @return a CompositeParent instance based on the result set data
   * @throws SQLException               when there is a problem getting data from the results
   * @throws InvalidParametersException when a required value is null
   */
  public static CompositeParent getCompositeParentData(IPrimaryKey key, ResultSet rs) throws
    SQLException, InvalidParametersException {
    return new CompositeParent(key, rs.getInt("parentId"), rs.getString("parentColumnData"));
  }

  /**
   * <p> Get a primary key for a CompositeParent based on a result set that must contain the parent
   * primary key column. The method creates a natural primary key.</p>
   *
   * @param rs     a JDBC result set with primary key columns
   * @param prefix an optional prefix string for derived column names in associations
   * @return a NaturalKey primary key
   * @throws SQLException               when there is a problem getting data from the result set
   * @throws InvalidParametersException when there is a problem creating a key
   */
  public static IPrimaryKey getCompositeParentPrimaryKey(ResultSet rs, String prefix) throws
    SQLException, InvalidParametersException {
    IPrimaryKey key;
    if (prefix == null) {
      prefix = "";
    }
    java.util.ArrayList<com.poesys.db.col.AbstractColumnValue> list = new java.util.ArrayList<>();

    list.add(new com.poesys.db.col.IntegerColumnValue(prefix + "parentId", rs.getInt("parentId")));
    key =
      com.poesys.db.pk.PrimaryKeyFactory.createNaturalKey(list, "com.poesys.db.dto.CompositeParent");
    return key;
  }

  /**
   * <p> Get a primary key for a CompositeParent based on input key attributes. The method creates a
   * natural primary key. </p>
   *
   * @param parentId a unique identifier for the object
   * @return a FiscalYear NaturalKey primary key
   * @throws InvalidParametersException when there is a problem creating a key
   */
  public static IPrimaryKey getCompositeParentPrimaryKey(Integer parentId) throws
    InvalidParametersException {
    IPrimaryKey key = null;
    // Track generated inputs for nullity.
    boolean noNulls = true;
    java.util.ArrayList<com.poesys.db.col.AbstractColumnValue> list = new java.util.ArrayList<>();
    if (parentId != null) {
      list.add(new com.poesys.db.col.IntegerColumnValue("parentId", parentId));
    } else {
      noNulls = false;
    }
    if (noNulls) {
      key =
        com.poesys.db.pk.PrimaryKeyFactory.createNaturalKey(list, "com.poesys.db.dto.CompositeParent");
    }
    return key;
  }

  /**
   * Retrieve the ChildRootClass data from a result set's current row and create a
   * ConcreteChildSubClass object.
   *
   * @param key the primary key for the ChildRootClass
   * @param rs  the query result set
   * @return a CompositeParent instance based on the result set data
   * @throws SQLException               when there is a problem getting data from the results
   * @throws InvalidParametersException when a required value is null
   */
  public static ChildRootClass getChildRootClassData(IPrimaryKey key, ResultSet rs) throws
    SQLException, InvalidParametersException {
    return new ChildRootClass(key, rs.getInt("parentId"), rs.getInt("childId"),
                              rs.getString("rootColumnData"));
  }

  /**
   * <p> Get a primary key for a ChildRootClass based on a result set that must contain the parent
   * primary key column. The method creates a natural primary key.</p>
   *
   * @param rs     a JDBC result set with primary key columns
   * @param prefix an optional prefix string for derived column names in associations
   * @return a NaturalKey primary key
   * @throws SQLException               when there is a problem getting data from the result set
   * @throws InvalidParametersException when there is a problem creating a key
   */
  public static IPrimaryKey getChildRootClassPrimaryKey(ResultSet rs, String prefix) throws
    SQLException, InvalidParametersException {
    IPrimaryKey key;
    if (prefix == null) {
      prefix = "";
    }
    java.util.ArrayList<com.poesys.db.col.AbstractColumnValue> list = new java.util.ArrayList<>();

    list.add(new com.poesys.db.col.IntegerColumnValue(prefix + "parentId", rs.getInt("parentId")));
    list.add(new com.poesys.db.col.IntegerColumnValue(prefix + "childId", rs.getInt("childId")));
    key =
      com.poesys.db.pk.PrimaryKeyFactory.createNaturalKey(list, "com.poesys.db.dto.ChildRootClass");
    return key;
  }

  /**
   * <p> Get a primary key for a ChildRootClass based on input key attributes. The method creates a
   * natural primary key. </p>
   *
   * @param parentId unique identifier for the parent
   * @param childId  unique identifier for child within parent
   * @return a ConcreteChildSubClass NaturalKey primary key
   * @throws InvalidParametersException when there is a problem creating a key
   */
  public static IPrimaryKey getChildRootClassPrimaryKey(Integer parentId, Integer childId) throws
    InvalidParametersException {
    IPrimaryKey key = null;
    // Track generated inputs for nullity.
    boolean noNulls = true;
    java.util.ArrayList<com.poesys.db.col.AbstractColumnValue> list = new java.util.ArrayList<>();
    if (parentId != null) {
      list.add(new com.poesys.db.col.IntegerColumnValue("parentId", parentId));
      if (childId != null) {
        list.add(new com.poesys.db.col.IntegerColumnValue("childId", childId));
      } else {
        noNulls = false;
      }
    } else {
      noNulls = false;
    }
    if (noNulls) {
      key =
        com.poesys.db.pk.PrimaryKeyFactory.createNaturalKey(list, "com.poesys.db.dto.ChildRootClass");
    }
    return key;
  }

  /**
   * Retrieve the ConcreteChildSubClass data from a result set's current row and create a
   * ConcreteChildSubClass object.
   *
   * @param key the primary key for the ConcreteChildSubClass
   * @param rs  the query result set
   * @return a CompositeParent instance based on the result set data
   * @throws SQLException               when there is a problem getting data from the results
   * @throws InvalidParametersException when a required value is null
   */
  public static ConcreteChildSubClass getConcreteChildSubClassData(IPrimaryKey key, ResultSet rs)
    throws SQLException, InvalidParametersException {
    return new ConcreteChildSubClass(key, rs.getInt("parentId"), rs.getInt("childId"),
                                     rs.getString("rootDataColumn"),
                                     rs.getString("childDataColumn"));
  }

  /**
   * <p> Get a primary key for a ConcreteChildSubClass based on a result set that must contain the
   * parent primary key column. The method creates a natural primary key.</p>
   *
   * @param rs     a JDBC result set with primary key columns
   * @param prefix an optional prefix string for derived column names in associations
   * @return a NaturalKey primary key
   * @throws SQLException               when there is a problem getting data from the result set
   * @throws InvalidParametersException when there is a problem creating a key
   */
  public static IPrimaryKey getConcreteChildSubClassPrimaryKey(ResultSet rs, String prefix)
    throws SQLException, InvalidParametersException {
    IPrimaryKey key;
    if (prefix == null) {
      prefix = "";
    }
    java.util.ArrayList<com.poesys.db.col.AbstractColumnValue> list = new java.util.ArrayList<>();

    list.add(new com.poesys.db.col.IntegerColumnValue(prefix + "parentId", rs.getInt("parentId")));
    list.add(new com.poesys.db.col.IntegerColumnValue(prefix + "childId", rs.getInt("childId")));
    key =
      com.poesys.db.pk.PrimaryKeyFactory.createNaturalKey(list, "com.poesys.db.dto.ConcreteChildSubClass");
    return key;
  }

  /**
   * <p> Get a primary key for a ConcreteChildSubClass based on input key attributes. The method
   * creates a natural primary key. </p>
   *
   * @param parentId unique identifier for the parent
   * @param childId  unique identifier for child within parent
   * @return a ConcreteChildSubClass NaturalKey primary key
   * @throws InvalidParametersException when there is a problem creating a key
   */
  public static IPrimaryKey getConcreteChildSubClassPrimaryKey(Integer parentId, Integer childId)
    throws InvalidParametersException {
    IPrimaryKey key = null;
    // Track generated inputs for nullity.
    boolean noNulls = true;
    java.util.ArrayList<com.poesys.db.col.AbstractColumnValue> list = new java.util.ArrayList<>();
    if (parentId != null) {
      list.add(new com.poesys.db.col.IntegerColumnValue("parentId", parentId));
      if (childId != null) {
        list.add(new com.poesys.db.col.IntegerColumnValue("childId", childId));
      } else {
        noNulls = false;
      }
    } else {
      noNulls = false;
    }
    if (noNulls) {
      key =
        com.poesys.db.pk.PrimaryKeyFactory.createNaturalKey(list, "com.poesys.db.dto.ConcreteChildSubClass");
    }
    return key;
  }
}