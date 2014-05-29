/*
 * Copyright (c) 2012 Carnegie Institution for Science. All rights reserved.
 */

package com.poesys.db.connection;


import com.poesys.db.connection.IConnectionFactory.DBMS;


/**
 * An interface for a JDBC driver factory; this interface is an abstract factory
 * that allows setting various components, then generates a valid URL from the
 * components and a valid driver name. Implementations must implement the
 * correct form for a specific JDBC driver and must code the standard driver
 * name, usually as a constant.
 * 
 * @author Robert J. Muller
 */
public interface IJdbcDriver {
  /**
   * Set the name or IP address of the database server.
   * 
   * @param host the host name or IP address
   */
  void setHost(String host);

  /**
   * Set the server port number for database server access.
   * 
   * @param port the port number
   */
  void setPort(String port);

  /**
   * Set the name of the database or instance.
   * 
   * @param database the database or instance name
   */
  void setDatabase(String database);

  /**
   * Set the "additional parameter" string appropriate to this database.
   * 
   * @param parameters the parameter string
   */
  void setParameters(String parameters);

  /**
   * Set the JDBC driver fully qualified name.
   * 
   * @param driver the driver name
   */
  void setDriver(String driver);

  /**
   * Get the URL based on the set components.
   * 
   * @return the URL
   * */
  String getUrl();

  /**
   * Get the fully qualified JDBC driver name.
   * 
   * @return the driver name
   */
  String getDriver();

  /**
   * Get the string that represents the type of DBMS within Poesys/DB.
   * 
   * @return the type of DBMS
   */
  DBMS getDbms();
}
