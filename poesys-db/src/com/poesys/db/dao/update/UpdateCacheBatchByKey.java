/*
 * Copyright (c) 2010 Poesys Associates. All rights reserved.
 */
package com.poesys.db.dao.update;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import com.poesys.db.BatchException;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDtoCache;


/**
 * A subclass of the UpdateBatchByKey class that removes all the data transfer
 * objects (DTOs) in the updated list from the DTO cache
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to update
 */
public class UpdateCacheBatchByKey<T extends IDbDto> extends
    UpdateBatchByKey<T> implements IUpdateBatch<T> {
  /** The cache */
  private IDtoCache<T> cache;

  /**
   * Create a UpdateCacheBatchByKey object.
   * 
   * @param sql the SQL UPDATE statement specification
   * @param cache the cache of IDtos from which to remove the updated DTOs
   */
  public UpdateCacheBatchByKey(IUpdateSql<T> sql, IDtoCache<T> cache) {
    super(sql);
    this.cache = cache;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dao.update.UpdateBatchByKey#update(java.sql.Connection,
   * java.util.Collection, int)
   */
  @Override
  public void update(Connection connection, Collection<T> dtos, int size)
      throws SQLException, BatchException {
    super.update(connection, dtos, size);
    // Only remove from cache if DTOs exists and isn't empty.
    if (dtos != null && dtos.size() > 0) {
      for (T dto : dtos) {
        cache.remove(dto.getPrimaryKey());
      }
    }
  }
}
