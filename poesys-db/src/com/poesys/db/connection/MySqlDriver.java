/*
 * Copyright (c) 2012 Carnegie Institution for Science. All rights reserved.
 */

package com.poesys.db.connection;


import java.util.List;

import com.poesys.db.connection.IConnectionFactory.DBMS;


/**
 * The implementation of the IJdbcDriver interface for the MySQL 5.5 JDBC driver
 * 
 * https://dev.mysql.com/doc/refman/5.5/en/connector-j-reference-configuration-
 * properties.html
 * 
 * @author Robert J. Muller
 */
public class MySqlDriver implements IJdbcDriver {

  /** the DBMS server host name or IP address */
  private String host;
  /** a list of failover server host names or IP addresses */
  private List<String> failoverHosts;
  /** the server port (default 3306) */
  private String port = "3306";
  /** the MySQL database name */
  private String database;
  /** the JDBC parameter string (see URL in the class comment) */
  private String parameters = "";
  /** the string builder for the URL */
  private StringBuilder url = new StringBuilder();

  /**
   * Create a MySqlDriver object with the minimal necessary information.
   * 
   * @param host the DBMS server name or IP address
   * @param database the MySQL database name
   */
  MySqlDriver(String host, String database) {
    this.host = host;
    this.database = database;
  }

  @Override
  public void setHost(String host) {
    this.host = host;
  }

  /**
   * Set the failover hosts.
   * 
   * @param hosts a list of failover host names or IP addresses
   */
  public void setFailoverHosts(List<String> hosts) {
    failoverHosts = hosts;
  }

  @Override
  public void setPort(String port) {
    this.port = port;
  }

  @Override
  public void setDatabase(String database) {
    this.database = database;
  }

  @Override
  public void setParameters(String parameters) {
    this.parameters = parameters;
  }

  @Override
  public void setDriver(String driver) {
    // Does nothing
  }

  @Override
  public String getUrl() {
    // jdbc:mysql://[host][,failoverhost...][:port]/[database] È
    // [?propertyName1][=propertyValue1][&propertyName2][=propertyValue2]...
    if (url.length() == 0) {
      url.append("jdbc:mysql://");
      url.append(host);
      for (String failoverHost : failoverHosts) {
        url.append(",");
        url.append(failoverHost);
      }
      url.append(":");
      url.append(port);
      url.append("/");
      url.append(database);
      url.append(parameters);
    }
    return url.toString();
  }

  @Override
  public String getDriver() {
    return "com.mysql.jdbc.Driver";
  }

  @Override
  public DBMS getDbms() {
    return DBMS.MYSQL;
  }
}
