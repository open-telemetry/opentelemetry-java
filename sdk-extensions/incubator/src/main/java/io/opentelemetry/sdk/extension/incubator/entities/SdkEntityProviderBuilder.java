/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities;

import io.opentelemetry.sdk.extension.incubator.entities.detectors.ServiceDetector;
import io.opentelemetry.sdk.extension.incubator.entities.detectors.TelemetrySdkDetector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

/** A builder for {@link SdkEntityProvider}. */
public final class SdkEntityProviderBuilder {
  private final List<ResourceDetector> detectors = new ArrayList<>();
  private boolean includeDefaults = true;
  // TODO - add configuraiton settings for this.
  private ExecutorService executorService = new CurrentThreadExecutorService();

  /**
   * Adds a {@link ResourceDetector} that will be run when constructing this provider.
   *
   * @param detector The resource detector.
   * @return this
   */
  public SdkEntityProviderBuilder addDetector(ResourceDetector detector) {
    this.detectors.add(detector);
    return this;
  }

  /**
   * Sets the excutor service which isolates entity listeners and resource detectors.
   *
   * @param executorService The executor service to use for async tasks.
   * @return this
   */
  SdkEntityProviderBuilder setListenerExecutorService(ExecutorService executorService) {
    this.executorService = executorService;
    return this;
  }

  /**
   * Configure whether to include SDK default resoruce detection.
   *
   * @param include true if defaults should be used.
   * @return this
   */
  public SdkEntityProviderBuilder includeDefaults(boolean include) {
    this.includeDefaults = include;
    return this;
  }

  public SdkEntityProvider build() {
    // TODO - have defaults in the front?
    if (includeDefaults) {
      detectors.add(new ServiceDetector());
      detectors.add(new TelemetrySdkDetector());
    }
    return new SdkEntityProvider(executorService, Collections.unmodifiableCollection(detectors));
  }
}
