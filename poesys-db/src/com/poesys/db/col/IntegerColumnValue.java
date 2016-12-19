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


import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.poesys.db.InvalidParametersException;
import com.poesys.ms.col.ColumnValueImpl;
import com.poesys.ms.col.IColumnValue;


/**
 * A concrete ColumnValue that contains an Integer value.
 * 
 * @author Robert J. Muller
 */
public class IntegerColumnValue extends AbstractColumnValue {
  /**
   * serial version UID for Serializable object
   */
  private static final long serialVersionUID = 1L;
  
  /** The Integer value */
  private Integer value = null;

  /**
   * Create an IntegerColumnValue object.
   * 
   * @param name the column name
   * @param value the integer value for the object
   * @throws InvalidParametersException when an input name or value is null
   */
  public IntegerColumnValue(String name, Integer value)
      throws InvalidParametersException {
    super(name, value);
    if (value == null) {
      throw getException(null);
    } else {
      this.value = value;
    }
  }

  @Override
  public boolean valueEquals(AbstractColumnValue value) {
    boolean ret = false;
    if (value instanceof IntegerColumnValue) {
      ret = this.value.equals(((IntegerColumnValue)value).value);
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
      stmt.setInt(nextIndex, value);
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
  Integer getValue() {
    return value;
  }

  @Override
  protected void accept(IColumnVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return value.toString();
  }

  @Override
  public AbstractColumnValue copy() {
    return new IntegerColumnValue(name, value);
  }

  @Override
  public IColumnValue<?> getMessageObject() {
    IColumnValue<?> col =
      new ColumnValueImpl<Integer>(name,
                                      IColumnValue.ColumnType.Integer,
                                      value);
    return col;
  }
}
