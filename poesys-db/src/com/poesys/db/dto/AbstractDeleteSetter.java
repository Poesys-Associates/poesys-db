/*
 * Copyright (c) 2009 Poesys Associates. All rights reserved.
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
import com.poesys.db.dao.delete.IDelete;
import com.poesys.db.dao.delete.IDeleteSql;
import com.poesys.db.dto.IDbDto.Status;


/**
 * An abstract implementation of the ISet interface for a Strategy-pattern class
 * that deletes a single DTO as part of a more comprehensive transaction, taking
 * in a connection and not closing it.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to delete
 */
abstract public class AbstractDeleteSetter<T extends IDbDto> extends
    AbstractSetter<T> implements ISet {

  /** Serial version UID for Serializable object */
  private static final long serialVersionUID = 1L;

  /**
   * Create a AbstractDeleteSetter object.
   * 
   * @param subsystem the subsystem for the setter
   * @param expiration the time in milliseconds after which the object expires
   *          in a cache that supports expiration
   */
  public AbstractDeleteSetter(String subsystem, Integer expiration) {
    super(subsystem, expiration);
    setterName = AbstractDeleteSetter.class.getName();
  }

  @Override
  protected void doSet(PoesysTrackingThread thread) {
    IDaoManager manager = DaoManagerFactory.getManager(subsystem);
    IDaoFactory<T> factory =
      manager.getFactory(getClassName(), subsystem, expiration);
    IDelete<T> dao = factory.getDelete(getSql());
    try {
      T dto = getDto();
      dao.delete(dto);
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
   * Get the DTO to delete.
   * 
   * @return a DTO
   */
  abstract protected T getDto();

  public boolean isSet() {
    return Status.DELETED.compareTo(getDto().getStatus()) == 0;
  }
}
