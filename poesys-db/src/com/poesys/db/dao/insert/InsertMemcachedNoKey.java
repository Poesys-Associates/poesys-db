/*
 * Copyright (c) 2011 Poesys Associates. All rights reserved.
 * 
 * This file is part of Poesys-DB.
 * 
 * Poesys-DB is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Poesys-DB is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Poesys-DB. If not, see <http://www.gnu.org/licenses/>.
 */
package com.poesys.db.dao.insert;


import java.sql.Connection;
import java.sql.SQLException;

import com.poesys.db.BatchException;
import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dto.IDbDto;


/**
 * A subclass of InsertNoKey that caches the inserted object in a memcached
 * cache after key generation.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to insert and cache
 */
public class InsertMemcachedNoKey<T extends IDbDto> extends InsertNoKey<T>
    implements IInsert<T> {
  /** the name of the subsystem containing the T class */
  private final String subsystem;
  /** the memcached expiration time in milliseconds for T objects */
  private final int expiration;

  /**
   * Create a InsertCacheNoKey object.
   * 
   * @param sql the SQL statement specification for INSERT
   * @param subsystem the name of the subsystem containing the T class
   * @param expiration the memcached expiration time in milliseconds for T
   *          objects
   */
  public InsertMemcachedNoKey(IInsertSql<T> sql,
                              String subsystem,
                              int expiration) {
    super(sql);
    this.subsystem = subsystem;
    this.expiration = expiration;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void insert(Connection connection, IDbDto dto) throws SQLException,
      BatchException {
    super.insert(connection, (T)dto);
    IDaoManager manager = DaoManagerFactory.getManager(subsystem);
    manager.putObjectInCache(dto.getPrimaryKey().getCacheName(),
                             expiration,
                             dto);
  }

  @Override
  public void close() {
  }
}
