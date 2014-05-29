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
package com.poesys.db.dao.query;


import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.poesys.db.dto.TestSequence;


/**
 * 
 * @author Robert J. Muller
 */
public class TestSequenceParameterizedCountSql implements
    IParameterizedCountSql<TestSequence> {
  /** SQL statement that counts TestSequence objects based on col1 value */
  private static final String SQL =
      "SELECT count(*) AS count FROM TestSequence WHERE col1 = ?";

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dao.query.IParameterizedCountSql#bindParameters(java.sql.PreparedStatement,
   *      com.poesys.db.dto.IDto)
   */
  public void bindParameters(PreparedStatement stmt, TestSequence parameters)
      throws SQLException {
    stmt.setString(1, parameters.getCol1());
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dao.query.IParameterizedCountSql#getSql()
   */
  public String getSql() {
    return SQL;
  }
}
