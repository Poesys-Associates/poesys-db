/*
 * Copyright (c) 2016 Poesys Associates. All rights reserved.
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
package com.poesys.db.pool;

/**
 * An exception indicating there was a problem shutting down an object pool.
 * 
 * @author Robert J. Muller
 */
public class PoolShutdownException extends Exception {

  /** serial version UID for serializable object */
  private static final long serialVersionUID = 1L;

  private static final String MESSAGE = "Problem shutting down object pool";

  /**
   * Create a PoolShutdownException object.
   */
  public PoolShutdownException() {
    super(MESSAGE);
  }

  /**
   * Create a PoolShutdownException object.
   * 
   * @param message a message for the exception
   */
  public PoolShutdownException(String message) {
    super(message);
  }

  /**
   * Create a PoolShutdownException object.
   * 
   * @param cause the causing exception
   */
  public PoolShutdownException(Throwable cause) {
    super(cause);
  }

  /**
   * Create a PoolShutdownException object.
   * 
   * @param message a message for the exception
   * @param cause the causing exception
   */
  public PoolShutdownException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Create a PoolShutdownException object.
   * 
   * @param message a message for the exception
   * @param cause the causing exception
   * @param enableSuppression enable suppression
   * @param writableStackTrace generate a writable stack trace
   */
  public PoolShutdownException(String message,
                               Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
