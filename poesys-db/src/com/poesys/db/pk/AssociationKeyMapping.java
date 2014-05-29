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
package com.poesys.db.pk;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.poesys.db.InvalidParametersException;


/**
 * <p>
 * A helper class that represents the mapping of names in a list of foreign keys
 * in an association. Each association has a list of primary keys that
 * represents the primary key of the association. Each key in that list has a
 * list of columns. When creating the keys from the database, the DAO knows what
 * to name these columns. When creating the keys from other keys being linked,
 * the factory must know how to map from the linked table column names to the
 * association column names. These latter names must be unique across the entire
 * set of column names in the association key, as they represent the attributes
 * in the association's namespace. The AssociationKeyMapping class represents
 * the meta data required to map the column names for each of the individual
 * keys making up the association key to the actual association key column
 * names.
 * </p>
 * <p>
 * For example, take a recursive, many-to-many association. Table A has a key
 * consisting of two columns, A1 and A2. Table AtoA is a many-to-many
 * association table that links an A object to another A object. The column
 * names in the two A keys are A1 and A2, but in AtoA they must change, as you
 * cannot have two A1s and two A2s. So A1 maps to A1_1 in the first key and to
 * A1_2 in the second key, and A2 maps to A2_1 and A2_2.
 * </p>
 * 
 * @author Robert J. Muller
 */
public class AssociationKeyMapping {
  /** List of maps of column names */
  private final List<Map<String, String>> keyList;
  /** Error message resource for invalid index into list */
  private static final String INVALID_INDEX =
    "com.poesys.db.pk.msg.invalid_key_index";
  /** Error message resource for invalid number of keys in mapping */
  private static final String INVALID_NUMBER_OF_KEYS =
    "com.poesys.db.pk.msg.not_enough_keys";
  /** Error message resource for duplicate column name error */
  private static final String DUP_COL_NAME =
    "com.poesys.db.pk.msg.duplicate_column_name";

  /**
   * Create a AssociationKeyMapping object containing a list of a specified
   * number of column maps.
   * 
   * @param numberOfKeys the number of keys to map
   * @throws InvalidParametersException when the number of keys is less than 2
   */
  public AssociationKeyMapping(Integer numberOfKeys)
      throws InvalidParametersException {
    if (numberOfKeys < 2) {
      InvalidParametersException e =
        new InvalidParametersException(INVALID_NUMBER_OF_KEYS);
      List<String> parameters = e.getParameters();
      parameters.clear();
      parameters.add(numberOfKeys.toString());
      e.setParameters(parameters);
      throw e;
    }
    keyList = new ArrayList<Map<String, String>>(numberOfKeys);
    for (int i = 0; i < numberOfKeys; i++) {
      keyList.add(new TreeMap<String, String>());
    }

  }

  /**
   * Map a source column to a target column for the specified key. You cannot
   * re-map a column by supplying the same name a second time.
   * 
   * @param keyIndex the 0-based index of the key in the list of keys
   * @param source the source column name
   * @param target the target column name
   * @throws InvalidParametersException when the key index is not in the list or
   *             you've tried to add the same column as a previously mapped
   *             column
   */
  public void map(Integer keyIndex, String source, String target)
      throws InvalidParametersException {
    checkIndex(keyIndex);

    Map<String, String> map = keyList.get(keyIndex);

    // Don't allow duplicate column names for the key. Copy-paste error.
    if (map.get(source) == null) {
      map.put(source, target);
    } else {
      InvalidParametersException e =
        new InvalidParametersException(DUP_COL_NAME);
      List<String> parameters = e.getParameters();
      parameters.clear();
      parameters.add(source);
      throw e;
    }
  }

  /**
   * Check that the key index is within the range of the actual list.
   * 
   * @param keyIndex the index to check
   * @throws InvalidParametersException when the index is greater than or equal
   *             to the size of the internal list
   */
  private void checkIndex(Integer keyIndex) throws InvalidParametersException {
    if (keyIndex >= keyList.size()) {
      InvalidParametersException e =
        new InvalidParametersException(INVALID_INDEX);
      List<String> parameters = e.getParameters();
      parameters.clear();
      parameters.add(keyIndex.toString());
      parameters.add(Integer.toString(keyList.size()));
      e.setParameters(parameters);
      throw e;
    }
  }

  /**
   * Look up the mapped column name for the specified column name in the
   * specified key.
   * 
   * @param keyIndex the 0-based index to the key
   * @param name the column name to look up
   * @return the column name mapped ot the specified column name
   * @throws InvalidParametersException when the key index is not in the list
   */
  public String lookUp(Integer keyIndex, String name)
      throws InvalidParametersException {
    checkIndex(keyIndex);
    return keyList.get(keyIndex).get(name);
  }
}
