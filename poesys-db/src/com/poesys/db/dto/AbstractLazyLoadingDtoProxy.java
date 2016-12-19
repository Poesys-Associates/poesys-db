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
import java.sql.PreparedStatement;
import java.util.List;

import org.apache.log4j.Logger;

import com.poesys.db.dao.DataEvent;
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

  private static final Logger logger =
    Logger.getLogger(AbstractLazyLoadingDtoProxy.class);

  /** proxied data transfer object */
  protected IDbDto dto;

  /** the deserializer used by the readOnly method */
  private static final Deserializer<AbstractLazyLoadingDtoProxy> deserializer =
    new Deserializer<AbstractLazyLoadingDtoProxy>();

  /** List of de-serialization setters for the DTO */
  protected List<ISet> readObjectSetters = null;

  /** List of de-serialization key-cache setters for the DTO */
  protected List<ISet> connectionCacheSetters = null;

  /** List of de-serialization key-cache unsetters for the DTO */
  protected List<ISet> connectionCacheUnsetters = null;

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
    logger.debug("Deserializing object of class " + this.getClass().getName()
                 + " with readObject in AbstractLazyLoadingDtoProxy");
    // Do the read-object deserialization.
    deserializer.doReadObject(in, this);
  }

  @Override
  public void deserializeNestedObjects() {
    if (readObjectSetters != null) {
      for (ISet set : readObjectSetters) {
        set.set();
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
  public Status getStatus() {
    return dto.getStatus();
  }

  @Override
  public void insertNestedObjects() {
    dto.insertNestedObjects();
  }

  @Override
  public List<IInsert<? extends IDbDto>> getInserters() {
    return dto.getInserters();
  }

  @Override
  public void postprocessNestedObjects() {
    dto.postprocessNestedObjects();
  }

  @Override
  public void preprocessNestedObjects() {
    dto.preprocessNestedObjects();
  }

  @Override
  public void queryNestedObjects() {
    dto.queryNestedObjects();
    if (readObjectSetters != null) {
      for (ISet set : readObjectSetters) {
        if (!set.isSet()) {
          set.set();
        }
      }
    }
  }

  @Override
  public void queryNestedObjectsForValidation() {
    dto.queryNestedObjectsForValidation();
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
  public void validateForDelete() {
    dto.validateForDelete();
  }

  @Override
  public void validateForInsert() {
    dto.validateForInsert();
  }

  @Override
  public void validateForQuery() {
    dto.validateForQuery();
  }

  @Override
  public void validateForUpdate() {
    dto.validateForUpdate();
  }

  @Override
  public int compareTo(IDbDto o) {
    return dto.compareTo(o);
  }

  @Override
  public void finalizeInsert(PreparedStatement stmt) {
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
}
