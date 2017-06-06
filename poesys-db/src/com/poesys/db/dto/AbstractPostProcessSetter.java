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


import java.util.Collection;

import org.apache.log4j.Logger;

import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An abstract implementation of the ISet interface for a Strategy-pattern class
 * that handles the recursive post-processing of a collection of DTOs. The
 * abstract methods parameterize the class with objects that the set() method
 * uses in post processing. You use this class as the abstract superclass for a
 * nested class that handles recursive post-processing of a data member of the
 * outside class, and you implement the getDtos() method to either create a list
 * from a single-element data member or to return a collection of elements from
 * a multiple-element data member.
 * </p>
 * <p>
 * The end result is to post-process all the DTOs, calling the
 * postProcessNestedObjects() method on each DTO to perform the post processing,
 * then recurse to the next level of nested objects by calling the post-process
 * setters.
 * </p>
 * <p>
 * The set() method will track any untracked DTOs in the tracking thread and
 * will mark each DTO as processed before recursing into the DTO post
 * processing.
 * </p>
 * 
 * @author Robert J. Muller
 */
abstract public class AbstractPostProcessSetter extends AbstractSetter<IDbDto>
    implements ISet {
  /** Logger for this class */
  private static final Logger logger =
    Logger.getLogger(AbstractPostProcessSetter.class);

  /** Serial version UID for Serializable object */
  private static final long serialVersionUID = 1L;

  /**
   * Create a AbstractCollectionPostProcessSetter object.
   * 
   * @param subsystem the subsystem for the setter
   * @param expiration the time in milliseconds after which the object expires
   *          in a cache that supports expiration
   */
  public AbstractPostProcessSetter(String subsystem, Integer expiration) {
    super(subsystem, expiration);
    setterName = AbstractPostProcessSetter.class.getName();
  }

  @Override
  protected void doSet(PoesysTrackingThread thread) {
    // Process any DTOs not already post-processed.
    if (getDtos() != null) {
      for (IDbDto dto : getDtos()) {
        IPrimaryKey key = dto.getPrimaryKey();

        // Mark the DTO as processed in the tracking thread.
        if (thread.getDto(key) != null) {
          // Not already tracked, track the DTO.
          thread.addDto(dto);
        }

        if (!thread.isProcessed(key)) {
          thread.setProcessed(dto, true);
          logger.debug("Set " + key.getStringKey()
                       + ", post-processing nested objects");
          // DTO post setters do the actual post processing.
          dto.postprocessNestedObjects();
        } else {
          logger.debug(key.getStringKey() + " already set, returning");
        }
      }
    }
  }

  /**
   * Get the DTOs that contain the data to insert.
   * 
   * @return the collection of DTOs
   */
  abstract protected Collection<IDbDto> getDtos();

  @Override
  public boolean isSet() {
    // Return true if already processed.
    boolean set = false;
    // Return false if not in tracking thread.
    if (Thread.currentThread() instanceof PoesysTrackingThread) {
      PoesysTrackingThread thread =
        (PoesysTrackingThread)Thread.currentThread();
      if (getDtos() != null) {
        for (IDbDto dto : getDtos()) {
          if (dto.getPrimaryKey() == null) {
            // No primary key, not a database object, always set
            set = true;
          } else {
            set = thread.isProcessed(dto.getPrimaryKey());
          }
          if (!set) {
            if (thread.getDto(dto.getPrimaryKey()) == null) {
              // add the DTO to the thread history
              logger.debug("Adding " + dto.getPrimaryKey().getStringKey()
                           + " to thread history for thread " + thread.getId());
              thread.addDto(dto);
            }
            logger.debug("DTO " + dto.getPrimaryKey().getStringKey()
                         + " not set in thread " + thread.getId());
            // At least one DTO not set, end check.
            break;
          }
        }
      } else {
        // If there are no DTOs, the data member is set as there is nothing to
        // do for post processing.
        set = true;
      }
    }

    return set;
  }
}
