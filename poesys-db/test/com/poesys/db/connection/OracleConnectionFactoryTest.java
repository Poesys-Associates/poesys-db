package com.poesys.db.connection;


import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.junit.Test;


public class OracleConnectionFactoryTest {
  private static final Logger logger =
    Logger.getLogger(OracleConnectionFactoryTest.class);
  private static final String SERVICE = "tair.arabidopsis.org";
  private static final String HOST = "dbprod.arabidopsis.org";
  private static final Integer PORT = 1521;
  private static final String USER = "TAIR";
  private static final String PW = "set password here during test";
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
