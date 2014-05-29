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
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.poesys.db.Message;


/**
 * A factory for Java Naming and Directory Interface (JNDI) data connections.
 * JNDI uses a single name for lookup of the data source, which in turn contains
 * the usual information required to log on to the database (including the
 * password).
 * 
 * @author Robert J. Muller
 */
public class JndiConnectionFactory implements IConnectionFactory {
  private static final Logger logger =
    Logger.getLogger(JndiConnectionFactory.class);
  /**
   * Error resource for the invalid JNDI error message
   */
  private static final String INVALID_JNDI =
    "com.poesys.db.connection.msg.invalid_jndi";

  /** JDBC data source for subsystem */
  private DataSource ds;

  /** JNDI data source name */
  private String name;

  @Override
  public Connection getConnection() throws SQLException {
    Connection connection = null;

    lookUpContext();

    try {
      connection = ds.getConnection();
    } catch (Exception e) {
      // Try again with a new data source, maybe the old one went away.
      ds = null;
      lookUpContext();
      connection = ds.getConnection();
    } finally {
      if (connection != null) {
        logger.debug("Acquired connection " + connection);
      }
    }

    // Set autocommit off to enable transaction processing in delegates.
    if (connection != null) {
      connection.setAutoCommit(false);
    }

    return connection;
  }

  @Override
  public Connection getConnection(String password) throws SQLException {
    Connection connection = null;

    // JNDI embeds the password in the configuration file, so ignore it.
    lookUpContext();

    try {
      connection = ds.getConnection();
    } catch (Exception e) {
      // Try again with a new data source, maybe the old one went away.
      ds = null;
      lookUpContext();
      connection = ds.getConnection();
    } finally {
      if (connection != null) {
        logger.debug("Acquired explicit-password connection " + connection);
      }
    }

    // Set autocommit off to enable transaction processing in delegates.
    if (connection != null) {
      connection.setAutoCommit(false);
    }

    return connection;
  }

  /**
   * Look up the data source in the JNDI context.
   * 
   * @throws SQLException if there is a naming error
   */
  private void lookUpContext() throws SQLException {
    try {
      if (ds == null) {
        InitialContext context = new InitialContext();
        ds = (DataSource)context.lookup(name);
      }
    } catch (NamingException e) {
      Object[] args = { name };
      String msg = Message.getMessage(INVALID_JNDI, args);
      SQLException e2 = new SQLException(msg + ": " + e.getMessage());
      throw e2;
    }
  }

  @Override
  public void setDatabase(String database) {
    this.name = database;
  }

  @Override
  public void setHost(String host) {
    // Do nothing
  }

  @Override
  public void setPassword(String password) {
    // Do nothing
  }

  @Override
  public void setPort(Integer port) {
    // Do nothing
  }

  @Override
  public void setUser(String user) {
    // Do nothing
  }

  @Override
  public void flush() throws ConnectionException {
    InitialContext context;
    try {
      context = new InitialContext();
      ds = (DataSource)context.lookup(name);
    } catch (NamingException e) {
      ConnectionException e2 = new ConnectionException(INVALID_JNDI, e);
      List<String> list = e2.getParameters();
      list.add(name);
    }
  }

  @Override
  public DBMS getDbms() {
    return DBMS.JNDI;
  }
}
