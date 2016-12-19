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
package com.poesys.db.dao.query;


import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.poesys.db.DbErrorException;
import com.poesys.db.dto.TestSequence;
import com.poesys.db.pk.IPrimaryKey;
import com.poesys.db.pk.PrimaryKeyFactory;


/**
 * SQL statement with parameters.
 * 
 * @author Robert J. Muller
 */
public class TestSequenceQueryWithParametersSql implements
    IParameterizedQuerySql<TestSequence, TestSequence> {
  private static final String SQL =
    "SELECT pkey, col1 FROM TestSequence WHERE col1 = ?";
  private static final String CLASS_NAME = "com.poesys.test.TestSequence";

  @Override
  public void bindParameters(PreparedStatement stmt, TestSequence parameters) {
    try {
      stmt.setString(1, parameters.getCol1());
    } catch (SQLException e) {
      throw new DbErrorException("SQL error", e);
    }
  }

  public String getParameterValues(TestSequence parameters) {
    return "[col1=" + parameters.getCol1() + "]";
  }

  @Override
  public IPrimaryKey getPrimaryKey(ResultSet rs) {
    BigInteger pkey;
    try {
      pkey = rs.getBigDecimal("pkey").toBigInteger();
    } catch (SQLException e) {
      throw new DbErrorException("SQL error", e);
    }
    IPrimaryKey key =
      PrimaryKeyFactory.createSequenceKey("pkey", pkey, CLASS_NAME);
    return key;
  }

  @Override
  public TestSequence getData(ResultSet rs) {
    String col1;
    try {
      col1 = rs.getString("col1");
    } catch (SQLException e) {
      throw new DbErrorException("SQL error", e);
    }
    TestSequence dto = new TestSequence(getPrimaryKey(rs), col1);
    return dto;
  }

  @Override
  public String getSql() {
    return SQL;
  }

}
