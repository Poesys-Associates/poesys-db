/**
 * Copyright Phoenix Bioinformatics Corporation 2015. All rights reserved.
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
 */
public abstract class GenericBaseClass<T> implements Serializable {
  private static final long serialVersionUID = 1L;
  private static Logger logger = Logger.getLogger(GenericBaseClass.class);

  private T object;
  
  public GenericBaseClass() {
    logger.info("Generic base object constructed with default constructor");
  }

  /**
   * Create a GenericBaseClass object.
   */
  public GenericBaseClass(T object) {
    this.object = object;
    logger.info("Generic base object constructed with object constructor");
  }

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
   * @throws ClassNotFoundException when there is no class for the object to create
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    logger.info("GenericBaseClass readObject reached");
  }
}
