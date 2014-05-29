/**
 * Copyright 2013 Poesys Associates. All rights reserved.
 */
package com.poesys.db.connection;

/**
 * Create a special JNDI Factory for MySQL.
 * 
 * @author Robert J. Muller
 */
public class JndiMysqlConnectionFactory extends JndiConnectionFactory {

  @Override
  public DBMS getDbms() {
    return DBMS.JNDI_MYSQL;
  }

}
