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
CREATE TABLE RootClass (
  root_class_id decimal(38) NOT NULL PRIMARY KEY,
  root_col varchar(100) NOT NULL) ENGINE=InnoDB CHARSET=utf8;
  
CREATE TABLE SubClass (
  root_class_id decimal(38) NOT NULL PRIMARY KEY,
  sub_col varchar(100),
  CONSTRAINT SbC_RtC_FK FOREIGN KEY (root_class_id) REFERENCES RootClass (root_class_id) ON DELETE CASCADE
  ) ENGINE=InnoDB CHARSET=utf8;
  
CREATE TABLE SubSubClass (
  root_class_id decimal(38) NOT NULL PRIMARY KEY,
  sub_sub_col varchar(100),
  CONSTRAINT SbSC_SbC_FK FOREIGN KEY (root_class_id) REFERENCES SubClass (root_class_id) ON DELETE CASCADE
  ) ENGINE=InnoDB CHARSET=utf8;

INSERT INTO mysql_sequence values ('root_class', 0);
