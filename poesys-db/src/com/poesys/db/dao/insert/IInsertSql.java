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
package com.poesys.db.dao.insert;


import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An interface that defines the set of operations required by an IInsert or
 * IInsertBatch object to build a SQL INSERT statement for a data transfer
 * object (DTO). The concrete implementation provides package-access operations
 * for use by the concrete subclasses.
 * </p>
 * <p>
 * The implementation must generate the INSERT statement's list of column names
 * as well as its list of supplied value parameters. You get the column list by
 * calling the primary key's <code>getSqlInsertColumnList</code> method, and
 * you code the appropriate number of parameters directly into the string. This
 * necessitates coding the SQL statement in two parts, as the following example
 * shows:
 * </p>
 * 
 * <pre><code>
 * public class InsertSqlSeq implements IInsertSql {
 *   private static final String SQL1 = &quot;INSERT INTO Seq (&quot;;
 *   private static final String SQL2 = &quot;, col1) VALUES (?, ?)&quot;;
 * 
 *   public String getSql(IPrimaryKey key) {
 *     StringBuilder builder = new StringBuilder(SQL1);
 *     builder.append(key.getSqlInsertColumnList());
 *     builder.append(SQL2);
 *     return builder.toString();
 *   }
 * 
 *   public void setParams(PreparedStatement stmt, int next, Seq dto)
 *       throws SQLException {
 *     stmt.setString(next, dto.getCol1());
 *   }
 * }
 * </code></pre>
 * 
 * @see com.poesys.db.dto.IDbDto
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to insert
 */
public interface IInsertSql<T extends IDbDto> {
  /**
   * <p>
   * Get the SQL INSERT statement embedding the primary key elements and a
   * hard-coded set of columns.
   * </p>
   * 
   * <pre><code>
   * INSERT INTO Table ([column[,column]...]) VALUES (?[,?]...)
   * </code></pre>
   * 
   * <p>
   * where the first n columns are the primary key columns from the IPrimaryKey
   * object.
   * </p>
   * 
   * @param key the primary key for the object for which to generate SQL
   * 
   * @return the prefix SQL for the INSERT statement
   */
  String getSql(IPrimaryKey key);

  /**
   * Set the parameters after the primary key parameters in the INSERT
   * statement.
   * 
   * @param stmt the prepared SQL statement
   * @param next the next parameter index after the last parameter index set
   * @param dto the data transfer object of type T containing the values to set
   *            into the statement
   * @throws SQLException when the parameter setting fails with a SQL error
   */
  void setParams(PreparedStatement stmt, int next, T dto)
      throws SQLException;
}
