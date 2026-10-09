/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.internal;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Builder for {@link SdkResourceProvider}.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public final class SdkResourceProviderBuilder {

  private final List<ResourceDetector> detectors = new ArrayList<>();

  SdkResourceProviderBuilder() {}

  /**
   * Adds a constant {@link Resource} with no name.
   *
   * @param resource the resource to add
   * @return this builder
   */
  public SdkResourceProviderBuilder addConstantResource(Resource resource) {
    return addConstantResource(resource, null);
  }

  /**
   * Adds a constant {@link Resource}. Resources are merged in the order they are added, with
   * later-added attributes winning on conflict (per {@link Resource#merge}).
   *
   * @param resource the resource to add
   * @param name an informational name for logging / debugging, or {@code null} if unnamed
   * @return this builder
   */
  public SdkResourceProviderBuilder addConstantResource(Resource resource, @Nullable String name) {
    requireNonNull(resource, "resource");
    detectors.add(new ConstantResourceDetector(name, resource));
    return this;
  }

  /**
   * Returns a new {@link SdkResourceProvider}.
   *
   * @throws IllegalStateException if no resource has been added via {@link
   *     #addConstantResource(Resource)}.
   */
  public SdkResourceProvider build() {
    if (detectors.isEmpty()) {
      throw new IllegalStateException(
          "At least one Resource must be added via addConstantResource before calling build().");
    }
    return new SdkResourceProvider(detectors);
  }
}
