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


import com.poesys.db.DbErrorException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.col.IColumnValue;
import com.poesys.db.col.StringColumnValue;
import com.poesys.db.dto.TestNatural;
import com.poesys.db.pk.IPrimaryKey;
import com.poesys.db.pk.NaturalPrimaryKey;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * SQL query class for querying all TestNatural objects.
 * 
 * @author Robert J. Muller
 */
public class TestNaturalAllQuerySql implements IQuerySql<TestNatural> {
  /** SQL primary key query for TestNatural */
  private static final String SQL = "SELECT key1, key2, col1 FROM TestNatural";
  private static final String CLASS_NAME = "com.poesys.test.TestNatural";

  @Override
  public TestNatural getData(ResultSet rs) {
    String key1;
    String key2;
    BigDecimal col;
    try {
      key1 = rs.getString("key1");
      key2 = rs.getString("key2");
      col = rs.getBigDecimal("col1");
    } catch (SQLException e) {
      throw new DbErrorException("SQL error", e);
    }
    return new TestNatural(key1, key2, col);
  }

  @Override
  public String getSql() {
    return SQL;
  }

  @Override
  public IPrimaryKey getPrimaryKey(ResultSet rs) {
    List<IColumnValue> cols = new ArrayList<>(2);
    try {
      IColumnValue key1 =
        new StringColumnValue("key1", rs.getString("key1"));
      IColumnValue key2 =
        new StringColumnValue("key2", rs.getString("key2"));
      cols.add(key1);
      cols.add(key2);
    } catch (InvalidParametersException e) {
      throw new DbErrorException("Invalid parameters", e);
    } catch (SQLException e) {
      throw new DbErrorException("SQL error", e);
    }
    return new NaturalPrimaryKey(cols, CLASS_NAME);
  }
}
