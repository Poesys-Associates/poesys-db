/*
 * Copyright (c) 2009 Poesys Associates. All rights reserved.
 */
package com.poesys.db.dto;


import java.sql.Connection;
import java.sql.SQLException;

import com.poesys.db.BatchException;
import com.poesys.db.ConstraintViolationException;
import com.poesys.db.DbErrorException;
import com.poesys.db.connection.IConnectionFactory.DBMS;
import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dao.update.IUpdate;
import com.poesys.db.dao.update.IUpdateSql;
import com.poesys.db.dto.IDbDto.Status;


/**
 * An abstract implementation of the ISet interface for a Strategy-pattern class
 * that updates a single DTO as part of a more comprehensive transaction, taking
 * in a connection and not closing it.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to update
 */
abstract public class AbstractUpdateSetter<T extends IDbDto> extends
    AbstractSetter<T> implements ISet {

  /** Serial version UID for Serializable object */
  private static final long serialVersionUID = 1L;

  /**
   * Create a AbstractUpdateSetter object.
   * 
   * @param subsystem the subsystem for the setter
   * @param dbms the type of DBMS to which to connect
   * @param expiration the time in milliseconds after which the object expires
   *          in a cache that supports expiration
   */
  public AbstractUpdateSetter(String subsystem, DBMS dbms, Integer expiration) {
    super(subsystem, dbms, expiration);
  }

  @Override
  public void set(Connection connection) throws SQLException {
    IDaoManager manager = DaoManagerFactory.getManager(subsystem);
    IDaoFactory<T> factory =
      manager.getFactory(getClassName(), subsystem, expiration);
    IUpdate<T> dao = factory.getUpdate(getSql());
    try {
      dao.update(connection, getDto());
    } catch (ConstraintViolationException e) {
      throw new DbErrorException(e.getMessage(), e);
    } catch (BatchException e) {
      throw new DbErrorException(e.getMessage(), e);
    } catch (DtoStatusException e) {
      throw new DbErrorException(e.getMessage(), e);
    }
  }

  /**
   * Get the class name to use to look up a cached DTO to invalidate.
   * 
   * @return the class name
   */
  abstract protected String getClassName();

  /**
   * Get the SQL object that contains the UPDATE statement.
   * 
   * @return the SQL UPDATE object
   */
  abstract protected IUpdateSql<T> getSql();

  /**
   * Get the DTO to update.
   * 
   * @return a DTO
   */
  abstract protected T getDto();

  public boolean isSet() {
    // set if not CHANGED
    return Status.CHANGED.compareTo(getDto().getStatus()) != 0;
  }
}
