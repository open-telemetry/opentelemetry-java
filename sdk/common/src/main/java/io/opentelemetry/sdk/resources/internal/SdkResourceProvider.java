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
 * <p>All detectors are invoked once at construction and the merged {@link Resource} is cached for
 * the lifetime of this provider. {@link ResourceDetector#shouldReinvoke()} is part of the target
 * API shape but has no effect on runtime behavior in this iteration. A future iteration will add
 * TTL-based cache invalidation that re-invokes only {@code shouldReinvoke=true} detectors when the
 * cache expires. Until then, {@code getResource()} is a single field read on the hot path.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public final class SdkResourceProvider {

  private final List<ResourceDetector> detectors;
  private final Resource cachedResource;

  SdkResourceProvider(List<ResourceDetector> detectors) {
    this.detectors = Collections.unmodifiableList(new ArrayList<>(detectors));
    this.cachedResource = merge(this.detectors);
  }

  /** Returns a new {@link SdkResourceProviderBuilder}. */
  public static SdkResourceProviderBuilder builder() {
    return new SdkResourceProviderBuilder();
  }

  /**
   * Returns an {@link SdkResourceProvider} backed by a single {@linkplain
   * ResourceDetector#constant(Resource) constant detector} wrapping the given {@link Resource}.
   */
  public static SdkResourceProvider create(Resource resource) {
    requireNonNull(resource, "resource");
    return builder().addDetector(ResourceDetector.constant(resource)).build();
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
