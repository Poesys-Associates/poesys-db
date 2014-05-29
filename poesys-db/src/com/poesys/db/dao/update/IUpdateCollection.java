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
package com.poesys.db.dao.update;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import com.poesys.db.BatchException;
import com.poesys.db.dto.IDbDto;


/**
 * An interface for a Command pattern class that updates a collection of data
 * transfer objects (DTOs) in the database. The implementation identifies the
 * objects to update based on the primary key contained in the DTO. The concrete
 * implementation should specify what aspects of the object it updates. The
 * implementation should update in the database only if isChanged() is true.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to update
 */
public interface IUpdateCollection<T extends IDbDto> {
  /**
   * An interface for a Command-pattern class that updates the contents of a
   * collection of objects in the database. The data transfer objects (DTOs)
   * must contain values for the primary key and must have all modifiable fields
   * set to reflect the complete, desired state for the object in the database,
   * including any composite aggregate parts belonging to the object.
   * 
   * @param connection the database connection with which to update the data
   * @param dtos the data transfer objects containing the desired state of the
   *          objects
   * @throws SQLException when there is a SQL error with an update
   * @throws BatchException when there is some kind of batch processing error
   */
  public void update(Connection connection, Collection<T> dtos)
      throws SQLException, BatchException;

  /**
   * Is this inserter inserting into a leaf class?
   * 
   * @return true if leaf, false if superclass of a leaf
   */
  public boolean isLeaf();

  /**
   * Set the inserter to be a leaf inserter.
   * 
   * @param isLeaf true for a leaf inserter, false for a superclass inserter
   */
  public void setLeaf(boolean isLeaf);

  /**
   * Close any resources allocated by the Command.
   */
  public void close();
}
