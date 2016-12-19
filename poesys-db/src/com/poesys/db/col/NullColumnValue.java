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

import com.poesys.db.InvalidParametersException;
import com.poesys.ms.col.ColumnValueImpl;
import com.poesys.ms.col.IColumnValue;


/**
 * A concrete ColumnValue that contains a BigInteger value.
 * 
 * @author Robert J. Muller
 */
public class NullColumnValue extends AbstractColumnValue {
  /**
   * serial version UID for Serializable object
   */
  private static final long serialVersionUID = 1L;

  /** The JDBC java.sql.type type for the column */
  int jdbcType;

  /**
   * Create a NullColumnValue object.
   * 
   * @param name the column name
   * @param jdbcType the java.sql.type value corresponding to the actual data
   *          type
   * @throws InvalidParametersException when an input name or value is null
   */
  public NullColumnValue(String name, int jdbcType)
      throws InvalidParametersException {
    // Pass string to avoid "null value" exception
    super(name, "null");
    this.jdbcType = jdbcType;
  }

  @Override
  public boolean valueEquals(AbstractColumnValue value) {
    return false;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public int setParam(PreparedStatement stmt, int nextIndex) {
    // Set the value to the java.sql.type type input in the constructor.
    try {
      stmt.setNull(nextIndex, jdbcType);
    } catch (SQLException e) {
      throwDbError(e);
    }
    return ++nextIndex;
  }

  @Override
  public boolean hasValue() {
    return false;
  }

  @Override
  protected void accept(IColumnVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return "null";
  }

  @Override
  public AbstractColumnValue copy() {
    return new NullColumnValue(name, jdbcType);
  }

  @Override
  public IColumnValue<?> getMessageObject() {
    IColumnValue<?> col =
      new ColumnValueImpl<Long>(name, IColumnValue.ColumnType.Null, null);
    return col;
  }
}
