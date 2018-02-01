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
import com.poesys.db.col.json.BigIntegerJsonColumnValue;
import com.poesys.db.col.json.IJsonColumnValue;
import com.poesys.ms.col.ColumnValueImpl;
import com.poesys.ms.col.IColumnValue;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A concrete ColumnValue that contains a BigInteger value.
 *
 * @author Robert J. Muller
 */
public class BigIntegerColumnValue extends AbstractColumnValue {
  /** logger for this class */
  private static final Logger logger = Logger.getLogger(BigIntegerColumnValue.class);

  /**
   * serial version UID for Serializable object
   */
  private static final long serialVersionUID = 1L;

  /** The BigInteger value */
  private BigInteger value = null;

  /**
   * Create a BigIntegerColumnValue object.
   *
   * @param name  the column name
   * @param value the big-integer value for the object
   * @throws InvalidParametersException when an input name or value is null
   */
  public BigIntegerColumnValue(String name, BigInteger value) throws InvalidParametersException {
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
    if (value instanceof BigIntegerColumnValue) {
      ret = this.value.equals(((BigIntegerColumnValue)value).value);
    }
    return ret;
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public int setParam(PreparedStatement stmt, int nextIndex) {
    // Use the BigDecimal setter for the BigInteger by creating a BigDecimal
    // with the integer value.
    try {
      stmt.setBigDecimal(nextIndex, new BigDecimal(value));
      logger.debug(
        "Set key parameter " + nextIndex + " with column " + name + " with BigInteger value " +
        value);
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
  BigInteger getValue() {
    return value;
  }

  @Override
  public void accept(IColumnVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public IJsonColumnValue getJsonColumnValue() {
    return new BigIntegerJsonColumnValue(name, getClass().getName(), value.toString());
  }

  @Override
  public String toString() {
    return value.toString();
  }

  @Override
  public AbstractColumnValue copy() {
    return new BigIntegerColumnValue(name, value);
  }

  @Override
  public IColumnValue<?> getMessageObject() {
    return new ColumnValueImpl<>(name, IColumnValue.ColumnType.BigInteger, value);
  }
}
