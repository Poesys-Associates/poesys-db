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


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.poesys.db.DuplicateKeyNameException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.col.AbstractColumnValue;
import com.poesys.ms.col.IColumnValue;


/**
 * <p>
 * Implements the IPrimaryKey interface with a multi-valued column primary key
 * comprising any number of arbitrary columns.
 * </p>
 * <p>
 * The natural key is a series of one or more attributes that comprise a primary
 * key for a table. The natural key can have any number of attributes of any
 * database data type. Note that most DBMS products have internal limitations on
 * such keys such as a total data length for the combined key data.
 * </p>
 * <p>
 * A natural key is usually a unique column or a set of such columns that
 * contain usable data that identifies the object, such as a social security
 * number or a combination of qualities (chromosome, locus type, and sequence
 * start, for example).
 * </p>
 * <p>
 * You can construct a natural key by subclassing this class or by using the
 * add() method to add a value to the multi-valued key by name.
 * </p>
 * 
 * @author Robert J. Muller
 */
public class NaturalPrimaryKey extends AbstractMultiValuedPrimaryKey {
  /** Serial version UID for Serializable class */
  private static final long serialVersionUID = 1L;
  /** Duplicate name in key exception */
  private static final String DUP_NAME = "com.poesys.db.pk.msg.dup_col_name";

  /**
   * Create a NaturalPrimaryKey object with a list of the key column values.
   * 
   * @param list the list of column values
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   * 
   * @throws DuplicateKeyNameException when two or more of the columns in the
   *           list have the same name
   * @throws InvalidParametersException when the list has no columns in it
   */
  public NaturalPrimaryKey(List<AbstractColumnValue> list, String className)
      throws DuplicateKeyNameException, InvalidParametersException {
    super(list, className);
  }

  /**
   * Create a NaturalPrimaryKey object based on a Poesys/MS message primary key.
   * 
   * @param messageKey the message primary key
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   * @throws DuplicateKeyNameException when the list contains duplicate column
   *           names
   * @throws InvalidParametersException when there are no keys in the list.
   */
  public NaturalPrimaryKey(com.poesys.ms.pk.NaturalPrimaryKey messageKey,
                           String className) throws DuplicateKeyNameException,
      InvalidParametersException {
    // Make the initial list null, then set it after extracting cols.
    super(className);
    List<AbstractColumnValue> cols =
      new ArrayList<AbstractColumnValue>(messageKey.getColumnValues().size());
    for (IColumnValue<?> col : messageKey.getColumnValues()) {
      cols.add(MessageKeyFactory.getColumnValue(col));
    }
    setList(cols);
  }

  @Override
  public IPrimaryKey copy() {
    return new NaturalPrimaryKey(super.copyList(), className);
  }

  @Override
  public com.poesys.ms.pk.IPrimaryKey getMessageObject() {
    // Extract columns from list and create DTO.
    List<IColumnValue<?>> msgList = new ArrayList<IColumnValue<?>>();
    for (AbstractColumnValue col : this) {
      msgList.add(col.getMessageObject());
    }
    return new com.poesys.ms.pk.NaturalPrimaryKey(msgList, className);
  }

  @Override
  public Set<String> getColumnNames() throws DuplicateKeyNameException {
    Set<String> set = new HashSet<String>();
    int setSize = 0;
    for (AbstractColumnValue c : this) {
      set.add(c.getName());
      setSize++;
      if (setSize != set.size()) {
        // Set eliminated a duplicate name.
        List<String> list = new ArrayList<String>();
        DuplicateKeyNameException e = new DuplicateKeyNameException(DUP_NAME);
        list.add(c.getName());
        e.setParameters(list);
        throw e;
      }
    }
    return set;
  }
}