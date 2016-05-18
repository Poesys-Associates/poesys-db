/*
 * Copyright (c) 2009 Poesys Associates. All rights reserved.
 */
package com.poesys.db.dto;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import com.poesys.db.BatchException;
import com.poesys.db.ConstraintViolationException;
import com.poesys.db.DbErrorException;
import com.poesys.db.connection.IConnectionFactory.DBMS;
import com.poesys.db.dao.insert.IInsert;
import com.poesys.db.dto.IDbDto.Status;


/**
 * An abstract implementation of the ISet interface for a Strategy-pattern class
 * that inserts a single DTO as part of a more comprehensive transaction, taking
 * in a connection and not closing it. Note that if the DTO is part of an
 * inheritance hierarchy, the setter will insert one row for each class in the
 * hierarchy from the root down.
 * 
 * @author Robert J. Muller
 */
abstract public class AbstractInsertSetter extends AbstractSetter<IDbDto>
    implements ISet {

  /** Serial version UID for Serializable object */
  private static final long serialVersionUID = 1L;

  /**
   * Create an AbstractInsertSetter object.
   * 
   * @param subsystem the subsystem for the setter
   * @param dbms the type of DBMS to which to connect
   * @param expiration the time in milliseconds after which the object expires
   *          in a cache that supports expiration
   */
  public AbstractInsertSetter(String subsystem, DBMS dbms, Integer expiration) {
    super(subsystem, dbms, expiration);
  }

  @Override
  public void set(Connection connection) throws SQLException {
    try {
      // Go through all the DTOs and insert them.
      if (getDtos() != null) {
        for (IDbDto dto : getDtos()) {
          // Only attempt an insert if the object is there and needs to be
          // inserted!
          if (dto != null && !dto.isProcessed()
              && dto.getStatus() == IDbDto.Status.NEW) {
            // Insert class portions of object from root to leaf.
            if (dto.getInserters() != null) {
              // Suppress further nested inserts until the last insert.
              int i = 0;
              int last = dto.getInserters().size() - 1;
              // If only 1, last will be 0. Process in that case.
              boolean savedSuppress = dto.isSuppressNestedInserts();
              if (last > 0) {
                dto.setSuppressNestedInserts(true);
              }
              for (IInsert<? extends IDbDto> dao : dto.getInserters()) {
                dao.insert(connection, dto);
                i++;
                if (i == last) {
                  // Restore the current suppression setting.
                  dto.setSuppressNestedInserts(savedSuppress);
                }
              }
              // After everything has inserted, set the DTO status to EXISTING.
              dto.setExisting();
            }
          }
        }
      }
    } catch (ConstraintViolationException e) {
      throw new DbErrorException(e.getMessage(), e);
    } catch (BatchException e) {
      throw new DbErrorException(e.getMessage(), e);
    } catch (DtoStatusException e) {
      throw new DbErrorException(e.getMessage(), e);
    }
  }

  /**
   * Get the class name to use to look up a cached DTO to invalidate.
   * 
   * @return the class name
   */
  abstract protected String getClassName();

  /**
   * Should the DTO factory create a primary key? You should supply false for
   * classes with identity keys.
   * 
   * @return true for inserts that supply a key value, false for identity-key
   *         inserts that autogenerate a key value
   */
  abstract protected boolean createKey();

  /**
   * Get the DTOs to insert.
   * 
   * @return a collection of DTOs
   */
  abstract protected Collection<IDbDto> getDtos();

  public boolean isSet() {
    boolean set = true;
    // Set if status is not NEW in the DTOs
    for (IDbDto dto : getDtos()) {
      if (Status.NEW.compareTo(dto.getStatus()) == 0) {
        // At least one DTO has status NEW, so not set
        set = false;
        break;
      }
    }
    return set;
  }
}
