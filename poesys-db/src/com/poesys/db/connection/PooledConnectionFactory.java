/*
 * Copyright (c) 2011 Poesys Associates. All rights reserved.
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
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import com.poesys.db.Message;


/**
 * A factory for JDBC connections. Each factory is specific to a database and
 * contains three connection pools: a read-only, a read-write, and an owner
 * connection pool. The default connection pool returns read-write connections.
 * If you need to access more than one database with a connection pool, you need
 * to create multiple factories, one per target database. See
 * http://tomcat.apache.org/tomcat-7.0-doc/jdbc-pool.html for complete
 * documentation of the Apache connection pool parameters.
 * 
 * <p>
 * TODO: Implement separate pools for read-only and owner data sources; Support
 * DataSource as well as URL JDBC access
 * </p>
 * 
 * @author Robert J. Muller
 */
public class PooledConnectionFactory implements IConnectionFactory {
  /** Log4j logger for this class */
  private static final Logger logger =
    Logger.getLogger(PooledConnectionFactory.class);

  /** Cached JDBC read-write data source */
  private static DataSource readWriteDataSource = new DataSource();

  private IJdbcDriver driver = null;

  /** Write-only user name for the current user to which to connect */
  private String user = null;

  /** Write-only JDBC user password for the Oracle JDBC driver. */
  private String password = null;

  /** the initial size of the connection pool */
  final private int initialSize;

  /** the minimum number of idle connections in the pool */
  final private int minIdleSize;

  /** the maximum number of idle connections in the pool */
  final private int maxIdleSize;

  /** the maximum size of the connection pool */
  final private int maxSize;

  /** A query to use to validate the connection, set by constructor */
  private String validationQuery;

  /** Constant validation interval in milliseconds */
  private static final int VALIDATION_INTERVAL = 30000;

  /** Constant time between idle-connection eviction runs in milliseconds */
  private static final int EVICTION_INTERVAL = 5 * 60 * 1000;

  /**
   * Constant minimum amount of time an object may sit idle in the pool before
   * it is eligible for eviction; twice EVICTION_INTERVAL
   */
  private static final int IDLE_TIME = 2 * EVICTION_INTERVAL;

  /** Constant maximum wait for an available connection in milliseconds */
  private static final int MAX_WAIT = 10000;

  // Error messages
  /** Error when can't open pooled data source */
  private static final String NO_POOLED_DATASOURCE_ERROR =
    "com.poesys.db.connection.msg.no_pooled_datasource";
  /** Error when pooled connection already closed */
  private static final String CONN_CLOSED_ERROR =
    "com.poesys.db.connection.msg.pooled_conn_already_closed";

  /**
   * Create a PooledOracleConnectionFactory object. The arguments are those
   * specific to the connection pool
   * 
   * @param initialSize the initial number of connections in the pool
   * @param minSize the minimum number of idle connections to maintain in the
   *          pool after eviction
   * @param maxSize the maximum size of the connection pool
   * @param driver the name of the JDBC driver
   * @param user the login user name
   * @param password the login user password
   * @param validationQuery the query to use to validate a connection
   */
  public PooledConnectionFactory(Integer initialSize,
                                 Integer minSize,
                                 Integer maxSize,
                                 IJdbcDriver driver,
                                 String user,
                                 String password,
                                 String validationQuery) {
    this.initialSize = initialSize;
    this.minIdleSize = minSize;
    this.maxIdleSize = 50;
    this.maxSize = maxSize;
    this.validationQuery = validationQuery;
    this.driver = driver;
    this.user = user;
    this.password = password;
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
   * Set the name of the server.
   * 
   * @param host the current server to which to connect
   */
  public void setHost(String host) {
    this.driver.setHost(host);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.IConnectionFactory#setPort(java.lang.String)
   */
  public void setPort(Integer port) {
    this.driver.setPort(port.toString());
  }

  /**
   * Set the password.
   * 
   * @param password value to which to set the password
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Set the driver. This method controls the JDBC driver you wish to use. This
   * is unique to the connection pool factory, which can manage a pool of
   * connections from any database.
   * 
   * @param driver a Poesys/DB IJdbcDriver object representing the JDBC driver
   */
  public void setDriver(IJdbcDriver driver) {
    this.driver = driver;
  }

  @Override
  public void setDatabase(String database) {
    // Set the SID for the database instance.
    this.driver.setDatabase(database);
  }

  @Override
  public Connection getConnection(String password) throws SQLException {
    if (readWriteDataSource == null) {
      throw new SQLException(Message.getMessage(NO_POOLED_DATASOURCE_ERROR,
                                                null));
    }

    // Initialize the singleton default data source, a read-write data source.
    if (readWriteDataSource.getDriverClassName() == null) {
      PoolProperties p = new PoolProperties();
      String url = driver.getUrl();
      logger.debug("Connecting with pooled JDBC URL: " + url);
      p.setUrl(url);
      p.setDriverClassName(driver.getDriver());
      p.setUsername(user);
      logger.debug("Connecting as user " + user);
      p.setPassword(password);
      p.setJmxEnabled(false);
      p.setTestWhileIdle(false);
      p.setTestOnBorrow(true);
      p.setValidationQuery(validationQuery);
      p.setTestOnReturn(false);
      p.setValidationInterval(VALIDATION_INTERVAL);
      p.setMinEvictableIdleTimeMillis(IDLE_TIME);
      p.setTimeBetweenEvictionRunsMillis(EVICTION_INTERVAL);
      p.setMinIdle(minIdleSize);
      p.setMaxIdle(maxIdleSize);
      p.setMaxActive(maxSize);
      p.setInitialSize(initialSize);
      p.setMaxWait(MAX_WAIT);

      // The following settings support long-running queries. The setup is
      // suitable for batch processing and systems with long queries but would
      // not be helpful in a high-throughput, short-transaction system.
      // p.setSuspectTimeout(TIMEOUT); // warn about possibly abandoned
      // connections
      p.setRemoveAbandoned(true);
      p.setRemoveAbandonedTimeout(1000);
      p.setAbandonWhenPercentageFull(100);
      // Reset abandoned timer for long-running queries
      p.setJdbcInterceptors("ResetAbandonedTimer");

      readWriteDataSource.setPoolProperties(p);
      logger.debug("Set JDBC connection pool properties");
    }

    Connection connection = readWriteDataSource.getConnection();
    logger.debug("Acquired JDBC pooled connection " + connection.hashCode());

    int retries = 10;
    if (connection.isClosed() && retries > 0) {
      // closed connection, try another
      logger.debug("Retrying closed connection");
      connection = readWriteDataSource.getConnection();
      logger.debug("Acquired JDBC pooled connection " + connection.hashCode());
      retries--;
    } else if (connection.isClosed()) {
      throw new SQLException(Message.getMessage(CONN_CLOSED_ERROR, null));
    }

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
    readWriteDataSource.close(true);
    readWriteDataSource = new DataSource();
  }

  @Override
  public void close() throws ConnectionException {
    readWriteDataSource.close(true);
  }

  @Override
  public DBMS getDbms() {
    // Get the actual DBMS from the JDBC driver class.
    return driver.getDbms();
  }

}
