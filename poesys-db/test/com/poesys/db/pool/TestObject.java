/**
 * Copyright Phoenix Bioinformatics Corporation 2015. All rights reserved.
 */
package com.poesys.db.pool;

/**
 * A simple test object to be pooled for unit tests, supports basic things the
 * pool does to the object.
 * 
 * @author Robert J. Muller
 */
public class TestObject {
  /** a data member */
  String value = null;
  boolean closeException = false;

  /**
   * Create a TestObject object.
   * 
   * @param value the data for the object
   */
  public TestObject(String value) {
    this.value = value;
  }

  /**
   * Display a string representation of the object.
   */
  public String toString() {
    return "[TestObject [value = " + value + "]]";
  }

  /**
   * Shut down the object gracefully.
   *
   */
  public void close() {
    if (closeException) {
      throw new RuntimeException("Exception closing test object");
    }
  }

  /**
   * Set the object to throw a runtime exception on closing.
   *
   */
  public void setCloseException(boolean exception) {
    closeException = exception;
  }
}
