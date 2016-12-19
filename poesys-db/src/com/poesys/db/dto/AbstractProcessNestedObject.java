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


import com.poesys.db.ConstraintViolationException;
import com.poesys.db.DbErrorException;
import com.poesys.db.dao.PoesysTrackingThread;


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
 *   protected void doNew(Connection connection, IDbDto dto),
 *       BatchException {
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
  }

  @Override
  public void set() {
    T dto = getDto();
    boolean isProcessed = false;
    PoesysTrackingThread thread = (PoesysTrackingThread)Thread.currentThread();

    if (dto != null && !isProcessed) {
      if (Thread.currentThread() instanceof PoesysTrackingThread) {
        // Currently processing in hierarchy, check processed status
        String key = dto.getPrimaryKey().getStringKey();
        if (thread.getDto(key) != null)
          isProcessed = thread.isProcessed(key);
      }

      try {
        IDbDto.Status status = dto.getStatus();
        if (status == IDbDto.Status.NEW) {
          doNew(dto);
        } else if (status == IDbDto.Status.CHANGED
                   || status == IDbDto.Status.EXISTING) {
          // Process changed object or nested objects for existing object
          doChanged(dto);
        } else if (status == IDbDto.Status.DELETED
                   || status == IDbDto.Status.CASCADE_DELETED) {
          doDeleted(dto);
        } // otherwise, ignore the DTO completely
      } catch (ConstraintViolationException e) {
        throw new DbErrorException(e.getMessage(), thread, e);
      }
    }
  }

  /**
   * Get the DTO to process.
   * 
   * @return a DTO
   */
  abstract protected T getDto();

  public boolean isSet() {
    // Set if the nested DTO is not null
    return getDto() != null;
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
