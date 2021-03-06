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

import com.poesys.db.InvalidParametersException;
import com.poesys.db.col.json.IJsonColumnValue;
import com.poesys.db.col.json.TimestampJsonColumnValue;
import com.poesys.ms.col.ColumnValueImpl;
import com.poesys.ms.col.IColumnValue;
import org.apache.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * A concrete ColumnValue that contains a Timestamp value (date and time).
 *
 * @author Robert J. Muller
 */
public class TimestampColumnValue extends AbstractColumnValue {
  /** logger for this class */
  private static final Logger logger = Logger.getLogger(TimestampColumnValue.class);

  /** date pattern for JSON strings */
  private static final String PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
  /** simple date format object based on date pattern for JSON strings */
  private static final SimpleDateFormat format = new SimpleDateFormat(PATTERN);

  /**
   * serial version UID for Serializable object
   */
  private static final long serialVersionUID = 1L;

  /** The Timestamp value */
  private Timestamp value;

  /**
   * Create a TimestampColumnValue object.
   *
   * @param name  the column name
   * @param value the date-time value for the object
   * @throws InvalidParametersException when the name or value is null
   */
  public TimestampColumnValue(String name, Timestamp value) throws InvalidParametersException {
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
   * @see com.poesys.db.dto.AbstractColumnValue#valueEquals(com.poesys.db.dto.
   * AbstractColumnValue)
   */
  @Override
  public boolean valueEquals(com.poesys.db.col.IColumnValue value) {
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
  public int setParam(PreparedStatement stmt, int nextIndex) {
    // Use the Timestamp setter for the value.
    try {
      stmt.setTimestamp(nextIndex, value);
      logger.debug(
        "Set key parameter " + nextIndex + " with column " + name + " with Timestamp value " +
        value);
    } catch (SQLException e) {
      throwDbError(e);
    }
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
  public void accept(IColumnVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public IJsonColumnValue getJsonColumnValue() {
    return new TimestampJsonColumnValue(name, getClass().getName(), format.format(value));
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
    return new ColumnValueImpl<>(name, IColumnValue.ColumnType.Timestamp, value);
  }
}
