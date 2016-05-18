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

import com.poesys.db.BatchException;
import com.poesys.db.NoPrimaryKeyException;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * An interface for Command-pattern data access objects (DAOs) that query by a
 * key within a data transfer object (DTO)
 * 
 * @see com.poesys.db.dto.IDbDto
 * @see com.poesys.db.pk.IPrimaryKey
 * @see QueryByKey
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to query
 */
public interface IQueryByKey<T extends IDbDto> {
  /**
   * Query a DTO by the key of the object expressed through the same type of
   * DTO.
   * 
   * @param key the primary key with which to query an object; generic parameter
   *          must be a concrete implementation of IPrimaryKey
   * @return a new DTO with the queried object
   * @throws SQLException when there is a problem with the query
   * @throws NoPrimaryKeyException when the input DTO does not contain a valid
   *           key
   * @throws BatchException when a nested set of objects has a problem with a
   *           batched query
   */
  public T queryByKey(IPrimaryKey key) throws SQLException,
      NoPrimaryKeyException, BatchException;

  /**
   * Set the expiration of objects queried by the queryByKey method. This setter
   * allows you to change the expiration from the default value set by the
   * factory.
   * 
   * @param expiration the time in milliseconds until the object expires in the
   *          cache
   */
  public void setExpiration(int expiration);

  /**
   * Close any inter-query resources allocated by the Command.
   */
  public void close();
}
