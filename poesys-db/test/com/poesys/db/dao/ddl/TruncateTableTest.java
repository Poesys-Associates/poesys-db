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
package com.poesys.db.dao.ddl;


import org.junit.Test;

import com.poesys.db.dao.ConnectionTest;


/**
 * CUT: TruncateTableSql, IExcecuteSql
 * 
 * @author Robert J. Muller
 */
public class TruncateTableTest extends ConnectionTest {

  /**
   * Test method for {@link com.poesys.db.dao.ddl.TruncateTableSql#getSql()}.
   */
  @Test
  public void testGetSql() {
    ISql sql = new TruncateTableSql("TestNatural");
    IExecuteSql executive = new ExecuteSql(sql, getSubsystem());
    executive.execute();
  }
}
