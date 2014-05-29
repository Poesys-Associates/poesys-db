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
 * Implements the IColumnVisitor Visitor pattern interface to do a compareTo
 * operation on two objects of the same type.
 * 
 * @author Robert J. Muller
 */
public class CompareColumnVisitor implements IColumnVisitor {
  /** The BigDecimal value to compare to the BigDecimal value passed in */
  private BigDecimalColumnValue bdValue = null;

  /** The BigInteger value to compare to the BigInteger value passed in */
  private BigIntegerColumnValue biValue = null;

  /** The Integer value to compare to the Integer value passed in */
  private IntegerColumnValue iValue = null;

  /** The Long value to compare to the Long value passed in */
  private LongColumnValue lValue = null;

  /** The Date value to compare to the Date value passed in */
  private DateColumnValue dtValue = null;

  /** The String value to compare to the String value passed in */
  private StringColumnValue stValue = null;

  /** The Timestamp value to compare to the Timestamp value passed in */
  private TimestampColumnValue tmValue = null;

  /** The UUID value to compare to the UUID value passed in */
  private UuidColumnValue uuValue = null;

  /** The comparison done on the two column values */
  private Integer comparison = null;

  /**
   * Message for runtime exception when no comparison has been done private
   */
  static final String NO_COMP_MSG = "comp.poesys.db.col.msg.no_comparison_done";

  /**
   * Get the comparison. If no comparison has yet been done, the method will
   * throw a runtime exception with the message.
   * 
   * @return Returns the comparison.
   */
  public int getComparison() {
    if (comparison == null) {
      throw new RuntimeException(NO_COMP_MSG);
    }
    return comparison.intValue();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.col.ICompareColumnVisitor#compare(com.poesys.db.col.BigDecimalColumnValue)
   */
  public void visit(BigDecimalColumnValue value) {
    // For first visit, save the value for later comparison. For second visit,
    // do the comparison.
    if (this.bdValue == null) {
      this.bdValue = value;
    } else if (value != null) {
      comparison = bdValue.getValue().compareTo(value.getValue());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.col.ICompareColumnVisitor#compare(com.poesys.db.col.BigIntegerColumnValue)
   */
  public void visit(BigIntegerColumnValue value) {
    // For first visit, save the value for later comparison. For second visit,
    // do the comparison.
    if (this.biValue == null) {
      this.biValue = value;
    } else if (value != null) {
      comparison = biValue.getValue().compareTo(value.getValue());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.col.IColumnVisitor#visit(com.poesys.db.col.IntegerColumnValue)
   */
  public void visit(IntegerColumnValue value) {
    // For first visit, save the value for later comparison. For second visit,
    // do the comparison.
    if (this.iValue == null) {
      this.iValue = value;
    } else if (value != null) {
      comparison = iValue.getValue().compareTo(value.getValue());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.col.IColumnVisitor#visit(com.poesys.db.col.LongColumnValue)
   */
  public void visit(LongColumnValue value) {
    // For first visit, save the value for later comparison. For second visit,
    // do the comparison.
    if (this.lValue == null) {
      this.lValue = value;
    } else if (value != null) {
      comparison = lValue.getValue().compareTo(value.getValue());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.col.ICompareColumnVisitor#compare(com.poesys.db.col.DateColumnValue)
   */
  public void visit(DateColumnValue value) {
    // For first visit, save the value for later comparison. For second visit,
    // do the comparison.
    if (this.dtValue == null) {
      this.dtValue = value;
    } else if (value != null) {
      comparison = dtValue.getValue().compareTo(value.getValue());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.col.ICompareColumnVisitor#compare(com.poesys.db.col.StringColumnValue)
   */
  public void visit(StringColumnValue value) {
    // For first visit, save the value for later comparison. For second visit,
    // do the comparison.
    if (this.stValue == null) {
      this.stValue = value;
    } else if (value != null) {
      comparison = stValue.getValue().compareTo(value.getValue());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.col.ICompareColumnVisitor#compare(com.poesys.db.col.TimestampColumnValue)
   */
  public void visit(TimestampColumnValue value) {
    // For first visit, save the value for later comparison. For second visit,
    // do the comparison.
    if (this.tmValue == null) {
      this.tmValue = value;
    } else if (value != null) {
      comparison = tmValue.getValue().compareTo(value.getValue());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.col.ICompareColumnVisitor#compare(com.poesys.db.col.UuidColumnValue)
   */
  public void visit(UuidColumnValue value) {
    // For first visit, save the value for later comparison. For second visit,
    // do the comparison.
    if (this.uuValue == null) {
      this.uuValue = value;
    } else if (value != null) {
      comparison = uuValue.getValue().compareTo(value.getValue());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.db.col.IColumnVisitor#visit(com.poesys.db.col.NullColumnValue)
   */
  public void visit(NullColumnValue value) {
    // Always return 0.
    comparison = 0;
  }
}
