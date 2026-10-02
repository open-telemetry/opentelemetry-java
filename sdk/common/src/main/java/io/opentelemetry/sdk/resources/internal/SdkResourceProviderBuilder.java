/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.internal;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

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
   * Adds a {@link ResourceDetector}. Detectors are merged in the order they are added, with
   * later-added attributes winning on conflict (per {@link
   * io.opentelemetry.sdk.resources.Resource#merge}).
   */
  public SdkResourceProviderBuilder addDetector(ResourceDetector detector) {
    requireNonNull(detector, "detector");
    detectors.add(detector);
    return this;
  }

  /**
   * Returns a new {@link SdkResourceProvider}.
   *
   * @throws IllegalStateException if no detector has been added via {@link
   *     #addDetector(ResourceDetector)}.
   */
  public SdkResourceProvider build() {
    if (detectors.isEmpty()) {
      throw new IllegalStateException(
          "At least one ResourceDetector must be added via addDetector before calling build().");
    }
    return new SdkResourceProvider(detectors);
  }
}
