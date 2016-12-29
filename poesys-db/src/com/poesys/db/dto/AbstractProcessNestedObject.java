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


import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.dto.IDbDto.Status;


/**
 * <p>
 * An abstract implementation of the ISet interface that contains the logic for
 * processing a single DTO, determining which operation it requires. The
 * concrete subclass is usually a nested, private class within a DTO. The logic
 * uses the status of the DTO to determine whether to call the insert, update,
 * or delete method. The command then calls the appropriate abstract method
 * (insert, update, delete) with the appropriate collection. The class also
 * contains the abstract method getDto(), the implementation of which returns
 * the single DTO to process. If a DTO has a status other than NEW, CHANGED, or
 * DELETED, the processing ignores it.
 * </p>
 * 
 * <pre>
 * private class PostprocessChild extends AbstractProcessNestedObject {
 *   &#064;Override
 *   protected IDbDto getDto() {
 *     return getChild();
 *   }
 * 
 *   &#064;Override
 *   protected void doNew(Connection connection, IDbDto dto) {
 *     // Insert the child.
 *     IInsert dao =
 *       DaoManager.getFactory(Parent.class.getName()).getInsert(new InsertSqlChild());
 *     dao.insert(connection, dto);
 *   }
 * 
 *   &#064;Override
 *   protected void doChanged(Connection connection, IDbDto dtos) {
 *     // Update the child.
 *     IUpdate dao =
 *       DaoManager.getFactory(Parent.class.getName()).getUpdate(new UpdateSqlChild());
 *     dao.update(connection, dto);
 *   }
 * 
 *   &#064;Override
 *   protected void doDeleted(Connection connection, IDbDto dto) {
 *     // Do nothing, child already deleted
 *   }
 * }
 * </pre>
 * 
 * 
 * @see AbstractDto
 * 
 * @author Robert J. Muller
 * @param <T> the type of DTO to process
 */
public abstract class AbstractProcessNestedObject<T extends IDbDto> extends
    AbstractSetter<T> implements ISet {
  /** Serial version UID for Serializable object */
  private static final long serialVersionUID = 1L;

  /**
   * Create a AbstractProcessNestedObject object.
   * 
   * @param subsystem the subsystem in which the processing occurs
   * @param expiration the time in milliseconds after which the object expires
   *          in a cache that supports expiration
   */
  public AbstractProcessNestedObject(String subsystem, Integer expiration) {
    super(subsystem, expiration);
    setterName = AbstractProcessNestedObject.class.getName();
  }

  @Override
  protected void doSet(PoesysTrackingThread thread) {
    T dto = getDto();

    if (dto != null) {
      Status status = dto.getStatus();
      if (status == Status.NEW) {
        doNew(dto);
      } else if (status == Status.CHANGED) {
        // Process changed object or nested objects for existing object
        doChanged(dto);
      } else if (status == Status.DELETED
                 || status == IDbDto.Status.CASCADE_DELETED) {
        doDeleted(dto);
      }
    }
  }

  /**
   * Get the DTO to process.
   * 
   * @return a DTO
   */
  abstract protected T getDto();

  @Override
  public boolean isSet() {
    boolean isSet = false;
    if (getDto() != null) {
      // Check the tracking thread for processed status.
      PoesysTrackingThread thread =
        (PoesysTrackingThread)Thread.currentThread();
      if (thread.isProcessed(getDto().getPrimaryKey())) {
        // Processed, don't process.
        isSet = true;
      }
    } else {
      // Null DTO, don't process.
      isSet = true;
    }
    return isSet;
  }

  /**
   * Should the DTO factory create a primary key? You should supply false for
   * classes with identity keys.
   * 
   * @return true for inserts that supply a key value, false for identity-key
   *         inserts that autogenerate a key value
   */
  abstract protected boolean createKey();

  /**
   * Process the NEW DTO.
   * 
   * @param dto the collection of NEW DTOs
   */
  abstract protected void doNew(T dto);

  /**
   * Process the CHANGED DTO.
   * 
   * @param dto the CHANGED DTO
   */
  abstract protected void doChanged(T dto);

  /**
   * Process the DELETED DTO.
   * 
   * @param dto the DELETED DTO
   */
  abstract protected void doDeleted(T dto);
}
