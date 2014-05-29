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
package com.poesys.db.dao.ddl;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.Test;

import java.sql.Connection;
import com.poesys.db.connection.IConnectionFactory.DBMS;
import com.poesys.db.dao.ConnectionTest;

/**
 * 
 * @author Robert J. Muller
 */
public class TruncateTableTest extends ConnectionTest {

  /**
   * Test method for {@link com.poesys.db.dao.ddl.TruncateTableSql#getSql()}.
   * @throws IOException when can't get property
   * @throws SQLException when can't truncate table
   */
  @Test
  public void testGetSql() throws IOException, SQLException {
    Connection connection = null;
    ISql sql = new TruncateTableSql("TestNatural");
    IExecuteSql executive = new ExecuteSql(sql);
    try {
      connection = getConnection(DBMS.MYSQL, "com.poesys.db.poesystest.mysql");
    } catch (SQLException e) {
      throw new RuntimeException("Connect failed: " + e.getMessage(), e);
    }
    executive.execute(connection);
  }

}
