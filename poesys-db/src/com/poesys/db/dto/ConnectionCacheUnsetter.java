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
 * A setter that un-caches a connection identified by a primary key in the
 * Memcached connection cache.
 * 
 * @author Robert J. Muller
 */
public class ConnectionCacheUnsetter implements ISet {
  /** default serial version UID for serializable object */
  private static final long serialVersionUID = 1L;
  /** logger for this class */
  private static final Logger logger =
    Logger.getLogger(ConnectionCacheUnsetter.class);
  /** primary key to use to index connection in cache */
  private final IPrimaryKey key;
  /** whether the key is set */
  private Boolean set = Boolean.FALSE;

  /**
   * Create a PrimaryKeyCacheSetter object.
   * 
   * @param key the primary key to use to get the connection
   */
  public ConnectionCacheUnsetter(IPrimaryKey key) {
    this.key = key;
  }

  @Override
  public void set(Connection connection) throws SQLException {
    MemcachedDaoManager.removeConnection(key);
    logger.debug("Removed connection " + connection + " for object "
                 + key.getStringKey() + " in unsetter");
    set = true; // whether setter has run, not whether key is set
  }

  @Override
  public boolean isSet() {
    return set;
  }

}
