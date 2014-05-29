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


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * An implementation of the ISql interface that executes the SQL statement on
 * demand.
 * 
 * @author Robert J. Muller
 */
public class ExecuteSql implements IExecuteSql {
  /** The SQL statement to execute */
  ISql sql = null;

  /**
   * Create an ExecuteSql object, supplying the SQL statement object.
   * 
   * @param sql the SQL statement object
   */
  public ExecuteSql(ISql sql) {
    this.sql = sql;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dao.ddl.IExecuteSql#execute(java.sql.Connection)
   */
  public void execute(Connection connection) throws SQLException {
    Statement statement = connection.createStatement();
    if (statement != null) {
      statement.execute(sql.getSql());
      statement.close();
    }
  }
}
