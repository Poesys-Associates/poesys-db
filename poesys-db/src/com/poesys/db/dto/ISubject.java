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
package com.poesys.db.dto;


import com.poesys.db.dao.DataEvent;
import com.poesys.db.pk.IPrimaryKey;


/**
 * The DTO subject provides the subject part of the Observer design pattern
 * interface for DTO objects. The Subject DTO can attach itself to a parent
 * observer, detach itself from a parent observer, or notify all attached
 * observers of a particular data event. The DTO implements this interface if it
 * is a child to some number of parent observers. The implementation must
 * distinguish its registered observers by the kind of events they observe.
 * 
 * @author Robert J. Muller
 */
public interface ISubject {
  /**
   * Attach the subject child to an observer parent. The parent calls this
   * method on the child with a self reference.
   * 
   * @param observer the parent to which to attach the subject child
   * @param event the specific event for the parent observer to observe
   */
  void attach(IObserver observer, DataEvent event);

  /**
   * Detach the subject child from an observer parent. The parent calls this
   * method on the child with a self reference.
   * 
   * @param observer the parent to which to attached the subject child
   * @param event the specific event being observed by the parent observer
   */
  void detach(IObserver observer, DataEvent event);

  /**
   * Instruct the subject child to update its observer parents of an event. A
   * client of the child, such as a DAO, calls this method after doing the
   * actions that constitute the event. If necessary, the child itself can call
   * the method to notify itself of the event.
   * 
   * @param event the kind of event that has affected the child
   */
  void notify(DataEvent event);

  /**
   * Get the primary key for the subject DTO.
   * 
   * @return a primary key that identifies the DTO in its class
   */
  IPrimaryKey getPrimaryKey();
}
