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


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.poesys.db.BatchException;
import com.poesys.db.connection.ConnectionFactoryFactory;
import com.poesys.db.connection.IConnectionFactory;
import com.poesys.db.dto.IDbDto;


/**
 * An implementation of the IQueryList interface that queries a list of data
 * transfer objects (DTOs) based on a parameterized SQL statement and a list of
 * keys. The list is guaranteed to be thread safe, and the DTOs should also be
 * thread safe if you are going to use them in a multi-threaded environment.
 * 
 * @see com.poesys.db.dto.IDbDto
 * @see IParameterizedQuerySql
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to query
 */
public class QueryListWithKeyList<T extends IDbDto> implements IQueryList<T> {
  /** Logger for debugging */
  private static final Logger logger =
    Logger.getLogger(QueryListWithKeyList.class);

  /** Internal Strategy-pattern object containing the SQL query with key list */
  protected final IKeyListQuerySql<T> sql;
  /** Number of rows to fetch at once, optimizes query fetching */
  protected final int rows;
  /** the client subsystem owning the queried object */
  protected final String subsystem;

  /** Error getting resource bundle, can't resolve to bundle text so a constant */
  private static final String RESOURCE_BUNDLE_ERROR =
    "Problem getting Poesys/DB resource bundle";

  /**
   * Create a QueryListWithParameters object.
   * 
   * @param sql the SQL statement specification
   * @param subsystem the subsystem that owns the object to query
   * @param rows the number of rows to fetch at once; optimizes results fetching
   */
  public QueryListWithKeyList(IKeyListQuerySql<T> sql,
                              String subsystem,
                              int rows) {
    this.sql = sql;
    this.subsystem = subsystem;
    this.rows = rows;
  }

  public List<T> query() throws SQLException, BatchException {
    List<T> list = new ArrayList<T>();
    PreparedStatement stmt = null;
    ResultSet rs = null;

    Connection connection = null;

    // Query the list of objects based on the parameters.
    try {
      IConnectionFactory factory =
        ConnectionFactoryFactory.getInstance(subsystem);
      connection = factory.getConnection();
      stmt = connection.prepareStatement(sql.getSql());
      stmt.setFetchSize(rows);
      sql.bindKeys(stmt);
      logger.debug("Querying list with key list: " + sql.getSql());
      logger.debug("Binding key list: " + sql.getKeyValues());
      rs = stmt.executeQuery();

      // Loop through and fetch all the results, adding each to the result list.
      int count = 0;
      while (rs.next()) {
        T object = getObject(rs);
        if (object != null) {
          list.add(object);
          count++;
        }
      }
      logger.debug("Fetched " + count + " objects");
    } catch (SQLException e) {
      // Log the message and the SQL statement, then rethrow the exception.
      logger.error("Query list with key list error: " + e.getMessage());
      logger.error("Query list with key list sql: " + sql.getSql() + "\n");
      logger.debug("SQL statement in class: " + sql.getClass().getName());
      throw e;
    } catch (IOException e) {
      // Problem with resource bundle, rethrow as SQLException
      throw new SQLException(RESOURCE_BUNDLE_ERROR);
    } finally {
      // Close the statement and result set as required.
      if (stmt != null) {
        stmt.close();
      }
      if (rs != null) {
        rs.close();
      }
      if (connection != null) {
        String connectionString = connection.toString();
        connection.close();
        logger.debug("Closed connection " + connectionString);
      }
    }

    queryNestedObjectsForList(list);

    return list;
  }

  /**
   * Query the nested objects for all the objects in a list of objects of type
   * C. You can override this method in a subclass to provide a session id for a
   * caching session.
   * 
   * @param list the list of objects of type C
   * @throws SQLException when there is a database problem
   * @throws BatchException when there is a batch processing problem
   */
  protected void queryNestedObjectsForList(List<T> list) throws SQLException,
      BatchException {
    // Query any nested objects. This is outside the fetch above to make sure
    // that the statement and result set are closed before recursing.
    for (T object : list) {
      object.queryNestedObjects();
    }
  }

  /**
   * Get a DTO from a SQL result set. Subclasses can override this method to
   * provide caching or other services for the object.
   * 
   * @param rs the result set from an executed SQL statement
   * @return the object
   * @throws SQLException when there is a database access problem
   * @throws BatchException when batch processing fails for a nested object
   */
  protected T getObject(ResultSet rs)
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
