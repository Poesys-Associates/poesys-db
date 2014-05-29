/*
 * Copyright (c) 2008, 2011 Poesys Associates. All rights reserved.
 */
package com.poesys.db.dao.delete;




/**
 * SQL statement specification for deleting objects identified by a query. The concrete
 * subclass contains the SQL statement and an implementation of
 * <code>IDeleteSql.getSql()</code> that calls the <code>getSql</code> method in
 * this class if it needs to add something to the query:
 * 
 * <pre>
 * public class DeleteTestSql extends AbstractDeleteSqlWithQuery {
 * 
 *   private static final String SQL = &quot;DELETE FROM Test&quot;;
 *   
 *   public DeleteTestSql() {
 *     super(SQL);
 *   }
 * 
 *   public String getSql(IPrimaryKey key) {
 *     return super.getSql(key, SQL) + &quot; WHERE testtype = 'Test'&quot;;
 *   }
 * }
 * </pre>
 * 
 * @author Robert J. Muller
 */
public abstract class AbstractDeleteSqlWithQuery implements
    IDeleteSqlWithQuery {
  
  /** the DELETE SQL statement */
  private final String SQL;
  
  /**
   * Create a AbstractDeleteSqlWithQuery object with the SQL string for the
   * DELETE statement.
   * 
   * @param sql the DELETE SQL statement
   */
  public AbstractDeleteSqlWithQuery(String sql) {
    SQL = sql;
  }

  @Override
  public String getSql() {
    return SQL;
  }
}
