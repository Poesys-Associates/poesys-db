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

import com.poesys.db.dao.insert.IInsertSql;
import com.poesys.db.pk.IPrimaryKey;

import java.sql.PreparedStatement;

/**
 * SQL statement specification for inserting a CompositeParent
 * 
 * @author Robert J. Muller
 */
public class InsertCompositeParent implements IInsertSql<CompositeParent> {
  private static final String SQL =
    "INSERT INTO CompositeParent (parentId, parentDataColumn) VALUES (?, ?)";

  @Override
  public String getSql(IPrimaryKey key) {
    return SQL;
  }

  @Override
  public void setParams(PreparedStatement stmt, int index, 
                        CompositeParent object) {
    try {
      stmt.setString(index, object.getParentDataColumn());
    } catch (java.sql.SQLException e) {
      String message = com.poesys.db.Message.getMessage("com.poesys.db.sql.msg.parameter", null);
      throw new com.poesys.db.DbErrorException(message, e);
    }
    index++;
  }
  
  @Override
  public String getParamString(CompositeParent object) {
    StringBuilder builder = new StringBuilder();

    // Get the primary key string
    builder.append(object.getPrimaryKey().getStringKey());
    return builder.toString();
  }
}
