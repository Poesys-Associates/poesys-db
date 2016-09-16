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
package com.poesys.db.dao;


import java.util.Collection;

import com.poesys.db.dao.delete.DeleteMemcachedBatchByKey;
import com.poesys.db.dao.delete.DeleteMemcachedByKey;
import com.poesys.db.dao.delete.DeleteMemcachedCollectionByKey;
import com.poesys.db.dao.delete.DeleteWithParameters;
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
import com.poesys.db.dao.insert.InsertMemcached;
import com.poesys.db.dao.insert.InsertMemcachedBatch;
import com.poesys.db.dao.insert.InsertMemcachedCollection;
import com.poesys.db.dao.insert.InsertMemcachedNoKey;
import com.poesys.db.dao.insert.InsertMemcachedNoKeyCollection;
import com.poesys.db.dao.query.IKeyListQuerySql;
import com.poesys.db.dao.query.IKeyQuerySql;
import com.poesys.db.dao.query.IParameterizedQuerySql;
import com.poesys.db.dao.query.IQueryByKey;
import com.poesys.db.dao.query.IQueryList;
import com.poesys.db.dao.query.IQueryListWithParameters;
import com.poesys.db.dao.query.IQuerySql;
import com.poesys.db.dao.query.QueryDatabaseMemcachedByKey;
import com.poesys.db.dao.query.QueryMemcachedByKey;
import com.poesys.db.dao.query.QueryMemcachedList;
import com.poesys.db.dao.query.QueryMemcachedListWithKeyList;
import com.poesys.db.dao.query.QueryMemcachedListWithParameters;
import com.poesys.db.dao.update.IUpdate;
import com.poesys.db.dao.update.IUpdateBatch;
import com.poesys.db.dao.update.IUpdateCollection;
import com.poesys.db.dao.update.IUpdateSql;
import com.poesys.db.dao.update.IUpdateWithParameters;
import com.poesys.db.dao.update.UpdateMemcachedBatchByKey;
import com.poesys.db.dao.update.UpdateMemcachedByKey;
import com.poesys.db.dao.update.UpdateMemcachedCollectionByKey;
import com.poesys.db.dao.update.UpdateWithParameters;
import com.poesys.db.dto.IDbDto;


/**
 * <p>
 * Implements the IDaoFactory interface for objects cached in a memcached
 * distributed object cache.
 * </p>
 * <p>
 * The memcached distributed cache is an external cache server that caches
 * serialized Java objects. The implementation in Poesys/DB is based on the DTO
 * subsystem, the package that contains a set of DTO classes. There is a
 * stereotype on the Subsystem object that tags it as Distributed, which causes
 * code generation to create all the appropriate code to cache DTOs in the
 * external cache. As well, memcached supports object expiration in
 * milliseconds, and you can set that through the constructor on this factory.
 * There should be a tag on the Persistent stereotype that specifies the desired
 * expiration time in milliseconds for all the DTOs of a given type.
 * </p>
 * 
 * @see IDaoFactory
 * @see DaoDirectFactory
 * @see CacheDaoManager
 * @see MemcachedDaoManager
 * 
 * @author Robert J. Muller
 * @param <T> the type of database DTO to cache
 */
public class DaoMemcachedFactory<T extends IDbDto> implements IDaoFactory<T> {
  /** the name of the subsystem containing class T */
  private final String subsystem;
  /** The time in milliseconds an object remains in the cache */
  private final Integer expiration;

  /**
   * Create a DaoMemcachedFactory object based on a cache name, the subsystem
   * name for the subsystem containing class T, and the memcached expiration
   * time for objects in the cache of type T.
   * 
   * @param name the cache name (usually the fully qualified class name of the
   *          cached objects)
   * @param subsystem the name of the subsystem containing class T
   * @param expiration the time in milliseconds the object remains in the cache;
   *          default is Integer.MAX_VALUE
   */
  public DaoMemcachedFactory(String name, String subsystem, Integer expiration) {
    this.subsystem = subsystem;
    this.expiration = expiration == null ? Integer.MAX_VALUE : expiration;
  }

  @Override
  public IQueryByKey<T> getQueryByKey(IKeyQuerySql<T> sql, String subsystem) {
    return new QueryMemcachedByKey<T>(sql, subsystem, expiration);
  }

  @Override
  public IQueryByKey<T> getDatabaseQueryByKey(IKeyQuerySql<T> sql,
                                              String subsystem) {
    return new QueryDatabaseMemcachedByKey<T>(sql, subsystem, expiration);
  }

