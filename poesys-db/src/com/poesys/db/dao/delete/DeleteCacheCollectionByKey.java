/*
 * Copyright (c) 2010 Poesys Associates. All rights reserved.
 */
package com.poesys.db.dao.delete;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import com.poesys.db.BatchException;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDtoCache;


/**
 * A subclass of the DeleteCollectionByKey class that removes the deleted data
 * transfer object (DTO) from the cache.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to delete
 */
public class DeleteCacheCollectionByKey<T extends IDbDto> extends
    DeleteCollectionByKey<T> implements IDeleteCollection<T> {
  /** The cache */
  private IDtoCache<T> cache;

  /**
   * Create a DeleteCacheCollectionByKey object.
   * 
   * @param sql the SQL DELETE statement specification
   * @param cache the DTO cache from which to remove the deleted DTO
   */
  public DeleteCacheCollectionByKey(IDeleteSql<T> sql, IDtoCache<T> cache) {
    super(sql);
    this.cache = cache;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.poesys.db.dao.delete.DeleteCollectionByKey#delete(java.sql.Connection,
   * java.util.Collection)
   */
  @Override
  public void delete(Connection connection, Collection<T> dtos)
      throws SQLException, BatchException {
    // Delete only happens for DELETED objects, not CASCADE_DELETED.
    super.delete(connection, dtos);
    for (IDbDto dto : dtos) {
      // Only proceed if the dto is DELETED or CASCADE_DELETED.
      if (dto.getStatus() == IDbDto.Status.DELETED
          || dto.getStatus() == IDbDto.Status.CASCADE_DELETED) {
        cache.remove(dto.getPrimaryKey());
      }
    }
  }
}
