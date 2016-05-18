/*
 * Copyright (c) 2011 Poesys Associates. All rights reserved.
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

/**
 * An abstract class that centralizes things for all setters.
 * 
 * @author Robert J. Muller
 * @param <T> the kind of IDbDto processed by the setter
 */
public abstract class AbstractSetter<T extends IDbDto> implements ISet {
  /** Serial version UID for Serializable object */
  private static final long serialVersionUID = 1L;

  /** The subsystem in which the setter accomplishes its tasks */
  protected String subsystem;
  /** the cache expiration time in milliseconds for T objects */
  protected final Integer expiration;

  /**
   * Create an AbstractSetter object.
   * 
   * @param subsystem the subsystem for the setter
   * @param expiration the cache expiration time in milliseconds for T objects
   */
  public AbstractSetter(String subsystem, Integer expiration) {
    this.subsystem = subsystem;
    this.expiration = expiration == null ? Integer.MAX_VALUE : expiration;
  }

  /**
   * Get the class name to use to look up a cached DTO.
   * 
   * @return the DTO class name
   */
  abstract protected String getClassName();
}
