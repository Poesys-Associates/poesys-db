/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db.col.json;

import com.poesys.db.Message;
import com.poesys.db.col.IColumnValue;
import com.poesys.db.col.TimestampColumnValue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Implementation of the IJsonColumnValue interface for Timestamp data; date must be in standard
 * format yyyy-MM-dd HH:mm:ss.SSS
 */
public class TimestampJsonColumnValue extends JsonColumnValue {
  private static final String pattern = "yyyy-MM-dd HH:mm:ss.SSS";
  private static final SimpleDateFormat format = new SimpleDateFormat(pattern);
  private static final String INVALID_FORMAT_ERROR = "com.poesys.db.dto.msg.invalid_date_format";

  /**
   * Member value constructor for concrete subclasses to call using super()
   *
   * @param name  the column name
   * @param type  the conrete Java fully qualified class name for the concrete subclass
   * @param value the actual data value for the column as a String
   */
  public TimestampJsonColumnValue(String name, String type, String value) {
    super(name, type, value);
  }

  @Override
  public IColumnValue getColumnValue() {
    Date date;
    try {
      date = format.parse(value);
    } catch (ParseException e) {
      String[] args = {value};
      String message = Message.getMessage(INVALID_FORMAT_ERROR, args);
      throw new RuntimeException(message, e);
    }
    return new TimestampColumnValue(name, new java.sql.Timestamp(date.getTime()));
  }
}
