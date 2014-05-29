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

import com.poesys.db.InvalidParametersException;
import com.poesys.db.dto.TestSequence;
import com.poesys.db.pk.IPrimaryKey;
import com.poesys.db.pk.PrimaryKeyFactory;


/**
 * 
 * @author Bob Muller (muller@computer.org)
 */
public class TestSequenceQueryWithParametersSql implements
    IParameterizedQuerySql<TestSequence, TestSequence> {
  private static final String SQL =
    "SELECT pkey, col1 FROM TestSequence WHERE col1 = ?";
  private static final String CLASS_NAME = "com.poesys.test.TestSequence";

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.poesys.db.dao.query.IParameterizedQuerySql#bindParameters(java.sql.
   * PreparedStatement, com.poesys.db.dto.IDbDto)
   */
  public void bindParameters(PreparedStatement stmt, TestSequence parameters)
      throws SQLException {
    stmt.setString(1, parameters.getCol1());
  }

  public String getParameterValues(TestSequence parameters) {
    return "[col1=" + parameters.getCol1() + "]";
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.poesys.db.dao.query.IParameterizedQuerySql#getPrimaryKey(java.sql.ResultSet
   * )
   */
  public IPrimaryKey getPrimaryKey(ResultSet rs) throws SQLException,
      InvalidParametersException {
    BigInteger pkey = rs.getBigDecimal("pkey").toBigInteger();
    IPrimaryKey key =
      PrimaryKeyFactory.createSequenceKey("pkey", pkey, CLASS_NAME);
    return key;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.poesys.db.dao.query.IParameterizedQuerySql#getData(java.sql.ResultSet)
   */
  public TestSequence getData(ResultSet rs) throws SQLException {
    String col1 = rs.getString("col1");
    TestSequence dto = new TestSequence(getPrimaryKey(rs), col1);
    return dto;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dao.query.IParameterizedQuerySql#getSql()
   */
  public String getSql() {
    return SQL;
  }

}
