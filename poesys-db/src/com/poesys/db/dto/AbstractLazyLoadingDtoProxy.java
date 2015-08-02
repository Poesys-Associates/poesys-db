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


import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import com.poesys.db.BatchException;
import com.poesys.db.Message;
import com.poesys.db.dao.CacheDaoManager;
import com.poesys.db.dao.DataEvent;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dao.insert.IInsert;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * The AbstractLazyLoadingDtoProxy is a Proxy-pattern class that abstracts the
 * shared services required to proxy a data transfer object (DTO) to add
 * lazy-loading services to its members. Lazy loading is the capability of
 * retrieving a DTO with one or more members (usually association collections)
 * unpopulated, then populating those members when a client calls the proxy
 * accessor for the member (getX(), where x is the member). Because lazy loading
 * usually accesses the database, the implementation must get an appropriate
 * connection from the ConnectionConnectionFactory. The lazy loading accessor
 * tests a boolean flag that tells it whether to load and uses the local
 * connection to process the loading with a setter (ISet instance).
 * <p>
 * 
 * @see com.poesys.db.dto.ISet
 * @see com.poesys.db.dto.IDbDto
 * 
 * @author Robert J. Muller
 */
public abstract class AbstractLazyLoadingDtoProxy implements IDbDto {
  /** Serial version UID for Serializable object */
  private static final long serialVersionUID = 1L;
  /** Log4j logging */
  private static final Logger logger =
    Logger.getLogger(AbstractLazyLoadingDtoProxy.class);

  /** proxied data transfer object */
  protected IDbDto dto;

  /** List of de-serialization setters for the proxy */
  protected List<ISet> readObjectSetters = null;

  /**
   * Message string when attempting to de-serialize a cached object and there is
   * some kind of exception
   */
  private static final String READ_OBJECT_MSG =
    "com.poesys.db.dto.msg.read_object";

  /**
   * Create a AbstractLazyLoadingDtoProxy object.
   * 
   * @param dto the proxied database data transfer object
   */
  public AbstractLazyLoadingDtoProxy(IDbDto dto) {
    this.dto = dto;
  }

  @Override
  public boolean isAbstractClass() {
    return dto.isAbstractClass();
  }

  @Override
  public void setAbstractClass(boolean isAbstract) {
    dto.setAbstractClass(isAbstract);
  }

  @Override
  public String getSubsystem() {
    return dto.getSubsystem();
  }

