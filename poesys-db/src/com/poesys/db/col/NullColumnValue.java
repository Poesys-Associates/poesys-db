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
   * Generated serial version UID for Serializable object
   */
  private static final long serialVersionUID = -6564706433028662068L;

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

  /*
   * (non-Javadoc)
   * 
   * @seecom.poesys.db.dto.AbstractColumnValue#vallueEquals(com.poesys.db.dto.
   * AbstractColumnValue)
   */
  @Override
  public boolean valueEquals(AbstractColumnValue value) {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dto.ColumnValue#hashCode()
   */
  @Override
  public int hashCode() {
    return 0;
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
    // Set the value to the java.sql.type type input in the constructor.
    stmt.setNull(nextIndex, jdbcType);
    return ++nextIndex;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dto.ColumnValue#hasValue()
   */
  @Override
  public boolean hasValue() {
    return false;
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
    return "null";
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.col.AbstractColumnValue#copy()
   */
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
