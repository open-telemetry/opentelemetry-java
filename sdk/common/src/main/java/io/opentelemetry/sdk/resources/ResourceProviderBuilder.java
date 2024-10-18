/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import java.util.ArrayList;
import java.util.List;

/** Builder of {@link ResourceProvider} */
public final class ResourceProviderBuilder {

  private final List<EntityDetector> entityDetectors = new ArrayList<>();
  private final List<Resource> detectedResources = new ArrayList<>();

  /** Adds an entity detector, which will detect {@link Entity}s to place in the resource. */
  public ResourceProviderBuilder addEntityDetector(EntityDetector detector) {
    this.entityDetectors.add(detector);
    return this;
  }

  /**
   * * Adds a discovered resource to include in resolving the SDK's resource.
   *
   * @deprecated Use {@link #addEntityDetector(EntityDetector)}.
   */
  @Deprecated
  public ResourceProviderBuilder addDetectedResource(Resource resource) {
    this.detectedResources.add(resource);
    return this;
  }

  private final Resource mergeDetectedAndRaw() {
    Resource result = Resource.empty();
    for (EntityDetector detector : entityDetectors) {
      result = result.merge(Resource.builder().addAll(detector.detectEntities()).build());
    }
    for (Resource next : detectedResources) {
      result = result.merge(next);
    }
    return result;
  }

  /**
   * Returns the SDK resource provider which uses these detectors.
   *
   * @return the ResourceProvider.
   */
  public ResourceProvider build() {
    return new ResourceProvider(mergeDetectedAndRaw());
  }
}
