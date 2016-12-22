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
import com.poesys.db.dao.query.IQueryByKey;


/**
 * An abstract implementation of the ISet interface for a Strategy-pattern class
 * that sets an object within a data transfer object as a lazy-loading
 * operation, taking in a connection and closing it. The abstract methods
 * parameterize the class with objects that the set() method uses in processing
 * the query.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to query
 */
abstract public class AbstractLazyObjectSetter<T extends IDbDto> extends
    AbstractObjectSetter<T> {

  /** Serial version UID for Serializable object */
  private static final long serialVersionUID = 1L;

  /**
   * Create a AbstractLazyObjectSetter object.
   * 
   * @param subsystem the subsystem for the setter
   * @param expiration the time in milliseconds after which the object expires
   *          in a cache that supports expiration
   */
  public AbstractLazyObjectSetter(String subsystem, Integer expiration) {
    super(subsystem, expiration);
    setterName = AbstractLazyObjectSetter.class.getName();
  }

  @Override
  protected void doSet(PoesysTrackingThread thread) {
    try {
      IDaoManager manager = DaoManagerFactory.getManager(subsystem);
      IDaoFactory<T> factory =
        manager.getFactory(getClassName(), subsystem, expiration);
      IQueryByKey<T> dao = factory.getQueryByKey(getSql(), subsystem);
      // If there is a key, query using it; otherwise, just return as there is
      // nothing to query and set.
      if (getKey() != null) {
        // Query using the outer object as parameters (that is, the parent
        // key).
        T dto = dao.queryByKey(getKey());
        set(dto);
      }
    } catch (ConstraintViolationException e) {
      throw new DbErrorException(e.getMessage(), thread, e);
    }
  }
}
