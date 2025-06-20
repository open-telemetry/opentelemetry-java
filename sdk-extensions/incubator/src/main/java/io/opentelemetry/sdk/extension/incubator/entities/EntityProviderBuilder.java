/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities;

import io.opentelemetry.sdk.resources.Resource;

/** Builder of {@link EntityProvider}. */
public interface EntityProviderBuilder {
  /** Adds an entity detector, which will detect {@link Entity}s to place in the resource. */
  EntityProviderBuilder addDetector(EntityDetector detector);

  /**
   * Adds a discovered resource to include in resolving the SDK's resource.
   *
   * @deprecated Use {@link #addDetector(EntityDetector)}.
   */
  @Deprecated
  EntityProviderBuilder addDetectedResource(Resource resource);

  /** Sets whether or not default entity detectors will be included. */
  EntityProviderBuilder includeDefaults(boolean include);

  /**
   * Returns the SDK entity provider which uses these detectors.
   *
   * @return the EntityProvider.
   */
  EntityProvider build();
}
