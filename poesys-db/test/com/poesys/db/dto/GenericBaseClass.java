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
import java.io.Serializable;

import org.apache.log4j.Logger;


/**
 * This class represents a generic, abstract base class for an inheritance
 * hierarchy.
 * 
 * @author Robert J. Muller
 * 
 * @param <T> the type of the nested object
 */
public abstract class GenericBaseClass<T> implements Serializable {
  private static final long serialVersionUID = 1L;
  private static Logger logger = Logger.getLogger(GenericBaseClass.class);

  private T object;

  /**
   * Create a GenericBaseClass object.
   */
  public GenericBaseClass() {
    logger.info("Generic base object constructed with default constructor");
  }

  /**
   * Create a GenericBaseClass object.
   * @param object the object
   */
  public GenericBaseClass(T object) {
    this.object = object;
    logger.info("Generic base object constructed with object constructor");
  }

  /**
   * Get the nested object.
   * 
   * @return the nested object
   */
  public T getNestedObject() {
    return object;
  }

  @Override
  public String toString() {
    return "GenericBaseClass [object=" + object + "]";
  }

  /**
   * Standard readObject method for deserialization
   *
   * @param in the serialized input stream
   * @throws IOException when there is a problem deserializing
   * @throws ClassNotFoundException when there is no class for the object to
   *           create
   */
  private void readObject(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.defaultReadObject();
    logger.info("GenericBaseClass readObject reached");
  }
}
