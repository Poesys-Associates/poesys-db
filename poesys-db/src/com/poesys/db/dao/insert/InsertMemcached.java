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
 * A subclass of Insert that caches the inserted object using memcached.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to query
 */
public class InsertMemcached<T extends IDbDto> extends Insert<T> implements
    IInsert<T> {
  /** the name of the subsystem containing the T class */
  private final String subsystem;
  /** the memcached expiration time in milliseconds for T objects */
  private final Integer expiration;

  /**
   * Create an InsertCache object.
   * 
   * @param sql the SQL insert statement object
   * @param subsystem the name of the subsystem containing the T class
   * @param expiration the memcached expiration time in milliseconds for T
   *          objects
   */
  public InsertMemcached(IInsertSql<T> sql, String subsystem, Integer expiration) {
    super(sql);
    this.subsystem = subsystem;
    this.expiration = expiration;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.dao.insert.Insert#insert(java.sql.Connection,
   * com.poesys.db.dto.AbstractDto)
   */
  @Override
  public void insert(Connection connection, IDbDto dto) throws SQLException,
      BatchException {
    super.insert(connection, dto);
    IDaoManager manager = DaoManagerFactory.getManager(subsystem);
    manager.putObjectInCache(dto.getPrimaryKey().getCacheName(),
                             expiration,
                             dto);
  }

  @Override
  public void close() {
  }
}
