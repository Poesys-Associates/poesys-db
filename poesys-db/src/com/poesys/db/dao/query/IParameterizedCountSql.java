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

import com.poesys.db.dto.IDbDto;


/**
 * <p>
 * An interface that defines the set of operations required by an IQueryCount
 * object to build and parameterize a SQL SELECT statement that returns an
 * integer count of a set of objects specified by a set of parameters defined in
 * an IDto of type P
 * </p>
 * <p>
 * An implemention of this interface should have a static string containing the
 * SQL SELECT and FROM clauses and the WHERE keyword and a space as the last
 * element.
 * </p>
 * <p>
 * <strong>You should alias the single column in the select list to the name
 * <code>count</code></strong>.
 * </p>
 * <p>
 * The <code>getSql</code> method should construct the complete SQL statement by
 * concatenating the static SQL with the dynamically generated primary key query
 * expression from the primary key object <code>getSqlWhereExpression</code>
 * method.
 * </p>
 * <p>
 * This example represents a parameterized query of the <code>Seq</code> table.
 * The example uses the Seq class for the parameter DTO type as the example
 * parameterizes the SQL query on the col1 column.
 * </p>
 * 
 * <pre>
 * public class SeqQueryCountSql implements IParameterizedCountSql&lt;Seq, Seq&gt; {
 *   private static final String SQL =
 *     &quot;SELECT count(*) AS count FROM Seq WHERE col1 = ?&quot;;
 * 
 *   public void bindParameters(PreparedStatement stmt, Seq parameters) {
 *     stmt.setString(1, parameters.getCol1());
 *   }
 * 
 *   public String getSql() {
 *     return SQL;
 *   }
 * }
 * </pre>
 * 
 * @see IQueryCount
 * @see com.poesys.db.dto.IDbDto
 * 
 * @author Robert J. Muller
 * @param <P> The DTO type that serves as a parameter container
 */
public interface IParameterizedCountSql<P extends IDbDto> {
  /**
   * <p>
   * Get the SQL SELECT statement embedding the parameters.
   * </p>
   * 
   * <pre>
   * SELECT select-list FROM &lt;table-expression&gt; WHERE &lt;select-expression&gt;
   * </pre>
   * 
   * @return the SQL for the SELECT statement
   */
  String getSql();

  /**
   * Bind the parameter values into the SQL statement.
   * 
   * @param stmt the prepared statement into which to bind the parameter values
   * @param parameters a DTO containing the parameter values
   */
  void bindParameters(PreparedStatement stmt, P parameters);
}
