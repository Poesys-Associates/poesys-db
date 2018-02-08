/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db.col.json;

import com.poesys.db.col.IColumnValue;
import com.poesys.db.col.StringColumnValue;

/**
 * Implementation of the IJsonColumnValue interface for Long data
 */
public class StringJsonColumnValue extends JsonColumnValue {
  /**
   * Member value constructor for concrete subclasses to call using super()
   *
   * @param name  the column name
   * @param type  the Java fully qualified class name for the concrete subclass
   * @param value the actual data value for the column as a String
   */
  public StringJsonColumnValue(String name, String type, String value) {
    super(name, type, value);
  }

  @Override
  public IColumnValue getColumnValue() {
    return new StringColumnValue(name, value);
  }
}
