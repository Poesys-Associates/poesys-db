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

import com.poesys.db.dao.update.IUpdateSql;
import com.poesys.db.pk.IPrimaryKey;

import java.sql.PreparedStatement;

/**
 * SQL statement specification for updating a CompositeParent with read/write properties
 *
 * @author Robert J. Muller
 */
public class UpdateCompositeParent implements IUpdateSql<CompositeParent> {
  /** SQL UPDATE statement for Entity */
  private static final String SQL = "UPDATE CompositeParent SET parentDataColumn = ? WHERE ";

  @Override
  public String getSql(IPrimaryKey key) {
    return SQL + key.getSqlWhereExpression("");
  }

  @Override
  public int setParams(PreparedStatement stmt, int index, CompositeParent object) {
    // sets primary key in where clause
    index = object.getPrimaryKey().setParams(stmt, index);
    try {
      stmt.setString(index, object.getParentDataColumn());
    } catch (java.sql.SQLException e) {
      throw new com.poesys.db.DbErrorException("SQL error setting parameters", e);
    }
    return index;
  }

  @Override
  public String getParamString(CompositeParent dto) {
    StringBuilder builder = new StringBuilder("Parameters: \"");
    builder.append("\", ");
    builder.append(dto.getParentDataColumn());
    builder.append("\"");
    return builder.toString();
  }
}
