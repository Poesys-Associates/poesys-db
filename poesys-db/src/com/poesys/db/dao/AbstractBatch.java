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
package com.poesys.db.dao;


import com.poesys.db.InvalidParametersException;
import com.poesys.db.Message;
import com.poesys.db.dto.IDbDto;


/**
 * An abstract superclass for batch-oriented data access object (DAO) classes
 * 
 * @see com.poesys.db.dto.IDbDto
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to process
 */
public abstract class AbstractBatch<T extends IDbDto> {

  /** the subsystem of the DTO class */
  protected final String subsystem;

  /** Error message when no subsystem supplied */
  private static final String NULL_SUBSYSTEM_ERROR =
    "com.poesys.db.dao.msg.null_subsystem";

  /**
   * Create a AbstractBatch object.
   *
   * @param subsystem the subsystem of the DTO classes processed
   */
  public AbstractBatch(String subsystem) {
    if (subsystem == null) {
      throw new InvalidParametersException(Message.getMessage(NULL_SUBSYSTEM_ERROR,
                                                              null));
    }
    this.subsystem = subsystem;
  }
}
