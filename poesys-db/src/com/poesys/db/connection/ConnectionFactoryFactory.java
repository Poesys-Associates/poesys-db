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


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import com.poesys.db.InvalidParametersException;


/**
 * Creates and caches singleton database connection factories for subsystems.
 * Each factory becomes a singleton in the cache, mapped by subsystem name. The
 * class provides methods to create subsystem factories, to get those factories,
 * and to restart the factories (clearing any caches or connections in the
 * subsystem factory).
 * 
 * @see IConnectionFactory
 * 
 * @author Robert J. Muller
 */
public class ConnectionFactoryFactory {
  /** Map of IConnectionFactory objects keyed on subsystem name (String) */
  private static Map<String, IConnectionFactory> cache = null;

  /** Name of the database properties resource bundle */
  protected static final String BUNDLE = "com.poesys.db.database";

  /** The resource bundle containing the subsystem properties. */
  protected static final ResourceBundle properties =
    ResourceBundle.getBundle(BUNDLE);

  /** The default subsystem resource prefix */
  protected static final String SUBSYSTEM = "com.poesys.db.default.subsystem";

  /** String representing the name of the default subsystem */
  private static final String DEFAULT = properties.getString(SUBSYSTEM);

  /** The property for the user name */
  private static final String USER = ".user";

  /** The property for the database name */
  private static final String DATABASE = ".database";

  /** The property for database pooling */
  private static final String POOLED = ".pooled";

  /** The property for database connection pool maximum size */
  private static final String MAXSIZE = ".max_pool_size";

  /** The property for the host name */
  private static final String HOST = ".host";

  /** The property for the port */
  private static final String PORT = ".port";

  /** The property for the JNDI name */
  private static final String NAME = ".name";
  /** The property for the DBMS type */
  private static final String DBMS = ".dbms";
  /** The property for the database user's password */
  private static final String PASSWORD = ".password";

  /** Error message on seeing an unsupported database management system */
  protected static final String DBMS_NOT_SUPPORTED =
    "com.poesys.db.connection.msg.dbms_not_supported";

  /** Error message on getting a null or empty string for subsystem */
  protected static final String NO_SUBSYSTEM =
    "com.poesys.db.connection.msg.no_subsystem";

  /** Error message on requesting an invalid subsystem */
  protected static final String INVALID_SUBSYSTEM =
    "com.poesys.db.connection.msg.invalid_subsystem";

  /** Error message for non-integer port number */
  protected static final String NON_INTEGER_PORT =
    "com.poesys.db.connection.msg.non_integer_port";

  /**
   * Initialize a specific subsystem if no such subsystem exists, creating the
   * factory and setting the mapped instance for the subsystem name.
   * 
   * @param subsystem the subsystem to create, if necessary
   * @param dbms the type of DBMS the subsystem uses
   * @throws IOException if there is a problem initializing the subsystem
   * @throws InvalidParametersException if the subsystem string is null or empty
   */
  private static void initializeSubsystem(String subsystem,
                                          IConnectionFactory.DBMS dbms)
      throws IOException, InvalidParametersException {
    IConnectionFactory factory = null;

    // Make sure the cache is initialized.
    initializeCache();

    // If the subsystem is null, use the DEFAULT subsystem from the properties
    // file.
    String key = subsystem == null ? DEFAULT : subsystem;

    // Check for null subsystem.
    if (subsystem == null || subsystem.equals("")) {
      throw new InvalidParametersException(NO_SUBSYSTEM);
    }

    // If the DBMS is null, get it from the properties file.
    if (dbms == null) {
      dbms = getDbms(subsystem);
    }

    // Get the connection pool flag and max size from the properties file.
    Boolean pooled = Boolean.FALSE;
    Integer maxSize = 1000;

    try {
      pooled =
        properties.getString(subsystem + POOLED) == null ? Boolean.FALSE
            : new Boolean(properties.getString(subsystem + POOLED));

      // Get the max pool size from the properties file.
      maxSize =
        (properties.getString(subsystem + MAXSIZE) == null) ? 1000
            : new Integer(properties.getString(subsystem + MAXSIZE));
    } catch (Exception e) {
      // ignore, just use defaults
    }

    if (!cache.containsKey(key)) {
      switch (dbms) {
      case ORACLE:
        if (pooled) {
          IJdbcDriver driver = new OracleDriver(null, null);
          factory =
            new PooledConnectionFactory(5,
                                        5,
                                        maxSize,
                                        driver,
                                        null,
                                        null,
                                        "SELECT 1 FROM DUAL");

        } else {
          factory = new OracleConnectionFactory();
        }
        break;
      case MYSQL:
        if (pooled) {
          IJdbcDriver driver = new MySqlDriver(null, null);
          factory =
            new PooledConnectionFactory(5,
                                        5,
                                        maxSize,
                                        driver,
                                        null,
                                        null,
                                        "SELECT 1");

        } else {
          factory = new MySqlConnectionFactory();
        }
        break;
      case SYBASE:
        if (pooled) {
          IJdbcDriver driver = new JtdsDriver(null, null);
          factory =
            new PooledConnectionFactory(5,
                                        5,
                                        maxSize,
                                        driver,
                                        null,
                                        null,
                                        "SELECT 1");

        } else {
          factory = new SybaseConnectionFactory();
        }
        break;
      case JNDI:
        factory = new JndiConnectionFactory();
        break;
      case JNDI_MYSQL:
        factory = new JndiMysqlConnectionFactory();
        break;
      case JNDI_ORACLE:
        factory = new JndiOracleConnectionFactory();
        break;
      case JNDI_SYBASE:
        factory = new JndiSybaseConnectionFactory();
        break;
      default:
        InvalidParametersException e =
          new InvalidParametersException(DBMS_NOT_SUPPORTED);
        List<String> list = e.getParameters();
        list.add(getDbmsString(dbms));
        throw e;
      }

      // Cache the subsystem factory and initialize the subsystem.
      cache.put(key, factory);
      initialize(key, dbms);
    }
  }

