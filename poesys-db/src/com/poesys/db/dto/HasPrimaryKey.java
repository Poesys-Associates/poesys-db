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


import com.poesys.db.NoPrimaryKeyException;


/**
 * Implements a generic validator for checking that a Data Transfer Object (DTO)
 * has a primary key. Use this validator with any DTO.
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to validate
 */
public class HasPrimaryKey<T extends IDbDto> implements IValidate {
  /** Serial version UID for Serializable object */
  private static final long serialVersionUID = 1L;

  /** The DTO to validate */
  T dto = null;

  /** The no-primary-key-supplied message */
  private static final String NO_KEY_MSG =
    "com.poesys.db.dto.msg.no_primary_key";

  /**
   * Create a primary key validator for a specified DTO.
   * 
   * @param dto the DTO to validate
   */
  public HasPrimaryKey(T dto) {
    this.dto = dto;
  }

  @Override
  public void validate() {
    if (dto == null || dto.getPrimaryKey() == null) {
      throw new NoPrimaryKeyException(NO_KEY_MSG);
    }
  }
}
