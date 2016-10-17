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
package com.poesys.db.dto;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.poesys.db.BatchException;
import com.poesys.db.ConstraintViolationException;
import com.poesys.db.DbErrorException;
import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dao.query.IParameterizedQuerySql;
import com.poesys.db.dao.query.IQueryListWithParameters;


/**
 * An abstract implementation of the ISet interface for a Strategy-pattern class
 * that sets a list element of a data transfer object as part of a more
 * comprehensive transaction, taking in a connection and not closing it. The
 * abstract methods parameterize the class with objects that the set() method
 * uses in processing the query. The class that implements a concrete subclass
 * of this setter implementation calls the set(Connection) method, which queries
 * the objects and then calls the set(List) method to update the internal list.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to query
 * @param <S> the type of IDbDto that contains parameters for the query
 * @param <C> the Collection type of the set of DTOs to set into the object
 */
abstract public class AbstractListSetter<T extends IDbDto, S extends IDbDto, C extends Collection<T>>
    extends AbstractSetter<T> implements ISet {
  /** Logger for debugging */
  private static final Logger logger =
    Logger.getLogger(AbstractListSetter.class);

  /** Serial version UID for Serializable object */
  private static final long serialVersionUID = 1L;

  /**
   * Create a AbstractListSetter object.
   * 
   * @param subsystem the subsystem for the setter
   * @param expiration the time in milliseconds after which the object expires
   *          in a cache that supports expiration
   */
  public AbstractListSetter(String subsystem, Integer expiration) {
    super(subsystem, expiration);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void set(Connection connection) throws SQLException {
    IDaoManager manager = DaoManagerFactory.getManager(subsystem);
    IDaoFactory<T> factory =
      manager.getFactory(getClassName(), subsystem, expiration);
    IQueryListWithParameters<T, S, C> dao =
      factory.getQueryListWithParameters(getSql(), subsystem, getFetchSize());
    // Query using the outer object as parameters (that is, the parent key).
    C list = null;
    try {
      list = dao.query(getParametersDto());
    } catch (ConstraintViolationException e) {
      throw new DbErrorException(e.getMessage(), e);
    } catch (BatchException e) {
      throw new DbErrorException(e.getMessage(), e);
    } catch (DtoStatusException e) {
      throw new DbErrorException(e.getMessage(), e);
    } catch (Throwable t) {
      logger.error("Unexpected exception during list setting", t);
    }
    if (list == null) {
      list = (C)new ArrayList<T>();
    }
    set(list);
  }

  /**
   * Get the class name to use to look up a cached DTO.
   * 
   * @return the class name
   */
  abstract protected String getClassName();

  /**
   * Get the SQL object that contains the parameterized query.
   * 
   * @return the SQL query object
   */
  abstract protected IParameterizedQuerySql<T, S> getSql();

  /**
   * Get the JDBC fetch size, the number of rows to fetch in a single fetch
   * operation and cache; typically this is the number of rows expected for the
   * list, but for very long lists it can be a partial subset such as 1,000 or
   * 10,000.
   * 
   * @return the fetch size
   */
  abstract protected int getFetchSize();

  /**
   * Get the DTO that contains the parameters for the query.
   * 
   * @return the parameters DTO
   */
  abstract protected S getParametersDto();

  /**
   * Set the data member with the list.
   * 
   * @param list the list to set into the data member
   */
  abstract protected void set(C list);

  abstract public boolean isSet();
}
