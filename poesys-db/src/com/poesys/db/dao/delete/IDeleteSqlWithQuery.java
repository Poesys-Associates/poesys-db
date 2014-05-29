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




/**
 * <p>
 * An interface that defines the set of operations required by an
 * IDeleteQuery object to build a SQL DELETE statement that has no parameters
 * and deletes a set of objects based on a query.
 * </p>
 * 
 * @see IDeleteQuery
 * 
 * @author Robert J. Muller
 */
public interface IDeleteSqlWithQuery {
  /**
   * <p>
   * Get the SQL DELETE statement.
   * </p>
   * @return the SQL for the DELETE statement
   */
  String getSql();
}
