/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db.col.json;

import com.poesys.db.Message;
import com.poesys.db.col.IColumnValue;

/**
 * A base-class implementation of the JSON column value interface that abstracts anything that
 * all the implementations share, which is all the data; the concrete implementations add a
 * factory method that generates the appropriate Poesys column value object given the type.
 * The JsonColumnValueFactory method getJsonColumnValue() accepts an object of this class and
 * returns an object of the appropriate concrete implementation of IJsonColumnValue given the type.
 * The idea is to parse JSON into this class, then transform it into a concrete subclass for use
 * in a primary key object. This class has the default constructor, as GSON does not need
 * either a specific constructor or accessor methods, but it also has a member constructor to
 * enable the creation of concrete subclasses by the factory. Also, the implementations of
 * IColumnValue each contain a factory method that generates the appropriate implementation of
 * IJsonColumnValue.
 */
public class JsonColumnValue implements IJsonColumnValue {
  /** the column name */
  String name;
  /** the concrete Java fully qualified class name for the IColumnValue implementation */
  String type;
  /** the actual data value for the column in its String representation */
  String value;

  // messages
  private static final String INSTANTIATION_ERROR =
    "com.poesys.db.col.json.msg.json_column_value_base_instance";

  /**
   * Create an empty JSON column value. (Used by GSON)
   */
  public JsonColumnValue() {
  }

  /**
   * Member value constructor for concrete subclasses to call using super()
   *
   * @param name  the column name
   * @param type  the Java fully qualified class name for the concrete subclass
   * @param value the actual data value for the column as a String
   */
  public JsonColumnValue(String name, String type, String value) {
    this.name = name;
    this.type = type;
    this.value = value;
  }

  /**
   * Get the column value object from the JSON data.
   *
   * @return an IColumnValue object
   */
  @Override
  public IColumnValue getColumnValue() {
    throw new RuntimeException(Message.getMessage(INSTANTIATION_ERROR, null));
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof JsonColumnValue)) {
      return false;
    }

    JsonColumnValue that = (JsonColumnValue)o;

    if (!name.equals(that.name)) {
      return false;
    }
    if (!type.equals(that.type)) {
      return false;
    }
    return value.equals(that.value);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + type.hashCode();
    result = 31 * result + value.hashCode();
    return result;
  }
}
