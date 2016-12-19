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
package com.poesys.db.dao.insert;


import java.sql.PreparedStatement;

import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An interface for a Command pattern class that inserts an object into the
 * database based on values in a data transfer object (DTO). The object may be a
 * standalone object or it may contain sub-objects linked to the top-level
 * object. This implementation of the interface IInsert handles the case of
 * identity or auto-generated keys, which for inserts involve submitting the
 * insert with no key at all, then retrieving the auto-generated key using the
 * JDBC auto-generated key result set feature. Note that this is not true for
 * sequence-generated keys, which the system creates independently and supplies
 * as though it were a natural key.
 * </p>
 * <p>
 * The logic of tree insertion requires that certain nested objects be inserted
 * while others are not. Only objects that cannot exist without the object being
 * inserted should also be inserted. The following kinds of nested object should
 * have a corresponding ISet command associated with the field:
 * </p>
 * <ul>
 * <li>A <em>composite</em> object or collection of objects; that is,
 * parent-child relationships or ownership relationships</li>
 * <li>An <em>association</em> object or collection of objects as opposed to the
 * associated objects; this object represents the association itself, and the
 * parent object owns the relationship.
 * </ul>
 * <p>
 * Only those association objects where all associated objects other than the
 * current object are already inserted in the database should be inserted, as
 * otherwise you will get a foreign key failure. The ISet command must check the
 * associated objects for database status using the isNew method on the IDto.
 * </p>
 * <p>
 * Override the finalizeInsert method on the DTO class to copy the generated key
 * value set by the key finalizeInsert into nested objects before they are
 * inserted by the InsertNestedObjects call.
 * </p>
 * 
 * @see com.poesys.db.dto.IDbDto
 * @see com.poesys.db.dto.ISet
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to insert
 */
public class InsertNoKey<T extends IDbDto> extends Insert<T> {

  /**
   * Create an Insert object by supplying the concrete implementation of the
   * SQL-statement generator and JDBC setter.
   * 
   * @param sql the SQL INSERT statement generator object
   * @param subsystem the subsystem of the DTO class T
   */
  public InsertNoKey(IInsertSql<T> sql, String subsystem) {
    super(sql, subsystem);
  }

  @Override
  protected int setKeyParams(PreparedStatement stmt, IPrimaryKey key) {
    // Do nothing, no primary key field to set; return field 1 as next field to
    // set
    return 1;
  }
}
