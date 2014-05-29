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


import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;


/**
 * <p>
 * A Strategy-pattern interface for strategies that validate data members of a
 * Data Transfer Object. Each concrete implementation of the interface validates
 * one constraint on the DTO (of any complexity). The AbstractDto contains a
 * list of IValidate strategies for each operation (query, insert, update, and
 * delete). The DAO calls the appropriate method on the DTO to execute the list
 * of validators. The external IValidate implementation contains a reference to
 * the IDto parent object set by the constructor when the DTO factory builds the
 * DTO. The validate() method takes a connection but may or may not need to use
 * it to perform validation if the DTO has all the information required by the
 * constraint. If the DTO violates the constraint, the validate() method throws
 * an appropriate exception with a message resource string.
 * </p>
 * <p>
 * Because most validation involves the internal data members of the specific
 * DTO class, most validators are better structured as internal classes within
 * the specific DTO class. This allows them access to all the data without using
 * generics or passing in references to the DTO of the specific type. See the
 * AbstractDto documentation for an example of an internal validator.
 * </p>
 * <p>
 * The strategy pattern decouples the logic of DTO constraint validation from
 * the DTO and DAO while providing a mechanism for the DAO to validate the DTO
 * for a given operation. You can change validation dynamically in the DTO code
 * by managing the lists in the parent AbstractDto. You can code reusable
 * validators by creating them as external classes, usually with a generic DTO
 * type.
 * </p>
 * <p>
 * An example implementation of a concrete IValidate class:
 * </p>
 * 
 * <pre>
 * <code>
 * public class HasPrimaryKey implements IValidate {
 *   //The DTO to validate
 *   IDto dto = null;
 * 
 *   // The no-primary-key-supplied message
 *   private static final String NO_KEY_MSG =
 *       &quot;com.poesys.db.dto.msg.no_primary_key&quot;;
 * 
 *   // Create a primary key validator for a specified DTO.
 *   public HasPrimaryKey(IDto dto) {
 *     this.dto = dto;
 *   }
 * 
 *   public void validate(Connection connection) throws SQLException {
 *     if (dto.getPrimaryKey() == null) {
 *       throw new NoPrimaryKeyException(NO_KEY_MSG);
 *     }
 *   }
 * }
 * </code>
 * </pre>
 * 
 * @author Robert J. Muller
 * @see AbstractDto
 */
public interface IValidate extends Serializable {
  /**
   * Is the DTO valid in the current context? The method may or may not use the
   * supplied transaction connection to access information in the database to
   * validate the DTO.
   * 
   * @param connection the optional SQL connection for the current transaction
   * @throws SQLException when there is a problem querying data from the
   *           database
   */
  public void validate(Connection connection) throws SQLException;
}
