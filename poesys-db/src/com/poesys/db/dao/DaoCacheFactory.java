/*
 * Copyright (c) 2008, 2011 Poesys Associates. All rights reserved.
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

import com.poesys.db.dao.delete.DeleteCacheBatchByKey;
import com.poesys.db.dao.delete.DeleteCacheByKey;
import com.poesys.db.dao.delete.DeleteCacheCollectionByKey;
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
import com.poesys.db.dao.insert.InsertCache;
import com.poesys.db.dao.insert.InsertCacheBatch;
import com.poesys.db.dao.insert.InsertCacheCollection;
import com.poesys.db.dao.insert.InsertCacheNoKey;
import com.poesys.db.dao.insert.InsertCacheNoKeyCollection;
import com.poesys.db.dao.query.IKeyListQuerySql;
import com.poesys.db.dao.query.IKeyQuerySql;
import com.poesys.db.dao.query.IParameterizedQuerySql;
import com.poesys.db.dao.query.IQueryByKey;
import com.poesys.db.dao.query.IQueryList;
import com.poesys.db.dao.query.IQueryListWithParameters;
import com.poesys.db.dao.query.IQuerySql;
import com.poesys.db.dao.query.QueryCacheByKey;
import com.poesys.db.dao.query.QueryCacheList;
import com.poesys.db.dao.query.QueryCacheListWithKeyList;
import com.poesys.db.dao.query.QueryCacheListWithParameters;
import com.poesys.db.dao.query.QueryDatabaseCacheByKey;
import com.poesys.db.dao.update.IUpdate;
import com.poesys.db.dao.update.IUpdateBatch;
import com.poesys.db.dao.update.IUpdateCollection;
import com.poesys.db.dao.update.IUpdateSql;
import com.poesys.db.dao.update.IUpdateWithParameters;
import com.poesys.db.dao.update.UpdateCacheBatchByKey;
import com.poesys.db.dao.update.UpdateCacheByKey;
import com.poesys.db.dao.update.UpdateCacheCollectionByKey;
import com.poesys.db.dao.update.UpdateWithParameters;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDtoCache;


/**
 * <p>
 * Implements the IDaoFactory interface for cached-object operations (operations
 * that process a previously cached object or cache the new object then return
 * it).
 * </p>
 * <p>
 * This class's constructor manages the actual cache for the data transfer
 * objects (DTOs). The cache itself is independent of the factory instance. Just
 * create the factory when you need a DAO and the caching will happen
 * automatically.
 * </p>
 * 
 * @see IDaoFactory
 * @see DaoDirectFactory
 * @see com.poesys.db.dao.CacheDaoManager
 * 
 * @author Robert J. Muller
 * @param <T> the type of database DTO to cache
 */
public class DaoCacheFactory<T extends IDbDto> implements IDaoFactory<T> {
  /** The cache of DTOS * */
  private IDtoCache<T> cache;

  /**
   * Create a QueryCacheFactory object for a particular DTO class.
   * 
   * @param name the cache name (usually the fully qualified class name of the
   *          cached objects)
   * @param manager the DAO manager that created this factory
   */
  @SuppressWarnings("unchecked")
  public DaoCacheFactory(String name, CacheDaoManager manager) {
    if (!CacheDaoManager.getInstance().isCached(name)) {
      // Not cached, tell the manager to create a cache for the class.
      cache = (IDtoCache<T>)manager.createCache(name);
    } else {
      // Already cached, get the cache for the factory.
      cache = (IDtoCache<T>)manager.getCache(name);
    }
  }

  @Override
  public IQueryByKey<T> getQueryByKey(IKeyQuerySql<T> sql) {
    return new QueryCacheByKey<T>(sql, cache);
  }

  @Override
  public IQueryByKey<T> getDatabaseQueryByKey(IKeyQuerySql<T> sql) {
    return new QueryDatabaseCacheByKey<T>(sql, cache);
  }

  @Override
  public IQueryList<T> getQueryList(IQuerySql<T> sql, int rows) {
    return new QueryCacheList<T>(sql, cache, rows);
  }

  @Override
  public IQueryList<T> getQueryListWithKeyList(IKeyListQuerySql<T> sql, int rows) {
    return new QueryCacheListWithKeyList<T>(sql, cache, rows);
  }

  @Override
  public <S extends IDbDto, C extends Collection<T>> IQueryListWithParameters<T, S, C> getQueryListWithParameters(IParameterizedQuerySql<T, S> sql,
                                                                                                                  int rows) {
    return new QueryCacheListWithParameters<T, S, C>(sql, cache, rows);
  }

  @Override
  public IDelete<T> getDelete(IDeleteSql<T> sql) {
    DeleteCacheByKey<T> deleter = null;
    if (sql != null) {
      deleter = new DeleteCacheByKey<T>(sql, cache);
    }
    return deleter;
  }

  @Override
  public IDeleteBatch<T> getDeleteBatch(IDeleteSql<T> sql) {
    DeleteCacheBatchByKey<T> deleter = null;
    if (sql != null) {
      deleter = new DeleteCacheBatchByKey<T>(sql, cache);
    }
    return deleter;
  }

  @Override
  public IDeleteCollection<T> getDeleteCollection(IDeleteSql<T> sql) {
    DeleteCacheCollectionByKey<T> deleter = null;
    if (sql != null) {
      deleter = new DeleteCacheCollectionByKey<T>(sql, cache);
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
    return key ? new InsertCache<T>(sql, cache)
        : new InsertCacheNoKey<T>(sql, cache);
  }

  @Override
  public IInsertBatch<T> getInsertBatch(IInsertSql<T> sql) {
    return new InsertCacheBatch<T>(sql, cache);
  }

  @Override
  public IInsertCollection<T> getInsertCollection(IInsertSql<T> sql, Boolean key) {
    return key ? new InsertCacheCollection<T>(sql, cache)
        : new InsertCacheNoKeyCollection<T>(sql, cache);
  }

  @Override
  public IUpdate<T> getUpdate(IUpdateSql<T> sql) {
    UpdateCacheByKey<T> updater = null;
    if (sql != null) {
      updater = new UpdateCacheByKey<T>(sql, cache);
    }
    return updater;
  }

  @Override
  public IUpdateBatch<T> getUpdateBatch(IUpdateSql<T> sql) {
    UpdateCacheBatchByKey<T> updater = null;
    if (sql != null) {
      updater = new UpdateCacheBatchByKey<T>(sql, cache);
    }
    return updater;
  }

  @Override
  public IUpdateCollection<T> getUpdateCollection(IUpdateSql<T> sql) {
    UpdateCacheCollectionByKey<T> updater = null;
    if (sql != null) {
      updater = new UpdateCacheCollectionByKey<T>(sql, cache);
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
    if (cache != null) {
      cache.clear();
    }
  }
}
