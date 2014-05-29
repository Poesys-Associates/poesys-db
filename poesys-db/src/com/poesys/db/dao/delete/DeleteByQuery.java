/*
 * Copyright (c) 2012 Carnegie Institution for Science. All rights reserved.
 */

package com.poesys.db.dao.delete;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;


/**
 * 
 * @author Robert J. Muller
 */
public class DeleteByQuery implements IDeleteQuery {

  /** Log4j logger for this class */
  private static final Logger logger = Logger.getLogger(DeleteByQuery.class);

  /** SQL statement object */
  private IDeleteSqlWithQuery sql;

  /**
   * Create a DeleteByKey object by supplying the concrete implementation of the
   * SQL-statement generator and JDBC setter.
   * 
   * @param sql the SQL UPDATE statement generator object
   */
  public DeleteByQuery(IDeleteSqlWithQuery sql) {
    this.sql = sql;
  }

  @Override
  public void delete(Connection connection) throws SQLException {
    PreparedStatement stmt = null;

    if (sql != null) {
      String sqlText = null;

      try {
        sqlText = sql.getSql();
        stmt = connection.prepareStatement(sqlText);
        logger.debug("Delete by query: " + sqlText);

        stmt.executeUpdate();

      } finally {
        if (stmt != null) {
          stmt.close();
        }
      }
    }
  }

  @Override
  public void close() {
    // Nothing to do
    // Note that query-based delete does not affect caches at all.
  }
}
