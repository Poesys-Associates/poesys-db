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
 * A subclass of the DeleteCollectionByKey class that removes the deleted data
 * transfer object (DTO) from the memcached cache.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to delete
 */
public class DeleteMemcachedCollectionByKey<T extends IDbDto> extends
    DeleteCollectionByKey<T> implements IDeleteCollection<T> {
  /** the name of the subsystem containing the T class */
  private final String subsystem;

  /**
   * Create a DeleteCacheCollectionByKey object.
   * 
   * @param sql the SQL DELETE statement specification
   * @param subsystem the name of the subsystem containing the T class
   */
  public DeleteMemcachedCollectionByKey(IDeleteSql<T> sql, String subsystem) {
    super(sql);
    this.subsystem = subsystem;
  }

  @Override
  public void delete(Connection connection, Collection<T> dtos)
      throws SQLException, BatchException {
    // Delete only happens for DELETED objects, not CASCADE_DELETED.
    super.delete(connection, dtos);
    DaoManagerFactory.initMemcachedManager(subsystem);
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

  @Override
  public void close() {
  }
}
