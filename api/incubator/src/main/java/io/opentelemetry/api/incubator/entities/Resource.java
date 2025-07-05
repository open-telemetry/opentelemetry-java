/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.entities;

/** The active resource for which Telemetry is being generated. */
public interface Resource {
  /**
   * Adds an {@link Entity} to this resource.
   *
   * <p>If the entity already exists, this updates the description.
   *
   * @param e The entity
   * @return true if the entity was added or updated, false if there was a conflict.
   */
  public boolean addOrUpdate(Entity e);

  /**
   * Removes an {@link Entity} from this resource.
   *
   * @param e The entity
   * @return true if entity was found and removed.
   */
  public boolean removeEntity(Entity e);

  /**
   * Returns a builder that can construct an {@link Entity}.
   *
   * @param entityType The type of the entity.
   * @return A builder that can construct an entity.
   */
  public EntityBuilder createEntity(String entityType);
}
