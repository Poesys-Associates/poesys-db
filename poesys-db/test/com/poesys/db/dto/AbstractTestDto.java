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
package com.poesys.db.dto;

/**
 * An abstract superclass that contains methods implemented for all the test
 * objects. These are methods that don't require any custom code for testing.
 * 
 * @author Robert J. Muller
 */
public abstract class AbstractTestDto extends AbstractDto {

  /** Serial version UID for the Serialized object */
  private static final long serialVersionUID = 1L;

  /**
   * Create a AbstractTestDto object.
   */
  public AbstractTestDto() {
    super();
  }

  @Override
  public String getSubsystem() {
    return "com.poesys.db.poesystest.mysql";
  }
}