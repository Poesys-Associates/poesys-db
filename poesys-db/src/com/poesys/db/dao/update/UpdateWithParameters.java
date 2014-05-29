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

import com.poesys.db.InvalidParametersException;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An implementation of the IUpdateWithParameters interface that updates the
 * database with a single UPDATE statement parameterized with the contents of a
 * data transfer object (DTO). Use this class to update a set of objects with a
 * single update or to update a single column in one or a range of objects. You
 * need to construct a special DTO that contains just the parameters along with
 * the IUpdateSql strategy object that processes the parameter values. The
 * validators validate the parameters, but there are no nested objects and
 * therefore no setters executed for this update. The status of the DTO is not
 * relevant either; the update will always happen.
 * </p>
 * <p>
 * <em>
 * Note: There is no implementation of the IUpdateWithParameters interface that
 * takes the DTO cache into account, as this command is not related to DTOs
 * specifically but rather is intended for use in database maintenance. If the
 * updated data affects DTOs in some way, the client must take care of the
 * cache by removing those DTOs from the cache.
 * </em>
 * </p>
 * 
 * @see com.poesys.db.dto.AbstractDto
 * @see com.poesys.db.dto.IDbDto
 * @see IUpdateSql
 * 
 * @author Robert J. Muller
 * @param <P> the type of DTO that contains the parameters for the update
 */
public class UpdateWithParameters<P extends IDbDto> implements
    IUpdateWithParameters<P> {
  private static final Logger logger =
    Logger.getLogger(UpdateWithParameters.class);
  /** Internal Strategy-pattern object containing the SQL query */
  private IUpdateSql<P> sql;
  /** Error message when no DTO supplied */
  private static final String NO_DTO_MSG =
    "com.poesys.db.dao.update.msg.no_dto";

  /** Indicates whether this is a leaf update */
  private boolean leaf = false;

  /**
   * Create an UpdateWithParameters object by supplying the concrete
   * implementation of the SQL-statement generator and JDBC setter.
   * 
   * @param sql the SQL UPDATE statement specification
   */
  public UpdateWithParameters(IUpdateSql<P> sql) {
    this.sql = sql;
  }

  @Override
  public void update(Connection connection, P parameters) throws SQLException {
    PreparedStatement stmt = null;

    if (parameters == null) {
      throw new InvalidParametersException(NO_DTO_MSG);
    } else {
      parameters.validateForUpdate(connection);
    }

    try {
      IPrimaryKey key = parameters.getPrimaryKey();
      String sqlStmt = sql.getSql(key);
      if (sqlStmt != null) {
        stmt = connection.prepareStatement(sqlStmt);
        sql.setParams(stmt, 1, parameters);

        logger.debug("Executing update with parameters key " + key);
        logger.debug("SQL: " + sqlStmt);

        stmt.executeUpdate();
      }
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
  public boolean isLeaf() {
    return leaf;
  }

  @Override
  public void setLeaf(boolean isLeaf) {
    leaf = isLeaf;
  }
}
