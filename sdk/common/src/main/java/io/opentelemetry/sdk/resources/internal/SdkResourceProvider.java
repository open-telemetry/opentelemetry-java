/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.internal;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides the {@link Resource} used by the SDK by merging the results of a configured, ordered
 * list of {@link ResourceDetector}s.
 *
 * <p>All detectors are invoked once when the provider is built. The merged {@link Resource} is
 * cached for the lifetime of this provider.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public final class SdkResourceProvider {

  private final List<ResourceDetector> detectors;
  // TODO: Add TTL-based cache refresh that re-invokes only refreshable detectors while keeping
  // hot-path resource reads cheap.
  private final Resource cachedResource;

  private SdkResourceProvider(List<ResourceDetector> detectors, Resource cachedResource) {
    this.detectors = detectors;
    this.cachedResource = cachedResource;
  }

  static SdkResourceProvider createFromDetectors(List<ResourceDetector> detectors) {
    List<ResourceDetector> detectorSnapshot =
        Collections.unmodifiableList(new ArrayList<>(detectors));
    return new SdkResourceProvider(detectorSnapshot, merge(detectorSnapshot));
  }

  /** Returns a new {@link SdkResourceProviderBuilder}. */
  public static SdkResourceProviderBuilder builder() {
    return new SdkResourceProviderBuilder();
  }

  /** Returns an {@link SdkResourceProvider} backed by a single constant {@link Resource}. */
  public static SdkResourceProvider create(Resource resource) {
    requireNonNull(resource, "resource");
    return builder().addConstantResource(resource).build();
  }

  /** Returns the merged {@link Resource}. */
  public Resource getResource() {
    return cachedResource;
  }

  private static Resource merge(List<ResourceDetector> detectors) {
    Resource resource = Resource.empty();
    for (ResourceDetector detector : detectors) {
      resource = resource.merge(detector.getResource());
    }
    return resource;
  }

  @Override
  public String toString() {
    return "SdkResourceProvider{detectors=" + detectors + "}";
  }
}
