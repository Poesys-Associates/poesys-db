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
package com.poesys.db.dao.delete;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.poesys.db.BatchException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.dao.DataEvent;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * An implementation of the IDelete interface that updates an object,
 * identifying the object in the database using the primary key of the data
 * transfer object (DTO). Note that the SQL statement can be null for deletes
 * that are done through the database rather than through the application.
 * 
 * @see IDeleteSql
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to delete
 */
public class DeleteByKey<T extends IDbDto> implements IDelete<T> {
  private static final Logger logger = Logger.getLogger(DeleteByKey.class);

  /** The Strategy-pattern object for the SQL statement */
  IDeleteSql<T> sql;
  /** Error message when no DTO supplied */
  private static final String NO_DTO_MSG =
    "com.poesys.db.dao.delete.msg.no_dto";

  /**
   * Create a DeleteByKey object by supplying the concrete implementation of the
   * SQL-statement generator and JDBC setter.
   * 
   * @param sql the SQL UPDATE statement generator object
   */
  public DeleteByKey(IDeleteSql<T> sql) {
    this.sql = sql;
  }

  @Override
  public void delete(Connection connection, T dto) throws SQLException,
      BatchException {
    PreparedStatement stmt = null;

    if (dto == null) {
      throw new InvalidParametersException(NO_DTO_MSG);
    } else if (dto.getStatus() == IDbDto.Status.DELETED && sql != null) {
      dto.validateForDelete();
      dto.preprocessNestedObjects(connection);
      
      String sqlText = null;

      try {
        IPrimaryKey key = dto.getPrimaryKey();
        sqlText = sql.getSql(key);
        stmt = connection.prepareStatement(sqlText);
        logger.debug("Delete by key: " + sqlText);
        sql.setParams(stmt, 1, dto);
        logger.debug("Key: " + key.getStringKey());

        stmt.executeUpdate();

        postprocess(connection, dto);
        // Notify DTO to update its observer parents of the delete.
        dto.notify(DataEvent.DELETE);
      } catch (SQLException e) {
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
    } else if (dto.getStatus() == IDbDto.Status.CASCADE_DELETED) {
      // Just notify DTO to update its observer parents of the delete.
      dto.notify(DataEvent.DELETE);
    }
  }

  /**
   * Post-process the nested objects of a DTO. Override this method if you want
   * to add information such as the session ID.
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
  public void close() {
    // Nothing to do
    // Note that parameter-based delete does not affect caches at all.
  }
}
