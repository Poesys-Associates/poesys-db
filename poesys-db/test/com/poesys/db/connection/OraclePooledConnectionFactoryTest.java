package com.poesys.db.connection;


import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.junit.Test;


public class OraclePooledConnectionFactoryTest {
  private static final Logger logger =
    Logger.getLogger(OraclePooledConnectionFactoryTest.class);
  private static final String SERVICE = "tair.arabidopsis.org";
  private static final String HOST = "dbprod.arabidopsis.org";
  private static final Integer PORT = 1521;
  private static final String USER = "TAIR";
  private static final String PW = "JU!Ebom7";
  private static final String TEST_SQL = "SELECT COUNT(*) FROM Locus";

  @Test
  public void testGetConnection() {
    IJdbcDriver driver = new OracleDriver(HOST, null, SERVICE);
    PooledConnectionFactory pooledFactory = new PooledConnectionFactory(5, 1, 100, driver, USER, PW, "SELECT 1 FROM DUAL");
    pooledFactory.setHost(HOST);
    pooledFactory.setPort(PORT);
    pooledFactory.setUser(USER);
    pooledFactory.setPassword(PW);
    Connection connection = null;
    PreparedStatement stmt = null;

    try {
      connection = pooledFactory.getConnection();
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
          logger.warn("Error closing SQL statement", e);
        }
      }
      if (connection != null) {
        try {
          connection.close();
        } catch (SQLException e) {
          logger.warn("Error closing connection", e);
        }
      }
    }
  }
}
