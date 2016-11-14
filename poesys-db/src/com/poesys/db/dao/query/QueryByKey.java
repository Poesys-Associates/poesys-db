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

import org.apache.log4j.Logger;

import com.poesys.db.BatchException;
import com.poesys.db.ConstraintViolationException;
import com.poesys.db.DbErrorException;
import com.poesys.db.NoPrimaryKeyException;
import com.poesys.db.connection.ConnectionFactoryFactory;
import com.poesys.db.connection.IConnectionFactory;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An implementation of the IQueryByKey interface that implements the basic
 * elements of a query by primary key. The class creates data transfer objects
 * (DTOs) with a Strategy-pattern object that contains the SQL required to query
 * the data, and the query method is a factory method that produces a single
 * data transfer object containing the data from the database, including any
 * nested objects. The DTO should be thread-safe if it is going to be cached or
 * otherwise used in a multi-threaded environment.
 * </p>
 * <p>
 * Use an IDtoFactory class to generate the appropriate kind of object (cached
 * or direct) om the IKeyQuerySql implementation.
 * </p>
 * 
 * @see com.poesys.db.dto.IDbDto
 * @see com.poesys.db.pk.IPrimaryKey
 * @see com.poesys.db.dao.IDaoFactory
 * @see IKeyQuerySql
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to query
 */
public class QueryByKey<T extends IDbDto> implements IQueryByKey<T> {
  /** Logger for debugging */
  private static final Logger logger = Logger.getLogger(QueryByKey.class);
  /** Internal Strategy-pattern object containing the SQL query */
  protected final IKeyQuerySql<T> sql;
  /** the client subsystem owning the queried object */
  protected final String subsystem;
  /** Error message about not having a primary key with which to query */
  protected static final String NO_PRIMARY_KEY_MSG =
    "com.poesys.db.dao.query.msg.no_primary_key";
  /** Error getting resource bundle, can't resolve to bundle text so a constant */
  private static final String RESOURCE_BUNDLE_ERROR =
    "Problem getting Poesys/DB resource bundle";

  /**
   * Create a QueryByKey object.
   * 
   * @param sql the SQL SELECT statement object for the query
   * @param subsystem the subsystem that owns the object to query
   */
  public QueryByKey(IKeyQuerySql<T> sql, String subsystem) {
    this.sql = sql;
    this.subsystem = subsystem;
  }

  @Override
  public T queryByKey(IPrimaryKey key) throws SQLException, BatchException {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    T object = null;

    // Make sure the key is there.
    if (key == null) {
      throw new NoPrimaryKeyException(NO_PRIMARY_KEY_MSG);
    }

    Connection connection = null;

    try {
      IConnectionFactory factory =
        ConnectionFactoryFactory.getInstance(subsystem);
      connection = factory.getConnection();
      stmt = connection.prepareStatement(sql.getSql(key));
      key.setParams(stmt, 1);
      logger.debug("Querying by key: " + sql.getSql(key));
      logger.debug("Setting key value: " + key.getValueList());
      rs = stmt.executeQuery();

      // Get a single result from the ResultSet and create the IDto.
      if (rs.next()) {
        object = sql.getData(key, rs);
        // Only proceed if object retrieved.
        if (object != null) {
          // Query any nested objects.
          object.queryNestedObjects();
          // Set the new and changed flags to show this object exists and is
          // unchanged from the version in the database.
        }
        logger.debug("Queried object by key: "
                     + object.getPrimaryKey().getValueList());
      }
    } catch (ConstraintViolationException e) {
      throw new DbErrorException(e.getMessage(), e);
    } catch (SQLException e) {
      // Log the message and the SQL statement, then rethrow the exception.
      logger.error("Query by key error: " + e.getMessage());
      logger.error("Query by key sql: " + sql.getSql(key) + "\n");
      logger.debug("SQL statement in class: " + sql.getClass().getName());
      throw e;
    } catch (IOException e) {
      // Problem with resource bundle, rethrow as SQLException
      throw new SQLException(RESOURCE_BUNDLE_ERROR);
    } finally {
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

    // Query any nested objects. This is outside the fetch above to make sure
    // that the statement and result set are closed before recursing.
    if (object != null) {
      object.queryNestedObjects();
      object.setExisting();
    }

    return object;
  }

  @Override
  public void close() {
    // Nothing to do
  }

  @Override
  public void setExpiration(int expiration) {
    // Does nothing, no expiration on objects here
  }
}
