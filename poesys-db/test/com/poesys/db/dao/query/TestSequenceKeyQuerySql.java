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

import com.poesys.db.DbErrorException;
import com.poesys.db.dto.TestSequence;
import com.poesys.db.pk.IPrimaryKey;


/**
 * SQL query class for querying TestSequence objects that have sequence primary
 * keys.
 * 
 * @author Robert J. Muller
 */
public class TestSequenceKeyQuerySql implements IKeyQuerySql<TestSequence> {
  /** SQL primary key query for TestSequence */
  private static final String SQL = "SELECT col1 FROM TestSequence WHERE ";

  @Override
  public TestSequence getData(IPrimaryKey key, ResultSet rs) {
    String col;
    try {
      col = rs.getString("col1");
    } catch (SQLException e) {
      throw new DbErrorException("SQL error", e);
    }
    return new TestSequence(key, col);
  }

  @Override
  public String getSql(IPrimaryKey key) {
    return SQL + key.getSqlWhereExpression("");
  }
}
