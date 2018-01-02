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



import com.poesys.db.dao.delete.AbstractDeleteSql;
import com.poesys.db.pk.IPrimaryKey;

/**
 * SQL statement specification for deleting a Account
 * 
 * @author Robert J. Muller
 */
public class DeleteChildRootClass extends AbstractDeleteSql<ChildRootClass> {
  private static final String SQL =
    "DELETE FROM ChildRootClass WHERE ";

  @Override
  public String getSql(IPrimaryKey key) {
    return super.getSql(key, SQL);
  }

  @Override
  public String getParamString(ChildRootClass dto) {
    return null;
  }
}
