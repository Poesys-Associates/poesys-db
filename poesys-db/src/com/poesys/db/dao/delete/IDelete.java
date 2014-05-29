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

import com.poesys.db.BatchException;
import com.poesys.db.dto.IDbDto;


/**
 * An interface for a Command pattern class that deletes an object in the
 * database. The implementation identifies the object to delete based on the
 * primary key of the object contained in the input data transfer object (DTO).
 * It must also delete on the basis of the isDeleted() flag on the DTO.
 * 
 * @author Robert J. Muller
 * @param <T> the IDbDto class to delete
 */
public interface IDelete<T extends IDbDto> {
  /**
   * Delete the contents of a DTO object in the database. The DTO must contain
   * values for the primary key. The DTO must have any setters required to
   * preprocess nested objects that must be handled before deleting the object.
   * 
   * @param connection the database connection with which to delete the data
   * @param dto the IDto data transfer object containing the primary key
   * @throws SQLException when there is a SQL error with the delete
   * @throws BatchException when there is a problem deleting or updating a
   *           nested collection of linked objects
   */
  public void delete(Connection connection, T dto) throws SQLException,
      BatchException;

  /**
   * Close any resources allocated by the Command.
   */
  public void close();
}
