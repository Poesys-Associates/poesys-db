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


import java.util.ArrayList;
import java.util.Collection;

import com.poesys.db.ConstraintViolationException;
import com.poesys.db.DbErrorException;
import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.dao.query.IQueryListWithParameters;


/**
 * An abstract implementation of the ISet interface for a Strategy-pattern class
 * that sets a list element of a data transfer object as a lazy-loading
 * operation, taking in a connection and closing it. The abstract methods
 * parameterize the class with objects that the set() method uses in processing
 * the query.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to query
 * @param <P> the type of IDbDto that contains parameters for the query
 * @param <C> the Collection type of the set of DTOs to set into the object
 */
abstract public class AbstractLazyListSetter<T extends IDbDto, P extends IDbDto, C extends Collection<T>>
    extends AbstractListSetter<T, P, C> {

  /** Serial version UID for Serializable object */
  private static final long serialVersionUID = 1L;

  /**
   * Create a AbstractLazyListSetter object.
   * 
   * @param subsystem the subsystem for the setter
   * @param expiration the time in milliseconds after which the object expires
   *          in a cache that supports expiration
   */
  public AbstractLazyListSetter(String subsystem, Integer expiration) {
    super(subsystem, expiration);
    setterName = AbstractLazyListSetter.class.getName();
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void doSet(PoesysTrackingThread thread) {
    IDaoManager manager = null;

    try {
      manager = DaoManagerFactory.getManager(subsystem);
      IDaoFactory<T> factory =
        manager.getFactory(getClassName(), subsystem, expiration);
      IQueryListWithParameters<T, P, C> dao =
        factory.getQueryListWithParameters(getSql(), subsystem, getFetchSize());
      // Query using the outer object as parameters (that is, the parent key).
      C list = dao.query(getParametersDto());
      if (list == null) {
        list = (C)new ArrayList<T>();
      }
      set(list);
    } catch (ConstraintViolationException e) {
      throw new DbErrorException(e.getMessage(), thread, e);
    }
  }
}
