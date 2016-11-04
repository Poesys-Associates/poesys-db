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

import org.apache.log4j.Logger;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;


/**
 * A factory for MySQL connections.
 * 
 * @author Robert J. Muller
 */
public class MySqlConnectionFactory implements IConnectionFactory {
  /** logger for this class */
  private static final Logger logger =
    Logger.getLogger(MySqlConnectionFactory.class);
  /** Cached data source object */
  private MysqlDataSource ds;

  /** Write-only MySQL user name for the current user to which to connect */
  private String user = null;

  /** Write-only MySQL host name for the current host to which to connect */
  private String host = null;

  /** Write-only MySQL host portnumber for the current host to which to connect */
  private Integer port = null;

  /** Write-only JDBC user password for the MySQL JDBC driver. */
  private String password = null;

  /** Write-only JDBC database name for the MySQL JDBC driver. */
  private String database = null;

  /**
   * Set the database name to which to connect.
   * 
   * @param database The database name to set.
   */
  public void setDatabase(String database) {
    this.database = database;
  }

  /**
   * Set the current user to which to connect
   * 
   * @param user the user to which to connect
   */
  public void setUser(String user) {
    this.user = user;
  }

  /**
   * Set the Oracle SID of the target database
   * 
   * @param host the current host to which to connect
   */
  public void setHost(String host) {
    this.host = host;
  }

  @Override
  public void setPort(Integer port) {
    this.port = port;
  }

  @Override
  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public Connection getConnection(String password) throws SQLException {
    // Create a new data source if one doesn't already exist.
    if (ds == null) {
      ds = new MysqlDataSource();
      ds.setServerName(host);
      ds.setPort(port);
      ds.setDatabaseName(database);
      ds.setUser(user);
      logger.debug("Created MySQL data source " + user + "@" + host + ":"
                   + port + "/" + database);
    }

    // Set the password in case it has changed.
    ds.setPassword(password);

    // Get the connection.
    Connection connection = ds.getConnection();

    // Set the autocommit feature off to handle transaction logic in the
    // business delegates or remote interfaces.
    if (connection != null) {
      logger.debug("Acquired direct MySQL JDBC connection " + connection);
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
    ds = null;
  }

  @Override
  public void close() throws ConnectionException {
    ds = null;
  };

  @Override
  public DBMS getDbms() {
    return DBMS.MYSQL;
  }
}
