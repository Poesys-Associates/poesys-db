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
 * 
 */
package com.poesys.db.col;

/**
 * A Visitor pattern abstract visitor interface that has one visit method for
 * each ColumnValue data type; this structure permits the implementation of a
 * concrete comparison visitor that encapsulates all the logic for an operation
 * on multiple elements of the ColumnValue class hierarchy, removing the need
 * for separate operation implementations in the subclasses that downcast the
 * input to the data type.
 * 
 * @author Robert J. Muller
 */
public interface IColumnVisitor {
  /**
   * Visit the BigDecimalColumnValue to perform an operation.
   * 
   * @param value the BigDecimalColumnValue on which to operate
   */
  void visit(BigDecimalColumnValue value);

  /**
   * Visit the BigIntegerColumnValue to perform an operation.
   * 
   * @param value the BigIntegerColumnValue on which to operate
   */
  void visit(BigIntegerColumnValue value);

  /**
   * Visit the IntegerColumnValue to perform an operation.
   * 
   * @param value the IntegerColumnValue on which to operate
   */
  void visit(IntegerColumnValue value);

  /**
   * Visit the LongColumnValue to perform an operation.
   * 
   * @param value the LongColumnValue on which to operate
   */
  void visit(LongColumnValue value);

  /**
   * Visit the DateColumnValue to perform an operation.
   * 
   * @param value the DateColumnValue on which to operate
   */
  void visit(DateColumnValue value);

  /**
   * Visit the StringColumnValue to perform an operation.
   * 
   * @param value the StringColumnValue on which to operate
   */
  void visit(StringColumnValue value);

  /**
   * Visit the TimestampColumnValue to perform an operation.
   * 
   * @param value the TimestampColumnValue on which to operate
   */
  void visit(TimestampColumnValue value);

  /**
   * Visit the UuidColumnValue to perform an operation.
   * 
   * @param value the UuidColumnValue on which to operate
   */
  void visit(UuidColumnValue value);
  
  /**
   * Visit a null column value to perform an operation.
   * 
   * @param value the NullColumnValue on which to operate
   */
  void visit(NullColumnValue value);
}
