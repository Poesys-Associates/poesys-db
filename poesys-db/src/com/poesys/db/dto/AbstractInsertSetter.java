/*
 * Copyright (c) 2009 Poesys Associates. All rights reserved.
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


import java.util.Collection;

import com.poesys.db.ConstraintViolationException;
import com.poesys.db.DbErrorException;
import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.dao.insert.IInsert;
import com.poesys.db.dto.IDbDto.Status;


/**
 * An abstract implementation of the ISet interface for a Strategy-pattern class
 * that inserts a single DTO as part of a more comprehensive transaction, taking
 * in a connection and not closing it. Note that if the DTO is part of an
 * inheritance hierarchy, the setter will insert one row for each class in the
 * hierarchy from the root down.
 * 
 * @author Robert J. Muller
 */
abstract public class AbstractInsertSetter extends AbstractSetter<IDbDto>
    implements ISet {

  /** Serial version UID for Serializable object */
  private static final long serialVersionUID = 1L;

  /**
   * Create an AbstractInsertSetter object.
   * 
   * @param subsystem the subsystem for the setter
   * @param expiration the time in milliseconds after which the object expires
   *          in a cache that supports expiration
   */
  public AbstractInsertSetter(String subsystem, Integer expiration) {
    super(subsystem, expiration);
    setterName = AbstractInsertSetter.class.getName();
  }

  @Override
  protected void doSet(PoesysTrackingThread thread) {
    try {
      // Go through all the DTOs and insert them.
      if (getDtos() != null) {
        for (IDbDto dto : getDtos()) {
          // Only attempt an insert if the object is there and needs to be
          // inserted!
          if (dto != null && dto.getStatus() == IDbDto.Status.NEW) {
            // Insert class portions of object from root to leaf.
            if (dto.getInserters() != null) {
              // Suppress further nested inserts until the last insert.
              int i = 0;
              int last = dto.getInserters().size() - 1;
              // If only 1, last will be 0. Process in that case.
              boolean savedSuppress = dto.isSuppressNestedInserts();
              if (last > 0) {
                dto.setSuppressNestedInserts(true);
              }
              for (IInsert<? extends IDbDto> dao : dto.getInserters()) {
                dao.insert(dto);
                i++;
                if (i == last) {
                  // Restore the current suppression setting.
                  dto.setSuppressNestedInserts(savedSuppress);
                }
              }
              // After everything has inserted, set the DTO status to EXISTING.
              dto.setExisting();
            }
          }
        }
      }
    } catch (ConstraintViolationException e) {
      throw new DbErrorException(e.getMessage(), thread, e);
    }
  }

  /**
   * Get the class name to use to look up a cached DTO to invalidate.
   * 
   * @return the class name
   */
  abstract protected String getClassName();

  /**
   * Should the DTO factory create a primary key? You should supply false for
   * classes with identity keys.
   * 
   * @return true for inserts that supply a key value, false for identity-key
   *         inserts that autogenerate a key value
   */
  abstract protected boolean createKey();

  /**
   * Get the DTOs to insert.
   * 
   * @return a collection of DTOs
   */
  abstract protected Collection<IDbDto> getDtos();

  public boolean isSet() {
    boolean set = true;
    // Set if status is not NEW in the DTOs
    for (IDbDto dto : getDtos()) {
      if (Status.NEW.compareTo(dto.getStatus()) == 0) {
        // At least one DTO has status NEW, so not set
        set = false;
        break;
      }
    }
    return set;
  }
}
