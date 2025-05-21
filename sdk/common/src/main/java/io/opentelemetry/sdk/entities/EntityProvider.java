/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.entities;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;

public interface EntityProvider {

  /**
   * Returns the current representation of the Resource, as materialized by the Entities contained
   * herein.
   */
  Resource getResource();

  /**
   * Adds a listener to the end of the list of EntityListeners. The listener will be notified when
   * changes are made to the EntityProvider.
   *
   * @param listener - an EntityListener
   */
  void addListener(EntityListener listener);

  /**
   * Adds a new Entity to the EntityProvider. If an Entity with the same id already exists, it will
   * be removed and a new instance will be inserted at the end of the list of entities. After the
   * entity is added, the resource is rebuilt and the listeners are notified.
   */
  void addEntity(String id, String name, Attributes attributes);

  /**
   * Adds a new Entity to the EntityProvider. If an Entity with the same id already exists, it will
   * be removed and a new instance will be inserted at the end of the list of entities. After the
   * entity is added, the resource is rebuilt and the listeners are notified.
   */
  default void addEntity(Entity entity) {
    addEntity(entity.getId(), entity.getName(), entity.getAttributes());
  }

  /**
   * Updates an existing Entity with the given id with new attributes. If an Entity with the given
   * id does not exist, this is effective a no-op. This method does not change the order of ids, so
   * the change is effectively made "in-place". After the Entity has been updated, the listeners
   * will be notified.
   */
  void updateEntity(String id, Attributes attributes);

  /**
   * Deletes the Entity with the given id. If the Entity does not exist within this EntityProvider,
   * then it is effectively a no-op. If the Entity is removed, the Resource will be rebuilt and
   * listeners will then be notified.
   */
  void deleteEntity(String id);
}
