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

import com.poesys.db.InvalidParametersException;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An implementation of the IDeleteWithParameters interface that deletes a set
 * of objects with a single DELETE statement parameterized with the contents of
 * a data transfer object. Use this class to delete a set of objects with a
 * single delete. You need to construct a special DTO that contains just the
 * parameters along with the IDeleteSql helper that processes the parameter
 * values. The validators validate the parameters, but there are no nested
 * objects and therefore no setters executed for this delete. The status of the
 * DTO is not relevant either; the delete will always happen.
 * </p>
 * <p>
 * <em>
 * Note: There is no implementation of the IDeleteWithParameters interface that
 * takes the DTO cache into account, as this command is not related to DTOs
 * specifically but rather is intended for use in database maintenance. If the
 * deleted data affects DTOs in some way, the client must take care of the
 * cache by removing those DTOs from the cache.
 * </em>
 * </p>
 * 
 * @see IDeleteSql
 * 
 * @author Robert J. Muller
 * @param <T> the type of DTO to delete
 * @param <P> the type of DTO that contains the identifying parameters
 */
public class DeleteWithParameters<T extends IDbDto, P extends IDbDto>
    implements IDeleteWithParameters<T, P> {
  private static final Logger logger =
    Logger.getLogger(DeleteWithParameters.class);
  /** The Strategy-pattern object for the SQL statement */
  IDeleteSqlWithParameters<T, P> sql;
  /** Error message when no DTO supplied */
  private static final String NO_DTO_MSG =
    "com.poesys.db.dao.delete.msg.no_dto";

  /**
   * Create a DeleteWithParameters object by supplying the concrete
   * implementation of the SQL-statement generator and JDBC setter.
   * 
   * @param sql the SQL DELETE statement generator object
   */
  public DeleteWithParameters(IDeleteSqlWithParameters<T, P> sql) {
    this.sql = sql;
  }

  @Override
  public void delete(Connection connection, P parameters) throws SQLException {
    PreparedStatement stmt = null;

    if (parameters == null) {
      throw new InvalidParametersException(NO_DTO_MSG);
    } else {
      parameters.validateForDelete();
    }

    String sqlText = null;
    
    try {
      IPrimaryKey key = parameters.getPrimaryKey();
      sqlText = sql.getSql(key);
      stmt = connection.prepareStatement(sqlText);
      logger.debug("Deleting with parameters: " + sqlText);
      sql.setParams(stmt, 1, parameters);
      stmt.executeUpdate();
    } catch (SQLException e) {
      parameters.setFailed();
      throw e;
    } catch (RuntimeException e) {
      parameters.setFailed();
      throw e;
    } finally {
      if (stmt != null) {
        stmt.close();
      }
    }
  }

  @Override
  public void close() {
    // Nothing to do
    // Note that parameter-based delete does not affect caches at all.
  }
}
