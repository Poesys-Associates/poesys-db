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
package com.poesys.db.dao.insert;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.poesys.db.dto.RootClass;
import com.poesys.db.pk.IPrimaryKey;

/**
 * <p>
 * An implementation of IInsertSql for the RootClass table that provides an INSERT
 * statement and parameter setting for a RootClass object.
 * </p>
 * 
 * @see com.poesys.db.dto.TestNatural
 * 
 * @author Bob Muller (muller@computer.org)
 */
public class InsertSqlRootClass implements IInsertSql<RootClass> {
	private static final String SQL1 = "INSERT INTO RootClass (";
	private static final String SQL2 = ", root_col) VALUES (?, ?)";

	  /*
	   * (non-Javadoc)
	   * 
	   * @see com.poesys.db.dao.insert.IInsertSql#getSql(com.poesys.db.pk.IPrimaryKey)
	   */
	  public String getSql(IPrimaryKey key) {
	    StringBuilder builder = new StringBuilder(SQL1);
	    builder.append(key.getSqlInsertColumnList());
	    builder.append(SQL2);
	    return builder.toString();
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see com.poesys.db.dao.insert.IInsertSql#setParams(java.sql.PreparedStatement,
	   *      int, java.lang.Object)
	   */
	  public void setParams(PreparedStatement stmt, int next, RootClass dto)
	      throws SQLException {
	    stmt.setString(next, dto.getRootCol());
	  }
}
