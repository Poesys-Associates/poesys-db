/*
 * Copyright (c) 2010 Poesys Associates. All rights reserved.
 */
package com.poesys.db.dao.update;


import java.sql.Connection;
import java.sql.SQLException;

import com.poesys.db.BatchException;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDtoCache;


/**
 * A subclass of the UpdateByKey class that removes the updated data transfer
 * object (DTO) from the cache.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to update and cache
 */
public class UpdateCacheByKey<T extends IDbDto> extends UpdateByKey<T>
    implements IUpdate<T> {
  /** The cache */
  private IDtoCache<T> cache;

  /**
   * Create a UpdateCacheByKey object.
   * 
   * @param sql the SQL UPDATE statement specification
   * @param cache the cache of IDtos from which to remove the updated DTO
   */
  public UpdateCacheByKey(IUpdateSql<T> sql, IDtoCache<T> cache) {
    super(sql);
    this.cache = cache;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dao.update.UpdateByKey#update(java.sql.Connection,
   *      com.poesys.db.dto.IDto)
   */
  @Override
  public void update(Connection connection, T dto) throws SQLException,
      BatchException {
    super.update(connection, dto);
    // Only remove from cache if DTO exists.
    if (dto != null) {
      cache.remove(dto.getPrimaryKey());
    }
  }
}
