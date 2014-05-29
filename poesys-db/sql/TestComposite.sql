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
CREATE TABLE Parent
(
   parent_id varchar(40) NOT NULL PRIMARY KEY, 
   col1 varchar(10)
)
ENGINE=InnoDB CHARSET=utf8;

CREATE TABLE Child
(
   parent_id varchar(40) NOT NULL,
   child_number decimal(12) NOT NULL,
   col1 varchar(10),
   CONSTRAINT Ch_PK PRIMARY KEY (parent_id, child_number),
   CONSTRAINT Pr_Ch_FK FOREIGN KEY (parent_id)  REFERENCES Parent ON DELETE CASCADE
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;
