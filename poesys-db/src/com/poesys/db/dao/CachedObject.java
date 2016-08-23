/*
 * Copyright (c) 2016 Poesys Associates. All rights reserved.
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


import com.poesys.db.dto.IDbDto;


/**
 * A container for an IDbDto object retrieved from an object cache
 * 
 * @author Robert J. Muller
 *
 */
public class CachedObject {
  private final IDbDto dto;
  boolean isProcessed = false;

  /**
   * Create a CachedObject, wrapping a DTO.
   *
   * @param dto the object retrieved from the cache
   */
  public CachedObject(IDbDto dto) {
    super();
    this.dto = dto;
  }

  /**
   * Has the DTO been processed in the current thread? Generally, a value of
   * true means you should not further process the DTO.
   * 
   * @return true if processed, false if not
   */
  public boolean isProcessed() {
    return isProcessed;
  }

  /**
   * Mark the DTO as processed in the current thread.
   * 
   * @param isProcessed
   */
  public void setProcessed(boolean isProcessed) {
    this.isProcessed = isProcessed;
  }

  /**
   * Get the DTO.
   * 
   * @return the DTO
   */
  public IDbDto getDto() {
    return dto;
  }

  /**
   * Get a string representation of the primary key of the DTO; this is the key
   * that you use to access the object in the cache, it contains the fully
   * qualified class name in addition to the primary key attributes, providing a
   * globally unique key to the object.
   * 
   * @return the key string
   */
  public String getPrimaryKeyValue() {
    return dto.getPrimaryKey().getStringKey();
  }
}
