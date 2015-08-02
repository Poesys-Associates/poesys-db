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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;


/**
 * Interface for connection factory classes for database management systems;
 * each class that supports this interface provides the appropriate setup and
 * API use to get a single connection. The interface is an instance of the
 * FactoryMethod design pattern.
 * 
 * @author Robert J. Muller
 */
public interface IConnectionFactory {

  /**
   * Enum type for the valid kinds of DBMS products the factory supports.
   */
  public enum DBMS {
    /** Oracle DBMS */
    ORACLE("ORACLE"),
    /** JNDI Oracle DBMS */
    JNDI_ORACLE("JNDI_ORACLE"),
    /** MySQL DBMS */
    MYSQL("MYSQL"),
    /** JNDI MySQL DBMS */
    JNDI_MYSQL("JNDI_MYSQL"),
    /** Sybase DBMS */
    SYBASE("SYBASE"),
    /** JNDI Sybase DBMS */
    JNDI_SYBASE("JNDI_SYBASE"),
    /** JNDI DataSource (default Oracle */
    JNDI("JNDI");

    /** The internal string representation */
    private String string;

    /** Map of internal string representations for lookup */
    private static final Map<String, DBMS> values = new HashMap<String, DBMS>();

    // Initialize the map statically
    static {
      for (DBMS d : EnumSet.allOf(DBMS.class)) {
        values.put(d.toString(), d);
      }
    }

    /**
     * Create a DBMS object.
     * 
     * @param string the string with which to construct the internal
     *          representation
     */
    private DBMS(String string) {
      this.string = string;
    }

    public String toString() {
      return string;
    }

    /**
     * Look up the DBMS based on its string representation.
     * 
     * @param value the string to look up
     * @return the DBMS corresponding to the string or null if there is no such
     *         DBMS
     */
    public static DBMS stringValue(String value) {
      return values.get(value);
    }
  };

  /**
   * Get a SQL connection from a DBMS to the default user on the default host
   * using a supplied password. This permits storing the password separately
   * from the rest of the DBMS parameters, such as in an encrypted wallet, or
   * even prompting the user for it.
   * 
   * @param password The user-entered password for the user
   * @return the JDBC connection to the user
   * @throws SQLException when there is a problem getting a connection
   */
  public abstract Connection getConnection(String password) throws SQLException;

  /**
   * Get a SQL connection from a DBMS to the default user on the default host
   * using the default password.
   * 
   * @return the JDBC connection to the user
   * @throws SQLException when there is a problem getting a connection
   */
  public abstract Connection getConnection() throws SQLException;

  /**
   * Set the current user to which to connect
   * 
   * @param user the user to which to connect
   */
  public abstract void setUser(String user);

  /**
   * Set the current host to which to connect
   * 
   * @param host the current host to which to connect
   */
  public abstract void setHost(String host);

  /**
   * Set the current port on the host to which to connect
   * 
   * @param port the port to which to connect on the host
   */
  public abstract void setPort(Integer port);

  /**
   * Set the JDBC driver password for the connection
   * 
   * @param password the database password to use for the connection
   */
  public abstract void setPassword(String password);

  /**
   * Set the JDBC database (or the JNDI name) for the connection
   * 
   * @param database the database name for the connection
   */
  public abstract void setDatabase(String database);

  /**
   * Get the type of JDBC database this factory represents.
   * 
   * @return a DBMS type
   */
  public abstract DBMS getDbms();

  /**
   * Flush any cached data to allow complete reinitialization of the factory.
   * 
   * @throws ConnectionException when there is a problem initializing or getting
   *           connections
   */
  public abstract void flush() throws ConnectionException;

  /**
   * Close the data source.
   * @throws ConnectionException when there is a problem closing the connection
   */
  void close() throws ConnectionException;
}