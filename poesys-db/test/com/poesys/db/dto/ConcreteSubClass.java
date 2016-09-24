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

import org.apache.log4j.Logger;


/**
 * Represents a generic instantiation as a concrete subclass of a generic base
 * class
 * 
 * @author Robert J. Muller
 */
public class ConcreteSubClass extends GenericBaseClass<String> {
  private static final long serialVersionUID = 1L;
  private static Logger logger = Logger.getLogger(ConcreteSubClass.class);

  public ConcreteSubClass() {
    super();
    logger.info("Concreted subobject constructed with default constructor");
  }
  public ConcreteSubClass(String object) {
    super(object);
    logger.info("Concrete subobject constructed with object constructor");
  }

  @Override
  public String toString() {
    return "ConcreteSubClass [" + super.toString() + "]";
  }

  /**
   * Standard readObject method for deserialization
   *
   * @param in the serialized input stream
   * @throws IOException when there is a problem deserializing
   * @throws ClassNotFoundException when there is no class for the object to create
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    logger.info("ConcreteSubClass readObject reached");
  }
}
