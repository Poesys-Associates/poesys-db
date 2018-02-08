/*
 * Copyright (c) 2011 Poesys Associates. All rights reserved.
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

import java.util.Comparator;

/**
 * A Comparator implementation that compares IColumnValue objects by
 * column name instead of using the default comparison by value.
 * 
 * @author Robert J. Muller
 */
public class ColumnNameComparator implements Comparator<IColumnValue> {

  @Override
  public int compare(IColumnValue col0, IColumnValue col1) {
    return col0.getName().compareTo(col1.getName());
  }
}
