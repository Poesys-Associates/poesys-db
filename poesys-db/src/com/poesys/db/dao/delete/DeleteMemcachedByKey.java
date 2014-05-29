/*
 * Copyright (c) 2011 Poesys Associates. All rights reserved.
 */
package com.poesys.db.dao.delete;


import java.sql.Connection;
import java.sql.SQLException;

import com.poesys.db.BatchException;
import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dto.IDbDto;


/**
 * A subclass of the DeleteByKey class that removes the deleted data transfer
 * object from the memcached distributed cache.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to delete
 */
public class DeleteMemcachedByKey<T extends IDbDto> extends DeleteByKey<T>
    implements IDelete<T> {
  /** the name of the subsystem containing the T class */
  private final String subsystem;

  /**
   * Create a DeleteCacheByKey object, supplying an appropriate kind of SQL
   * class object and the name of the subsystem that contains objects of type T.
   * 
   * @param sql the SQL DELETE statement specification
   * @param subsystem the name of the subsystem containing the T class
   */
  public DeleteMemcachedByKey(IDeleteSql<T> sql, String subsystem) {
    super(sql);
    this.subsystem = subsystem;
  }

  @Override
  public void delete(Connection connection, T dto) throws SQLException,
      BatchException {
    // Delete only happens for DELETED objects, not CASCADE_DELETED.
    super.delete(connection, dto);
    // Only proceed if the dto is DELETED or CASCADE_DELETED.
    if (dto.getStatus() == IDbDto.Status.DELETED
        || dto.getStatus() == IDbDto.Status.CASCADE_DELETED) {
      IDaoManager manager = DaoManagerFactory.getManager(subsystem);
      manager.removeObjectFromCache(null, dto.getPrimaryKey());
    }
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
