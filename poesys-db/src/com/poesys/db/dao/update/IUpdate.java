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
package com.poesys.db.dao.update;


import com.poesys.db.dto.IDbDto;


/**
 * An interface for a Command pattern class that updates a data transfer object
 * (DTO) in the database. The implementation identifies the object to update
 * based on the primary key of the object contained in the input DTO. The
 * concrete implementation should specify what aspects of the object it updates.
 * It must also update only if the DTO has status CHANGED. The caller should set
 * the DTO status to existing once <strong>all</strong> processing is complete
 * (over the entire inheritance hierarchy).
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to update
 */
public interface IUpdate<T extends IDbDto> {
  /**
   * An interface for a Command-pattern class that updates the contents of a DTO
   * in the database. The DTO must contain values for the primary key, which the
   * implementation will use to identify the object to update, and it must have
   * all fields set to reflect the complete, desired state for the object in the
   * database, including any composite aggregate parts belonging to the object.
   * The implementation must pre-process and post-process all composite parts
   * regardless of the update status of the top-level DTO.
   * 
   * @param dto the data transfer object containing the desired state of the
   *          object
   */
  public void update(T dto);

  /**
   * Close any resources allocated by the Command.
   */
  public void close();
}