  /**
   * Read an object from an input stream, de-serializing it. This custom
   * de-serialization method calls the default read-object method to read in all
   * non-transient fields then runs a series of setters in the readObjectSetters
   * list to de-serialize any transient elements.
   * 
   * @param in the object input stream
   * @throws ClassNotFoundException when a nested object class can't be found
   * @throws IOException when there is an IO problem reading the stream
   */
  private void readObject(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    Connection connection = null;

    // First de-serialize the non-transient data using the default process.
    in.defaultReadObject();

    // Cache the object in memory before getting nested objects.
    IDaoManager manager = CacheDaoManager.getInstance();
    manager.putObjectInCache(dto.getPrimaryKey().getCacheName(), 0, this);

    // Finally, iterate through the setters to process nested objects.
    if (readObjectSetters != null) {
      try {
        connection = getConnection();
        for (ISet set : readObjectSetters) {
          set.set(connection);
        }
      } catch (SQLException e) {
        // Should never happen, log and throw RuntimeException
        String msg = Message.getMessage(READ_OBJECT_MSG, null);
        logger.error(msg + ": Connection problem", e);
        throw new RuntimeException(READ_OBJECT_MSG, e);
      } catch (Throwable e) {
        // Probably a memcached problem, just log the error.
        String msg = Message.getMessage(READ_OBJECT_MSG, null);
        logger.error(msg, e);
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

  @Override
  public void delete() {
    dto.delete();
  }

  @Override
  public void markChildrenDeleted() {
    dto.markChildrenDeleted();
  }

  @Override
  public void cascadeDelete() {
    dto.cascadeDelete();
  }

  @Override
  public IPrimaryKey getPrimaryKey() {
    return dto.getPrimaryKey();
  }

  @Override
  public Connection getConnection() throws SQLException {
    return dto.getConnection();
  }

  @Override
  public Status getStatus() {
    return dto.getStatus();
  }

  @Override
  public void insertNestedObjects(Connection connection) throws SQLException,
      BatchException {
    dto.insertNestedObjects(connection);
  }

  @Override
  public List<IInsert<? extends IDbDto>> getInserters() {
    return dto.getInserters();
  }

  @Override
  public void postprocessNestedObjects(Connection connection)
      throws SQLException, BatchException {
    dto.postprocessNestedObjects(connection);
  }

  @Override
  public void preprocessNestedObjects(Connection connection)
      throws SQLException, BatchException {
    dto.preprocessNestedObjects(connection);
  }

  @Override
  public void queryNestedObjects(Connection connection) throws SQLException,
      BatchException {
    dto.queryNestedObjects(connection);
  }

  @Override
  public void queryNestedObjectsForValidation(Connection connection)
      throws SQLException, BatchException {
    dto.queryNestedObjectsForValidation(connection);
  }

  @Override
  public void setChanged() {
    dto.setChanged();
  }

  @Override
  public void setExisting() {
    dto.setExisting();
  }

  @Override
  public void setFailed() {
    dto.setFailed();
  }

  @Override
  public void undoStatus() {
    dto.undoStatus();
  }

  @Override
  public boolean hasStatusChanged() {
    return dto.hasStatusChanged();
  }

  @Override
  public boolean isQueried() {
    return dto.isQueried();
  }

  @Override
  public void setQueried(boolean queried) {
    dto.setQueried(queried);
  }

  @Override
  public void validateForDelete(Connection connection) throws SQLException {
    dto.validateForDelete(connection);
  }

  @Override
  public void validateForInsert(Connection connection) throws SQLException {
    dto.validateForInsert(connection);
  }

  @Override
  public void validateForQuery(Connection connection) throws SQLException {
    dto.validateForQuery(connection);
  }

  @Override
  public void validateForUpdate(Connection connection) throws SQLException {
    dto.validateForUpdate(connection);
  }

  @Override
  public int compareTo(IDbDto o) {
    return dto.compareTo(o);
  }

  @Override
  public void finalizeInsert(PreparedStatement stmt) throws SQLException {
    // No action required--default implementation
  }

  // Observer pattern interface methods: pass through to DTO

  @Override
  public void attach(IObserver observer, DataEvent event) {
    dto.attach(observer, event);
  }

  @Override
  public void detach(IObserver observer, DataEvent event) {
    dto.detach(observer, event);
  }

  @Override
  public void notify(DataEvent event) {
    dto.notify(event);

  }

  @Override
  public void update(ISubject subject, DataEvent event) {
    dto.update(subject, event);
  }

  @Override
  public void finalizeStatus() {
    dto.finalizeStatus();
  }

  @Override
  public boolean isSuppressNestedInserts() {
    return dto.isSuppressNestedInserts();
  }

  @Override
  public void setSuppressNestedInserts(boolean suppressNestedInserts) {
    dto.setSuppressNestedInserts(suppressNestedInserts);
  }

  @Override
  public boolean isSuppressNestedPreInserts() {
    return dto.isSuppressNestedPreInserts();
  }

  @Override
  public void setSuppressNestedPreInserts(boolean suppressNestedPreInserts) {
    dto.setSuppressNestedPreInserts(suppressNestedPreInserts);
  }

  @Override
  public boolean isProcessed() {
    return dto.isProcessed();
  }

  @Override
  public void setProcessed(boolean processed) {
    dto.setProcessed(processed);
  }
}
