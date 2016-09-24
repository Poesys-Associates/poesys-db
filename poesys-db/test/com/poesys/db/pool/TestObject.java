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
