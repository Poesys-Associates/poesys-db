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
package com.poesys.db.dao.query;


import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.poesys.db.DbErrorException;
import com.poesys.db.dto.Child;
import com.poesys.db.dto.Parent;
import com.poesys.db.pk.CompositePrimaryKey;
import com.poesys.db.pk.IPrimaryKey;
import com.poesys.db.pk.PrimaryKeyFactory;


/**
 * Implementation of the IParameterizedQuerySql interface for a query of all the
 * children of a parent.
 * 
 * @author Robert J. Muller
 */
public class ChildrenQuerySql implements IParameterizedQuerySql<Child, Parent> {
  private static final String SQL =
    "SELECT parent_id, child_number, col1 FROM Child WHERE parent_id = ? ORDER BY 1";
  private static final String CLASS_NAME = "com.poesys.test.Child";

  @Override
  public void bindParameters(PreparedStatement stmt, Parent parameters) {
    // Use the Parent primary key to set the parameter of the SQL statement.
    IPrimaryKey key = parameters.getPrimaryKey();
    key.setParams(stmt, 1);
  }

  @Override
  public String getParameterValues(Parent parameters) {
    // Use the Parent primary key to get the parameters of the SQL statement.
    return parameters.getPrimaryKey().getValueList();
  }

  @Override
  public IPrimaryKey getPrimaryKey(ResultSet rs) {
    // Build the composite key from the queried data.
    String parentId;
    BigInteger childNumber;
    try {
      parentId = rs.getString("parent_id");
      childNumber = rs.getBigDecimal("child_number").toBigInteger();
    } catch (SQLException e) {
      throw new DbErrorException("SQL error", e);
    }
    CompositePrimaryKey key =
      PrimaryKeyFactory.createCompositeKey("parent_id",
                                           parentId,
                                           "child_number",
                                           childNumber,
                                           CLASS_NAME);
    return key;
  }

  @Override
  public Child getData(ResultSet rs) {

    // Get the column value and build the output child.
    String col1;
    BigInteger childNumber;
    try {
      col1 = rs.getString("col1");
      childNumber = rs.getBigDecimal("child_number").toBigInteger();
    } catch (SQLException e) {
      throw new DbErrorException("SQL error", e);
    }
    return new Child((CompositePrimaryKey)getPrimaryKey(rs), childNumber, col1);
  }

  @Override
  public String getSql() {
    return SQL;
  }
}
