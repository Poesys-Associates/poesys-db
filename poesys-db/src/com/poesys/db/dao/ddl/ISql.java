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
package com.poesys.db.dao.ddl;

/**
 * <p>
 * An interface that defines the set of operations required by an ISql object to
 * build a SQL DDL statement, a simple SQL statement that has no parameters and
 * retrieves no data.
 * </p>
 * <p>
 * An implemention of this interface should have a static string containing the
 * SQL statement. Implementations may create generic statements with parts
 * supplied through the constructor such as table names or column names. This
 * strategy permits you to create generic SQL statements that you can
 * parameterize and execute at runtime. Note that most such SQL causes the
 * current transaction to commit. Also note that, since much DDL is
 * DBMS-specific, you will need to create different versions of the SQL
 * statement and use a factory to generate the correct SQL for the target DBMS.
 * </p>
 * <p>
 * This example represents a simple statement that truncates the TestNatural
 * table.
 * </p>
 * 
 * <pre><code>
 * public class TruncateSql implements ISql {
 *   String tableName;
 * 
 *   public TruncateSql(String tableName) {
 *     this.tableName = tableName;
 *   }
 * 
 *   private static final String SQL = &quot;TRUNCATE &quot;;
 * 
 *   public String getSql() {
 *     return SQL + tableName;
 *   }
 * }
 * </code></pre>
 * 
 * @author Robert J. Muller
 */

public interface ISql {
  /**
   * Get the SQL statement.
   * 
   * @return a SQL statement
   */
  String getSql();
}
