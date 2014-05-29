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
 * The column name supplied by the caller is an invalid column name for this
 * primary key.
 * 
 * @author Robert J. Muller
 */
public class InvalidPrimaryKeyColumnException extends
    ConstraintViolationException {

  /**
   * Serial version UID generated for this class
   */
  private static final long serialVersionUID = 1455500067006559568L;

  /**
   * Create a InvalidPrimaryKeyColumn object.
   * 
   * @param message the error message for the key error
   */
  public InvalidPrimaryKeyColumnException(String message) {
    super(message);
  }

  /**
   * Create a InvalidPrimaryKeyColumn object.
   * 
   * @param cause the exception causing the error
   */
  public InvalidPrimaryKeyColumnException(Throwable cause) {
    super(cause);
  }

  /**
   * Create a InvalidPrimaryKeyColumn object.
   * 
   * @param message the error message for the key error
   * @param cause the exception causing the error
   */
  public InvalidPrimaryKeyColumnException(String message, Throwable cause) {
    super(message, cause);
  }

}
