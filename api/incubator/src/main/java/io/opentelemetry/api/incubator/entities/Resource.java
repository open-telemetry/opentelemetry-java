/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.entities;

/** The active resource for which Telemetry is being generated. */
public interface Resource {
  /**
   * Removes an entity from this resource.
   *
   * @param entityType the type of entity to remove.
   * @return true if entity was found and removed.
   */
  boolean removeEntity(String entityType);

  /**
   * Attaches an entity to the current {@link Resource}.
   *
   * @param entityType The type of the entity.
   * @return A builder that can construct an entity.
   */
  EntityBuilder attachEntity(String entityType);
}
