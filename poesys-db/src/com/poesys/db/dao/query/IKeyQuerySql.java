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

import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An interface that defines the set of operations required by an IQueryByKey
 * object to build and parameterize a SQL SELECT statement for an IDto.
 * </p>
 * <p>
 * An implemention of this interface should have a static string containing the
 * SQL SELECT and FROM clauses and the WHERE keyword and a space as the last
 * element. The <code>getData</code> method is a factory method that produces
 * IDto objects.
 * </p>
 * <ol>
 * <li>retrieve the queried data elements into local variables using the JDBC
 * accessor methods of the type appropriate to the data</li>
 * <li>create the IDto object of a specific, non-lazy-loading type, supplying
 * the primary key and the relevant data elements from the local variables to
 * the constructor call</li>
 * </ol>
 * <p>
 * The <code>getSql</code> method should construct the complete SQL statement
 * by concatenating the static SQL with the dynamically generated primary key
 * query expression from the primary key object
 * <code>getSqlWhereExpression</code> method.
 * </p>
 * <p>
 * This example represents a simple query of the <code>Natural</code> table, a
 * table with a Natural primary key consisting of two columns, key1 and key2.
 * </p>
 * 
 * <pre>
 * public class NaturalKeyQuerySql implements IKeyQuerySql {
 *   private static final String SQL =
 *     &quot;SELECT key1, key2, col1 FROM Natural WHERE &quot;;
 * 
 *   public IDto getData(IPrimaryKey key, ResultSet rs) throws SQLException {
 *     String key1 = rs.getString(&quot;key1&quot;);
 *     String key2 = rs.getString(&quot;key2&quot;);
 *     BigDecimal col = rs.getBigDecimal(&quot;col1&quot;);
 *     return new Natural(key1, key2, col);
 *   }
 * 
 *   public String getSql(IPrimaryKey key) {
 *     return SQL + key.getSqlWhereExpression(&quot;&quot;);
 *   }
 * }
 * </pre>
 * 
 * @see IQueryByKey
 * @see com.poesys.db.dto.IDbDto
 * @see com.poesys.db.pk.IPrimaryKey
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to query
 */
public interface IKeyQuerySql<T extends IDbDto> {
  /**
   * <p>
   * Get the SQL SELECT statement embedding the primary key elements.
   * </p>
   * 
   * <pre>
   * <code>
   * SELECT select-list FROM &lt;table-expression&gt; WHERE &lt;key-expression&gt;
   * </code>
   * </pre>
   * 
   * <p>
   * 
   * @param key the primary key for the object for which to generate SQL
   * 
   * @return the SQL for the SELECT statement
   */
  String getSql(IPrimaryKey key);

  /**
   * Create the Data Transfer Object (DTO) from the result set instantiated by
   * the prepared statement execution. The object contains the primary key and
   * any additional columns retrieved from the database.
   * 
   * @param key the primary key of the object to return
   * @param rs the result set from the query execution
   * @return the DTO
   * @throws SQLException when the parameter setting fails with a SQL error
   */
  T getData(IPrimaryKey key, ResultSet rs) throws SQLException;
}
