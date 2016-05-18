/**
 * Copyright Phoenix Bioinformatics Corporation 2015. All rights reserved.
 */
package com.poesys.db.dto;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import com.poesys.db.dao.CacheDaoManager;
import com.poesys.db.dao.IDaoManager;


/**
 * A helper class that provides shared code for deserializing cached objects
 * 
 * @author Robert J. Muller
 */
public class Deserializer<T extends IDbDto> {

  /** log4j logger for this class */
  private static final Logger logger = Logger.getLogger(Deserializer.class);

  /**
   * Message string when attempting to de-serialize a cached object and there is
   * some kind of exception
   */
  private static final String READ_OBJECT_MSG =
    "com.poesys.db.dto.msg.read_object";
  /** Unexpected SQL error message */
  private static final String UNEXPECTED_SQL_ERROR =
    "com.poesys.db.dto.msg.unexpected_sql_error";
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
   * method, validates the DTO, then calls the readObjectSetters to set the
   * transient data elements by query or cache retrieval.
   *
   * @param in the input stream containing the serialized object(s)
   * @param object the object
   * @param readObjectSetters a list of setters for reading objects
   * @throws IOException when there is a problem getting data
   * @throws ClassNotFoundException when the class to deserialize doesn't exist
   */
  public void doReadObject(ObjectInputStream in, T object,
                           List<ISet> readObjectSetters) throws IOException,
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

    // Cache the object in memory before getting nested objects.
    IDaoManager manager = CacheDaoManager.getInstance();
    manager.putObjectInCache(object.getPrimaryKey().getCacheName(), 0, object);

    // Finally, iterate through the setters to process nested objects.
    if (readObjectSetters != null) {
      try {
        for (ISet set : readObjectSetters) {
          set.set(null);
        }
      } catch (SQLException e) {
        // Should never happen, log and throw RuntimeException
        logger.error(READ_OBJECT_MSG, e);
        throw new RuntimeException(READ_OBJECT_MSG, e);
      }
    }
  }
}
