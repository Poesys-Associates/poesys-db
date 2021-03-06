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
package com.poesys.db.dao;


import com.poesys.db.InvalidParametersException;
import com.poesys.db.connection.ConnectionFactoryFactory;
import com.poesys.db.connection.IConnectionFactory.DBMS;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;


/**
 * An abstract TestCase subclass that provides services for concrete test
 * subclasses that require a database connection; subclasses must implement the
 * getPassword method.
 * 
 * @author rmuller
 */
public abstract class ConnectionTest {
  /** Define a class logger. */
  private static Logger logger = Logger.getLogger(ConnectionTest.class);

  /**
   * Get a connection to the poesystest subsystem using MySQL JDBC. This method
   * calls a private method, getPassword(), to get the database password.
   * 
   * @return an open connection to the database
   * @throws SQLException when there is a database problem getting the
   *           connection
   * @throws IOException when there is a problem reading the database.properties
   *           file that initializes connections
   */
  protected Connection getConnection() throws SQLException, IOException, InvalidParametersException {
    Connection connection;
    try {
      connection =
        ConnectionFactoryFactory.getInstance(getSubsystem(), DBMS.MYSQL).getConnection(getPassword());
    } catch (SQLException | IOException e) {
      logger.error(e.getMessage(), e);
      throw e;
    }
    return connection;
  }

  protected String getSubsystem() {
    return "com.poesys.db.poesystest.mysql";
  }

  /**
   * Get the test user password.
   * 
   * @return a database password appropriate to the test suite being run
   */
  private String getPassword() {
    return "test";
  }
}
