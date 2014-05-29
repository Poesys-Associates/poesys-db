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
package com.poesys.db.connection;


import java.util.ArrayList;
import java.util.List;


/**
 * An exception that indicates that there is a problem connecting to the data
 * source. The exception contains a list of the parameters for the error
 * message; the final display of the message should include generating the
 * string with the parameters. You can get the list of parameter strings and
 * determine how many parameters there are by the length of the list. You can
 * use the Message class to get the resource-bundle text for the message and
 * to fill in parameters using Java's MessageFormat class.
 * 
 * @see com.poesys.db.Message
 * 
 * @author Robert J. Muller
 */
public class ConnectionException extends Throwable {
  /** The unique UID for this serializable object */
  private static final long serialVersionUID = 3475874859567465849L;

  /** List of String parameters for the message */
  private List<String> parameters = new ArrayList<String>(1);

  /**
   * Constructor with message
   * 
   * @param arg0 the message
   */
  public ConnectionException(String arg0) {
    super(arg0);
  }

  /**
   * Constructor with message and causing exception
   * 
   * @param arg0 the message
   * @param arg1 the cause
   */
  public ConnectionException(String arg0, Throwable arg1) {
    super(arg0, arg1);
  }

  /**
   * Constructor with just the causing exception
   * 
   * @param arg0 the cause
   */
  public ConnectionException(Throwable arg0) {
    super(arg0);
  }

  /**
   * Get the parameters.
   * 
   * @return Returns the parameters.
   */
  public List<String> getParameters() {
    return parameters;
  }

  /**
   * Set the parameters.
   * 
   * @param parameters The parameters to set.
   */
  public void setParameters(List<String> parameters) {
    this.parameters = parameters;
  }
}
