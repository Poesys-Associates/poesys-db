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


import java.sql.ResultSet;
import java.sql.SQLException;

import com.poesys.db.InvalidParametersException;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An interface that defines the set of operations required by an IQueryList
 * object to build a SQL SELECT statement for a set of data transfer objects
 * (DTOs)
 * </p>
 * <p>
 * An implemention of this interface should have a static string containing the
 * SQL statement with no internal parameters (? characters). The
 * <code>getData</code> method is a factory method that produces IDto objects.
 * </p>
 * <ol>
 * <li>retrieve the queried data elements into local variables using the JDBC
 * accessor methods of the type appropriate to the data</li>
 * <li>create the IDto object, supplying the primary key and the relevant data
 * elements from the local variables to the constructor call</li>
 * </ol>
 * <p>
 * The <code>getPrimaryKey</code> method provides an interface for the primary
 * key, usually implemented through the subsystem factory
 * <code>getPrimaryKey</code> method. The <code>getData</code> method uses
 * this to instantiate a primary key from the result set to pass to the
 * constructor for the object; caching also uses this method to construct a
 * primary key object to look up an object in the cache based on a result set.
 * </p>
 * <p>
 * The <code>getSql</code> method should construct the complete SQL statement
 * by concatenating the static SQL with the dynamically generated primary key
 * query expression from the primary key object
 * <code>getSqlWhereExpression</code> method.
 * </p>
 * <p>
 * This example represents a simple query of the <code>Seq</code> table, a
 * table with a Sequence primary key (a unique integer generated in a sequence)
 * and a single <code>VARCHAR</code> column, col1.
 * </p>
 * 
 * <pre>
 * public class SequenceQuerySql implements IQuerySql {
 *   private static final String SQL = &quot;SELECT pkey, col1 FROM Seq ORDER BY pkey&quot;;
 * 
 *   public IDto getData(ResultSet rs) throws SQLException,
 *       InvalidParametersException {
 *     String col1 = rs.getString(&quot;col1&quot;);
 *     IPrimaryKey key = TestFactory.getTestSequencePrimaryKey(rs);
 *     TestSequence dto = new TestSequence(key, col1);
 *     return dto;
 *   }
 * 
 *   public String getSql() {
 *     return SQL;
 *   }
 * }
 * </pre>
 * 
 * &#064;author author Robert J. Muller
 * @param <T> the type of object to query
 * 
 */
public interface IQuerySql<T extends IDbDto> {
  /**
   * <p>
   * Get the SQL SELECT statement.
   * </p>
   * 
   * @return the SQL for the SELECT statement
   */
  String getSql();

  /**
   * Create the Data Transfer Object (DTO) from the result set instantiated by
   * the prepared statement execution. Each call returns one result from the
   * ResultSet.
   * 
   * @param rs the result set from the query execution
   * @return the DTO
   * @throws SQLException when the parameter setting fails with a SQL error
   * @throws InvalidParametersException when key generation fails due to a null
   *             key name or value
   */
  T getData(ResultSet rs) throws SQLException, InvalidParametersException;

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
   *             key name or value
   */
  IPrimaryKey getPrimaryKey(ResultSet rs) throws SQLException,
      InvalidParametersException;
}
