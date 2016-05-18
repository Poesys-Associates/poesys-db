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
package com.poesys.db.dto;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.poesys.db.BatchException;
import com.poesys.db.ConstraintViolationException;
import com.poesys.db.DbErrorException;
import com.poesys.db.connection.IConnectionFactory.DBMS;


/**
 * <p>
 * An abstract implementation of the ISet interface that contains the logic for
 * looping over a set of DTOs, determining which operation to take for each. The
 * concrete subclass is usually a nested, private class within a DTO. The logic
 * uses the status of the DTO to determine whether to call the insert, update,
 * or delete method. The command packages each of the three types of dto into a
 * separate collection, then calls the appropriate abstract method (insert,
 * update, delete) with the appropriate collection. The class also contains the
 * abstract method getDtos(), the implementation of which returns the collection
 * of DTOs to pre process. If a DTO has a status other than NEW, CHANGED, or
 * DELETED, the pre-processing ignores it.
 * </p>
 * 
 * <pre>
 * private class PostprocessChildren extends AbstractProcessNestedObjects&lt;Child&gt; {
 *   &#064;Override
 *   protected void doDeleted(Connection connection, List&lt;Child&gt; dtos) {
 *     // Do nothing, children already deleted
 *   }
 * 
 *   &#064;Override
 *   protected List&lt;Child&gt; getDtos() {
 *     return getChildren();
 *   }
 * 
 *   &#064;Override
 *   protected void doNew(Connection connection, List&lt;Child&gt; dtos)
 *       throws SQLException, BatchException {
 *     IDaoFactory&lt;Child&gt; factory = DaoManager.getFactory(Parent.class.getName());
 *     // Insert the children.
 *     IInsertBatch&lt;Child&gt; dao = factory.getInsertBatch(new InsertSqlChild());
 *     dao.insert(connection, dtos, CHILD_BATCH_SIZE);
 *   }
 * 
 *   &#064;Override
 *   protected void doChanged(Connection connection, List&lt;Child&gt; dtos)
 *       throws SQLException, BatchException {
 *     IDaoFactory&lt;Child&gt; factory = DaoManager.getFactory(Parent.class.getName());
 * 
 *     // Update the children.
 *     IUpdateBatch&lt;Child&gt; dao = factory.getUpdateBatch(new UpdateSqlChild());
 *     dao.update(connection, dtos, CHILD_BATCH_SIZE);
 *   }
 * }
 * </pre>
 * 
 * 
 * @see AbstractDto
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto objects to process
 * @param <C> the collection type of the set of DTOs to process
 */
public abstract class AbstractProcessNestedObjects<T extends IDbDto, C extends Collection<T>>
    extends AbstractSetter<T> implements ISet {

  /** Serial version UID for Serializable object */
  private static final long serialVersionUID = 1L;

  /**
   * Create a AbstractProcessNestedObjects object.
   * 
   * @param subsystem the subsystem for the DTO
   * @param dbms the type of DBMS to which to connect
   * @param expiration the time in milliseconds after which the object expires
   *          in a cache that supports expiration
   */
  public AbstractProcessNestedObjects(String subsystem, DBMS dbms, Integer expiration) {
    super(subsystem, dbms, expiration);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void set(Connection connection) throws SQLException {
    C inserts = (C)new ArrayList<T>();
    C updates = (C)new ArrayList<T>();
    C deletes = (C)new ArrayList<T>();

    C dtos = getDtos();

    if (dtos != null) {
      for (T dto : dtos) {
        IDbDto.Status status = dto.getStatus();
        if (!dto.isProcessed()) {
          if (status == IDbDto.Status.NEW) {
            inserts.add(dto);
          } else if (status == IDbDto.Status.CHANGED) {
            updates.add(dto);
          } else if (status == IDbDto.Status.DELETED
                     || status == IDbDto.Status.CASCADE_DELETED) {
            deletes.add(dto);
          }
        }// otherwise, ignore the DTO completely
      }

      try {
        doNew(connection, inserts);
        doChanged(connection, updates);
        doDeleted(connection, deletes);
      } catch (ConstraintViolationException e) {
        throw new DbErrorException(e.getMessage(), e);
      } catch (BatchException e) {
        throw new DbErrorException(e.getMessage(), e);
      } catch (DtoStatusException e) {
        throw new DbErrorException(e.getMessage(), e);
      }
    }
  }

  /**
   * Get the list of DTOs to process.
   * 
   * @return a list of DTOs
   */
  abstract protected C getDtos();

  public boolean isSet() {
    // Set if the nested DTOs collection is not null
    return getDtos() != null;
  }

  /**
   * Pre-process the NEW DTOs.
   * 
   * @param connection the database connection
   * @param dtos the collection of NEW DTOs
   * @throws BatchException when there is a SQL batch processing problem
   * @throws SQLException when there is a SQL execution problem
   */
  abstract protected void doNew(Connection connection, C dtos)
      throws SQLException, BatchException;

  /**
   * Pre-process the CHANGED DTOs.
   * 
   * @param connection the database connection
   * @param dtos the collection of CHANGED DTOs
   * @throws BatchException when there is a SQL batch processing problem
   * @throws SQLException when there is a SQL execution problem
   */
  abstract protected void doChanged(Connection connection, C dtos)
      throws SQLException, BatchException;

  /**
   * Pre-process the DELETED DTOs.
   * 
   * @param connection the database connection
   * @param dtos the collection of DELETED DTOs
   * @throws BatchException when there is a SQL batch processing problem
   * @throws SQLException when there is a SQL execution problem
   */
  abstract protected void doDeleted(Connection connection, C dtos)
      throws SQLException, BatchException;
}
