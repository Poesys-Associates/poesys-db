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

import com.poesys.db.dto.IDbDto;


/**
 * An interface for a Command pattern class that deletes a set of objects based
 * on a set of parameters contained in a data transfer object (DTO). The
 * implementation uses the parameters to identify the set of objects to delete.
 * The implementation must also delete only when the status of the DTO is
 * DELETED.
 * 
 * @author Robert J. Muller
 * @param <T> the type of DTO to delete
 * @param <P> the type of DTO that contains the parameters for the delete
 */
public interface IDeleteWithParameters<T extends IDbDto, P extends IDbDto> {
  /**
   * Delete objects based on the contents of the input Data Transfer Object
   * containing the selection parameters.
   * 
   * @param connection the database connection with which to delete the data
   * @param parameters the IDto data transfer object containing the parameters
   * @throws SQLException when there is a SQL error with the update
   */
  public void delete(Connection connection, P parameters)
      throws SQLException;

  /**
   * Close any resources allocated by the Command. Note that parameter-based
   * delete does not affect caches at all, as the SQL doesn't search the 
   * cache, just the database, for the objects to delete. This method will
   * usually do nothing.
   */
  public void close();
}
