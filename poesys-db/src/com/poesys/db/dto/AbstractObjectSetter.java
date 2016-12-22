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


import com.poesys.db.ConstraintViolationException;
import com.poesys.db.DbErrorException;
import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.dao.query.IKeyQuerySql;
import com.poesys.db.dao.query.IQueryByKey;
import com.poesys.db.pk.IPrimaryKey;


/**
 * An abstract implementation of the ISet interface for a Strategy-pattern class
 * that sets an object element of a data transfer object as part of a more
 * comprehensive transaction, taking in a connection and not closing it. The
 * abstract methods parameterize the class with objects that the set() method
 * uses in processing the query.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to query
 */
abstract public class AbstractObjectSetter<T extends IDbDto> extends
    AbstractSetter<T> implements ISet {

  /** Serial version UID for Serializable object */
  private static final long serialVersionUID = 1L;

  /**
   * Create a AbstractObjectSetter object.
   * 
   * @param subsystem the subsystem for the setter
   * @param expiration the time in milliseconds after which the object expires
   *          in a cache that supports expiration
   */
  public AbstractObjectSetter(String subsystem, Integer expiration) {
    super(subsystem, expiration);
    setterName = AbstractObjectSetter.class.getName();
  }

  @Override
  protected void doSet(PoesysTrackingThread thread) {
    // No isSet() check here, always query the object.
    IDaoManager manager = DaoManagerFactory.getManager(subsystem);
    IDaoFactory<T> factory =
      manager.getFactory(getClassName(), subsystem, expiration);
    IQueryByKey<T> dao = factory.getQueryByKey(getSql(), subsystem);
    T dto = null;

    if (getKey() != null) {
      try {
        dto = dao.queryByKey(getKey());
        set(dto);
      } catch (ConstraintViolationException e) {
        throw new DbErrorException(e.getMessage(), thread, e);
      }
    }
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
  abstract protected IKeyQuerySql<T> getSql();

  /**
   * Get the primary key to use in the query.
   * 
   * @return the primary key
   */
  abstract protected IPrimaryKey getKey();

  /**
   * Set the data member with the DTO.
   * 
   * @param dto the DTO to set into the data member
   */
  abstract protected void set(T dto);
}
