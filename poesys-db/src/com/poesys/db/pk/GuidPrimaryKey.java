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
package com.poesys.db.pk;


import com.poesys.db.InvalidParametersException;
import com.poesys.db.col.IColumnValue;
import com.poesys.db.col.UuidColumnValue;
import com.poesys.db.pk.json.JsonPrimaryKey;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Implements the IPrimaryKey interface for a key constructed from a single GUID
 * value as a string. The structure and semantics of the GUID key are the same
 * as the sequence primary key except that the internal data type is a UUID, not
 * a BigInteger, and the GUID is generated by this class, not by the database.
 * The GUID representation in the database is a 36-character string.
 * 
 * @author Robert J. Muller
 */
public class GuidPrimaryKey extends AbstractSingleValuedPrimaryKey {
  /**
   * Serial version UID for Serializable class
   */
  private static final long serialVersionUID = 7931389459540514239L;

  /**
   * Create a randomized GUID primary key value.
   * 
   * @param name the name of the primary key column
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   * @throws InvalidParametersException when the column name is null or UUID
   *           generation fails
   */
  public GuidPrimaryKey(String name, String className)
      throws InvalidParametersException {
    // Call the default constructor in the superclass with no list.
    super(className);
    // Create a new list and populate it with a random UUID.
    list = new ArrayList<>();
    list.add(new UuidColumnValue(name, UUID.randomUUID()));
  }

  /**
   * Create a GUID primary key value with a GUID value. Use this constructor to
   * create an object from a String GUID retrieved from the database.
   * 
   * @param name the name of the primary key column
   * @param value the string representation of the GUID value
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   * @throws InvalidParametersException when the column name is null or the UUID
   *           conversion fails
   */
  public GuidPrimaryKey(String name, UUID value, String className)
      throws InvalidParametersException {
    // Call the default constructor in the superclass with no list.
    super(className);
    // Check the input value for being null.
    if (value == null) {
      throw new InvalidParametersException("Null UUID value");
    }
    // Create a new list and populate it with a UUID created from its string
    // representation.
    this.list = new ArrayList<>();
    list.add(new UuidColumnValue(name, value));
  }

  /**
   * Create a GuidPrimaryKey object using a list of column values; useful for
   * creating a key from another GuidPrimaryKey's list.
   * 
   * @param list the list of column values
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   */
  protected GuidPrimaryKey(List<IColumnValue> list, String className) {
    super(list, className);
  }

  /**
   * Create a GuidPrimaryKey object from a messaging key object.
   * 
   * @param key the messaging key
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   */
  public GuidPrimaryKey(com.poesys.ms.pk.GuidPrimaryKey key, String className) {
    // Call the default constructor in the superclass with no list.
    super(className);
    // Create a new list and populate it from the DTO.
    this.list = new ArrayList<>();
    list.add(new UuidColumnValue(key.getName(), key.getUuid()));
  }

  @Override
  public String getValueList() {
    IColumnValue col = list.get(0);
    StringBuilder str = new StringBuilder();
    str.append(" (");
    str.append(col.getName());
    str.append("=");
    str.append(col.toString());
    str.append(")");

    return str.toString();
  }

  /**
   * Get a serializable data transfer object for messaging.
   * 
   * @return the messaging DTO
   */
  public com.poesys.ms.pk.IPrimaryKey getMessageObject() {
    UuidColumnValue col = (UuidColumnValue)list.get(0);
    return new com.poesys.ms.pk.GuidPrimaryKey(col.getName(),
                                               col.getValue(),
                                               className);
  }

  @Override
  public JsonPrimaryKey getJsonPrimaryKey() {
    return new JsonPrimaryKey(GuidPrimaryKey.class.getName(), className, getJsonColumnValueList());
  }

  /**
   * Get the GUID as a string.
   * 
   * @return the GUID as a string value
   */
  public String getValue() {
    IColumnValue col = list.get(0);

    return col.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.pk.IPrimaryKey#copy()
   */
  public IPrimaryKey copy() {
    return new GuidPrimaryKey(super.copyList(), className);
  }
}
