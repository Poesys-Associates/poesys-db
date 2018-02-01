/*
 * Copyright (c) 2009 Poesys Associates. All rights reserved.
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
package com.poesys.db.col;


import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.poesys.db.col.json.IJsonColumnValue;
import com.poesys.db.col.json.LongJsonColumnValue;
import org.apache.log4j.Logger;

import com.poesys.db.InvalidParametersException;
import com.poesys.ms.col.ColumnValueImpl;
import com.poesys.ms.col.IColumnValue;


/**
 * A concrete ColumnValue that contains a Long value.
 * 
 * @author Robert J. Muller
 */
public class LongColumnValue extends AbstractColumnValue {
  /** logger for this class */
  private static final Logger logger = Logger.getLogger(LongColumnValue.class);

  /** serial version UID for Serializable object */
  private static final long serialVersionUID = 1L;

  /** The Long value */
  private Long value;

  /**
   * Create a LongColumnValue object.
   * 
   * @param name the column name
   * @param value the long value for the object
   * @throws InvalidParametersException when an input name or value is null
   */
  public LongColumnValue(String name, Long value)
      throws InvalidParametersException {
    super(name, value);
    if (value == null) {
      throw getException(null);
    } else {
      this.value = value;
    }
  }

  @Override
  public boolean valueEquals(com.poesys.db.col.IColumnValue value) {
    boolean ret = false;
    if (value instanceof LongColumnValue) {
      ret = this.value.equals(((LongColumnValue)value).value);
    }
    return ret;
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public int setParam(PreparedStatement stmt, int nextIndex) {
    // Use the Long setter.
    try {
      stmt.setLong(nextIndex, value);
      logger.debug("Set key parameter " + nextIndex + " with column " + name
                   + " with Long value " + value);
    } catch (SQLException e) {
      throwDbError(e);
    }
    return ++nextIndex;
  }

  @Override
  public boolean hasValue() {
    return value != null;
  }

  /**
   * Get the value; used by visitor for comparisons.
   * 
   * @return the value
   */
  Long getValue() {
    return value;
  }

  @Override
  public void accept(IColumnVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public IJsonColumnValue getJsonColumnValue() {
    return new LongJsonColumnValue(name, getClass().getName(), value.toString());
  }

  @Override
  public String toString() {
    return value.toString();
  }

  @Override
  public AbstractColumnValue copy() {
    return new LongColumnValue(name, value);
  }

  @Override
  public IColumnValue<?> getMessageObject() {
    return new ColumnValueImpl<>(name, IColumnValue.ColumnType.Long, value);
  }
}
