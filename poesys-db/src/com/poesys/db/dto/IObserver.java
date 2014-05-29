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


import java.io.Serializable;

import com.poesys.db.dao.DataEvent;


/**
 * The DTO observer provides the observer part of an Observer design pattern
 * interface for DTO objects. The Observer DTO receives update requests from
 * subjects that tell it to update itself as a result of a specified data event
 * affecting the specified subject. The observer will usually update its
 * internal lists of children based on the information in the update.
 * 
 * @author Robert J. Muller
 */
public interface IObserver extends Serializable {
  /**
   * Update the observer's state based on the specified child subject and the
   * specified data event.
   * 
   * @param subject the child subject notifying its parent of an event
   * @param event the event that has affected the child subject
   */
  void update(ISubject subject, DataEvent event);
}
