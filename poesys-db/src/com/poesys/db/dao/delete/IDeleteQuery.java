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
 * An interface for a Command pattern class that deletes a set of objects
 * identified by a query (a SELECT statement). This command simply deletes the
 * objects from the database, it does not handle caches, which must be updated
 * by the client or by the overall system.
 * 
 * @author Robert J. Muller
 */
public interface IDeleteQuery {
  /**
   * Delete the contents of a DTO object in the database. The DTO must contain
   * values for the primary key. The DTO must have any setters required to
   * preprocess nested objects that must be handled before deleting the object.
   */
  public void delete();

  /**
   * Close any resources allocated by the Command.
   */
  public void close();
}
