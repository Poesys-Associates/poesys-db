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
import java.util.UUID;

import com.poesys.db.InvalidParametersException;
import com.poesys.ms.col.ColumnValueImpl;
import com.poesys.ms.col.IColumnValue;


/**
 * A column value containing a value of type UUID, the Java version of a
 * globally unique identifier (GUID). The class stores the GUID in memory as a
 * Java UUID but translates to and from a character-string representation when
 * persisting the data to the database. The character-string representation is
 * always 36 characters long.
 * 
 * @author Robert J. Muller
 */
public class UuidColumnValue extends AbstractColumnValue {
  /**
   * serial version UID for Serializable object
   */
  private static final long serialVersionUID = 1L;

  /** The UUID value for the column */
  private UUID value = null;

  /**
   * Create a UuidColumnValue object.
   * 
   * @param name the column name
   * @param value the UUID value
   * @throws InvalidParametersException when the name or the value is null
   */
  public UuidColumnValue(String name, UUID value)
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
    if (value instanceof UuidColumnValue) {
      ret = this.value.equals(((UuidColumnValue)value).value);
    }
    return ret;
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public int setParam(PreparedStatement stmt, int nextIndex) {
    // Convert the UUID to its string representation for storage in the
    // database.
    try {
      stmt.setString(nextIndex, value.toString());
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
   * Get the UUID column value.
   * 
   * @return the column value
   */
  public UUID getValue() {
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
    return new UuidColumnValue(name, value);
  }

  @Override
  public IColumnValue<?> getMessageObject() {
    IColumnValue<?> col =
      new ColumnValueImpl<UUID>(name, IColumnValue.ColumnType.Uuid, value);
    return col;
  }
}
