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
package com.poesys.db.dao.insert;


import com.poesys.db.dto.IDbDto;


/**
 * <p>
 * An interface for a Command pattern class that inserts an object or link into
 * the database based on values in a data transfer object (DTO). The object may
 * be a stand-alone object or it may contain collections of sub-objects linked
 * to the top-level object.
 * </p>
 * <p>
 * The logic of tree insertion requires that certain nested objects be inserted
 * while others are not. Only objects that cannot exist without the object being
 * inserted should also be inserted. The following kinds of nested object should
 * have a corresponding ISet command associated with the field:
 * </p>
 * <ul>
 * <li>A <em>composite</em> object or collection of objects; that is,
 * parent-child relationships or ownership relationships</li>
 * <li>An <em>association</em> object or collection of objects as opposed to the
 * associated objects; this object represents the association itself, and the
 * parent object owns the relationship.
 * </ul>
 * <p>
 * Only those association objects where all associated objects other than the
 * current object are already inserted in the database should be inserted, as
 * otherwise you will get a foreign key failure. The ISet command must check the
 * associated objects for database status using the isNew method on the IDto.
 * </p>
 * <p>
 * After the insert operation, the status should be EXISTING. The current
 * implementation handles inheritance externally by undoing this status change
 * for superclass inserts, finalizing the status to EXISTING only after
 * inserting the concrete leaf class.
 * </p>
 * 
 * @see com.poesys.db.dto.ISet
 * @see com.poesys.db.dto.IDbDto
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to insert
 */
public interface IInsert<T extends IDbDto> {
  /**
   * Insert the contents of a new DTO object into the database.
   * 
   * @param dto the DTO containing the objects to insert into the database; the
   *          key data members must have values; the isNew() flag must be true
   */
  public void insert(IDbDto dto);

  /**
   * Close any resources allocated by the Command.
   */
  public void close();
}
