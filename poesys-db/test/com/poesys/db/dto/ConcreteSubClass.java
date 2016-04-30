/**
 * Copyright Phoenix Bioinformatics Corporation 2015. All rights reserved.
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
