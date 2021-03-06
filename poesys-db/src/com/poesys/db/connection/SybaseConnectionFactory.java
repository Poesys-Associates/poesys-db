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

import net.sourceforge.jtds.jdbcx.JtdsDataSource;


/**
 * A factory for Sybase connections.
 * 
 * @author Robert J. Muller
 */
public class SybaseConnectionFactory implements IConnectionFactory {
  /** cached jtds data source */
  private static JtdsDataSource ds = null;

  /** Class name of the Sybase JDBC driver */
  public static final String DRIVER = "net.sourceforge.jtds.jdbc.Driver";

  /** Write-only Sybase user name for the current user to which to connect */
  private String user = null;

  /** Write-only Sybase host name for the current host to which to connect */
  private String host = null;

  /** Write-only Sybase port number for the current host to which to connect */
  private Integer port = null;

  /** Write-only JDBC user password for the Sybase JDBC driver. */
  private String password = null;

  /** Write-only JDBC database name for the Sybase JDBC driver. */
  private String database = null;

  @Override
  public void setDatabase(String database) {
    this.database = database;
  }

  @Override
  public void setUser(String user) {
    this.user = user;
  }

  @Override
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
    if (ds == null) {
      ds = new JtdsDataSource();
      ds.setServerType(net.sourceforge.jtds.jdbc.Driver.SYBASE);
      ds.setServerName(host);
      ds.setPortNumber(port);
      ds.setDatabaseName(database);
      ds.setUser(user);
    }

    // Set the password in case it has changed.
    ds.setPassword(password);

    Connection connection = ds.getConnection();

    // Set the autocommit feature off to handle transaction logic in the
    // business delegates or remote interfaces.
    connection.setAutoCommit(false);
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
  }

  public DBMS getDbms() {
    return DBMS.SYBASE;
  }
}
