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
package com.poesys.db.dao.update;


import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.poesys.db.DbErrorException;
import com.poesys.db.dto.TestNatural;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An UPDATE command that updates the TestNatural table col1 column, a table
 * with two natural key columns and one non-key column (col1).
 * </p>
 * 
 * @see com.poesys.db.dto.TestNatural
 * 
 * @author Robert J. Muller
 */
public class UpdateSqlTestNatural implements IUpdateSql<TestNatural> {
  /** SQL statement that updates col1 */
  private static final String SQL = "UPDATE TestNatural SET col1 = ? WHERE ";

  @Override
  public String getSql(IPrimaryKey key) {
    StringBuilder builder = new StringBuilder(SQL);
    builder.append(key.getSqlWhereExpression(""));
    return builder.toString();
  }

  @Override
  public int setParams(PreparedStatement stmt, int next, TestNatural dto) {
    try {
      stmt.setBigDecimal(next, dto.getCol1());
      next++;
      next = dto.getPrimaryKey().setParams(stmt, next);
    } catch (SQLException e) {
      throw new DbErrorException("SQL error", e);
    }
    return next;
  }

  @Override
  public String getParamString(TestNatural dto) {
    StringBuilder builder = new StringBuilder("Parameters: \"");
    builder.append(dto.getCol1());
    builder.append("\"");
    return builder.toString();
  }
}
