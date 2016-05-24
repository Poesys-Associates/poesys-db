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
package com.poesys.db.dao.update;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.poesys.db.BatchException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * An implementation of the IUpdate interface that updates a data transfer
 * object (DTO) using a SQL statement defined in an IUpdateSql object,
 * identifying the object in the database using the primary key of the DTO. The
 * implementation should update in the database only if isChanged() is true.
 * The caller should set the DTO status to existing once <strong>all</strong>
 * processing is complete (over the entire inheritance hierarchy).
 * 
 * @see com.poesys.db.dto.AbstractDto
 * @see IUpdateSql
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to update
 */
public class UpdateByKey<T extends IDbDto> implements IUpdate<T> {
  private static final Logger logger = Logger.getLogger(UpdateByKey.class);
  /** Internal Strategy-pattern object containing the SQL query */
  private IUpdateSql<T> sql;
  /** Error message when no DTO supplied */
  private static final String NO_DTO_MSG =
    "com.poesys.db.dao.update.msg.no_dto";
  
  /** Indicates whether this is a leaf update */
  private boolean leaf = false;

  /**
   * Create an UpdateByKey object by supplying the concrete implementation of
   * the SQL-statement generator and JDBC setter.
   * 
   * @param sql the SQL UPDATE statement specification
   */
  public UpdateByKey(IUpdateSql<T> sql) {
    this.sql = sql;
  }

  @Override
  public void update(Connection connection, T dto) throws SQLException,
      BatchException {
    PreparedStatement stmt = null;

    if (dto == null) {
      throw new InvalidParametersException(NO_DTO_MSG);
    } else {
      dto.validateForUpdate();
      dto.preprocessNestedObjects(connection);
    }

    String sqlStmt = null;

    if (!dto.isProcessed() && dto.getStatus() == IDbDto.Status.CHANGED) {

      try {
        IPrimaryKey key = dto.getPrimaryKey();
        sqlStmt = sql.getSql(key);
        if (sqlStmt != null) {
          stmt = connection.prepareStatement(sqlStmt);
          sql.setParams(stmt, 1, dto);

          logger.debug("Executing update with key " + key);
          logger.debug("SQL: " + sqlStmt);

          stmt.executeUpdate();

          // Note that the caller must set the DTO status to EXISTING once ALL
          // processing is complete (over the entire inheritance hierarchy).
        }
      } catch (SQLException e) {
        logger.error(e.getMessage());
        logger.error(sqlStmt);
        logger.error("Updated object key: "
                     + dto.getPrimaryKey().getValueList());
        dto.setFailed();
        throw e;
      } catch (RuntimeException e) {
        dto.setFailed();
        throw e;
      } finally {
        if (stmt != null) {
          stmt.close();
        }
      }
    }
    
    dto.setProcessed(true);

    // After processing the object, post-process nested objects.
    // This gets done regardless of main object status.
    postprocess(connection, dto);
  }

  /**
   * Post-process the nested objects of a DTO. Override this method to supply
   * extra information like a session ID.
   * 
   * @param connection the JDBC connection
   * @param dto the DTO containing the nested objects
   * @throws SQLException when there is a database problem
   * @throws BatchException when there is a batch processing problem
   */
  protected void postprocess(Connection connection, T dto) throws SQLException,
      BatchException {
    dto.postprocessNestedObjects(connection);
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
