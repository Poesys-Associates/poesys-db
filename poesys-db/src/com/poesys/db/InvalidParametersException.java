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




/**
 * An exception that indicates that a parameterized method does not have a set
 * of parameters that fulfills preconditions for the method.
 * 
 * @author Robert J. Muller
 */
public class InvalidParametersException extends ConstraintViolationException {
  /** The unique UID for this serializable object */
  private static final long serialVersionUID = 3475874859567465849L;

  /**
   * Constructor with message
   * 
   * @param arg0 the message
   */
  public InvalidParametersException(String arg0) {
    super(arg0);
  }

  /**
   * Constructor with message and causing exception
   * 
   * @param arg0 the message
   * @param arg1 the cause
   */
  public InvalidParametersException(String arg0, Throwable arg1) {
    super(arg0, arg1);
  }

  /**
   * Constructor with just the causing exception
   * 
   * @param arg0 the cause
   */
  public InvalidParametersException(Throwable arg0) {
    super(arg0);
  }
}
