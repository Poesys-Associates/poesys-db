/*
 * Copyright (c) 2010 Poesys Associates. All rights reserved.
 */
package com.poesys.db.dao.delete;


import java.sql.Connection;
import java.sql.SQLException;

import com.poesys.db.BatchException;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDtoCache;


/**
 * A subclass of the DeleteByKey class that removes the deleted data transfer
 * object from the cache.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to delete
 */
public class DeleteCacheByKey<T extends IDbDto> extends DeleteByKey<T>
    implements IDelete<T> {
  /** The cache */
  private IDtoCache<T> cache;

  /**
   * Create a DeleteCacheByKey object.
   * 
   * @param sql the SQL DELETE statement specification
   * @param cache the DTO cache from which to remove the deleted DTO
   */
  public DeleteCacheByKey(IDeleteSql<T> sql, IDtoCache<T> cache) {
    super(sql);
    this.cache = cache;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dao.delete.DeleteByKey#delete(java.sql.Connection,
   * com.poesys.db.dto.IDto)
   */
  @Override
  public void delete(Connection connection, T dto) throws SQLException,
      BatchException {
    // Delete only happens for DELETED objects, not CASCADE_DELETED.
    super.delete(connection, dto);
    // Only proceed if the dto is DELETED or CASCADE_DELETED.
    if (dto.getStatus() == IDbDto.Status.DELETED
        || dto.getStatus() == IDbDto.Status.CASCADE_DELETED) {
      cache.remove(dto.getPrimaryKey());
    }
  }
}
