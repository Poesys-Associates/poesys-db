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
package com.poesys.db.col;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.poesys.db.InvalidParametersException;
import com.poesys.ms.col.ColumnValueImpl;
import com.poesys.ms.col.IColumnValue;


/**
 * A concrete ColumnValue that contains a BigInteger value.
 * 
 * @author Robert J. Muller
 */
public class BigIntegerColumnValue extends AbstractColumnValue {
  /**
   * Generated serial version UID for Serializable object
   */
  private static final long serialVersionUID = 4672732512858486239L;
  
  /** The BigInteger value */
  private BigInteger value = null;

  /**
   * Create a BigIntegerColumnValue object.
   * 
   * @param name the column name
   * @param value the big-integer value for the object
   * @throws InvalidParametersException when an input name or value is null
   */
  public BigIntegerColumnValue(String name, BigInteger value)
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
   * @see com.poesys.db.dto.AbstractColumnValue#vallueEquals(com.poesys.db.dto.AbstractColumnValue)
   */
  @Override
  public boolean valueEquals(AbstractColumnValue value) {
    boolean ret = false;
    if (value instanceof BigIntegerColumnValue) {
      ret = this.value.equals(((BigIntegerColumnValue)value).value);
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
   *      int)
   */
  @Override
  public int setParam(PreparedStatement stmt, int nextIndex)
      throws SQLException {
    // Use the BigDecimal setter for the BigInteger by creating a BigDecimal
    // with the integer value.
    stmt.setBigDecimal(nextIndex, new BigDecimal(value));
    return ++nextIndex;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dto.ColumnValue#hasValue()
   */
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

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.col.AbstractColumnValue#accept(com.poesys.db.col.IColumnVisitor)
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
    return new BigIntegerColumnValue(name, value);
  }

  @Override
  public IColumnValue<?> getMessageObject() {
    IColumnValue<?> col =
      new ColumnValueImpl<BigInteger>(name,
                                      IColumnValue.ColumnType.BigInteger,
                                      value);
    return col;
  }
}
