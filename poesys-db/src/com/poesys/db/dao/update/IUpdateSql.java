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
package com.poesys.db.dao.update;


import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An interface that defines the set of operations required by an IUpdate,
 * IUpdateBatch, or IUpdateCollection object to build a SQL UPDATE statement for
 * a data transfer object (DTO).
 * </p>
 * <p>
 * The implementation must have the SQL UPDATE statement as a static string with
 * the UPDATE and SET clauses fully specified with parameters and ending with
 * WHERE and a blank space. The getSql method uses the primary key's
 * <code>getSqlWhereExpression</code> method to supply the rest of the WHERE
 * clause. The setParams method sets the SET clause parameters one by one, then
 * sets the WHERE clause primary key columns with the DTO's primary key
 * <code>setParams</code> method. The following code example shows the
 * implementation for a simple natural-key class with one column to update, with
 * the column set with a BigDecimal and the WHERE clause containing the
 * encapsulated primary key comparison expression.
 * </p>
 * 
 * <pre>
 * public class UpdateSqlNatural implements IUpdateSql {
 *   private static final String SQL = &quot;UPDATE Natural SET col1 = ? WHERE &quot;;
 * 
 *   public String getSql(IPrimaryKey key) {
 *     StringBuilder builder = new StringBuilder(SQL);
 *     builder.append(key.getSqlWhereExpression(&quot;&quot;));
 *     return builder.toString();
 *   }
 * 
 *   public int setParams(PreparedStatement stmt, int next, IDto dto)
 *       throws SQLException {
 *     Natural test = (Natural)dto;
 *     stmt.setBigDecimal(next, test.getCol1());
 *     next++;
 *     next = dto.getPrimaryKey().setParams(stmt, next);
 *     return next;
 *   }
 * }
 * </pre>
 * 
 * @author Robert J. Muller
 * @param <T> the DTO type to update
 */
public interface IUpdateSql<T extends IDbDto> {
  /**
   * <p>
   * Get the SQL UPDATE statement embedding the primary key elements and a
   * hard-coded set of columns.
   * </p>
   * 
   * <pre><code>
   * UPDATE Table SET col=?, col=?, ... WHERE &lt;key expression&gt;
   * </code></pre>
   * 
   * @param key the primary key for the object for which to generate SQL
   * 
   * @return the SQL for the UPDATE statement
   */
  String getSql(IPrimaryKey key);

  /**
   * Set the parameters of the UPDATE statement up to the primary key
   * parameters, which come last.
   * 
   * @param stmt the prepared SQL statement
   * @param next the next parameter index after the last parameter index set
   * @param dto the data transfer object containing the values to set into the
   *            statement
   * @return the index of the next parameter to set (the first primary key
   *         value)
   * @throws SQLException when the parameter setting fails with a SQL error
   */
  int setParams(PreparedStatement stmt, int next, T dto) throws SQLException;
}
