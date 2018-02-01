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

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import com.poesys.db.col.json.DateJsonColumnValue;
import com.poesys.db.col.json.IJsonColumnValue;
import org.apache.log4j.Logger;

import com.poesys.db.InvalidParametersException;
import com.poesys.ms.col.ColumnValueImpl;
import com.poesys.ms.col.IColumnValue;

/**
 * A concrete ColumnValue that contains a Date value (no time).
 *
 * @author Robert J. Muller
 */
public class DateColumnValue extends AbstractColumnValue {
  /** logger for this class */
  private static final Logger logger = Logger.getLogger(DateColumnValue.class);
  /** date pattern for JSON strings */
  private static final String PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
  /** simple date format object based on date pattern for JSON strings */
  private static final SimpleDateFormat format = new SimpleDateFormat(PATTERN);

  /**
   * serial version UID for Serializable object
   */
  private static final long serialVersionUID = 1L;

  /** The Date value */
  private Date value;

  /**
   * Create a DateColumnValue object.
   *
   * @param name  the column name
   * @param value the date-only value for the object
   * @throws InvalidParametersException when the name or value is null
   */
  public DateColumnValue(String name, Date value) throws InvalidParametersException {
    super(name, value);
    if (value == null) {
      throw getException(null);
    } else {
      this.value = value;
    }
  }

  @Override
  protected boolean valueEquals(com.poesys.db.col.IColumnValue value) {
    boolean ret = false;
    if (value instanceof DateColumnValue) {
      ret = this.value.equals(((DateColumnValue)value).value);
    }
    return ret;
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public int setParam(PreparedStatement stmt, int nextIndex) {
    // Use the Date setter for the value.
    try {
      stmt.setDate(nextIndex, value);
      logger.debug(
        "Set key parameter " + nextIndex + " with column " + name + " with Date value " + value);
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
  Date getValue() {
    return value;
  }

  @Override
  public void accept(IColumnVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public IJsonColumnValue getJsonColumnValue() {
    return new DateJsonColumnValue(name, getClass().getName(), format.format(value));
  }

  @Override
  public String toString() {
    return value.toString();
  }

  @Override
  public AbstractColumnValue copy() {
    return new DateColumnValue(name, value);
  }

  @Override
  public IColumnValue<?> getMessageObject() {
    return new ColumnValueImpl<>(name, IColumnValue.ColumnType.Date, value);
  }
}
