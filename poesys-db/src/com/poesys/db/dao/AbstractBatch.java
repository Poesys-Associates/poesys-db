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
package com.poesys.db.dao;


import java.sql.PreparedStatement;
import java.util.List;

import com.poesys.db.InvalidParametersException;
import com.poesys.db.dto.IDbDto;


/**
 * An abstract superclass for batch-oriented data access object (DAO) classes
 * that provides helper methods for processing results and errors encountered
 * during JDBC batch processing of a specified kind of data transfer object
 * (DTO).
 * 
 * @see com.poesys.db.dto.IDbDto
 * 
 * @author Robert J. Muller
 * @param <T> the type of IDbDto to process
 */
public abstract class AbstractBatch<T extends IDbDto> {

  /**
   * Process the error codes from a JDBC batch. Extract the DTO corresponding to
   * any error from the list of DTOs in the batch and construct an error message
   * containing the primary key values of the object that failed to insert. Note
   * that all DTOs not in FAILED status have been successfully processed, so
   * there is no need to set the processed flag to off. The method that sets the
   * error message also marks the DTO as unprocessed.
   * 
   * @param codes the codes for the DTOs starting at the index; can be null
   * @param dtos the list of DTOs in the batch
   * @param builder a StringBuilder with any previous set of messages
   * @return true if there were errors, false if there were none
   * @throws InvalidParametersException when the sizes of the error code array
   *           and the list of DTOs does not match
   */
  protected boolean processErrors(int[] codes, List<T> dtos,
                                  StringBuilder builder)
      throws InvalidParametersException {
    boolean errors = false;
    int i = 0;
    // Process only if there are codes and DTOs.
    if (codes != null && codes.length > 0 && dtos != null && dtos.size() > 0) {
      // Check the sizes of the two arrays.
      if (codes.length != dtos.size()) {
        // Statement processing stopped at the first error, get failed DTO
        errors = appendErrorString(builder, dtos.get(codes.length));
      } else {
        // Statement processing continued, check for FAILED
        for (IDbDto dto : dtos) {
          if (codes[i] == PreparedStatement.EXECUTE_FAILED) {
            errors = appendErrorString(builder, dto);
          } else if (codes[i] == PreparedStatement.SUCCESS_NO_INFO
                     || codes[i] == 1) {
            // Leave the current status alone, fully processed.
          }
          i++; // increment the counter to check the next code
        }
      }
    } else if (codes != null && codes.length == 0 && dtos.size() > 0) {
      // 0-length means the first object failed
      errors = appendErrorString(builder, dtos.get(0));
    }
    return errors;
  }

  /**
   * Append the error information for a DTO to an input Builder and set the dto
   * to Failed status and unprocessed.
   * 
   * @param builder the builder to which to append
   * @param dto the DTO to represent as a string
   * @return true if there is an error, false if not
   */
  private boolean appendErrorString(StringBuilder builder, IDbDto dto) {
    boolean errors = false;
    // Operation failed for this DTO
    builder.append(dto.getClass().getName());
    builder.append(dto.getPrimaryKey().getValueList());
    builder.append("\n");
    dto.setFailed();
    // Mark the DTO as unprocessed as it did not complete processing.
    dto.setProcessed(false);
    errors = true;
    return errors;
  }

  /**
   * Set all the DTOs in a list as processed.
   * 
   * @param list a list of unprocessed DTOs
   * @param processed the value to which to set the flag (true, false)
   */
  protected void setProcessed(List<T> list, Boolean processed) {
    for (T dto : list) {
      dto.setProcessed(processed);
    }
  }
}
