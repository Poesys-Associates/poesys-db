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
DROP TABLE ManyToManyLink;
DROP TABLE Link1;
DROP TABLE Link2;
DROP TABLE Link3;
DELETE FROM mysql_sequence where name like 'link%';

CREATE TABLE Link1
(
   link1_id decimal(38) NOT NULL PRIMARY KEY, 
   col varchar(10)
)
ENGINE=InnoDB CHARSET=utf8;

CREATE TABLE Link2
(
   link2_id decimal(38) NOT NULL PRIMARY KEY, 
   col varchar(10)
)
ENGINE=InnoDB CHARSET=utf8;

CREATE TABLE Link3
(
   link3_id decimal(38) NOT NULL PRIMARY KEY, 
   col varchar(10)
)
ENGINE=InnoDB CHARSET=utf8;

CREATE TABLE ManyToManyLink
(
   link1_id decimal(38) NOT NULL,
   link2_id decimal(38) NOT NULL,
    col varchar(100) NOT NULL,
   CONSTRAINT MnTML_PK PRIMARY KEY (link1_id, link2_id),
   CONSTRAINT MnTML_Ln1_FK FOREIGN KEY (link1_id) REFERENCES Link1 (link1_id) ON DELETE CASCADE,
   CONSTRAINT MnTML_Ln2_FK FOREIGN KEY (link2_id) REFERENCES Link2 (link2_id) ON DELETE CASCADE
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE TernaryLink
(
   link1_id decimal(38) NOT NULL,
   link2_id decimal(38) NOT NULL,
   link3_id decimal(38) NOT NULL,
   col varchar(100) NOT NULL,
   CONSTRAINT TrL_PK PRIMARY KEY (link1_id, link2_id, link3_id),
   CONSTRAINT TrL_Ln1_FK FOREIGN KEY (link1_id) REFERENCES Link1 (link1_id) ON DELETE CASCADE,
   CONSTRAINT TrL_Ln2_FK FOREIGN KEY (link2_id) REFERENCES Link2 (link2_id) ON DELETE CASCADE,
   CONSTRAINT TrL_Ln3_FK FOREIGN KEY (link3_id) REFERENCES Link3 (link3_id) ON DELETE CASCADE
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO mysql_sequence values ('link1', 0);
INSERT INTO mysql_sequence values ('link2', 0);
INSERT INTO mysql_sequence values ('link3', 0);
