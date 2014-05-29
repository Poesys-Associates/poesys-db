/*
 * Copyright (c) 2011 Poesys Associates. All rights reserved.
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
import java.sql.Date;
import java.sql.Timestamp;
import java.util.UUID;

import com.poesys.db.col.AbstractColumnValue;
import com.poesys.db.col.BigDecimalColumnValue;
import com.poesys.db.col.BigIntegerColumnValue;
import com.poesys.db.col.DateColumnValue;
import com.poesys.db.col.IntegerColumnValue;
import com.poesys.db.col.LongColumnValue;
import com.poesys.db.col.NullColumnValue;
import com.poesys.db.col.StringColumnValue;
import com.poesys.db.col.TimestampColumnValue;
import com.poesys.db.col.UuidColumnValue;
import com.poesys.ms.col.IColumnValue;


/**
 * This class is a factory that transforms a messaging DTO into a Poesys/DB
 * primary key (IPrimaryKey). The single getKey() method uses instanceof to
 * detect the concrete type and produces a concrete primary key using the
 * appropriate data from the incoming message DTO.
 * 
 * @author Robert J. Muller
 */
public class MessageKeyFactory {
  /**
   * Create a primary key from a primary key message DTO.
   * 
   * @param messageKey the message DTO (GuidPrimaryKey)
   * @return an IPrimaryKey object (GuidPrimaryKey)
   */
  public static IPrimaryKey getKey(com.poesys.ms.pk.IPrimaryKey messageKey) {
    IPrimaryKey key = null;
    if (messageKey instanceof com.poesys.ms.pk.GuidPrimaryKey) {
      key =
        new GuidPrimaryKey((com.poesys.ms.pk.GuidPrimaryKey)messageKey,
                           messageKey.getClassName());
    } else if (messageKey instanceof com.poesys.ms.pk.SequencePrimaryKey) {
      key =
        new SequencePrimaryKey((com.poesys.ms.pk.SequencePrimaryKey)messageKey,
                               messageKey.getClassName());
    } else if (messageKey instanceof com.poesys.ms.pk.IdentityPrimaryKey) {
      key =
        new IdentityPrimaryKey((com.poesys.ms.pk.IdentityPrimaryKey)messageKey,
                               messageKey.getClassName());
    } else if (messageKey instanceof com.poesys.ms.pk.NaturalPrimaryKey) {
      key =
        new NaturalPrimaryKey((com.poesys.ms.pk.NaturalPrimaryKey)messageKey,
                              messageKey.getClassName());
    } else if (messageKey instanceof com.poesys.ms.pk.AssociationPrimaryKey) {
      key =
        new AssociationPrimaryKey((com.poesys.ms.pk.AssociationPrimaryKey)messageKey,
                                  messageKey.getClassName());
    } else if (messageKey instanceof com.poesys.ms.pk.CompositePrimaryKey) {
      key =
        new CompositePrimaryKey((com.poesys.ms.pk.CompositePrimaryKey)messageKey,
                                messageKey.getClassName());
    } else {
      throw new RuntimeException("com.poesys.db.pk.msg.unknown_key_type");
    }
    return key;
  }

  /**
   * Get an abstract column value instantiated with the correct concrete data
   * type based on an incoming message column value type.
   * 
   * @param col a message primary key column value
   * @return a Poesys/DB column value corresponding to the message column value
   */
  public static AbstractColumnValue getColumnValue(IColumnValue<?> col) {
    AbstractColumnValue colValue = null;
    if (col.getType() == IColumnValue.ColumnType.BigDecimal) {
      colValue =
        new BigDecimalColumnValue(col.getName(), (BigDecimal)col.getValue());
    } else if (col.getType() == IColumnValue.ColumnType.BigInteger) {
      colValue =
        new BigIntegerColumnValue(col.getName(), (BigInteger)col.getValue());
    } else if (col.getType() == IColumnValue.ColumnType.Date) {
      colValue = new DateColumnValue(col.getName(), (Date)col.getValue());
    } else if (col.getType() == IColumnValue.ColumnType.Integer) {
      colValue = new IntegerColumnValue(col.getName(), (Integer)col.getValue());
    } else if (col.getType() == IColumnValue.ColumnType.Long) {
      colValue = new LongColumnValue(col.getName(), (Long)col.getValue());
    } else if (col.getType() == IColumnValue.ColumnType.Null) {
      // Extract the JDBC type from the column.
      colValue = new NullColumnValue(col.getName(), (Integer)col.getValue());
    } else if (col.getType() == IColumnValue.ColumnType.String) {
      colValue = new StringColumnValue(col.getName(), (String)col.getValue());
    } else if (col.getType() == IColumnValue.ColumnType.Timestamp) {
      colValue =
        new TimestampColumnValue(col.getName(), (Timestamp)col.getValue());
    } else if (col.getType() == IColumnValue.ColumnType.Uuid) {
      colValue = new UuidColumnValue(col.getName(), (UUID)col.getValue());
    }
    return colValue;
  }
}
