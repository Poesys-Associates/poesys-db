/*
 * Copyright (c) 2011 Poesys Associates. All rights reserved.
 */
package com.poesys.db.dao.update;


import java.sql.Connection;
import java.sql.SQLException;

import com.poesys.db.BatchException;
import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dto.IDbDto;


/**
 * A subclass of the UpdateByKey class that removes the updated data transfer
 * object (DTO) from the cache.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to update and cache
 */
public class UpdateMemcachedByKey<T extends IDbDto> extends UpdateByKey<T>
    implements IUpdate<T> {
  /** the name of the subsystem containing the T class */
  private final String subsystem;

  /**
   * Create a UpdateCacheByKey object.
   * 
   * @param sql the SQL UPDATE statement specification
   * @param subsystem the name of the subsystem containing the T class
   */
  public UpdateMemcachedByKey(IUpdateSql<T> sql, String subsystem) {
    super(sql);
    this.subsystem = subsystem;
  }

  @Override
  public void update(Connection connection, T dto) throws SQLException,
      BatchException {
    // Only remove from cache if DTO exists and hasn't been processed.
    if (dto != null && !dto.isProcessed()) {
      IDaoManager manager = DaoManagerFactory.getManager(subsystem);
      manager.removeObjectFromCache(dto.getPrimaryKey().getCacheName(),
                                    dto.getPrimaryKey());
    }
    super.update(connection, dto);
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
