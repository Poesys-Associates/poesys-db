/*
 * Copyright (c) 2008, 2010 Poesys Associates. All rights reserved.
 */
package com.poesys.db.dao.delete;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import com.poesys.db.BatchException;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDtoCache;


/**
 * A subclass of the DeleteBatchByKey class that removes the deleted data
 * transfer object (DTO) from the cache.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to delete
 */
public class DeleteCacheBatchByKey<T extends IDbDto> extends
    DeleteBatchByKey<T> implements IDeleteBatch<T> {
  /** The cache */
  private IDtoCache<T> cache;

  /**
   * Create a DeleteCacheBatchByKey object.
   * 
   * @param sql the SQL DELETE statement specification
   * @param cache the DTO cache from which to remove the deleted DTO
   */
  public DeleteCacheBatchByKey(IDeleteSql<T> sql, IDtoCache<T> cache) {
    super(sql);
    this.cache = cache;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dao.delete.DeleteBatchByKey#delete(java.sql.Connection,
   * java.util.Collection, int)
   */
  @Override
  public void delete(Connection connection, Collection<T> dtos, int size)
      throws SQLException, BatchException {
    // Delete only happens for DELETED objects, not CASCADE_DELETED
    super.delete(connection, dtos, size);
    // Only remove from cache if collection exists and has objects.
    if (dtos != null && dtos.size() > 0) {
      for (IDbDto dto : dtos) {
        // Only proceed if the dto is DELETED or CASCADE_DELETED.
        if (dto.getStatus() == IDbDto.Status.DELETED
            || dto.getStatus() == IDbDto.Status.CASCADE_DELETED) {
          cache.remove(dto.getPrimaryKey());
        }
      }
    }
  }
}
