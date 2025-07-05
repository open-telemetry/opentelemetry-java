/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities;

import io.opentelemetry.sdk.extension.incubator.entities.detectors.ServiceDetector;
import io.opentelemetry.sdk.extension.incubator.entities.detectors.TelemetrySdkDetector;
import java.util.ArrayList;
import java.util.List;

/** A builder for {@link SdResourceProvider}. */
public final class SdkResourceProviderBuilder {
  private final List<ResourceDetector> detectors = new ArrayList<>();
  private boolean includeDefaults = true;

  /**
   * Adds a {@link ResourceDetector} that will be run when constructing this provider.
   *
   * @param detector The resource detector.
   * @return this
   */
  public SdkResourceProviderBuilder addDetector(ResourceDetector detector) {
    this.detectors.add(detector);
    return this;
  }

  /**
   * Configure whether to include SDK default resoruce detection.
   *
   * @param include true if defaults should be used.
   * @return this
   */
  public SdkResourceProviderBuilder includeDefaults(boolean include) {
    this.includeDefaults = include;
    return this;
  }

  public SdkResourceProvider build() {
    // TODO - have defaults in the front?
    if (includeDefaults) {
      detectors.add(new ServiceDetector());
      detectors.add(new TelemetrySdkDetector());
    }
    SdkResourceProvider result = new SdkResourceProvider();
    // TODO - Should we move these onto the resource provider?
    detectors.forEach(d -> d.configure(result.getResource()));
    return result;
  }
}
