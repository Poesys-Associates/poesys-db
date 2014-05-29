/*
 * Copyright (c) 2008, 2011 Poesys Associates. All rights reserved.
 */
package com.poesys.db.dao.delete;


import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * SQL statement specification for deleting a standard object. The concrete
 * subclass contains the SQL statement and an implementation of
 * <code>IDeleteSql.getSql()</code> that calls the <code>getSql</code> method in
 * this class:
 * 
 * <pre>
 * public class DeleteTestSql extends AbstractDeleteSql {
 * 
 *   private static final String SQL = &quot;DELETE FROM Test WHERE &quot;;
 * 
 *   public String getSql(IPrimaryKey key) {
 *     return super.getSql(key, SQL);
 *   }
 * }
 * </pre>
 * 
 * @author Robert J. Muller
 * @param <T> the DTO type
 */
public abstract class AbstractDeleteSql<T extends IDbDto> implements
    IDeleteSql<T> {

  /**
   * A helper method that takes the SQL from the caller (usually a concrete
   * subclass of this class) and builds a complete SQL statement by adding the
   * key to the WHERE clause
   * 
   * @param key the primary key to add to the WHERE clause
   * @param sql the SQL statement
   * @return a complete SQL statement
   */
  public String getSql(IPrimaryKey key, String sql) {
    StringBuilder builder = new StringBuilder(sql);
    builder.append(key.getSqlWhereExpression(""));
    return builder.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.poesys.db.dao.delete.IDeleteSql#setParams(java.sql.PreparedStatement,
   * int, com.poesys.db.dto.IDbDto)
   */
  public <P extends IDbDto> int setParams(PreparedStatement stmt, int next,
                                          P dto) throws SQLException {
    next = dto.getPrimaryKey().setParams(stmt, next);
    return next;
  }
}
