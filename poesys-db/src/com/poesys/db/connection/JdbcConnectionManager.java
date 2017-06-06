/*
 * Copyright (c) 2016 Poesys Associates. All rights reserved.
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
package com.poesys.db.connection;


import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.poesys.db.DbErrorException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.connection.ConnectionFactoryFactory;
import com.poesys.db.connection.IConnectionFactory.DBMS;


/**
 * An abstract class that provides a connection service for concrete test
 * subclasses that require a JDBC connection; use the static getConnection()
 * method to get a JDBC connection using the Poesys/DB connection settings.
 * 
 * @author Robert J. Muller
 */
public abstract class JdbcConnectionManager {
  /** Define a class logger. */
  private static Logger logger = Logger.getLogger(JdbcConnectionManager.class);
  /** I18N message name for no connection */
  protected static final String noConnectionError =
    "com.poesys.db.msg.noConnection";

  /**
   * Get a connection to the subsystem database using a JDBC driver appropriate
   * to the DBMS specified.
   * 
   * @param dbms the database manager enumerated type value that specifies which
   *          DBMS to use
   * @param subsystem the subsystem to test; corresponds to the actual test
   *          database
   * 
   * @return an open connection to the database
   * @throws SQLException when there is a database problem getting the
   *           connection
   * @throws IOException when there is a problem reading the database.properties
   *           file that initializes connections
   */
  static public Connection getConnection(DBMS dbms, String subsystem)
      throws SQLException, IOException {
    Connection connection = null;
    try {
      connection =
        ConnectionFactoryFactory.getInstance(subsystem, dbms).getConnection();
    } catch (SQLException e) {
      logger.error(e.getMessage(), e);
      throw e;
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      throw e;
    } catch (InvalidParametersException e) {
      logger.error(e.getMessage(), e);
      throw new DbErrorException("Couldn't get default connection", e);
    }
    return connection;
  }
}
