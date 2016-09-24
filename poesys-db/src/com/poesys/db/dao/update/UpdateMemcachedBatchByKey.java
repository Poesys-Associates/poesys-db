/*
 * Copyright (c) 2011 Poesys Associates. All rights reserved.
 */
package com.poesys.db.dao.update;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import com.poesys.db.BatchException;
import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dto.IDbDto;


/**
 * A subclass of the UpdateBatchByKey class that removes all the data transfer
 * objects (DTOs) in the updated list from the DTO cache
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to update
 */
public class UpdateMemcachedBatchByKey<T extends IDbDto> extends
    UpdateBatchByKey<T> implements IUpdateBatch<T> {
  /** the name of the subsystem containing the T class */
  private final String subsystem;

  /**
   * Create a UpdateCacheBatchByKey object.
   * 
   * @param sql the SQL UPDATE statement specification
   * @param subsystem the name of the subsystem containing the T class
   */
  public UpdateMemcachedBatchByKey(IUpdateSql<T> sql, String subsystem) {
    super(sql);
    this.subsystem = subsystem;
  }

  @Override
  public void update(Connection connection, Collection<T> dtos, int size)
      throws SQLException, BatchException {
    // Remove any CHANGED DTOs from the cache, then do the update, which resets
    // status to EXISTING.
    if (dtos != null && dtos.size() > 0) {
      DaoManagerFactory.initMemcachedManager(subsystem);
      IDaoManager manager = DaoManagerFactory.getManager(subsystem);
      for (T dto : dtos) {
        if (dto.hasStatusChanged()) {
          manager.removeObjectFromCache(dto.getPrimaryKey().getCacheName(),
                                        dto.getPrimaryKey());
        }
      }
    }
    super.update(connection, dtos, size);
  }

  @Override
  protected void postprocess(Connection connection, T dto) throws SQLException,
      BatchException {
    dto.postprocessNestedObjects(connection);
  }

  @Override
  public void close() {
  }
}
