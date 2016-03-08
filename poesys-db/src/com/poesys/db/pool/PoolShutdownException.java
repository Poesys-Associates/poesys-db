/**
 * Copyright Phoenix Bioinformatics Corporation 2015. All rights reserved.
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
