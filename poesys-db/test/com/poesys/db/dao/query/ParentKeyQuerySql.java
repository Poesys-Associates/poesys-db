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
 * 
 */
package com.poesys.db.dao.query;


import java.sql.ResultSet;
import java.sql.SQLException;

import com.poesys.db.dto.Parent;
import com.poesys.db.pk.IPrimaryKey;


/**
 * 
 * @author Bob Muller (muller@computer.org)
 */
public class ParentKeyQuerySql implements IKeyQuerySql<Parent> {
  /** SQL primary key query for TestSequence */
  private static final String SQL = "SELECT col1 FROM Parent WHERE ";

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dao.query.IKeyQuerySql#getData(java.sql.ResultSet)
   */
  public Parent getData(IPrimaryKey key, ResultSet rs) throws SQLException {
    String col = rs.getString("col1");
    // Create the setters.
    Parent dto = new Parent(key, col);
    return dto;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dao.query.IKeyQuerySql#getSql(com.poesys.db.pk.IPrimaryKey)
   */
  public String getSql(IPrimaryKey key) {
    return SQL + key.getSqlWhereExpression("");
  }
}
