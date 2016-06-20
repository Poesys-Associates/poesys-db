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
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.poesys.db.BatchException;
import com.poesys.db.connection.ConnectionFactoryFactory;
import com.poesys.db.connection.IConnectionFactory;
import com.poesys.db.dto.IDbDto;


/**
 * An implementation of the IQueryList interface that queries a list of data
 * transfer objects (DTOs) based on a SQL statement with no parameters. The list
 * is guaranteed to be thread safe, and the DTOs should also be thread safe if
 * you are going to use them in a multi-threaded environment.
 * 
 * @see com.poesys.db.dto.IDbDto
 * @see IQuerySql
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to query
 */
public class QueryList<T extends IDbDto> implements IQueryList<T> {
  /** Logger for debugging */
  private static final Logger logger = Logger.getLogger(QueryList.class);

  /**
   * Internal Strategy-pattern object containing the SQL query with no
   * parameters
   */
  protected final IQuerySql<T> sql;
  /** the client subsystem owning the queried object */
  protected final String subsystem;
  /** Number of rows to fetch at once, optimizes query fetching */
  protected final int rows;

  /** Error getting resource bundle, can't resolve to bundle text so a constant */
  private static final String RESOURCE_BUNDLE_ERROR =
    "Problem getting Poesys/DB resource bundle";

  /**
   * Create a QueryList object.
   * 
   * @param sql the SQL statement specification
   * @param subsystem the subsystem that owns the object being queried
   * @param rows the number of rows to fetch at once; optimizes results fetching
   */
  public QueryList(IQuerySql<T> sql, String subsystem, int rows) {
    this.sql = sql;
    this.subsystem = subsystem;
    this.rows = rows;
  }

  @Override
  public List<T> query() throws SQLException, BatchException {
    List<T> list = new ArrayList<T>();
    PreparedStatement stmt = null;
    ResultSet rs = null;
    int counter = 0;
    int total = 0;
    long time = System.currentTimeMillis();
    long startTime = time;

    // Query the list of objects.
    Connection connection = null;
    try {
      IConnectionFactory factory =
        ConnectionFactoryFactory.getInstance(subsystem);
      connection = factory.getConnection();
      stmt = connection.prepareStatement(sql.getSql());
      logger.debug("Querying list without parameters with SQL: " + sql.getSql());
      stmt.setFetchSize(rows);
      rs = stmt.executeQuery();

      // Loop through and fetch all the results, adding each to the result list.
      while (rs.next()) {
        T object = getObject(connection, rs);
        if (object != null) {
          list.add(object);
        }
        counter++;
        if (counter >= 1000) {
          long now = System.currentTimeMillis();
          long diff = now - time;
          time = now;
          total += counter;
          logger.debug("Fetching " + object.getClass().getName() + ", count: "
                       + counter + ", total: " + total + ", average ms: "
                       + (diff / counter) + ", total ms: " + (now - startTime));
          counter = 0;
        }
      }
    } catch (SQLException e) {
      // Log the message and the SQL statement, then rethrow the exception.
      logger.error(e.getMessage());
      logger.error(sql.getSql());
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
      // Close the connection.
      if (connection != null) {
        String connectionString = connection.toString();
        connection.close();
        logger.debug("Closed connection " + connectionString);
      }
    }

    queryNestedObjectsForList(list);

    // Copy the list to a threadsafe list.
    CopyOnWriteArrayList<T> threadsafeList = new CopyOnWriteArrayList<T>(list);
    return threadsafeList;
  }

  /**
   * Query the nested objects for all the objects in a list of objects of type
   * T. You can override this method in subclasses to provide a session ID in
   * the call to queryNestedObjects.
   * 
   * @param list the list of objects of type T
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
   * @param connection the database connection (for nested object queries)
   * @param rs the result set from an executed SQL statement
   * @return the database DTO
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
