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
package com.poesys.db.dao.insert;


import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.poesys.db.DbErrorException;
import com.poesys.db.dto.Link2;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An implementation of IInsertSql for the Link1 table that provides an INSERT
 * statement and parameter setting for a first link object.
 * </p>
 * 
 * @see com.poesys.db.dto.TestNatural
 * 
 * @author Robert J. Muller
 */
public class InsertSqlLink2 implements IInsertSql<Link2> {
  private static final String SQL1 = "INSERT INTO Link2 (";
  private static final String SQL2 = ", col) VALUES (?, ?)";

  @Override
  public String getSql(IPrimaryKey key) {
    StringBuilder builder = new StringBuilder(SQL1);
    builder.append(key.getSqlInsertColumnList());
    builder.append(SQL2);
    return builder.toString();
  }

  @Override
  public void setParams(PreparedStatement stmt, int next, Link2 dto) {
    try {
      stmt.setString(next, dto.getCol());
    } catch (SQLException e) {
      throw new DbErrorException("SQL error", e);
    }
  }
}
