/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db.col.json;

import com.poesys.db.col.IColumnValue;
import com.poesys.db.col.NullColumnValue;

/**
 * Implementation of the IJsonColumnValue interface for null data, which requires specification
 * of the desired JDBC type to make the appropriate JDBC null-setting call
 */
public class NullJsonColumnValue extends JsonColumnValue {
  private final int jdbcType;

  /**
   * Member value constructor for concrete subclasses to call using super()
   *
   * @param name     the column name
   * @param type     the conrete Java fully qualified class name for the concrete subclass
   * @param jdbcType the java.sql.type value for the object
   */
  public NullJsonColumnValue(String name, String type, int jdbcType) {
    super(name, type, null);
    this.jdbcType = jdbcType;
  }

 @Override
  public IColumnValue getColumnValue() {
    return new NullColumnValue(name, jdbcType);
  }
}
