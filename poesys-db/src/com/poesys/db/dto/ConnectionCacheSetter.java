/**
 * Copyright Phoenix Bioinformatics Corporation 2015. All rights reserved.
 */
package com.poesys.db.dto;


import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.poesys.db.dao.MemcachedDaoManager;
import com.poesys.db.pk.IPrimaryKey;


/**
 * A setter that caches a connection identified by a primary key in the
 * Memcached connection cache.
 * 
 * @author Robert J. Muller
 */
public class ConnectionCacheSetter implements ISet {
  /** default serial version UID for serializable object */
  private static final long serialVersionUID = 1L;
  private static final Logger logger =
      Logger.getLogger(ConnectionCacheSetter.class);
  /** primary key to use to index connection in cache */
  private final IPrimaryKey key;
  private Boolean set = Boolean.FALSE;

  /**
   * Create a PrimaryKeyCacheSetter object.
   * @param key the primary key to use to get the connection
   */
  public ConnectionCacheSetter(IPrimaryKey key) {
    this.key = key;
  }

  @Override
  public void set(Connection connection) throws SQLException {
      MemcachedDaoManager.putConnection(key, connection);
      logger.debug("Cached connection " + connection + " for object "
          + key.getStringKey() + " in setter");
      set = true;
  }

  @Override
  public boolean isSet() {
    return set;
  }
}
