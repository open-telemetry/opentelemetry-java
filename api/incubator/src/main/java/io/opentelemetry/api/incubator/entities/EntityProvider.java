/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.entities;

/**
 * A registry for interacting with {@link Resource}s. The name <i>Provider</i> is for consistency
 * with other languages and it is <b>NOT</b> loaded using reflection.
 *
 * @see Resource
 */
public interface EntityProvider {
  /**
   * Returns a no-op {@link EntityProvider} which only creates no-op {@link Resource}s which do not
   * record nor are emitted.
   */
  static EntityProvider noop() {
    return NoopEntityProvider.INSTANCE;
  }

  /**
   * Removes an entity from this resource.
   *
   * @param entityType the type of entity to remove.
   * @return true if entity was found and removed.
   */
  boolean removeEntity(String entityType);

  /**
   * Attaches an entity to the current {@code Resource}.
   *
   * <p>This will only add new entities or update description of existing entities.
   *
   * @param entityType The type of the entity.
   * @return A builder that can construct an entity.
   */
  EntityBuilder attachOrUpdateEntity(String entityType);
}