  @Override
  public IQueryList<T> getQueryList(IQuerySql<T> sql, String subsystem, int rows) {
    return new QueryMemcachedList<T>(sql, subsystem, expiration, rows);
  }

  @Override
  public IQueryList<T> getQueryListWithKeyList(IKeyListQuerySql<T> sql,
                                               String subsystem, int rows) {
    return new QueryMemcachedListWithKeyList<T>(sql,
                                                subsystem,
                                                expiration,
                                                rows);
  }

  @Override
  public <S extends IDbDto, C extends Collection<T>> IQueryListWithParameters<T, S, C> getQueryListWithParameters(IParameterizedQuerySql<T, S> sql,
                                                                                                                  String subsystem,
                                                                                                                  int rows) {
    return new QueryMemcachedListWithParameters<T, S, C>(sql,
                                                         subsystem,
                                                         expiration,
                                                         rows);
  }

  @Override
  public IDelete<T> getDelete(IDeleteSql<T> sql) {
    DeleteMemcachedByKey<T> deleter = null;
    if (sql != null) {
      deleter = new DeleteMemcachedByKey<T>(sql, subsystem);
    }
    return deleter;
  }

  @Override
  public IDeleteBatch<T> getDeleteBatch(IDeleteSql<T> sql) {
    DeleteMemcachedBatchByKey<T> deleter = null;
    if (sql != null) {
      deleter = new DeleteMemcachedBatchByKey<T>(sql, subsystem);
    }
    return deleter;
  }

  @Override
  public IDeleteCollection<T> getDeleteCollection(IDeleteSql<T> sql) {
    DeleteMemcachedCollectionByKey<T> deleter = null;
    if (sql != null) {
      deleter = new DeleteMemcachedCollectionByKey<T>(sql, subsystem);
    }
    return deleter;
  }

  @Override
  public <P extends IDbDto> IDeleteWithParameters<T, P> getDeleteWithParameters(IDeleteSqlWithParameters<T, P> sql) {
    // No cache involved with this delete--not related to DTOs
    DeleteWithParameters<T, P> deleter = null;
    if (sql != null) {
      deleter = new DeleteWithParameters<T, P>(sql);
    }
    return deleter;
  }

  @Override
  public IInsert<T> getInsert(IInsertSql<T> sql, Boolean key) {
    return key ? new InsertMemcached<T>(sql, subsystem, expiration)
        : new InsertMemcachedNoKey<T>(sql, subsystem, expiration);
  }

  @Override
  public IInsertBatch<T> getInsertBatch(IInsertSql<T> sql) {
    return new InsertMemcachedBatch<T>(sql, subsystem, expiration);
  }

  @Override
  public IInsertCollection<T> getInsertCollection(IInsertSql<T> sql, Boolean key) {
    return key ? new InsertMemcachedCollection<T>(sql, subsystem, expiration)
        : new InsertMemcachedNoKeyCollection<T>(sql, subsystem, expiration);
  }

  @Override
  public IUpdate<T> getUpdate(IUpdateSql<T> sql) {
    UpdateMemcachedByKey<T> updater = null;
    if (sql != null) {
      updater = new UpdateMemcachedByKey<T>(sql, subsystem);
    }
    return updater;
  }

  @Override
  public IUpdateBatch<T> getUpdateBatch(IUpdateSql<T> sql) {
    UpdateMemcachedBatchByKey<T> updater = null;
    if (sql != null) {
      updater = new UpdateMemcachedBatchByKey<T>(sql, subsystem);
    }
    return updater;
  }

  @Override
  public IUpdateCollection<T> getUpdateCollection(IUpdateSql<T> sql) {
    UpdateMemcachedCollectionByKey<T> updater = null;
    if (sql != null) {
      updater = new UpdateMemcachedCollectionByKey<T>(sql, subsystem);
    }
    return updater;
  }

  @Override
  public IUpdateWithParameters<T> getUpdateWithParameters(IUpdateSql<T> sql) {
    // Does nothing with the cache as no DTOs are involved in this update
    UpdateWithParameters<T> updater = null;
    if (sql != null) {
      updater = new UpdateWithParameters<T>(sql);
    }
    return updater;
  }

  @Override
  public void clear() {
    DaoManagerFactory.initMemcachedManager(subsystem);
    IDaoManager manager = DaoManagerFactory.getManager(subsystem);
    manager.clearCache(null);
  }
}
