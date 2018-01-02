/*
 * Copyright (c) 2018 Poesys Associates. All rights reserved.
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
package com.poesys.db.dto;

import com.poesys.db.connection.IConnectionFactory;
import com.poesys.db.connection.JdbcConnectionManager;
import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.dao.insert.IInsert;
import com.poesys.db.pk.IPrimaryKey;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CompositeParentTest {
  private static final Logger logger = Logger.getLogger(CompositeParentTest.class);
  /** timeout for the query thread, set higher for intensive debugging */
  private static final int TIMEOUT = 100 * 10000 * 60;
  private static final String SUBSYSTEM = "com.poesys.db.poesystest.mysql";

  /** Collection of parent objects for processing */
  private CompositeParent parent;

  /** DAO Manager singleton for factory generation */
  private final IDaoManager manager = DaoManagerFactory.getManager(SUBSYSTEM);

  /** Factory for parent DAOs */
  private final IDaoFactory<CompositeParent> parentFactory =
    manager.getFactory(CompositeParent.class.getName(), SUBSYSTEM, Integer.MAX_VALUE);
  /** Factory for child root DAOs */
  private final IDaoFactory<ChildRootClass> rootFactory =
    manager.getFactory(ChildRootClass.class.getName(), SUBSYSTEM, Integer.MAX_VALUE);
  /** Factory for concrete subclass DAOs */
  private final IDaoFactory<ConcreteChildSubClass> subclassFactory =
    manager.getFactory(ConcreteChildSubClass.class.getName(), SUBSYSTEM, Integer.MAX_VALUE);
  private IInsert<CompositeParent> parentInserter =
    parentFactory.getInsert(new InsertCompositeParent(), true);

  /**
   * Test the situation that occurs when a composite parent of a child inheritance hierarchy nests
   * children, resulting in multiple insert attempts. The PoesysTrackingThread isProcessed () method
   * should catch this situation and stop recursing.
   *
   * @throws Exception when there is some kind of processing problem
   */
  @Test
  public void testMultipleInsertBug() throws Exception {
    Connection connection = null;
    IPrimaryKey parentKey = TestFactory.getCompositeParentPrimaryKey(1);
    IPrimaryKey childKey = TestFactory.getConcreteChildSubClassPrimaryKey(1, 1);

    parent = new CompositeParent(parentKey, 1, "parent data");
    ChildRootClass child = new ConcreteChildSubClass(childKey, 1, 1, "root data", "child data");
    child.setCompositeParent(parent);

    parent.addChildren(child);

    // Once fixed, this code should not throw an exception.
    try {
      connection = JdbcConnectionManager.getConnection(IConnectionFactory.DBMS.MYSQL, SUBSYSTEM);

      // Clear the database for the test.

      clearDatabase(connection);

      parentInserter.insert(parent);

      assertTrue("parent not set to existing", parent.getStatus() == IDbDto.Status.EXISTING);
      assertTrue("child not set to existing", child.getStatus() == IDbDto.Status.EXISTING);
    } catch (Exception e) {
      logger.error("Failed on multiple-insert-bug test", e);
      fail("Exception when inserting parent-child combination twice: " + e.getMessage());
    } finally {
      clearDatabase(connection);
      if (connection != null) {
        connection.close();
      }
    }
  }

  private void clearDatabase(Connection connection) throws SQLException {
    try {
      if (connection != null) {
        Statement stmt = connection.createStatement();
        // Clears all three tables through cascaded deletes.
        stmt.execute("DELETE FROM CompositeParent");
      }
    } catch (SQLException e) {
      logger.error("SQL exception clearing database", e);
      connection.rollback();
      throw e;
    } finally {
      if (connection != null) {
        connection.commit();
      }
    }
  }
}