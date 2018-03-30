/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db;

import com.poesys.db.dto.IDbDto;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Abstract superclass for all generated JSON objects with shared data elements.
 */
public abstract class AbstractJsonObject {
  /** the object status for the JSON data transfer object */
  protected String status;

  public static final String NEW = "NEW";
  public static final String EXISTING = "EXISTING";
  public static final String CHANGED = "CHANGED";
  public static final String DELETED = "DELETED";

  // date-related constants for use by all concrete JSON objects
  public static final String PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
  public static final SimpleDateFormat FORMAT = new SimpleDateFormat(PATTERN);

  // messages available to all concrete subclasses
  protected static final String INVALID_STATUS_ERROR = "invalid JSON object status ";

  /**
   * Create the abstract portion of the object.
   *
   * @param status the object status (NEW, EXISTING, CHANGED, DELETED)
   */
  public AbstractJsonObject(String status) {
    this.status = status;
  }

  /**
   * Get the status of the object.
   * @return NEW, EXISTING, CHANGED, DELETED
   */
  public String getStatus() {
    return status;
  }

  /**
   * Set the status of the object from a textual status value.
   * @param status NEW, EXISTING, CHANGED, or DELETED
   */
  public void setStatus(String status) {
    switch (status) {
      case NEW:
      case EXISTING:
      case CHANGED:
      case DELETED:
        this.status = status;
        break;
      default:
        throw new InvalidParametersException(INVALID_STATUS_ERROR + status);
    }
  }

  /**
   * Set the status of the object from an IDbDto status.
   *
   * @param status an IDbDto status value
   */
  public void setStatus(IDbDto.Status status) {
    switch (status) {
      case NEW:
        setStatus(AbstractJsonObject.NEW);
        break;
      case EXISTING:
        setStatus(AbstractJsonObject.EXISTING);
        break;
      case CHANGED:
        setStatus(AbstractJsonObject.CHANGED);
        break;
      case DELETED:
        setStatus(AbstractJsonObject.DELETED);
        break;
      case CASCADE_DELETED:
        setStatus(AbstractJsonObject.DELETED);
        break;
      case DELETED_FROM_DATABASE:
        setStatus(AbstractJsonObject.DELETED);
        break;
      case FAILED:
      default:
        throw new InvalidParametersException(INVALID_STATUS_ERROR + status);
    }
  }

  /**
   * Generic method to get a deep hash code from a list of objects of a certain type T, returning
   * a default hash if the list is null or empty
   *
   * @param hash the initial hash code
   * @param list the list to hash
   * @param <T>  the type of the objects in the list
   * @return a new hash code based on the initial hash code and the objects in the list
   */
  protected <T> int hashList(int hash, List<T> list) {
    int result = hash;
    if (list == null || list.isEmpty()) {
      result = 31 * result;
    } else {
      for (T object : list) {
        result = 31 * result + object.hashCode();
      }
    }
    return result;
  }

  /**
   * Test two lists for equality.
   *
   * @param list1 the first list
   * @param list2 the second list
   * @param <T>   the generic type; type must override equals() method
   * @return true if the lists are both null OR both non-null with equal objects in the list
   */
  protected <T> boolean checkListEqual(List<T> list1, List<T> list2) {
    boolean isEqual = true;
    if (list1 != null && list2 != null) {
      if (list1.size() == list2.size()) {
        for (int i = 0; i < list1.size(); i++) {
          isEqual = list1.get(i).equals(list2.get(i));
          if (!isEqual) {
            break;
          }
        }
      }
    }
    return isEqual;
  }
}
