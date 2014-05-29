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

import com.poesys.db.dto.IDbDto;


/**
 * An interface for a Command-pattern class that updates a database based on a
 * set of parameters contained in a data transfer object (DTO). The
 * implementation may use the parameters to identify the set of objects to
 * update or as the values with which to update columns in the database. The
 * implementation should update in the database only if isChanged() is true.
 * 
 * @author Robert J. Muller
 * @param <P> The kind of DTO that contains the parameters with which to update
 */
public interface IUpdateWithParameters<P extends IDbDto> {
  /**
   * Update the database based on the contents of the input DTO containing the
   * selection parameters and update data.
   * 
   * @param connection the database connection with which to update the data
   * @param parameters the DTO containing the parameters
   * @throws SQLException when there is a SQL error with the update
   */
  public void update(Connection connection, P parameters) throws SQLException;

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
}
