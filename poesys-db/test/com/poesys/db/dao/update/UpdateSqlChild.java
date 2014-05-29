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
package com.poesys.db.dao.update;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.poesys.db.dto.Child;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An UPDATE command that updates the Child table childNumber and col1 columns.
 * </p>
 * 
 * @see com.poesys.db.dto.Child
 * 
 * @author Bob Muller (muller@computer.org)
 */
public class UpdateSqlChild implements IUpdateSql<Child> {
  /** SQL statement that updates col1 */
  private static final String SQL =
    "UPDATE Child SET child_number = ?, col1 = ? WHERE ";

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dao.update.IUpdateSql#getSql(com.poesys.db.pk.IPrimaryKey)
   */
  public String getSql(IPrimaryKey key) {
    StringBuilder builder = new StringBuilder(SQL);
    builder.append(key.getSqlWhereExpression(""));
    return builder.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dao.update.IUpdateSql#setParams(java.sql.PreparedStatement,
   *      int, java.lang.Object)
   */
  public int setParams(PreparedStatement stmt, int next,
                                          Child dto) throws SQLException {
    stmt.setBigDecimal(next, new BigDecimal(dto.getChildNumber()));
    next++;
    stmt.setString(next, dto.getCol1());
    next++;
    next = dto.getPrimaryKey().setParams(stmt, next);
    return next;
  }
}
