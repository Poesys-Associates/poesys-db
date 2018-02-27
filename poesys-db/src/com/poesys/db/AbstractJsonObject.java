/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db;

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

  // messages
  private static final String INVALID_STATUS_ERROR = "invalid JSON object status ";

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
   * Set the status of the object.
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
}