  /**
   * Get the DBMS for a specified subsystem from the subsystem database
   * properties. If the specified subsystem is null, the method returns the DBMS
   * for the default subsystem in the database properties file. Note that the
   * initializeSubsystem() method accesses this DBMS value only if it gets a
   * null passed in for DBMS. You can use this DBMS value to test for the
   * underlying DBMS for a subsystem even when the application is using
   * DBMS.JNDI or something similar, obscuring the actual DBMS with a software
   * layer that configures the underlying DBMS.
   * 
   * @param subsystem the subsystem for which to get the DBMS; may be null
   * @return the DBMS from the subsystem properties
   */
  public static IConnectionFactory.DBMS getDbms(String subsystem) {
    // If the subsystem is null, use the DEFAULT subsystem from the properties
    // file.
    String key = subsystem == null ? DEFAULT : subsystem;
    String dbms = properties.getString(key + DBMS).toUpperCase();
    return IConnectionFactory.DBMS.stringValue(dbms);
  }

  /**
   * Initialize the internal cache of subsystem connection factories.
   */
  private static void initializeCache() {
    // If there is no factory map, create one.
    if (cache == null) {
      // Initialize the map and put the default instance into it keyed on the
      // default subsystem name.
      cache = new TreeMap<String, IConnectionFactory>();
    }
  }

  /**
   * Get the singleton instance of the default connection factory. The factory
   * must produce connections for the specified DBMS and should implement a
   * connection pool if specified.
   * 
   * @param dbms the kind of DBMS
   * 
   * @return the default singleton connection factory instance
   * @throws IOException if there is a problem initializing the subsystem
   * @throws InvalidParametersException if the subsystem string is null or empty
   */
  public static IConnectionFactory getInstance(IConnectionFactory.DBMS dbms)
      throws IOException, InvalidParametersException {
    initializeCache();
    initializeSubsystem(null, dbms);

    return cache.get(DEFAULT);
  }

  /**
   * Get the singleton instance of the connection factory for a specific
   * subsystem given the DBMS.
   * 
   * @param subsystem the name of the subsystem factory to get, such as
   *          "com.poesys.test.mysql.writer"
   * @param dbms the optional type of DBMS the subsystem uses
   * @return a connection factory
   * @throws IOException when there is a problem reading the resource bundle
   * @throws InvalidParametersException when the subsystem string is null or
   *           empty
   */
  public static IConnectionFactory getInstance(String subsystem,
                                               IConnectionFactory.DBMS dbms)
      throws IOException, InvalidParametersException {
    IConnectionFactory factory = null;

    // Make sure the subsystem is initialized.
    initializeSubsystem(subsystem, dbms);

    // Get the requested factory.
    String key = subsystem == null ? DEFAULT : subsystem;
    if (cache != null) {
      factory = cache.get(key);
    } else {
      // Shouldn't be possible, but You Never Know.
      InvalidParametersException e =
        new InvalidParametersException(INVALID_SUBSYSTEM);
      List<String> list = e.getParameters();
      list.add(key);
      throw e;
    }

    return factory;
  }

