/*
 * Copyright (c) 2018 Poesys Associates. All rights reserved.
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

import com.poesys.db.dao.query.IKeyQuerySql;
import com.poesys.db.pk.IPrimaryKey;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <p> A query Command pattern object that implements a SQL key query for the CompositeParent. This
 * SQL specification contains a SQL statement that queries a single CompositeParent object from the
 * database using the primary key. </p>
 *
 * @author Poesys/DB Cartridge
 */
public class QueryCompositeParent implements IKeyQuerySql<CompositeParent> {
  private static final Logger logger = Logger.getLogger(QueryCompositeParent.class);
  /** SQL query statement for CompositeParent */
  private static final String SQL = "SELECT parentId, parentDataColumn FROM CompositeParent WHERE ";

  public CompositeParent getData(IPrimaryKey key, ResultSet rs) {
    try {
      return TestFactory.getCompositeParentData(key, rs);
    } catch (com.poesys.db.InvalidParametersException | SQLException e) {
      logger.error("Error getting data", e);
      throw new com.poesys.db.DbErrorException("Error getting data", e);
    }
  }

  @Override
  public String getSql(IPrimaryKey key) {
    return SQL + key.getSqlWhereExpression("");
  }
}