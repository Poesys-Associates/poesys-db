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


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import com.poesys.db.BatchException;
import com.poesys.db.dto.IDbDto;


/**
 * An interface for a Command pattern class that deletes a collection of objects
 * from the database. The implementation identifies the objects to delete based
 * on the primary key contained in the data transfer object (DTO). The
 * implementation only deletes a DTO if its status is DELETED.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to delete
 */
public interface IDeleteCollection<T extends IDbDto> {
  /**
   * Delete the contents of a collection of objects in the database. The DTOs
   * must contain values for the primary key.
   * 
   * @param connection the database connection with which to delete the data
   * @param dtos the data transfer objects containing the primary keys
   * @throws SQLException when there is a SQL error with an update
   * @throws BatchException when there is some kind of batch processing error
   */
  public void delete(Connection connection, Collection<T> dtos)
      throws SQLException, BatchException;

  /**
   * Close any resources allocated by the Command.
   */
  public void close();
}
