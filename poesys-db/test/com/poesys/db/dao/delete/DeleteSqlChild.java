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
package com.poesys.db.dao.delete;


import java.sql.PreparedStatement;

import com.poesys.db.dto.AbstractTestDto;
import com.poesys.db.dto.Child;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * A DELETE command that deletes the Child row identified by a key.
 * </p>
 * 
 * @see com.poesys.db.dto.Child
 * 
 * @author Robert J. Muller
 */
public class DeleteSqlChild implements IDeleteSql<Child> {
  /** SQL statement that deletes the Child row */
  private static final String SQL = "DELETE FROM Child WHERE ";

  @Override
  public String getSql(IPrimaryKey key) {
    StringBuilder builder = new StringBuilder(SQL);
    builder.append(key.getSqlWhereExpression(""));
    return builder.toString();
  }

  @Override
  public int setParams(PreparedStatement stmt, int next, IDbDto dto) {
    AbstractTestDto child = (AbstractTestDto)dto;
    next = child.getPrimaryKey().setParams(stmt, next);
    return next;
  }

  @Override
  public String getParamString(Child dto) {
    StringBuilder builder = new StringBuilder("Parameters: None");
    return builder.toString();
  }
}
