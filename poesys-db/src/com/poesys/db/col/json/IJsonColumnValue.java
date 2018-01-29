package com.poesys.db.col.json;/* Copyright (c) 2018 Poesys Associates. All rights reserved. */

import com.poesys.db.col.IColumnValue;

/**
 * An interface for implementations that represent the JSON data for column values suitable for
 * serialization and deserialization using GSON; requires data member getters to enable the JSON
 * column value factory to generate specific classes
 */
public interface IJsonColumnValue {
  /**
   * Get the column value object from the JSON data.
   *
   * @return an IColumnValue object
   */
  IColumnValue getColumnValue();

  /**
   * Get the column name.
   *
   * @return a column name
   */
  String getName();

  /** Get the fully qualified Java type of the concrete subclass.
   *
   * @return a fully qualified Java type
   */
  String getType();

  /** Get the JSON string value of the column.
   *
   * @return the column value
   */
  String getValue();
}
