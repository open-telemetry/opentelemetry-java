/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities;

import io.opentelemetry.sdk.resources.Resource;

/** A listener for changes in the EntityState of this SDK. */
public interface EntityListener {
  /**
   * Called when an entity has been added or its state has changed.
   *
   * @param state The current state of the entity.
   * @param resource The current state of the Resource.
   */
  public void onEntityState(EntityState state, Resource resource);

  /**
   * Called when an entity has been removed.
   *
   * @param state The current state of the removed entity.
   * @param resource The current state of the Resource.
   */
  public void onEntityDelete(EntityState state, Resource resource);
}
