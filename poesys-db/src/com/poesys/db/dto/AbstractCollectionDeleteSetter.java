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


import java.util.Collection;

import com.poesys.db.ConstraintViolationException;
import com.poesys.db.DbErrorException;
import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.dao.delete.IDeleteCollection;
import com.poesys.db.dao.delete.IDeleteSql;


/**
 * An abstract implementation of the ISet interface for a Strategy-pattern class
 * that deletes a collection of DTOs as part of a more comprehensive
 * transaction, taking in a connection and not closing it. The abstract methods
 * parameterize the class with objects that the set() method uses in processing
 * the non-batch delete.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to delete
 */
abstract public class AbstractCollectionDeleteSetter<T extends IDbDto> extends
    AbstractSetter<T> implements ISet {

  /** Serial version UID for Serializable object */
  private static final long serialVersionUID = 1L;

  /**
   * Create a AbstractCollectionDeleteSetter object.
   * 
   * @param subsystem the subsystem for the setter
   * @param expiration the time in milliseconds after which the object expires
   *          in a cache that supports expiration
   */
  public AbstractCollectionDeleteSetter(String subsystem, Integer expiration) {
    super(subsystem, expiration);
    setterName = AbstractCollectionDeleteSetter.class.getName();
  }

  @Override
  protected void doSet(PoesysTrackingThread thread) {
    IDaoManager manager = DaoManagerFactory.getManager(subsystem);
    IDaoFactory<T> factory =
      manager.getFactory(getClassName(), subsystem, expiration);
    IDeleteCollection<T> dao = factory.getDeleteCollection(getSql());
    Collection<T> links = getDtos();
    try {
      dao.delete(links);
    } catch (ConstraintViolationException e) {
      throw new DbErrorException(e.getMessage(), thread, e);
    }
  }

  /**
   * Get the class name to use to look up a cached DTO to invalidate.
   * 
   * @return the class name
   */
  abstract protected String getClassName();

  /**
   * Get the SQL object that contains the DELETE statement.
   * 
   * @return the SQL DELETE object
   */
  abstract protected IDeleteSql<T> getSql();

  /**
   * Get the DTOs that contain the data to delete.
   * 
   * @return the collection of DTOs
   */
  abstract protected Collection<T> getDtos();

  public boolean isSet() {
    // Always not set
    return false;
  }
}
