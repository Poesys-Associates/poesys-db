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
package com.poesys.db.col;


import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.poesys.db.InvalidParametersException;
import com.poesys.ms.col.ColumnValueImpl;
import com.poesys.ms.col.IColumnValue;


/**
 * A concrete ColumnValue that contains a Timestamp value (date and time).
 * 
 * @author Robert J. Muller
 */
public class TimestampColumnValue extends AbstractColumnValue {
  /**
   * Generated serial version UID for Serializable object
   */
  private static final long serialVersionUID = 258866656334124959L;

  /** The Timestamp value */
  private Timestamp value = null;

  /**
   * Create a TimestampColumnValue object.
   * 
   * @param name the column name
   * @param value the date-time value for the object
   * @throws InvalidParametersException when the name or value is null
   */
  public TimestampColumnValue(String name, Timestamp value)
      throws InvalidParametersException {
    super(name, value);
    if (value == null) {
      throw getException(null);
    } else {
      this.value = value;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @seecom.poesys.db.dto.AbstractColumnValue#valueEquals(com.poesys.db.dto.
   * AbstractColumnValue)
   */
  @Override
  public boolean valueEquals(AbstractColumnValue value) {
    boolean ret = false;
    if (value instanceof TimestampColumnValue) {
      ret = this.value.equals(((TimestampColumnValue)value).value);
    }
    return ret;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dto.ColumnValue#hashCode()
   */
  @Override
  public int hashCode() {
    return value.hashCode();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dto.ColumnValue#setParam(java.sql.PreparedStatement,
   * int)
   */
  @Override
  public int setParam(PreparedStatement stmt, int nextIndex)
      throws SQLException {
    // Use the Timestamp setter for the value.
    stmt.setTimestamp(nextIndex, value);
    return ++nextIndex;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dto.ColumnValue#hasValue()
   */
  @Override
  public boolean hasValue() {
    return value == null;
  }

  /**
   * Get the value; used by visitor for comparisons.
   * 
   * @return the value
   */
  Timestamp getValue() {
    return value;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.poesys.db.col.AbstractColumnValue#accept(com.poesys.db.col.IColumnVisitor
   * )
   */
  @Override
  protected void accept(IColumnVisitor visitor) {
    visitor.visit(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return value.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.col.AbstractColumnValue#copy()
   */
  @Override
  public AbstractColumnValue copy() {
    return new TimestampColumnValue(name, value);
  }

  @Override
  public IColumnValue<?> getMessageObject() {
    IColumnValue<?> col =
      new ColumnValueImpl<Timestamp>(name,
                                     IColumnValue.ColumnType.Timestamp,
                                     value);
    return col;
  }
}