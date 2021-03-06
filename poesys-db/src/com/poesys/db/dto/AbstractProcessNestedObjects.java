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


import java.util.ArrayList;
import java.util.Collection;

import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.dto.IDbDto.Status;


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
 *   protected void doNew(Connection connection, List&lt;Child&gt; dtos) {
 *     IDaoFactory&lt;Child&gt; factory = DaoManager.getFactory(Parent.class.getName());
 *     // Insert the children.
 *     IInsertBatch&lt;Child&gt; dao = factory.getInsertBatch(new InsertSqlChild());
 *     dao.insert(connection, dtos, CHILD_BATCH_SIZE);
 *   }
 * 
 *   &#064;Override
 *   protected void doChanged(Connection connection, List&lt;Child&gt; dtos) {
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
   * @param expiration the time in milliseconds after which the object expires
   *          in a cache that supports expiration
   */
  public AbstractProcessNestedObjects(String subsystem, Integer expiration) {
    super(subsystem, expiration);
    setterName = AbstractProcessNestedObjects.class.getName();
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void doSet(PoesysTrackingThread thread) {
    C inserts = (C)new ArrayList<T>();
    C updates = (C)new ArrayList<T>();
    C deletes = (C)new ArrayList<T>();

    C dtos = getDtos();

    if (dtos != null) {
      for (T dto : dtos) {
        Status status = dto.getStatus();
        if (status == Status.NEW) {
          inserts.add(dto);
        } else if (status == Status.CHANGED) {
          updates.add(dto);
        } else if (status == Status.DELETED || status == Status.CASCADE_DELETED) {
          deletes.add(dto);
        }
      }
    }

    doNew(inserts);
    doChanged(updates);
    doDeleted(deletes);
  }

  /**
   * Get the list of DTOs to process.
   * 
   * @return a list of DTOs
   */
  abstract protected C getDtos();

  public boolean isSet() {
    boolean isSet = false;
    if (getDtos() != null && getDtos().size() > 0) {
      // Check the tracking thread for processed status.
      PoesysTrackingThread thread =
        (PoesysTrackingThread)Thread.currentThread();
      for (T dto : getDtos()) {
        if (thread.isProcessed(dto.getPrimaryKey())) {
          // Processed, don't process.
          isSet = true;
        } else {
          // At least one unprocessed DTO, member needs processing.
          isSet = false;
          break;
        }
      }
    } else {
      // Null or empty DTO collection, don't process.
      isSet = true;
    }
    return isSet;
  }

  /**
   * Pre-process the NEW DTOs.
   * 
   * @param dtos the collection of NEW DTOs
   */
  abstract protected void doNew(C dtos);

  /**
   * Pre-process the CHANGED DTOs.
   * 
   * @param dtos the collection of CHANGED DTOs
   */
  abstract protected void doChanged(C dtos);

  /**
   * Pre-process the DELETED DTOs.
   * 
   * @param dtos the collection of DELETED DTOs
   */
  abstract protected void doDeleted(C dtos);
}
