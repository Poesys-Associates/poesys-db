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
package com.poesys.db.dao.query;


import java.sql.SQLException;
import java.util.Collection;

import com.poesys.db.BatchException;
import com.poesys.db.dto.IDbDto;


/**
 * An interface for a Command class that queries a list of data transfer objects
 * (DTOs). The input DTO provides a set of parameter values for the
 * parameterized query.
 * 
 * @see com.poesys.db.dto.IDbDto
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to query
 * @param <S> the type of IDbDto that holds parameters
 * @param <C> the collection type of the set of queried DTOs
 */
public interface IQueryListWithParameters<T extends IDbDto, S extends IDbDto, C extends Collection<T>> {
  /**
   * Query a list of DTOs using a set of parameters.
   * 
   * @param parameters A database DTO containing the query parameters; this can
   *          be either a standard DTO for a database object or a DTO created
   *          specifically to hold a set of parameters
   * @return a List of database DTO objects
   * @throws SQLException when there is a SQL problem with the query
   * @throws BatchException when an operation involving multiple objects fails
   */
  public C query(S parameters) throws SQLException, BatchException;

  /**
   * Set the expiration of objects queried by the query method. This setter
   * allows you to change the expiration from the default value set by the
   * factory.
   * 
   * @param expiration the time in milliseconds until the object expires in the
   *          cache
   */
  public void setExpiration(int expiration);

  /**
   * Close any resources allocated by the Command.
   */
  public void close();
}
