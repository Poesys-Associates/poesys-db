/**
 * Copyright Phoenix Bioinformatics Corporation 2015. All rights reserved.
 */
package com.poesys.db.dto;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import com.poesys.db.dao.CacheDaoManager;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dao.MemcachedDaoManager;


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
  /** Message string when the connection is null */
  private static final String NO_CONN_MSG =
    "com.poesys.db.connection.msg.noConnection";
  /** Message string when the DTO is null */
  private static final String NO_DTO_MSG = "com.poesys.db.dao.msg.no_dto";

  /** Was the connection created by this object? */
  private boolean createdConnection = false;

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

    // Get the connection to use in the setter if required.
    Connection connection = getConnection(object);

    // Finally, iterate through the setters to process nested objects.
    if (readObjectSetters != null) {
      try {
        for (ISet set : readObjectSetters) {
          set.set(connection);
        }
      } catch (SQLException e) {
        // Should never happen, log and throw RuntimeException
        logger.error(READ_OBJECT_MSG, e);
        throw new RuntimeException(READ_OBJECT_MSG, e);
      } finally {
        try {
          if (connection != null && !connection.isClosed() && createdConnection) {
            // created rather than from connection cache, close here
            connection.close();
          }
        } catch (SQLException e) {
          logger.error(READ_OBJECT_MSG, e);
          throw new RuntimeException(READ_OBJECT_MSG, e);
        }
      }
    }
  }

  /**
   * Get a SQL connection. This will be either a connection from the
   * MemcachedDaoManager connection cache or a fresh connection from the input
   * object.
   *
   * @param object the input object
   * @return a SQL connection
   */
  public Connection getConnection(T object) {
    // Get the database connection for the key from the static cache.
    Connection connection =
      MemcachedDaoManager.getConnection(object.getPrimaryKey());

    if (connection == null) {
      // Not cached for this key, get new connection from object
      try {
        connection = object.getConnection();
        createdConnection = true;
      } catch (SQLException e) {
        logger.error("Exception getting connection from object", e);
        throw new RuntimeException(NO_CONN_MSG);
      }
    } else {
      logger.debug("Got cached connection " + connection + " for key "
                   + object.getPrimaryKey().getStringKey());
    }

    if (connection == null) {
      // Still can't get connection, stop trying.
      throw new RuntimeException(NO_CONN_MSG);
    }
    return connection;
  }

  /**
  *
  *
  */
  public void runConnectionSetters(List<ISet> setters) {
    // Set the connection caches for the nested objects.
    if (setters != null) {
      for (ISet set : setters) {
        try {
          set.set(null);
        } catch (SQLException e) {
          // Should never happen, log and throw RuntimeException
          logger.error(UNEXPECTED_SQL_ERROR, e);
          throw new RuntimeException(UNEXPECTED_SQL_ERROR, e);
        }
      }
    }
  }
}
