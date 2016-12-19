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




/**
 * SQL statement specification for deleting objects identified by a query. The concrete
 * subclass contains the SQL statement and an implementation of
 * <code>IDeleteSql.getSql()</code> that calls the <code>getSql</code> method in
 * this class if it needs to add something to the query:
 * 
 * <pre>
 * public class DeleteTestSql extends AbstractDeleteSqlWithQuery {
 * 
 *   private static final String SQL = &quot;DELETE FROM Test&quot;;
 *   
 *   public DeleteTestSql() {
 *     super(SQL);
 *   }
 * 
 *   public String getSql(IPrimaryKey key) {
 *     return super.getSql(key, SQL) + &quot; WHERE testtype = 'Test'&quot;;
 *   }
 * }
 * </pre>
 * 
 * @author Robert J. Muller
 */
public abstract class AbstractDeleteSqlWithQuery implements
    IDeleteSqlWithQuery {
  
  /** the DELETE SQL statement */
  private final String SQL;
  
  /**
   * Create a AbstractDeleteSqlWithQuery object with the SQL string for the
   * DELETE statement.
   * 
   * @param sql the DELETE SQL statement
   */
  public AbstractDeleteSqlWithQuery(String sql) {
    SQL = sql;
  }

  @Override
  public String getSql() {
    return SQL;
  }
}
