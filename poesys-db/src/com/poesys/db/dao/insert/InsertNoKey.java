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


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.poesys.db.BatchException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDbDto.Status;
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
public class InsertNoKey<T extends IDbDto> implements IInsert<T> {
  /** The helper class for generating the SQL statement */
  private final IInsertSql<T> sql;
  /** Error message when no DTO supplied */
  private static final String NO_DTO_MSG =
    "com.poesys.db.dao.insert.msg.no_dto";

  private boolean leaf = false;

  /**
   * Create an Insert object by supplying the concrete implementation of the
   * SQL-statement generator and JDBC setter.
   * 
   * @param sql the SQL INSERT statement generator object
   */
  public InsertNoKey(IInsertSql<T> sql) {
    this.sql = sql;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void insert(Connection connection, IDbDto dto) throws SQLException,
      BatchException {
    PreparedStatement stmt = null;

    // Check whether the DTO is there and is NEW.
    if (dto == null) {
      throw new InvalidParametersException(NO_DTO_MSG);
    } else if (dto.getStatus() == Status.NEW) {
      // A NEW DTO, proceed.
      IPrimaryKey key = dto.getPrimaryKey();

      // Run any validation after querying nested objects to be able to use
      // them in validation.
      dto.queryNestedObjectsForValidation();
      dto.validateForInsert();

      try {
        // Prepare the statement, bind in the non-key column values, and execute
        // the insert.
        stmt =
          connection.prepareStatement(sql.getSql(key),
                                      Statement.RETURN_GENERATED_KEYS);
        sql.setParams(stmt, 1, (T)dto);
        // Tell JDBC to return the generated key result set.
        stmt.executeUpdate();
        // Finalize the insert by setting any auto-generated keys.
        key.finalizeInsert(stmt);
        // Finalize the insert by setting any auto-generated attributes.
        // This includes putting the generated key into nested objects.
        dto.finalizeInsert(stmt);

        /*
         * For a concrete class, insert any nested objects (composite children
         * or associations) Only need to insert here, not update or delete, as
         * parent is being inserted.
         */
        if (!dto.isAbstractClass()) {
          dto.insertNestedObjects(connection);
        }
      } finally {
        // Close the statement as required.
        if (stmt != null) {
          stmt.close();
        }
      }
    }
  }

  @Override
  public boolean isLeaf() {
    return leaf;
  }

  @Override
  public void setLeaf(boolean isLeaf) {
    leaf = isLeaf;
  }

  @Override
  public void close() {
    // Nothing to do
  }
}
