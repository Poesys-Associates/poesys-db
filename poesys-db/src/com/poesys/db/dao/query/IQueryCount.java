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
 * 
 */
package com.poesys.db.dao.query;


import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;

import com.poesys.db.InvalidParametersException;
import com.poesys.db.dto.IDbDto;


/**
 * An interface for Command data access objects (DAOs) that query a count of
 * some kind based on a set of parameters contained in an IDto of type P
 * 
 * @see com.poesys.db.dto.IDbDto
 * @see QueryCount
 * 
 * @author Robert J. Muller
 * @param <P> the DTO type that serves as a parameter container
 */
public interface IQueryCount<P extends IDbDto> {
  /**
   * Query a count of some kind from the database.
   * 
   * @param connection the database connection with which to query
   * @param parameters a DTO that contains any parameters to the query
   * @return a BigInteger count
   * @throws SQLException when there is a problem with the query
   * @throws InvalidParametersException when the input parameters are not valid
   */
  public BigInteger queryCount(Connection connection, P parameters)
      throws SQLException, InvalidParametersException;
}
