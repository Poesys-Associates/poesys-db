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
package com.poesys.db.dao.query;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.poesys.db.BatchException;
import com.poesys.db.dto.IDbDto;


/**
 * An implementation of the IQueryListWithParameters interface that queries a
 * list of data transfer objects (DTOs) based on a parameterized SQL statement.
 * The list is guaranteed to be thread safe, and the DTOs should also be thread
 * safe if you are going to use them in a multi-threaded environment.
 * 
 * @see com.poesys.db.dto.IDbDto
 * @see IParameterizedQuerySql
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to query
 * @param <S> the type of IDbDto that contains the parameters
 * @param <C> the collection type of the set of queried DTOs
 */
public class QueryListWithParameters<T extends IDbDto, S extends IDbDto, C extends Collection<T>>
    implements IQueryListWithParameters<T, S, C> {
  /** Logger for debugging */
  private static final Logger logger =
    Logger.getLogger(QueryListWithParameters.class);

  /** Internal Strategy-pattern object containing the SQL query with parameters */
  protected final IParameterizedQuerySql<T, S> sql;
  /** Number of rows to fetch at once, optimizes query fetching */
  protected final int rows;

  /**
   * Create a QueryListWithParameters object.
   * 
   * @param sql the SQL statement specification
   * @param rows the number of rows to fetch at once; optimizes results fetching
   */
  public QueryListWithParameters(IParameterizedQuerySql<T, S> sql, int rows) {
    this.sql = sql;
    this.rows = rows;
  }

  @SuppressWarnings("unchecked")
  public C query(Connection connection, S parameters) throws SQLException,
      BatchException {
    C list = (C)new ArrayList<T>();
    PreparedStatement stmt = null;
    ResultSet rs = null;

    validateParameters(connection, parameters);

    // Query the list of objects based on the parameters.
    try {
      stmt = connection.prepareStatement(sql.getSql());
      stmt.setFetchSize(rows);
      sql.bindParameters(stmt, parameters);
      logger.debug("Querying list with parameters: " + sql.getSql());
      logger.debug("Binding parameters: " + sql.getParameterValues(parameters));
      rs = stmt.executeQuery();

      // Loop through and fetch all the results, adding each to the result list.
      int count = 0;
      while (rs.next()) {
        T object = getObject(connection, rs);
        if (object != null) {
          list.add(object);
          count++;
        }
      }
      logger.debug("Fetched " + count + " objects");
    } catch (SQLException e) {
      // Log the message and the SQL statement, then rethrow the exception.
      logger.error("Query list with parameters error: " + e.getMessage());
      logger.error("Query list with parameters sql: " + sql.getSql() + "\n");
      logger.debug("SQL statement in class: " + sql.getClass().getName());
      throw e;
    } finally {
      // Close the statement and result set as required.
      if (stmt != null) {
        stmt.close();
      }
      if (rs != null) {
        rs.close();
      }
    }

    queryNestedObjectsForList(connection, list);

    return list;
  }

  /**
   * Validate the parameters. You can override this method in a subclass to
   * provide a valid session id for caching sessions.
   * 
   * @param connection the JDBC connection
   * @param parameters the parameters objects
   * @throws SQLException when there is a validation problem
   */
  protected void validateParameters(Connection connection, S parameters)
      throws SQLException {
    // Validate the parameters.
    parameters.validateForQuery(connection);
  }

  /**
   * Query the nested objects for all the objects in a list of objects of type
   * C. You can override this method in a subclass to provide a session id for a
   * caching session.
   * 
   * @param connection the JDBC connection
   * @param list the list of objects of type C
   * @throws SQLException when there is a database problem
   * @throws BatchException when there is a batch processing problem
   */
  protected void queryNestedObjectsForList(Connection connection, C list)
      throws SQLException, BatchException {
    // Query any nested objects. This is outside the fetch above to make sure
    // that the statement and result set are closed before recursing.
    for (T object : list) {
      object.queryNestedObjects(connection);
    }
  }

  /**
   * Get a DTO from a SQL result set. Subclasses can override this method to
   * provide caching or other services for the object.
   * 
   * @param connection the database connection (for nested object queries)
   * @param rs the result set from an executed SQL statement
   * @return the object
   * @throws SQLException when there is a database access problem
   * @throws BatchException when batch processing fails for a nested object
   */
  protected T getObject(Connection connection, ResultSet rs)
      throws SQLException, BatchException {
    T object = sql.getData(rs);
    // Set the new and changed flags to show this object exists and is
    // unchanged from the version in the database.
    object.setExisting();
    return object;
  }

  @Override
  public void close() {
    // Nothing to do
  }

  @Override
  public void setExpiration(int expiration) {
    // Does nothing in this class, no expiration    
  }
}
