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
 * A subclass of the UpdateCollectionByKey class that removes all the data
 * transfer objects (DTOs) in the updated list from the DTO cache
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to update
 */
public class UpdateMemcachedCollectionByKey<T extends IDbDto> extends
    UpdateCollectionByKey<T> implements IUpdateCollection<T> {
  /** the name of the subsystem containing the T class */
  private final String subsystem;

  /**
   * Create a UpdateCacheCollectionByKey object.
   * 
   * @param sql the SQL UPDATE statement specification
   * @param subsystem the name of the subsystem containing the T class
   */
  public UpdateMemcachedCollectionByKey(IUpdateSql<T> sql, String subsystem) {
    super(sql);
    this.subsystem = subsystem;
  }

  @Override
  public void update(Connection connection, Collection<T> dtos)
      throws SQLException, BatchException {
    super.update(connection, dtos);
    // Only remove from cache if there are dtos.
    if (dtos != null && dtos.size() > 0) {
      IDaoManager manager = DaoManagerFactory.getManager(subsystem);
      for (IDbDto dto : dtos) {
        manager.removeObjectFromCache(dto.getPrimaryKey().getCacheName(),
                                      dto.getPrimaryKey());
      }
    }
  }

  @Override
  public void close() {
  }
}
