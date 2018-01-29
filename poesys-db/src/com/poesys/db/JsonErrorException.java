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
package com.poesys.db;

import com.poesys.db.dao.PoesysTrackingThread;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * A generic subsystem exception for the Poesys DB library that supports JSON data errors.
 * </p>
 *
 * <pre>
 *  private static final String PARSE_ERROR = "com.poesys.db.dto.msg.invalid_date_foramt";
 *  ...
 * catch (ParseException e) {
 *   Object[] args = {arg1, arg2, arg3};
 *   Message.throwJsonException(PARSE_ERROR, args, e);
 * }
 * </pre>
 *
 * @author Robert J. Muller
 * @see Message
 */
public class JsonErrorException extends RuntimeException {
  /** The unique UID for this serializable object */
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new runtime exception with {@code null} as its
   * detail message.  The cause is not initialized, and may subsequently be
   * initialized by a call to {@link #initCause}.
   */
  public JsonErrorException() {
  }

  /**
   * Constructs a new runtime exception with the specified detail message.
   * The cause is not initialized, and may subsequently be initialized by a
   * call to {@link #initCause}.
   *
   * @param message the detail message. The detail message is saved for
   *                later retrieval by the {@link #getMessage()} method.
   */
  public JsonErrorException(String message) {
    super(message);
  }

  /**
   * Constructs a new runtime exception with the specified detail message and
   * cause.  <p>Note that the detail message associated with
   * {@code cause} is <i>not</i> automatically incorporated in
   * this runtime exception's detail message.
   *
   * @param message the detail message (which is saved for later retrieval
   *                by the {@link #getMessage()} method).
   * @param cause   the cause (which is saved for later retrieval by the
   *                {@link #getCause()} method).  (A <tt>null</tt> value is
   *                permitted, and indicates that the cause is nonexistent or
   *                unknown.)
   * @since 1.4
   */
  public JsonErrorException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new runtime exception with the specified cause and a
   * detail message of <tt>(cause==null ? null : cause.toString())</tt>
   * (which typically contains the class and detail message of
   * <tt>cause</tt>).  This constructor is useful for runtime exceptions
   * that are little more than wrappers for other throwables.
   *
   * @param cause the cause (which is saved for later retrieval by the
   *              {@link #getCause()} method).  (A <tt>null</tt> value is
   *              permitted, and indicates that the cause is nonexistent or
   *              unknown.)
   * @since 1.4
   */
  public JsonErrorException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new runtime exception with the specified detail
   * message, cause, suppression enabled or disabled, and writable
   * stack trace enabled or disabled.
   *
   * @param message            the detail message.
   * @param cause              the cause.  (A {@code null} value is permitted,
   *                           and indicates that the cause is nonexistent or unknown.)
   * @param enableSuppression  whether or not suppression is enabled
   *                           or disabled
   * @param writableStackTrace whether or not the stack trace should
   *                           be writable
   * @since 1.7
   */
  public JsonErrorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
