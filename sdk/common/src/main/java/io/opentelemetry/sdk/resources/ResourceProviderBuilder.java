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

  public ResourceProviderBuilder addEntityDetector(EntityDetector detector) {
    this.entityDetectors.add(detector);
    return this;
  }

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

  public ResourceProvider build() {
    return new ResourceProvider(mergeDetectedAndRaw());
  }
}
