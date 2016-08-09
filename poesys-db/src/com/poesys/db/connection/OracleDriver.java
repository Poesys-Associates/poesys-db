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
  private String service;
  private StringBuilder url = new StringBuilder();

  /**
   * Create a OracleDriver object with the minimal connection information.
   * 
   * @param host the database server name or IP address
   * @param database the ORACLE SID name
   * @param service the ORACLE service name
   */
  OracleDriver(String host, String database, String service) {
    this.host = host;
    this.database = database;
    this.service = service;
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
  
  public void setService(String service) {
    this.service = service;
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
    if (url.length() == 0) {
      if (service == null && database != null) {
        // Use the SID form of the URI.
        // jdbc:oracle:thin:@//<host>:<port>/<sid>
        url.append("jdbc:oracle:thin:@//");
        url.append(host);
        url.append(":");
        url.append(port);
        url.append("/");
        url.append(database);
      } else if (service != null) {
        // Use the service form of the URI.
        // jdbc:oracle:thin:@(DESCRIPTION = (ADDRESS = (PROTOCOL = TCP)(HOST =
        // <host>)(PORT = <port>))(CONNECT_DATA =(SERVER =
        // DEDICATED)(SERVICE_NAME = <service>)))
        url.append("jdbc:oracle:thin:@(DESCRIPTION = (ADDRESS = (PROTOCOL = TCP)(HOST = ");
        url.append(host);
        url.append(")(PORT = ");
        url.append(port);
        url.append("))(CONNECT_DATA =(SERVER = DEDICATED)(SERVICE_NAME = ");
        url.append(service);
        url.append(")))");
      }
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
