-- Copyright Poesys Associates 2009. All rights reserved.
-- This file is part of Poesys-DB.

-- Poesys-DB is free software: you can redistribute it and/or modify it under
-- the terms of the GNU General Public License as published by the Free Software
-- Foundation, either version 3 of the License, or (at your option) any later
-- version.
-- 
-- Poesys-DB is distributed in the hope that it will be useful, but WITHOUT ANY
-- WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
-- A PARTICULAR PURPOSE. See the GNU General Public License for more details.
-- 
-- You should have received a copy of the GNU General Public License along with
-- Poesys-DB. If not, see <http://www.gnu.org/licenses/>.



-- The Sequence table provides a way to generate sequence numbers in a MySQL
-- database. Insert one row for each sequence that you want to use. The
-- sequence name can be 30 characters or less, following the standard for SQL
-- names. Sequence names must be unique within the database.

-- Copy this file and add the sequences with appropriate starting numbers. You
-- can then maintain it as part of your application system.

CREATE TABLE Sequence (
  name VARCHAR(30) NOT NULL PRIMARY KEY,
  sequence BIGINT NOT NULL
) ENGINE=InnoDB CHARSET=utf8;

INSERT INTO Sequence (name, sequence) VALUES ('sequenceName', 0);
