/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db.col.json;

import com.poesys.db.InvalidParametersException;
import com.poesys.db.Message;

import java.sql.JDBCType;
import java.sql.Types;

/**
 * A Factory Method pattern class that generates IJsonColumnValue objects of appropriate concrete
 * types, including the Null type if the value is null and the type is a Java primitive type
 */
public class JsonColumnValueFactory {
  private static final String INVALID_TYPE_ERROR = "com.poesys.db.col.json.msg.invalid_db_type";

  public static IJsonColumnValue getJsonColumnValue(JsonColumnValue value) {
    IJsonColumnValue concreteValue = null;
    switch (value.type) {
      case "com.poesys.db.col.BigDecimalColumnValue":
        concreteValue =
          new BigDecimalJsonColumnValue(value.getName(), value.getType(), value.getValue());
        break;
      case "com.poesys.db.col.BigIntegerColumnValue":
        concreteValue =
          new BigIntegerJsonColumnValue(value.getName(), value.getType(), value.getValue());
        break;
      case "com.poesys.db.col.IntegerColumnValue":
        // primitive type int, product null column value
        if (value.getValue() == null) {
          concreteValue = new NullJsonColumnValue(value.getName(), value.getType(), Types.INTEGER);
        } else {
          concreteValue =
            new IntegerJsonColumnValue(value.getName(), value.getType(), value.getValue());
        }
        break;
      case "com.poesys.db.col.LongColumnValue":
        // primitive type long, product null column value
        if (value.getValue() == null) {
          concreteValue = new NullJsonColumnValue(value.getName(), value.getType(), Types.INTEGER);
        } else {
          concreteValue =
            new LongJsonColumnValue(value.getName(), value.getType(), value.getValue());
        }
        break;
      case "com.poesys.db.col.DateColumnValue":
        concreteValue = new DateJsonColumnValue(value.getName(), value.getType(), value.getValue());
        break;
      case "com.poesys.db.col.TimestampColumnValue":
        concreteValue =
          new TimestampJsonColumnValue(value.getName(), value.getType(), value.getValue());
        break;
      case "com.poesys.db.col.StringColumnValue":
        concreteValue =
          new StringJsonColumnValue(value.getName(), value.getType(), value.getValue());
        break;
      case "com.poesys.db.col.UuidColumnValue":
        concreteValue = new UuidJsonColumnValue(value.getName(), value.getType(), value.getValue());
        break;
      default:
        Object[] args = {value.type};
        String message = Message.getMessage(INVALID_TYPE_ERROR, args);
        throw new InvalidParametersException(message);
    }

    return concreteValue;
  }
}
