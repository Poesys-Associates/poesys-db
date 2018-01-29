/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db.col.json;

import com.poesys.db.Message;
import com.poesys.db.col.IColumnValue;
import com.poesys.db.col.UuidColumnValue;

import java.util.UUID;

/**
 * Implementation of the IJsonColumnValue interface for UUID (GUID) data
 */
public class UuidJsonColumnValue extends JsonColumnValue {
  private static final String INVALID_GUID_ERROR = "com.poesys.db.dto.msg.invalid_guid_format";

  /**
   * Member value constructor for concrete subclasses to call using super()
   *
   * @param name  the column name
   * @param type  the conrete Java fully qualified class name for the concrete subclass
   * @param value the actual data value for the column as a String
   */
  public UuidJsonColumnValue(String name, String type, String value) {
    super(name, type, value);
  }

  @Override
  public IColumnValue getColumnValue() {
    UUID guid = null;
    try {
      guid = UUID.fromString(value);
    } catch (Throwable e) {
      Object[] args = {value};
      Message.throwJsonException(INVALID_GUID_ERROR, args, e);
    }
    return new UuidColumnValue(name, guid);
  }
}
