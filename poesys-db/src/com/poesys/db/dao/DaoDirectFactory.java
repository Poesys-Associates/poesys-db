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

import com.poesys.db.dao.delete.DeleteBatchByKey;
import com.poesys.db.dao.delete.DeleteByKey;
import com.poesys.db.dao.delete.DeleteCollectionByKey;
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
import com.poesys.db.dao.insert.Insert;
import com.poesys.db.dao.insert.InsertBatch;
import com.poesys.db.dao.insert.InsertCollection;
import com.poesys.db.dao.insert.InsertNoKey;
import com.poesys.db.dao.insert.InsertNoKeyCollection;
import com.poesys.db.dao.query.IKeyListQuerySql;
import com.poesys.db.dao.query.IKeyQuerySql;
import com.poesys.db.dao.query.IParameterizedQuerySql;
import com.poesys.db.dao.query.IQueryByKey;
import com.poesys.db.dao.query.IQueryList;
import com.poesys.db.dao.query.IQueryListWithParameters;
import com.poesys.db.dao.query.IQuerySql;
import com.poesys.db.dao.query.QueryByKey;
import com.poesys.db.dao.query.QueryList;
import com.poesys.db.dao.query.QueryListWithKeyList;
import com.poesys.db.dao.query.QueryListWithParameters;
import com.poesys.db.dao.update.IUpdate;
import com.poesys.db.dao.update.IUpdateBatch;
import com.poesys.db.dao.update.IUpdateCollection;
import com.poesys.db.dao.update.IUpdateSql;
import com.poesys.db.dao.update.IUpdateWithParameters;
import com.poesys.db.dao.update.UpdateBatchByKey;
import com.poesys.db.dao.update.UpdateByKey;
import com.poesys.db.dao.update.UpdateCollectionByKey;
import com.poesys.db.dao.update.UpdateWithParameters;
import com.poesys.db.dto.IDbDto;


/**
 * Implements the IDaoFactory interface for direct operations (operations that
 * just process an object, a batched list of objects, or a collection of objects
 * without further processing such as cache management).
 * 
 * @see IDaoFactory
 * @see DaoCacheFactory
 * 
 * @author Robert J. Muller
 * @param <T> the type of database DTO to process
 */
public class DaoDirectFactory<T extends IDbDto> implements IDaoFactory<T> {
  public IQueryByKey<T> getQueryByKey(IKeyQuerySql<T> sql, String subsystem) {
    return new QueryByKey<T>(sql, subsystem);
  }

  @Override
  public IQueryByKey<T> getDatabaseQueryByKey(IKeyQuerySql<T> sql,
                                              String subsystem) {
    return new QueryByKey<T>(sql, subsystem);
  }

  @Override
  public IQueryList<T> getQueryList(IQuerySql<T> sql, String subsystem, int rows) {
    return new QueryList<T>(sql, subsystem, rows);
  }

  @Override
  public IQueryList<T> getQueryListWithKeyList(IKeyListQuerySql<T> sql,
                                               String subsystem, int rows) {
    return new QueryListWithKeyList<T>(sql, subsystem, rows);
  }

  @Override
  public <S extends IDbDto, C extends Collection<T>> IQueryListWithParameters<T, S, C> getQueryListWithParameters(IParameterizedQuerySql<T, S> sql,
                                                                                                                  String subsystem,
                                                                                                                  int rows) {
    return new QueryListWithParameters<T, S, C>(sql, subsystem, rows);
  }

  @Override
  public IDelete<T> getDelete(IDeleteSql<T> sql) {
    DeleteByKey<T> deleter = null;
    if (sql != null) {
      deleter = new DeleteByKey<T>(sql);
    }
    return deleter;
  }

  @Override
  public IDeleteBatch<T> getDeleteBatch(IDeleteSql<T> sql) {
    DeleteBatchByKey<T> deleter = null;
    if (sql != null) {
      deleter = new DeleteBatchByKey<T>(sql);
    }
    return deleter;
  }

  @Override
  public IDeleteCollection<T> getDeleteCollection(IDeleteSql<T> sql) {
    DeleteCollectionByKey<T> deleter = null;
    if (sql != null) {
      deleter = new DeleteCollectionByKey<T>(sql);
    }
    return deleter;
  }

  @Override
  public <P extends IDbDto> IDeleteWithParameters<T, P> getDeleteWithParameters(IDeleteSqlWithParameters<T, P> sql) {
    DeleteWithParameters<T, P> deleter = null;
    if (sql != null) {
      deleter = new DeleteWithParameters<T, P>(sql);
    }
    return deleter;
  }

  @Override
  public IInsert<T> getInsert(IInsertSql<T> sql, Boolean key) {
    return key ? new Insert<T>(sql) : new InsertNoKey<T>(sql);
  }

  @Override
  public IInsertBatch<T> getInsertBatch(IInsertSql<T> sql) {
    return new InsertBatch<T>(sql);
  }

  @Override
  public IInsertCollection<T> getInsertCollection(IInsertSql<T> sql, Boolean key) {
    return key ? new InsertCollection<T>(sql)
        : new InsertNoKeyCollection<T>(sql);
  }

  @Override
  public IUpdate<T> getUpdate(IUpdateSql<T> sql) {
    UpdateByKey<T> updater = null;
    if (sql != null) {
      updater = new UpdateByKey<T>(sql);
    }
    return updater;
  }

  @Override
  public IUpdateBatch<T> getUpdateBatch(IUpdateSql<T> sql) {
    UpdateBatchByKey<T> updater = null;
    if (sql != null) {
      updater = new UpdateBatchByKey<T>(sql);
    }
    return updater;
  }

  @Override
  public IUpdateCollection<T> getUpdateCollection(IUpdateSql<T> sql) {
    UpdateCollectionByKey<T> updater = null;
    if (sql != null) {
      updater = new UpdateCollectionByKey<T>(sql);
    }
    return updater;
  }

  @Override
  public IUpdateWithParameters<T> getUpdateWithParameters(IUpdateSql<T> sql) {
    UpdateWithParameters<T> updater = null;
    if (sql != null) {
      updater = new UpdateWithParameters<T>(sql);
    }
    return updater;
  }

  @Override
  public void clear() {
    // Does nothing for direct queries
  }
}
