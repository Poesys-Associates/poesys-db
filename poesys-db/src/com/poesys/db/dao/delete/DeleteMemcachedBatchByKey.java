/*
 * Copyright (c) 2011 Poesys Associates. All rights reserved.
 */
package com.poesys.db.dao.delete;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import com.poesys.db.BatchException;
import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dto.IDbDto;


/**
 * A subclass of the DeleteBatchByKey class that removes the deleted data
 * transfer object (DTO) from the memcached cache.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to delete
 */
public class DeleteMemcachedBatchByKey<T extends IDbDto> extends
    DeleteBatchByKey<T> implements IDeleteBatch<T> {
  /** the name of the subsystem containing the T class */
  private final String subsystem;

  /**
   * Create a DeleteCacheBatchByKey object.
   * 
   * @param sql the SQL DELETE statement specification
   * @param subsystem the name of the subsystem containing the T class
   */
  public DeleteMemcachedBatchByKey(IDeleteSql<T> sql, String subsystem) {
    super(sql);
    this.subsystem = subsystem;
  }

  @Override
  public void delete(Connection connection, Collection<T> dtos, int size)
      throws SQLException, BatchException {
    // Delete only happens for DELETED objects, not CASCADE_DELETED
    super.delete(connection, dtos, size);
    // Only remove from cache if collection exists and has objects.
    if (dtos != null && dtos.size() > 0) {
      IDaoManager manager = DaoManagerFactory.getManager(subsystem);
      for (IDbDto dto : dtos) {
        // Only proceed if the dto is DELETED or CASCADE_DELETED.
        if (dto.getStatus() == IDbDto.Status.DELETED
            || dto.getStatus() == IDbDto.Status.CASCADE_DELETED) {
          manager.removeObjectFromCache(dto.getPrimaryKey().getCacheName(),
                                        dto.getPrimaryKey());
        }
      }
    }
  }

  @Override
  protected void postprocess(Connection connection, IDbDto dto)
      throws SQLException, BatchException {
    dto.postprocessNestedObjects(connection);
  }

  @Override
  public void close() {
  }
}
