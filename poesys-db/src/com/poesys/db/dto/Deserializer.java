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


import java.io.IOException;
import java.io.ObjectInputStream;


/**
 * A helper class that provides shared code for deserializing cached objects
 * 
 * @author Robert J. Muller
 * 
 * @param <T> the type of DTO object
 */
public class Deserializer<T extends IDbDto> {

  /**
   * Message string when attempting to de-serialize a cached object and there is
   * some kind of exception
   */
  private static final String READ_OBJECT_MSG =
    "com.poesys.db.dto.msg.read_object";
  /** Message string when the primary key is null */
  private static final String NULL_KEY_MSG =
    "com.poesys.db.dto.msg.no_object_key";
  /** Message string when the DTO is null */
  private static final String NO_DTO_MSG = "com.poesys.db.dao.msg.no_dto";

  /**
   * Create a AbstractDbDto object.
   */
  public Deserializer() {
  }

  /**
   * Do the standard readObject operations on the DTO. This method implements
   * the operation shared by all the generated readObject methods, which can't
   * be inherited and must be private. The method calls the defaultReadObject()
   * method and validates the DTO.
   *
   * @param in the input stream containing the serialized object(s)
   * @param object the object
   * @throws IOException when there is a problem getting data
   * @throws ClassNotFoundException when the class to deserialize doesn't exist
   */
  public void doReadObject(ObjectInputStream in, T object) throws IOException,
      ClassNotFoundException {

    // Check the stream input.
    if (in == null) {
      throw new RuntimeException(READ_OBJECT_MSG);
    }

    // Check the object.
    if (object == null) {
      throw new RuntimeException(NO_DTO_MSG);
    }

    // First de-serialize the non-transient data using the default process.
    // Note: THIS MUST COME BEFORE ANY STREAM ACCESS.
    in.defaultReadObject();

    // Check for the primary key.
    if (object.getPrimaryKey() == null) {
      throw new RuntimeException(NULL_KEY_MSG);
    }
  }
}
