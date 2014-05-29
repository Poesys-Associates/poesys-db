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
 * A test implementation of the ISql interface that contains a generic
 * TRUNCATE statement.
 * 
 * @author Robert J. Muller
 */
public class TruncateTableSql implements ISql {
  /** Basic part of the TRUNCATE statement */
  StringBuilder sql = new StringBuilder("TRUNCATE TABLE ");
  
  /**
   * Create a TruncateTableSql object.
   * @param tableName the name of the table to truncate
   */
  public TruncateTableSql(String tableName) {
    sql.append(tableName);
  }

  /* (non-Javadoc)
   * @see com.poesys.db.dao.ddl.ISql#getSql()
   */
  public String getSql() {
    return sql.toString();
  }
}
