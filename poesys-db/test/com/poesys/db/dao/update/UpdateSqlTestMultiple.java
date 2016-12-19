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
import com.poesys.db.dto.TestMultipleParams;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An UPDATE command that updates multiple rows in the TestMultiple table, a
 * table with a sequence key, a text column col1, and a type column colType.
 * This IUpdateSql class uses the TestMultipleParams DTO, which contains the two
 * parameters for the UPDATE statement.
 * </p>
 * 
 * @author Robert J. Muller
 */
public class UpdateSqlTestMultiple implements IUpdateSql<TestMultipleParams> {
  /** SQL statement that updates col1 */
  private static final String SQL =
    "UPDATE TestMultiple SET col1 = ? WHERE colType = ?";

  @Override
  public String getSql(IPrimaryKey ignored) {
    StringBuilder builder = new StringBuilder(SQL);
    return builder.toString();
  }

  @Override
  public int setParams(PreparedStatement stmt, int ignored,
                       TestMultipleParams dto) {
    try {
      stmt.setString(1, dto.getCol1());
      stmt.setString(2, dto.getColType());
    } catch (SQLException e) {
      throw new DbErrorException("SQL error", e);
    }
    return 3;
  }

  @Override
  public String getParamString(TestMultipleParams dto) {
    StringBuilder builder = new StringBuilder("Parameters: \"");
    builder.append(dto.getCol1());
    builder.append("\", \"");
    builder.append(dto.getColType());
    builder.append("\"");
    return builder.toString();
  }
}
