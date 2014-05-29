/*
 * Copyright (c) 2012 Carnegie Institution for Science. All rights reserved.
 */

package com.poesys.db.connection;


import com.poesys.db.connection.IConnectionFactory.DBMS;


/**
 * The implementation of the IJdbcDriver interface for the JTDS SQL Server and
 * Sybase ASE 12.5 driver; defaults to Sybase
 * 
 * http://jtds.sourceforge.net/faq.html
 * 
 * @author Robert J. Muller
 */
public class JtdsDriver implements IJdbcDriver {

  /** DBMS server type: 'sqlserver' or 'sybase' (default) */
  private String serverType = "sybase";
  /** the DBMS server host name or IP address */
  private String host;
  /** the server port (default 3306) */
  private String port = "3306";
  /** the MySQL database name */
  private String database;
  /** the JDBC parameter string (see URL in the class comment) */
  private String parameters = "";
  /** the string builder for the URL */
  private StringBuilder url = new StringBuilder();
  /** the DBMS type within Poesys/DB */
  private DBMS dbms = DBMS.SYBASE;

  /**
   * Create a MySqlDriver object with the minimal necessary information.
   * 
   * @param host the DBMS server name or IP address
   * @param database the MySQL database name
   */
  JtdsDriver(String host, String database) {
    this.host = host;
    this.database = database;
  }

  @Override
  public void setHost(String host) {
    this.host = host;
  }

  /**
   * Set the server type to SQL Server or Sybase.
   * 
   * @param serverType "sqlserver" or "sybase"
   */
  public void setServerType(String serverType) {
    this.serverType = serverType;
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
    // jdbc:jtds:<server_type>://<server>[:<port>][/<database>][;<property>=<value>[;...]]
    if (url.length() == 0) {
      url.append("jdbc:jtds:");
      url.append(serverType);
      url.append("://");
      url.append(host);
      url.append(":");
      url.append(port);
      url.append("/");
      url.append(database);
      url.append(parameters);
    }
    return url.toString();
  }

  /**
   * Set the Poesys/DB database type to Sybase or SQL Server.
   * 
   * @param dbms DBMS.SYBASE or DBMS.SQL_SERVER (not yet supported)
   */
  public void setDbms(DBMS dbms) {
    this.dbms = dbms;
  }

  @Override
  public String getDriver() {
    return "net.sourceforge.jtds.jdbc.Driver";
  }

  @Override
  public DBMS getDbms() {
    return dbms;
  }
}
