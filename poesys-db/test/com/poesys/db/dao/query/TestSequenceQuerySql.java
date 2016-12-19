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
import java.sql.ResultSet;
import java.sql.SQLException;

import com.poesys.db.DbErrorException;
import com.poesys.db.dto.TestSequence;
import com.poesys.db.pk.IPrimaryKey;
import com.poesys.db.pk.PrimaryKeyFactory;


/**
 * SQL query for a query of all TestSequence objects.
 * 
 * @author Robert J. Muller
 */
public class TestSequenceQuerySql implements IQuerySql<TestSequence> {
  private static final String SQL =
    "SELECT pkey, col1 FROM TestSequence ORDER BY pkey";
  private static final String CLASS_NAME = "com.poesys.test.TestSequence";

  @Override
  public TestSequence getData(ResultSet rs) {
    String col1;
    try {
      col1 = rs.getString("col1");
    } catch (SQLException e) {
      throw new DbErrorException("SQL error", e);
    }
    IPrimaryKey key = getPrimaryKey(rs);
    TestSequence dto = new TestSequence(key, col1);
    return dto;
  }

  @Override
  public String getSql() {
    return SQL;
  }

  @Override
  public IPrimaryKey getPrimaryKey(ResultSet rs) {
    BigInteger pkey;
    try {
      pkey = rs.getBigDecimal("pkey").toBigInteger();
    } catch (SQLException e) {
      throw new DbErrorException("SQL error", e);
    }
    return PrimaryKeyFactory.createSequenceKey("pkey", pkey, CLASS_NAME);
  }
}
