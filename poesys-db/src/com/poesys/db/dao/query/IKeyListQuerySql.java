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


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.poesys.db.InvalidParametersException;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An interface that defines the set of operations required by an
 * IQueryListWithKeyList object to build and parameterize a SQL SELECT statement
 * for a set of data transfer objects (DTOs) specified by a list of key values
 * associated with the SQL statement through an IN clause.
 * </p>
 * <p>
 * An implementation of this interface should have a static string containing
 * the SQL SELECT and FROM clauses and the WHERE keyword and IN ( as the last
 * element. The <code>getData</code> method is a factory method that produces
 * IDto objects.
 * </p>
 * <ol>
 * <li>retrieve the queried data elements into local variables using the JDBC
 * accessor methods of the type appropriate to the data</li>
 * <li>create the IDto object, supplying the primary key data elements from the
 * SQL statement.</li>
 * </ol>
 * <p>
 * The <code>getSql</code> method should construct the complete SQL statement by
 * concatenating the static SQL with the dynamically generated primary key query
 * expression from the primary key list.
 * </p>
 * 
 * @see IQueryListWithParameters
 * @see com.poesys.db.dto.IDbDto
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to query
 */
public interface IKeyListQuerySql<T extends IDbDto> {
  /**
   * <p>
   * Get the SQL SELECT statement embedding the parameters.
   * </p>
   * 
   * <pre>
   * <code>
   * SELECT select-list FROM &lt;table-expression&gt; WHERE &lt;select-expression&gt;
   * </code>
   * </pre>
   * 
   * @return the SQL for the SELECT statement
   */
  String getSql();

  /**
   * Get the list of primary keys.
   * 
   * @return a list of primary keys for objects of class T
   */
  List<IPrimaryKey> getKeys();

  /**
   * Set the list of primary keys.
   * 
   * @param keys a list of primary keys for objects of class T
   */
  void setKeys(List<IPrimaryKey> keys);

  /**
   * Bind the key values into the SQL statement.
   * 
   * @param stmt the prepared statement into which to bind the parameter values
   * @param parameters a DTO containing the parameter values
   * @throws SQLException when there is a bind error
   */
  void bindKeys(PreparedStatement stmt) throws SQLException;

  /**
   * Create a primary key object of the appropriate implementation for the DTO
   * object from the result set instantiated by the prepared statement
   * execution. Each call returns one primary key from the result set. You can
   * use this method to get the primary key for an object rather than the entire
   * object to use for lookup--for example, in a cache.
   * 
   * @param rs the result set from the query execution
   * @return the primary key
   * @throws SQLException when the parameter setting fails with a SQL error
   * @throws InvalidParametersException when key generation fails due to a null
   *           key name or value
   */
  IPrimaryKey getPrimaryKey(ResultSet rs) throws SQLException,
      InvalidParametersException;

  /**
   * Get the key values for display.
   * 
   * @return a string displaying the name and value of each parameter value
   */
  String getKeyValues();

  /**
   * Create the Data Transfer Object (DTO) from the result set instantiated by
   * the prepared statement execution. Each call returns one result from the
   * ResultSet.
   * 
   * @param rs the result set from the query execution
   * @return the DTO
   * @throws SQLException when the parameter setting fails with a SQL error
   */
  T getData(ResultSet rs) throws SQLException;
}
