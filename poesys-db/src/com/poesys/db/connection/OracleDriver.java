/*
 * Copyright (c) 2012 Carnegie Institution for Science. All rights reserved.
 */

package com.poesys.db.connection;


import com.poesys.db.connection.IConnectionFactory.DBMS;


/**
 * An implementation of the IJdbcDriver interface for the ojdbc6 Oracle driver.
 * 
 * @author Robert J. Muller
 */
public class OracleDriver implements IJdbcDriver {

  private String host;
  private String port = "1521";
  private String database;
  private StringBuilder url = new StringBuilder();

  /**
   * Create a OracleDriver object with the minimal connection information.
   * 
   * @param host the database server name or IP address
   * @param database the ORACLE SID or service name
   */
  OracleDriver(String host, String database) {
    this.host = host;
    this.database = database;
  }

  @Override
  public void setHost(String host) {
    this.host = host;
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
    // Does nothing for ojdbc6, no parameters supported
  }

  @Override
  public void setDriver(String driver) {
    // Does nothing for ojdbc6, driver name doesn't change
  }

  @Override
  public String getUrl() {
    // jdbc:oracle:thin:@//<host>:<port>/<service_name>
    if (url.length() == 0) {
      url.append("jdbc:oracle:thin:@//");
      url.append(host);
      url.append(":");
      url.append(port);
      url.append("/");
      url.append(database);
    }
    return url.toString();
  }

  @Override
  public String getDriver() {
    return "oracle.jdbc.OracleDriver";
  }

  @Override
  public DBMS getDbms() {
    return DBMS.ORACLE;
  }
}
