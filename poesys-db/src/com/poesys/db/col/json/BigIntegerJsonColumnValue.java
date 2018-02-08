/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db.col.json;

import com.poesys.db.Message;
import com.poesys.db.col.BigIntegerColumnValue;
import com.poesys.db.col.IColumnValue;

import java.math.BigInteger;

/**
 * Implementation of the IJsonColumnValue interface for BigInteger data
 */
public class BigIntegerJsonColumnValue extends JsonColumnValue {
  private static final String INVALID_NUMBER_ERROR = "com.poesys.db.col.json.msg.invalid_number_format";

  /**
   * Member value constructor for concrete subclasses to call using super()
   *
   * @param name  the column name
   * @param type  the Java fully qualified class name for the concrete subclass
   * @param value the actual data value for the column as a String
   */
  public BigIntegerJsonColumnValue(String name, String type, String value) {
    super(name, type, value);
  }

  @Override
  public IColumnValue getColumnValue() {
    BigInteger integer = null;
    try {
      integer = new BigInteger(value);
    } catch (Throwable e) {
      Object[] args = {value};
      Message.throwJsonException(INVALID_NUMBER_ERROR, args, e);
    }
    return new BigIntegerColumnValue(name, integer);
  }
}
