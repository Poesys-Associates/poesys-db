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
package com.poesys.db.dao;


import java.util.Collection;

import com.poesys.db.dao.delete.IDelete;
import com.poesys.db.dao.delete.IDeleteBatch;
import com.poesys.db.dao.delete.IDeleteCollection;
import com.poesys.db.dao.delete.IDeleteSql;
import com.poesys.db.dao.delete.IDeleteSqlWithParameters;
import com.poesys.db.dao.delete.IDeleteWithParameters;
import com.poesys.db.dao.insert.IInsert;
import com.poesys.db.dao.insert.IInsertBatch;
import com.poesys.db.dao.insert.IInsertCollection;
import com.poesys.db.dao.insert.IInsertSql;
import com.poesys.db.dao.query.IKeyListQuerySql;
import com.poesys.db.dao.query.IKeyQuerySql;
import com.poesys.db.dao.query.IParameterizedQuerySql;
import com.poesys.db.dao.query.IQueryByKey;
import com.poesys.db.dao.query.IQueryList;
import com.poesys.db.dao.query.IQueryListWithParameters;
import com.poesys.db.dao.query.IQuerySql;
import com.poesys.db.dao.update.IUpdate;
import com.poesys.db.dao.update.IUpdateBatch;
import com.poesys.db.dao.update.IUpdateCollection;
import com.poesys.db.dao.update.IUpdateSql;
import com.poesys.db.dao.update.IUpdateWithParameters;
import com.poesys.db.dto.IDbDto;


/**
 * An interface for a generic data access object (DAO) factory. The IDaoFactory
 * lets you generate a DAO object appropriate to your desired framework.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto that the DAOs handle
 */
public interface IDaoFactory<T extends IDbDto> {

  /**
   * Generate a QueryByKey DAO that queries a single object using a SQL
   * key-query specification.
   * 
   * @param sql the SQL key-query specification
   * @param subsystem the subsystem that owns the object to query
   * @return the query DAO
   */
  IQueryByKey<T> getQueryByKey(IKeyQuerySql<T> sql, String subsystem);

  /**
   * Generate a QueryByKey DAO that queries a single object using a SQL
   * key-query specification without looking in any cache.
   * 
   * @param sql the SQL key-query specification
   * @param subsystem the subsystem that owns the object to query
   * @return the query DAO
   */
  IQueryByKey<T> getDatabaseQueryByKey(IKeyQuerySql<T> sql, String subsystem);

  /**
   * Generate a QueryList DAO that queries a list of objects using a
   * non-parameterized SQL query specification. Set the number of rows to allow
   * fetching more than one row at a time, usually to a value that trades off
   * memory use and network access.
   * 
   * @param sql the SQL query specification
   * @param subsystem the subsystem that owns the objects to query
   * @param rows the number of rows to fetch at once, optimizes the query
   * @return the query DAO
   */
  IQueryList<T> getQueryList(IQuerySql<T> sql, String subsystem, int rows);

  /**
   * Generate a QueryList DAO that queries a list of objects using a
   * non-parameterized SQL query specification. Set the number of rows to allow
   * fetching more than one row at a time, usually to a value that trades off
   * memory use and network access.
   * 
   * @param sql the SQL query specification
   * @param subsystem the subsystem that owns the objects to query
   * @param rows the number of rows to fetch at once, optimizes the query
   * @return the query DAO
   */
  IQueryList<T> getQueryListWithKeyList(IKeyListQuerySql<T> sql,
                                        String subsystem, int rows);

  /**
   * Generate a QueryListWithParameters DAO that queries a list of objects based
   * on a set of parameters using a SQL query specification that contains
   * parameters and parameter-binding capabilities.
   * 
   * @param <S> the type of IDbDto that contains parameters
   * @param <C> the collection type of the set of queried DTOs
   * 
   * @param sql the SQL query specification
   * @param subsystem the subsystem that owns the objects to query
   * @param rows the number of rows to fetch at once, optimizes the results
   *          fetching
   * @return the query DAO
   */
  <S extends IDbDto, C extends Collection<T>> IQueryListWithParameters<T, S, C> getQueryListWithParameters(IParameterizedQuerySql<T, S> sql,
                                                                                                           String subsystem,
                                                                                                           int rows);

