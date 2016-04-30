/**
 * Copyright Phoenix Bioinformatics Corporation 2015. All rights reserved.
 */
package com.poesys.db.dto;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import com.poesys.db.BatchException;
import com.poesys.db.dao.CacheDaoManager;
import com.poesys.db.dao.DataEvent;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dao.insert.IInsert;
import com.poesys.db.pk.IPrimaryKey;


/**
 * The base implementation of the IDbDto interface for DTOs and proxy DTOs;
 * contains code and data shared between all DTOs.
 * 
 * @author Robert J. Muller
 */
abstract public class AbstractDbDto implements IDbDto {

  /** serial version UID */
  private static final long serialVersionUID = 1L;

  /** log4j logger for this class */
  private static final Logger logger = Logger.getLogger(AbstractDbDto.class);

  /** primary key */
  protected IPrimaryKey key;

  /**
   * Message string when attempting to de-serialize a cached object and there is
   * some kind of exception
   */
  private static final String READ_OBJECT_MSG =
    "com.poesys.db.dto.msg.read_object";
  /** Message string when the primary key is null */
  private static final String NULL_KEY_MSG =
    "com.poesys.db.dto.msg.no_object_key";

  /** List of de-serialization setters for the DTO */
  protected List<ISet> readObjectSetters = null;

  /**
   * Create a AbstractDbDto object.
   */
  public AbstractDbDto() {
  }

  @Override
  abstract public int compareTo(IDbDto o);

  @Override
  abstract public void attach(IObserver observer, DataEvent event);

  @Override
  abstract public void detach(IObserver observer, DataEvent event);

  @Override
  abstract public void notify(DataEvent event);

  @Override
  abstract public void update(ISubject subject, DataEvent event);

  @Override
  abstract public Status getStatus();

  @Override
  abstract public boolean isAbstractClass();

  @Override
  abstract public void setAbstractClass(boolean isAbstract);

  @Override
  abstract public void setExisting();

  @Override
  abstract public void setChanged();

  @Override
  abstract public void setFailed();

  @Override
  abstract public void undoStatus();

  @Override
  abstract public boolean hasStatusChanged();

  @Override
  abstract public void finalizeStatus();

  @Override
  abstract public boolean isQueried();

  @Override
  abstract public boolean isProcessed();

  @Override
  abstract public void setProcessed(boolean processed);

  @Override
  abstract public void setQueried(boolean queried);

  @Override
  abstract public void queryNestedObjects(Connection connection)
      throws SQLException, BatchException;

  @Override
  abstract public void queryNestedObjectsForValidation(Connection connection)
      throws SQLException, BatchException;

  @Override
  abstract public void insertNestedObjects(Connection connection)
      throws SQLException, BatchException;

  @Override
  abstract public List<IInsert<? extends IDbDto>> getInserters();;

  @Override
  abstract public void preprocessNestedObjects(Connection connection)
      throws SQLException, BatchException;

  @Override
  abstract public void postprocessNestedObjects(Connection connection)
      throws SQLException, BatchException;

  @Override
  abstract public void validateForInsert(Connection connection)
      throws SQLException;

  @Override
  abstract public void validateForUpdate(Connection connection)
      throws SQLException;

  @Override
  abstract public void validateForDelete(Connection connection)
      throws SQLException;

  @Override
  abstract public void validateForQuery(Connection connection)
      throws SQLException;

  @Override
  public IPrimaryKey getPrimaryKey() {
    return key;
  }

  @Override
  abstract public void finalizeInsert(PreparedStatement stmt)
      throws SQLException;

  @Override
  abstract public void delete();

  @Override
  abstract public void markChildrenDeleted();

  @Override
  abstract public void cascadeDelete();

  @Override
  abstract public Connection getConnection() throws SQLException;

  @Override
  abstract public String getSubsystem();

  @Override
  abstract public boolean isSuppressNestedInserts();

  @Override
  abstract public void setSuppressNestedInserts(boolean suppressNestedInserts);

  @Override
  abstract public boolean isSuppressNestedPreInserts();

  @Override
  abstract public void setSuppressNestedPreInserts(boolean suppressNestedPreInserts);

  /**
   * Do the standard readObject operations on the DTO. This method implements
   * the operation shared by all the generated readObject methods, which can't
   * be inherited and must be private. The method calls the defaultReadObject()
   * method, validates the DTO, then calls the readObjectSetters to set the
   * transient data elements by query or cache retrieval.
   *
   * @param in the input stream
   * @throws IOException when there is a problem getting data
   * @throws ClassNotFoundException when the class to deserialize doesn't exist
   */
  public void doReadObject(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    Connection connection = null;

    logger.info("AbstractDto readObject method reached");
    // Check the stream input.
    if (in == null) {
      throw new RuntimeException(READ_OBJECT_MSG);
    }

    // First de-serialize the non-transient data using the default process.
    // Note: THIS MUST COME BEFORE ANY STREAM ACCESS.
    in.defaultReadObject();

    // Check for the primary key.
    if (key == null) {
      throw new RuntimeException(NULL_KEY_MSG);
    }

    // Cache the object in memory before getting nested objects.
    IDaoManager manager = CacheDaoManager.getInstance();
    manager.putObjectInCache(key.getCacheName(), 0, this);

    // Finally, iterate through the setters to process nested objects.
    if (readObjectSetters != null) {
      try {
        connection = getConnection();
        for (ISet set : readObjectSetters) {
          set.set(connection);
        }
      } catch (SQLException e) {
        // Should never happen, log and throw RuntimeException
        logger.error(READ_OBJECT_MSG, e);
        throw new RuntimeException(READ_OBJECT_MSG, e);
      } finally {
        try {
          if (connection != null && !connection.isClosed()) {
            connection.close();
          }
        } catch (SQLException e) {
          logger.error(READ_OBJECT_MSG, e);
          throw new RuntimeException(READ_OBJECT_MSG, e);
        }
      }
    }
  }
}
