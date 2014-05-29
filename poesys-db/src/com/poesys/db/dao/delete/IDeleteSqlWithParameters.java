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
package com.poesys.db.dao.delete;


import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An interface that defines the set of operations required by an
 * IDeleteWithParameters object to build a SQL DELETE statement for a data
 * transfer object (DTO) that is parameterized by a second DTO. The SQL
 * statement must contain as many parameters as the setParams method sets.
 * </p>
 * 
 * @see IDeleteWithParameters
 * 
 * @author Robert J. Muller
 * @param <T> the type of DTO to delete
 * @param <P> the type of DTO that contains the parameters
 */
public interface IDeleteSqlWithParameters<T extends IDbDto, P extends IDbDto> {
  /**
   * <p>
   * Get the SQL DELETE statement with a hard-coded set of columns for type T
   * and a set of embedded JDBC parameters (?).
   * </p>
   * 
   * <pre>
   * <code>
   * DELETE Table WHERE &lt;expression&gt; = ?
   * </code>
   * </pre>
   * 
   * @param key the primary key for the object for which to generate SQL
   * 
   * @return the SQL for the DELETE statement
   */
  String getSql(IPrimaryKey key);

  /**
   * Set the parameters of the DELETE statement.
   * 
   * @param stmt the prepared SQL statement
   * @param next the next parameter index after the last parameter index set
   * @param dto the data transfer object of type T containing the primary key
   *          values to set into the statement
   * @return the index of the next parameter to set (the first primary key
   *         value)
   * @throws SQLException when the parameter setting fails with a SQL error
   */
  int setParams(PreparedStatement stmt, int next, P dto) throws SQLException;
}
