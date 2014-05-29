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
package com.poesys.db.dao.delete;


import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.poesys.db.dto.TestMultipleParams;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * A DELETE command that deletes multiple rows in the TestMultiple table, a
 * table with a sequence key. This IDeleteSql class uses the TestMultipleParams
 * DTO, which contains the parameters for the DELETE statement.
 * </p>
 * 
 * @author Bob Muller (muller@computer.org)
 */
public class DeleteSqlTestMultiple implements IDeleteSqlWithParameters<TestMultipleParams, TestMultipleParams> {
  /** SQL statement that deletes a row */
  private static final String SQL =
    "DELETE FROM TestMultiple WHERE colType = ?";

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dao.insert.IInsertSql#getSql(com.poesys.db.pk.IPrimaryKey)
   */
  public String getSql(IPrimaryKey ignored) {
    StringBuilder builder = new StringBuilder(SQL);
    return builder.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dao.insert.IInsertSql#setParams(java.sql.PreparedStatement,
   *      int, java.lang.Object)
   */
  public int setParams(PreparedStatement stmt, int ignored, TestMultipleParams dto)
      throws SQLException {
    TestMultipleParams test = dto;
    stmt.setString(1, test.getColType());
    return 2;
  }
}
