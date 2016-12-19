/*
 * Copyright (c) 2008, 2011 Poesys Associates. All rights reserved.
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

import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * SQL statement specification for deleting a standard object. The concrete
 * subclass contains the SQL statement and an implementation of
 * <code>IDeleteSql.getSql()</code> that calls the <code>getSql</code> method in
 * this class:
 * 
 * <pre>
 * public class DeleteTestSql extends AbstractDeleteSql {
 * 
 *   private static final String SQL = &quot;DELETE FROM Test WHERE &quot;;
 * 
 *   public String getSql(IPrimaryKey key) {
 *     return super.getSql(key, SQL);
 *   }
 * }
 * </pre>
 * 
 * @author Robert J. Muller
 * @param <T> the DTO type
 */
public abstract class AbstractDeleteSql<T extends IDbDto> implements
    IDeleteSql<T> {
  /**
   * A helper method that takes the SQL from the caller (usually a concrete
   * subclass of this class) and builds a complete SQL statement by adding the
   * key to the WHERE clause
   * 
   * @param key the primary key to add to the WHERE clause
   * @param sql the SQL statement
   * @return a complete SQL statement
   */
  public String getSql(IPrimaryKey key, String sql) {
    StringBuilder builder = new StringBuilder(sql);
    builder.append(key.getSqlWhereExpression(""));
    return builder.toString();
  }

  @Override
  public <P extends IDbDto> int setParams(PreparedStatement stmt, int next,
                                          P dto) {
    next = dto.getPrimaryKey().setParams(stmt, next);
    return next;
  }
}
