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
package com.poesys.db.dao.delete;


import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An interface that defines the set of operations required by an IDelete or
 * IDeleteBatch object to build a SQL DELETE statement for a data transfer
 * object (DTO). The SQL statement should end in the keyword WHERE followed by a
 * blank space, and the <code>getSql</code> method should append the primary
 * key using the <code>getSqlWhereExpression</code> method.
 * </p>
 * <p>
 * This example shows a SQL statement that deletes from the TestNatural table, a
 * table with rows identified by a natural key.
 * </p>
 * 
 * <pre><code>
 * public class DeleteSqlTestNatural implements IDeleteSql {
 *   private static final String SQL = &quot;DELETE FROM TestNatural WHERE &quot;;
 * 
 *   public String getSql(IPrimaryKey key) {
 *     StringBuilder builder = new StringBuilder(SQL);
 *     builder.append(key.getSqlWhereExpression(&quot;&quot;));
 *     return builder.toString();
 *   }
 * 
 *   public int setParams(PreparedStatement stmt, int next, TestNatural dto)
 *       throws SQLException {
 *     next = dto.getPrimaryKey().setParams(stmt, next);
 *     return next;
 *   }
 * }
 * </code></pre>
 * 
 * @see IDelete
 * @see IDeleteBatch
 * @see IDeleteCollection
 * @see IDeleteWithParameters
 * 
 * @author Robert J. Muller
 * @param <T> the type of DTO to delete
 */
public interface IDeleteSql<T extends IDbDto> {
  /**
   * <p>
   * Get the SQL DELETE statement embedding the primary key elements and a
   * hard-coded set of columns.
   * </p>
   * 
   * <pre><code>
   * DELETE Table WHERE &lt;key expression&gt;
   * </code></pre>
   * 
   * @param key the primary key for the object for which to generate SQL
   * 
   * @return the SQL for the DELETE statement
   */
  String getSql(IPrimaryKey key);

  /**
   * Set the primary key parameters of the DELETE statement.
   * @param <P> the type of DTO containing parameters
   * 
   * @param stmt the prepared SQL statement
   * @param next the next parameter index after the last parameter index set
   * @param dto the data transfer object of type T containing the primary key
   *            values to set into the statement
   * @return the index of the next parameter to set (the first primary key
   *         value)
   * @throws SQLException when the parameter setting fails with a SQL error
   */
  <P extends IDbDto> int setParams(PreparedStatement stmt, int next, P dto)
      throws SQLException;
}
