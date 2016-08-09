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
package com.poesys.db.connection;


import java.sql.Connection;
import java.sql.SQLException;

import oracle.jdbc.pool.OracleDataSource;

import org.apache.log4j.Logger;


/**
 * A factory for Oracle connections.
 * 
 * @author Robert J. Muller
 */
public class OracleConnectionFactory implements IConnectionFactory {
  private static final Logger logger =
    Logger.getLogger(OracleConnectionFactory.class);
  /** Cached Oracle data source */
  private OracleDataSource ods = null;

  /** Write-only user name for the current user to which to connect */
  private String user = null;

  /** Write-only host name for the current host to which to connect */
  private String host = null;

  /** Write-only port number for the current host to which to connect */
  private Integer port = null;

  /** Write-only Oracle SID name for the database instance */
  private String database = null;

  /** Write-only Oracle service name for the database instance */
  private String service = null;

  /** Write-only JDBC user password for the Oracle JDBC driver. */
  private String password = null;

  /**
   * Set the current user to which to connect
   * 
   * @param user the user to which to connect
   */
  public void setUser(String user) {
    this.user = user;
  }

  /**
   * Set the name of the server.
   * 
   * @param host the current server to which to connect
   */
  public void setHost(String host) {
    this.host = host;
  }

  @Override
  public void setPort(Integer port) {
    this.port = port;
  }

  /**
   * Set the Oracle-specific service name, which replaces the database name if
   * present.
   * 
   * @param service the Oracle service name
   */
  public void setService(String service) {
    this.service = service;
  }

  @Override
  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public void setDatabase(String database) {
    // Set the SID for the database instance.
    this.database = database;
  }

  @Override
  public Connection getConnection(String password) throws SQLException {
    if (ods == null) {
      ods = new OracleDataSource();
      ods.setDriverType("thin");
      ods.setNetworkProtocol("tcp");
      ods.setServerName(host);
      ods.setPortNumber(port);
      if (service == null) {
        // SID, not service name
        ods.setDatabaseName(database);
        logger.debug("Connecting to Oracle host " + host + ":" + port + " SID "
                     + database + " as user " + user);
      } else {
        // Service, not SID
        ods.setServiceName(service);
        logger.debug("Connecting to Oracle host " + host + ":" + port + " service "
            + service + " as user " + user);
      }
      ods.setUser(user);
    }

    // Set the password in case it has changed.
    ods.setPassword(password);

    Connection connection = ods.getConnection();

    if (connection != null) {
      logger.debug("Acquired direct Oracle JDBC connection " + connection);
      // Set the autocommit feature off to handle transaction logic in the
      // business delegates or remote interfaces.
      connection.setAutoCommit(false);
    }

    return connection;
  }

  @Override
  public Connection getConnection() throws SQLException {
    return getConnection(password);
  }

  @Override
  public void flush() throws ConnectionException {
    ods = null;
  }

  @Override
  public void close() throws ConnectionException {
    ods = null;
  }

  @Override
  public DBMS getDbms() {
    return DBMS.ORACLE;
  }

}
