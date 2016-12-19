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


import java.util.List;

import com.poesys.db.BatchException;
import com.poesys.db.dto.IDbDto;


/**
 * An interface for a Command class that queries a list of data transfer objects
 * (DTOs)
 * 
 * @see com.poesys.db.dto.IDbDto
 * @see QueryList
 * 
 * @author Robert J. Muller
 * @param <T> the type of database-layer IDbDto to query
 */
public interface IQueryList<T extends IDbDto> {
  /**
   * Query a list of DTOs.
   * 
   * @return a List of DTO objects
   * @throws BatchException when a multiple-object operation fails
   */
  public List<T> query() throws BatchException;

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
   * Close any allocated resources.
   */
  public void close();
}
