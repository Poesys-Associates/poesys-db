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
 * 
 */
package com.poesys.db;


import java.util.ArrayList;
import java.util.List;


/**
 * <p>
 * A generic subsystem exception for the Poesys DB library that supports a list
 * of arguments for error message processing, allowing an error processing
 * client to construct an error message from a resource bundle by supplying any
 * number of string arguments. This exception is a subclass of the
 * RuntimeException class to permit use in implementations of library methods
 * with fixed exception signatures. You can use the Message class to get the
 * resource-bundle text for the message and to fill in parameters using Java's
 * MessageFormat class.
 * </p>
 * <p>
 * One use case for throwing this exception explicitly (as opposed to throwing
 * a more-specific subclass exception) is when you get a SQL exception and need
 * to rethrow it. Instead of throwing a new SQLException, which does not take
 * the SQLException as an argument, throw a DbErrorException that wraps the
 * thrown SQLException as its cause and add any parameters if you supply your
 * own message in place of the standard SQL message.
 * </p>
 * <pre>
 * catch (SQLException e) {
 *   throw new DbErrorException(e.getMessage(), e);
 * }
 * </pre>
 * 
 * @see Message
 * 
 * @author Robert J. Muller
 */
public class DbErrorException extends RuntimeException {
  /** The unique UID for this serializable object */
  private static final long serialVersionUID = -178428330849550512L;
  /** List of String parameters for the message */
  private List<String> parameters = new ArrayList<String>();

  /**
   * Constructor with message
   * 
   * @param arg0 the message
   */
  public DbErrorException(String arg0) {
    super(arg0);
  }

  /**
   * Constructor with message and causing exception
   * 
   * @param arg0 the message
   * @param arg1 the cause
   */
  public DbErrorException(String arg0, Throwable arg1) {
    super(arg0, arg1);
  }

  /**
   * Constructor with just the causing exception
   * 
   * @param arg0 the cause
   */
  public DbErrorException(Throwable arg0) {
    super(arg0);
  }

  /**
   * Get the parameters.
   * 
   * @return Returns the parameters.
   */
  public synchronized List<String> getParameters() {
    return parameters;
  }

  /**
   * Set the parameters.
   * 
   * @param parameters The parameters to set.
   */
  public synchronized void setParameters(List<String> parameters) {
    this.parameters = parameters;
  }
}
