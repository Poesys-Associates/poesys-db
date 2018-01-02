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

import com.poesys.db.dao.query.IParameterizedQuerySql;
import com.poesys.db.pk.IPrimaryKey;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <p>
 * A query Command pattern object that implements a SQL query of a collection of 
 * Account objects using the primary key of an associated 
 * Entity object through the association Accounts. This
 * is a many-to-one association from Account to Entity.
 * </p>
 * <p>
 * This SQL specification contains a SQL statement that queries a collection of
 * Account objects from the database using the foreign key type
 * Entity.
 * </p>
 * 
 * @author Poesys/DB Cartridge
 */
public class QueryChildrenByParent
    implements IParameterizedQuerySql<ChildRootClass, CompositeParent> {
  /** SQL query statement for ChildRootClass */
  private static final String SQL =
    "ChildRootClass.parentId, ChildRootClass.dataColumn, ConcreteChildSubClass.childDataColumn, 'ConcreteChildSubClass' AS discriminant FROM ChildRootClass LEFT OUTER JOIN " +
      "ConcreteChildSubClass ConcreteChildSubClass ON ChildRootClass.parentId = ConcreteChildSubClass.parentId AND ChildRootClass.rootId = ConcreteChildSubClass.rootId WHERE ChildRootClass.parentId = ?";

  @Override
  public void bindParameters(PreparedStatement stmt, CompositeParent parameters) {
    // Set the parameters starting with the first parameter.
    parameters.getPrimaryKey().setParams(stmt, 1);
  }

  @Override
  public String getParameterValues(CompositeParent parameters) {
    // Create the output string with the key parameters.
    return parameters.getPrimaryKey().getValueList();
  }

  @Override
  public ChildRootClass getData(ResultSet rs) {
    try {
      return TestFactory.getChildRootClassData(getPrimaryKey(rs), rs);
    } catch (com.poesys.db.InvalidParametersException | SQLException e) {
      throw new com.poesys.db.DbErrorException("Error getting data", e);
    }
  }

  @Override
  public IPrimaryKey getPrimaryKey(ResultSet rs) {
    try {
      return TestFactory.getChildRootClassPrimaryKey(rs, "");
    } catch (com.poesys.db.InvalidParametersException | SQLException e) {
      throw new com.poesys.db.DbErrorException("Error getting primary key", e);
    }
  }

  @Override
  public String getSql() {
    return SQL;
  }
}