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
 * 
 */
package com.poesys.db.dao.query;


import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.poesys.db.dto.IDbDto;


/**
 * An implementation of the IQueryCount interface that performs the basic count
 * query given a parameter data transfer object (DTO) of type P
 * 
 * @see IParameterizedCountSql
 * 
 * @author Robert J. Muller
 * @param <P> the class for the parameter DTO
 */
public class QueryCount<P extends IDbDto> implements IQueryCount<P> {
  /** Logger for debugging */
  private static final Logger logger = Logger.getLogger(QueryCount.class);

  /** The parameterized count SQL statement object */
  private final IParameterizedCountSql<P> sql;

  /**
   * Create a parameterized count query.
   * 
   * @param sql the SQL generator for the parameterized query
   */
  public QueryCount(IParameterizedCountSql<P> sql) {
    this.sql = sql;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dao.query.IQueryCount#queryCount(java.sql.Connection,
   *      com.poesys.db.dto.IDto)
   */
  public BigInteger queryCount(Connection connection, P parameters)
      throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    BigInteger count = null;

    // Validate the parameters.
    parameters.validateForQuery(connection);

    // Query the count based on the parameters.
    try {
      stmt = connection.prepareStatement(sql.getSql());
      sql.bindParameters(stmt, parameters);
      rs = stmt.executeQuery();

      // Fetch the result, adding each to the result list.
      if (rs.next()) {
        count = rs.getBigDecimal("count").toBigInteger();
      }
    } catch (SQLException e) {
      // Log the message and the SQL statement, then rethrow the exception.
      logger.error(e.getMessage());
      logger.error(sql.getSql());
      logger.debug("SQL statement in class: " + sql.getClass().getName());
      throw e;
    } finally {
      // Close the statement and result set as required.
      if (stmt != null) {
        stmt.close();
      }
    }
    return count;
  }
}
