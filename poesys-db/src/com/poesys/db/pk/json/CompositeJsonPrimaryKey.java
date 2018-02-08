/* Copyright (c) 2018 Poesys Associates. All rights reserved. */
package com.poesys.db.pk.json;

import com.poesys.db.pk.AssociationPrimaryKey;
import com.poesys.db.pk.CompositePrimaryKey;
import com.poesys.db.pk.IPrimaryKey;

import java.util.ArrayList;
import java.util.List;

/**
 * A JSON DTO for composite primary keys
 */
public class CompositeJsonPrimaryKey extends JsonPrimaryKey {
  /**
   * Create a composite JSON primary key DTO with a specified DTO class name, a parent key, and a
   * child key.
   *
   * @param className the fully qualified name of the DTO class for the objects that the
   *                  primary key identifies
   * @param parentKey a JSON primary key serving as the parent portion of the composite key
   * @param childKey  a JSON primary key serving as the child portion of the composite key
   */
  public CompositeJsonPrimaryKey(String className, JsonPrimaryKey parentKey, JsonPrimaryKey
    childKey) {
    super(className, parentKey, childKey);
  }

  @Override
  public IPrimaryKey getPrimaryKey() {
    // Create the concrete parent key.
    IJsonPrimaryKey jsonParentKey = JsonPrimaryKeyFactory.getJsonPrimaryKey(getParentKey());
    // Create the concrete child key.
    IJsonPrimaryKey jsonChildKey = JsonPrimaryKeyFactory.getJsonPrimaryKey(getChildKey());
    // Create the composite primary key.
    return new CompositePrimaryKey(jsonParentKey.getPrimaryKey(), jsonChildKey.getPrimaryKey(),
                                   getClassName());
  }
}