  /**
   * Get the singleton instance of the connectionfactory for a specific
   * subsystem using the default DBMS defined in the database.properties file.
   * 
   * @param subsystem the name of the subsystem factory to get, such as
   *          "com.poesys.test.mysql.writer"
   * @return a connection factory
   * @throws IOException when there is a problem reading the resource bundle
   * @throws InvalidParametersException when the subsystem string is null or
   *           empty
   */
  public static IConnectionFactory getInstance(String subsystem)
      throws IOException, InvalidParametersException {
    String dbmsString = properties.getString(subsystem + DBMS);
    IConnectionFactory.DBMS dbms = IConnectionFactory.DBMS.valueOf(dbmsString);
    return getInstance(subsystem, dbms);
  }

  /**
   * Restart the singleton instance of the connection factory. This method
   * resets the connection parameters to whatever they are in their property
   * file. The method takes a string that is the subsystem prefix for the
   * properties in the property file. If no factory instance exists, this method
   * creates one. The method will clear any existing cache of the data source
   * and connection pool.
   * 
   * @param subsystem the property string representing the subsystem
   * @param dbms the type of DBMS to restart
   * @throws InvalidParametersException when the subsystem string is null or
   *           empty
   * @throws IOException when there is a problem reading the resource bundle
   * @throws ConnectionException when there is a problem getting a connection
   */
  public static void restart(String subsystem, IConnectionFactory.DBMS dbms)
      throws IOException, InvalidParametersException, ConnectionException {
    // Make sure the default subsystem and the map are initialized and there is
    // a subsystem factory initialized.
    IConnectionFactory factory = getInstance(subsystem, dbms);

    // Clear the data source cache, if any, including any connection pool.
    factory.flush();

    // Reset the connection parameters from the resource bundle.
    initialize(subsystem, dbms);
  }

  /**
   * Get the properties required to initialize the instance from the
   * database.properties file and initialize the instance. If for some reason
   * the subsystem doesn't exist in the map, the method will create an instance.
   * 
   * @param subsystem the property string representing the subsystem
   * @param dbms the type of DBMS the subsystem uses
   * @throws InvalidParametersException when the subsystem string is null or
   *           empty or the port is not an integer number
   * @throws IOException when there is a problem reading the resource bundle
   */
  protected static void initialize(String subsystem,
                                   IConnectionFactory.DBMS dbms)
      throws IOException, InvalidParametersException {
    IConnectionFactory factory = getInstance(subsystem, dbms);
    Integer port = null;

    if (factory != null && properties != null) {
      if (dbms == IConnectionFactory.DBMS.JNDI
          || dbms == IConnectionFactory.DBMS.JNDI_MYSQL
          || dbms == IConnectionFactory.DBMS.JNDI_ORACLE
          || dbms == IConnectionFactory.DBMS.JNDI_SYBASE) {
        // Use "database" for the JNDI resource name
        factory.setDatabase(properties.getString(subsystem + NAME));
      } else {
        factory.setUser(properties.getString(subsystem + USER));
        factory.setDatabase(properties.getString(subsystem + DATABASE));
        factory.setHost(properties.getString(subsystem + HOST));
        factory.setPassword(properties.getString(subsystem + PASSWORD));
        try {
          port = new Integer(properties.getString(subsystem + PORT));
          factory.setPort(port);
        } catch (NumberFormatException e) {
          InvalidParametersException e2 =
            new InvalidParametersException(NON_INTEGER_PORT, e);
          List<String> list = e2.getParameters();
          list.add(port.toString());
          throw e2;
        }
      }
    }
  }

  /**
   * Translates the DBMS enum into a string for messages.
   * 
   * @param dbms the type of DBMS
   * @return a String representing the DBMS type
   */
  private static String getDbmsString(IConnectionFactory.DBMS dbms) {
    String returnString = null;
    switch (dbms) {
    case ORACLE:
      returnString = "Oracle";
      break;
    case MYSQL:
      returnString = "MySQL";
      break;
    case SYBASE:
      returnString = "Sybase";
      break;
    case JNDI:
      returnString = "JNDI";
      break;
    default:
      returnString = "unknown DBMS type" + dbms;
    }
    return returnString;
  }
}
