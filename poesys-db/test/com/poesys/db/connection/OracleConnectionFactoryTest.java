/*
 * Copyright (c) 2008 Poesys Associates. All rights reserved.
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
package com.poesys.db.connection;


import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.junit.Test;


/**
 * CUT: OracleConnectionFactory
 * 
 * Note: the Oracle password is in the test.properties file, which you need to
 * create before running this test. It isn't stored in the git repository.
 * 
 * @author Robert J. Muller
 */
public class OracleConnectionFactoryTest {
  /** Name of the Oracle test properties resource bundle */
  protected static final String BUNDLE = "com.poesys.db.connection.test";

  /** The resource bundle containing the test properties. */
  protected static final ResourceBundle properties =
    ResourceBundle.getBundle(BUNDLE);

  /** property name for password */
  private static final String PASSWORD = "password";

  private static final Logger logger =
    Logger.getLogger(OracleConnectionFactoryTest.class);

  private static final String SERVICE = "tair.arabidopsis.org";
  private static final String HOST = "dbprod.arabidopsis.org";
  private static final Integer PORT = 1521;
  private static final String USER = "TAIRTEST";
  // Get password from the properties file to keep it secure.
  private static final String PW = properties.getString(PASSWORD);
  private static final String TEST_SQL = "SELECT COUNT(*) FROM Locus";

  @Test
  public void testGetConnection() {
    OracleConnectionFactory oracleFactory = new OracleConnectionFactory();
    oracleFactory.setService(SERVICE);
    oracleFactory.setHost(HOST);
    oracleFactory.setPort(PORT);
    oracleFactory.setUser(USER);
    oracleFactory.setPassword(PW);
    Connection connection = null;
    PreparedStatement stmt = null;

    try {
      connection = oracleFactory.getConnection();
      stmt = connection.prepareStatement(TEST_SQL);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        System.out.println("Locus table has " + rs.getInt(1) + " rows.");
      } else {
        fail("Could not count any rows with service connection");
      }
      connection.commit();
    } catch (SQLException e) {
      logger.error("Connection by service got SQL exception", e);
      fail("Connection by service got SQL exception " + e.getMessage());
    } finally {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          logger.error("Error closing SQL statement", e);
        }
      }
      if (connection != null) {
        try {
          connection.close();
        } catch (SQLException e) {
          logger.error("Error closing connection", e);
        }
      }
    }
  }
}
