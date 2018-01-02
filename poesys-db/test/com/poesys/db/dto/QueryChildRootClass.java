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

import com.poesys.db.DbErrorException;
import com.poesys.db.dao.query.IKeyQuerySql;
import com.poesys.db.pk.IPrimaryKey;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <p> A query Command pattern object that implements a SQL key query for the Account. This SQL
 * specification contains a SQL statement that queries a single Account object from the database
 * using the primary key. </p>
 *
 * @author Poesys/DB Cartridge
 */
public class QueryChildRootClass implements IKeyQuerySql<ChildRootClass> {
  private static final Logger logger = Logger.getLogger(QueryChildRootClass.class);
  /** SQL query statement for Account */
  // @formatter:off
  private static final String SQL =
    "SELECT ChildRootClass.parentId, ChildRootClass.dataColumn, ConcreteChildSubClass.childDataColumn, 'ConcreteChildSubClass' AS discriminant " +
      "FROM ChildRootClass LEFT OUTER JOIN " +
           "ConcreteChildSubClass ConcreteChildSubClass ON ChildRootClass.parentId = ConcreteChildSubClass.parentId AND ChildRootClass.rootId = ConcreteChildSubClass.rootId WHERE ";
  // @formatter:on

  public ChildRootClass getData(IPrimaryKey key, ResultSet rs) {
    try {
      // Account has concrete subclasses, so the query returns an object of the actual
      // type rather than just of type Account. It uses a discriminant expression
      // that the result set returns to figure out which class to instantiate.

      // Get the discriminant from the result set.
      String discriminant = rs.getString("discriminant");

      // Check whether the discriminant is null and throw exception.
      if (discriminant == null) {
        throw new DbErrorException(
          "Missing subclass for queried object of superclass ChildRootClass");
      }

      ChildRootClass data = null;
       // Check for ConcreteChildSubClass, set return only if not already set
      if (discriminant.equals("ConcreteChildSubClass") && data == null) {
        // Use the account factory to get the data.
        data = TestFactory.getChildRootClassData(key, rs);
      }
      return data;
    } catch (com.poesys.db.InvalidParametersException | SQLException e) {
      logger.error("Error getting data", e);
      throw new com.poesys.db.DbErrorException("Error getting data", e);
    }
  }

  @Override
  public String getSql(IPrimaryKey key) {
    return SQL + key.getSqlWhereExpression("Account");
  }
}