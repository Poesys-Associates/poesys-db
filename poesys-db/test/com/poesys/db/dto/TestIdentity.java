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
package com.poesys.db.dto;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.poesys.db.pk.IPrimaryKey;
import com.poesys.db.pk.IdentityPrimaryKey;


/**
 * A data transfer object for the TestIdentity table. This table supports a test
 * of operations on an auto-generated MySQL primary key table.
 * 
 * <pre>
 * <code>
 * CREATE TABLE TestIdentity (
 *   pkey BIGINT AUTO_GENERATED NOT NULL PRIMARY KEY,
 *   col1 varchar(10));
 * </code>
 * </pre>
 * 
 * @author Bob Muller (muller@computer.org)
 */
public class TestIdentity extends AbstractTestDto {
  /** Generated serial version UID for Serializable object */
  private static final long serialVersionUID = -1668228497511041897L;

  /** The identity primary key for the object */
  private final IdentityPrimaryKey key;

  /** The primary key value attribute for the object */
  private BigInteger id;

  /** The String column col1 */
  private String col1 = null;

  /** Message for null compare object */
  private static final String NULL_COMP_MSG =
    "Null object supplied to comparison";

  /**
   * Create a TestIdentity object.
   * 
   * @param key the identity key of the object
   * @param id the generated id for the object
   * @param col1 the first non-key column of the object
   */
  public TestIdentity(IdentityPrimaryKey key, BigInteger id, String col1) {
    // No nested objects to set or constraints to validate
    this.key = key;
    this.id = id;
    this.col1 = col1;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dto.IDto#getPrimaryKey()
   */
  public IPrimaryKey getPrimaryKey() {
    return key;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(IDbDto o) {
    if (o == null) {
      throw new RuntimeException(NULL_COMP_MSG);
    }
    return this.key.compareTo(o.getPrimaryKey());
  }

  /**
   * Get the id.
   * 
   * @return a id
   */
  public BigInteger getId() {
    return id;
  }

  /**
   * Set the id.
   * 
   * @param id a id
   */
  public void setId(BigInteger id) {
    this.id = id;
  }

  /**
   * Get the col1.
   * 
   * @return Returns the col1.
   */
  public synchronized String getCol1() {
    return col1;
  }

  /**
   * Set the col1.
   * 
   * @param col1 The col1 to set.
   */
  public synchronized void setCol1(String col1) {
    this.col1 = col1;
    setChanged();
  }

  @Override
  public boolean equals(Object arg0) {
    return this.compareTo((IDbDto)arg0) == 0;
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dto.IDbDto#markChildrenDeleted()
   */
  public void markChildrenDeleted() {
    // Do nothing, no children.
  }

  @Override
  public void finalizeInsert(PreparedStatement stmt) throws SQLException {
    // Set the key attribute.
    ResultSet rs = stmt.getGeneratedKeys();
    if (rs.next()) {
      // Get the key value.
      BigDecimal decimalValue = rs.getBigDecimal(1);
      // Convert the value to a big integer and assign.
      id = decimalValue.toBigInteger();
    }
  }
}
