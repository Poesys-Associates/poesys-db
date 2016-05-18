/*
 * Copyright (c) 2011 Poesys Associates. All rights reserved.
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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.poesys.db.BatchException;
import com.poesys.db.ConstraintViolationException;
import com.poesys.db.DbErrorException;
import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dao.query.IKeyQuerySql;
import com.poesys.db.dao.query.IQueryByKey;
import com.poesys.db.pk.IPrimaryKey;


/**
 * An abstract implementation of the ISet interface for a Strategy-pattern class
 * that sets a list element of a data transfer object as part of a
 * de-serialization read-object process. The abstract methods parameterize the
 * class with objects that the set() method uses in processing the query. The
 * class that implements a concrete subclass of this setter implementation calls
 * the set(Connection) method, which queries the objects and then calls the
 * set(List) method to update the internal list. The class accesses a list of
 * primary keys for T objects and queries them individually using a query-by-key
 * DAO. The query-by-key process will look in any cache implemented for the DTO
 * and will retrieve the object from the cache if it's there, or it will query
 * the object from the database if it isn't cached.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to query
 */
abstract public class AbstractListReadSetter<T extends IDbDto> extends
    AbstractSetter<T> implements ISet {

  /** Serial version UID for this Serializable class */
  private static final long serialVersionUID = 1L;

  /**
   * Create a AbstractListSetter object.
   * 
   * @param subsystem the subsystem for the setter
   * @param expiration the time in milliseconds after which the object expires
   *          in a cache that supports expiration
   */
  public AbstractListReadSetter(String subsystem, Integer expiration) {
    super(subsystem, expiration);
  }

  @Override
  public void set(Connection connection) throws SQLException {
    IDaoManager manager = DaoManagerFactory.getManager(subsystem);
    IDaoFactory<T> factory =
      manager.getFactory(getClassName(), subsystem, expiration);
    IQueryByKey<T> dao = factory.getQueryByKey(getSql(), subsystem);
    List<T> list = null;

    // Query using the primary keys.
    if (getPrimaryKeys() != null) {
      list = getEmptyList();
      try {
        for (IPrimaryKey key : getPrimaryKeys()) {
          T dto = dao.queryByKey(key);
          list.add(dto);
        }
      } catch (ConstraintViolationException e) {
        throw new DbErrorException(e.getMessage(), e);
      } catch (BatchException e) {
        throw new DbErrorException(e.getMessage(), e);
      } catch (DtoStatusException e) {
        throw new DbErrorException(e.getMessage(), e);
      }
    }

    // Convert the array list to a thread-safe list.
    list = getThreadSafeList(list);

    set(list);
  }

  /**
   * Get an empty object list for T objects. This will either be a newly
   * allocated list or an existing list in the implementing class which has been
   * cleared. The method optimizes the allocated size of the list by the size of
   * the list of primary keys, if available.
   * 
   * @return the empty list
   */
  private List<T> getEmptyList() {
    List<T> list = null;
    if (isSet()) {
      list = getObjectList();
      if (list != null) {
        list.clear();
      } else if (getPrimaryKeys() != null) {
        list = new ArrayList<T>(getPrimaryKeys().size());
      } else {
        list = new ArrayList<T>();
      }
    } else if (getPrimaryKeys() != null) {
      list = new ArrayList<T>(getPrimaryKeys().size());
    } else {
      list = new ArrayList<T>();
    }
    return list;
  }

  /**
   * Get a thread-safe list based on an input list that is guaranteed not to be
   * null. If the input list has elements, the output list will have the same
   * elements.
   * 
   * @param list the input list, which may be null or empty
   * @return a non-null, thread-safe list (possibly empty)
   */
  private List<T> getThreadSafeList(List<T> list) {
    if (list == null) {
      list = new CopyOnWriteArrayList<T>();
    } else {
      list = new CopyOnWriteArrayList<T>(list);
    }
    return list;
  }

  /**
   * Get the list of primary keys to read.
   * 
   * @return a List of IPrimaryKey objects
   */
  abstract protected List<IPrimaryKey> getPrimaryKeys();

  /**
   * Get the list of objects. This will usually be null, as it is transient and
   * won't be constructed by the default read-object process.
   * 
   * @return the current object list
   */
  abstract protected List<T> getObjectList();

  /**
   * Get the class name to use to look up a cached DTO.
   * 
   * @return the class name
   */
  abstract protected String getClassName();

  /**
   * Get the SQL object that contains the key query.
   * 
   * @return the SQL query object
   */
  abstract protected IKeyQuerySql<T> getSql();

  /**
   * Set the data member with the list.
   * 
   * @param list the list to set into the data member
   */
  abstract protected void set(List<T> list);

  @Override
  public boolean isSet() {
    // Check the list of objects; if it isn't null, it's been set.
    return getObjectList() != null;
  }
}
