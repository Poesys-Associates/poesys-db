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


import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.poesys.db.DbErrorException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.Message;
import com.poesys.db.col.AbstractColumnValue;
import com.poesys.db.col.BigIntegerColumnValue;
import com.poesys.db.col.NullColumnValue;
import com.poesys.ms.col.IColumnValue;


/**
 * Implements the abstract single-valued primary key and the IPrimaryKey
 * interface for a key constructed from a single integer value created by an
 * identity or auto-increment column (a column that gets its value generated
 * automatically when you insert and which you do not supply through the INSERT
 * statement). The class represents the value with a BigInteger object, so you
 * are not limited to any specific size of integer. On the other hand, you
 * cannot guarantee that the value will fit into a Java Integer or Long value,
 * so you cannot use this value in any context that requires such a value. The
 * initial value is zero (0) and gets replaced with the auto-generated value
 * when you finalize the insert in the DAO.
 * 
 * @author Robert J. Muller
 */
public class IdentityPrimaryKey extends AbstractSingleValuedPrimaryKey {
  /** serial version UID for Serializable class */
  private static final long serialVersionUID = 1L;
  /** Message for invalid parameter exception */
  private static final String INVALID_PARAMETER =
    "com.poesys.db.pk.msg.invalid_identity_parameter";
  /** Message for unexpected SQL error */
  private static final String SQL_ERROR =
    "com.poesys.db.dto.msg.unexpected_sql_error";

  /**
   * Create a primary key with a null column value.
   * 
   * @param name the name of the primary key column
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   * @throws InvalidParametersException when the name is null
   */
  public IdentityPrimaryKey(String name, String className)
      throws InvalidParametersException {
    super(new ArrayList<AbstractColumnValue>(), className);
    NullColumnValue col = new NullColumnValue(name, java.sql.Types.BIGINT);
    list.add(col);
    list = new CopyOnWriteArrayList<AbstractColumnValue>(list);
  }

  /**
   * Create a primary key with a column value. Use this constructor to build a
   * key from a value coming from the database for an existing object. If the
   * input value is null, the key will not include a column value. This allows
   * the finalizeInsert method to update the key value later.
   * 
   * @param name the name of the primary key column
   * @param value the integer key value
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   * 
   * @throws InvalidParametersException when the name is null
   */
  public IdentityPrimaryKey(String name, BigInteger value, String className)
      throws InvalidParametersException {
    super(new ArrayList<AbstractColumnValue>(), className);
    AbstractColumnValue col = null;
    if (value != null) {
      col = new BigIntegerColumnValue(name, value);
    } else {
      col = new NullColumnValue(name, java.sql.Types.BIGINT);
    }
    list.add(col);
    list = new CopyOnWriteArrayList<AbstractColumnValue>(list);
  }

  /**
   * Create a IdentityPrimaryKey object with a list; useful for creating a copy
   * based on a list from another IdentityPrimaryKey.
   * 
   * @param list the list of column values
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   */
  protected IdentityPrimaryKey(List<AbstractColumnValue> list, String className) {
    super(list, className);
  }

  /**
   * Create an IdentityPrimaryKey object from a messaging key object.
   * 
   * @param key the messaging key
   * @param className the name of the IDbDto class of the object that the
   *          primary key identifies
   */
  public IdentityPrimaryKey(com.poesys.ms.pk.IdentityPrimaryKey key,
                            String className) {
    // Call the default constructor in the superclass with no list.
    super(className);
    // Create a new list and populate it from the DTO.
    this.list = new ArrayList<AbstractColumnValue>();
    list.add(new BigIntegerColumnValue(key.getName(), key.getValue()));
    list = new CopyOnWriteArrayList<AbstractColumnValue>(list);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.pk.AbstractPrimaryKey#getSqlInsertColumnList()
   */
  @Override
  public String getSqlInsertColumnList() {
    // The identity column does not appear in the INSERT statement.
    return "";
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.poesys.db.pk.AbstractPrimaryKey#setInsertParams(java.sql.PreparedStatement
   * , int)
   */
  @Override
  public int setInsertParams(PreparedStatement stmt, int nextIndex) {
    // The identity column does not have a parameter, so this does nothing.
    return nextIndex;
  }

  @Override
  public void finalizeInsert(PreparedStatement stmt) {
    // Extract the column value to get the name.
    AbstractColumnValue col = list.get(0);
    BigInteger value = null;

    try {
      // Use the JDBC method to get the identity value just generated.
      ResultSet rs = stmt.getGeneratedKeys();
      if (rs.next()) {
        // Get the key value.
        BigDecimal decimalValue = rs.getBigDecimal(1);
        // Convert the value to a big integer.
        value = decimalValue.toBigInteger();
        BigIntegerColumnValue newCol =
          new BigIntegerColumnValue(col.getName(), value);
        // Clear the current column value from the list and add the new one.
        list.clear();
        list.add(newCol);
      }
    } catch (InvalidParametersException e) {
      List<String> list = new ArrayList<String>();
      DbErrorException d =
        new DbErrorException(Message.getMessage(INVALID_PARAMETER, null));
      list.add(col.getName());
      list.add(value.toString());
      e.setParameters(list);
      throw d;
    } catch (SQLException e) {
      throw new DbErrorException(Message.getMessage(SQL_ERROR, null));
    }
  }

  @Override
  public String getValueList() {
    AbstractColumnValue col = list.get(0);
    StringBuilder str = new StringBuilder();
    str.append("(");
    str.append(col.getName());
    str.append("=");
    str.append(col.toString());
    str.append(")");

    return str.toString();
  }

  @Override
  public IPrimaryKey copy() {
    return new IdentityPrimaryKey(super.copyList(), className);
  }

  @SuppressWarnings("unchecked")
  @Override
  public com.poesys.ms.pk.IPrimaryKey getMessageObject() {
    // Extract column from list and create DTO.
    AbstractColumnValue col = list.get(0);
    IColumnValue<BigInteger> msgCol =
      (IColumnValue<BigInteger>)col.getMessageObject();
    return new com.poesys.ms.pk.IdentityPrimaryKey(msgCol.getName(),
                                                   msgCol.getValue(),
                                                   className);
  }
}
