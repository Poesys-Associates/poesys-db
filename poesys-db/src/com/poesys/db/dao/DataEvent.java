/*
 * Copyright (c) 2010 Poesys Associates. All rights reserved.
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


import com.poesys.db.dto.IObserver;
import com.poesys.db.dto.ISubject;


/**
 * <p>
 * The DataEvent enumeration specifies the different events that can occur in
 * the data access system that are of interest to components of the system.
 * </p>
 * <ul>
 * <li>QUERY: a DAO has queried an object or collection of objects from a data
 * store</li>
 * <li>INSERT: a DAO has inserted a new object to a data store</li>
 * <li>UPDATE: a DAO has updated an existing object in a data store</li>
 * <li>DELETE: a DAO has deleted an existing object from a data store</li>
 * <li>MARKED_DELETED: a client has marked a subject DTO as deleted</li>
 * </ul>
 * 
 * @see ISubject
 * @see IObserver
 * 
 * @author Robert J. Muller
 */
public enum DataEvent {
  /** a DAO has queried an object or collection or objects from a data store */
  QUERY,
  /** a DAO has inserted a new object to a data store */
  INSERT,
  /** a DAO has updated an existing object in a data store */
  UPDATE,
  /** a DAO has deleted an existing object from a data store */
  DELETE,
  /** a client has marked a subject DTO as deleted */
  MARKED_DELETED;
}