  /**
   * Generate an IInsert DAO that inserts a single object using a SQL insert
   * specification. The DAO may generate a key as specified if the object has an
   * identity primary key. The database generates the key, then the DAO
   * retrieves it using the key-retrieval feature of JDBC.
   * 
   * @param sql the SQL INSERT specification
   * @param key true if you want to supply a key, false if the database
   *          management system generates the key and JDBC returns it separately
   * @return the insert DAO
   */
  IInsert<T> getInsert(IInsertSql<T> sql, Boolean key);

  /**
   * Generate an IInsertBatch DAO that inserts a batched list of objects using a
   * SQL insert specification. The batch processing does not enable key
   * generation through identity keys.
   * 
   * @param sql the SQL INSERT specification
   * @return the insert DAO
   */
  IInsertBatch<T> getInsertBatch(IInsertSql<T> sql);

  /**
   * Generate an IInsertCollection DAO that inserts a collection of objects
   * using a SQL insert specification. The DAO may generate keys as specified if
   * the object has an identity primary key.
   * 
   * @param sql the SQL INSERT specification
   * @param key true if you want to supply a key, false if you want the key
   *          generated (identity keys)
   * @return the insert DAO
   */
  IInsertCollection<T> getInsertCollection(IInsertSql<T> sql, Boolean key);

  /**
   * Generate an IUpdate DAO that updates a single object based on its primary
   * key using a SQL update specification.
   * 
   * @param sql the SQL UPDATE specification
   * @return the update DAO
   */
  IUpdate<T> getUpdate(IUpdateSql<T> sql);

  /**
   * Generate an IUpdateWithParameters DAO that updates a set of objects based
   * on a set of parameters using a SQL update specification.
   * 
   * @param sql the SQL UPDATE specification
   * @return the update DAO
   */
  IUpdateWithParameters<T> getUpdateWithParameters(IUpdateSql<T> sql);

  /**
   * Generate an IUpdateBatch DAO that updates a batched list of objects using a
   * SQL update specification.
   * 
   * @param sql the SQL UPDATE specification
   * @return the update DAO
   */
  IUpdateBatch<T> getUpdateBatch(IUpdateSql<T> sql);

  /**
   * Generate an IUpdateCollection DAO that updates a collection of objects
   * using a SQL update specification.
   * 
   * @param sql the SQL UPDATE specification
   * @return the update DAO
   */
  IUpdateCollection<T> getUpdateCollection(IUpdateSql<T> sql);

  /**
   * Generate an IDelete DAO that deletes a single object using a SQL delete
   * specification that deletes an object based on the primary key.
   * 
   * @param sql the SQL DELETE specification
   * @return the delete DAO
   */
  IDelete<T> getDelete(IDeleteSql<T> sql);

  /**
   * Generate an IDeleteWithParameters DAO that deletes a set of objects based
   * on a set of parameters.
   * 
   * @param sql the SQL DELETE specification
   * @return the delete DAO
   * @param <P> the type of DTO that contains the identifying parameters
   */
  <P extends IDbDto> IDeleteWithParameters<T, P> getDeleteWithParameters(IDeleteSqlWithParameters<T, P> sql);

  /**
   * Generate an IDeleteBatch DAO that deletes a batched list of objects.
   * 
   * @param sql the SQL DELETE specification
   * @return the delete DAO
   */
  IDeleteBatch<T> getDeleteBatch(IDeleteSql<T> sql);

  /**
   * Generate an IDeleteCollection DAO that deletes a collection of objects (no
   * batching).
   * 
   * @param sql the SQL DELETE specification
   * @return the delete DAO
   */
  IDeleteCollection<T> getDeleteCollection(IDeleteSql<T> sql);

  /**
   * Clear any resources used by the factory. In the caching implementation of
   * this interface, for example, this method clears the cache.
   */
  void clear();
}