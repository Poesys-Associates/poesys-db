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


/**
 * An interface for Command-pattern data access objects (DAOs) that execute a
 * SQL DDL statement.
 * 
 * @author Robert J. Muller
 */
public interface IExecuteSql {
  /**
   * Execute the registered SQL DDL statement. 
   * 
   * @param connection the database connection with which to execute the statement
   * @throws SQLException when there is a problem with the query
   */
  public void execute(Connection connection) throws SQLException;
}
