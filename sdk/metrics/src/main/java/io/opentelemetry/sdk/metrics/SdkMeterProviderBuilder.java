/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.internal.SystemClock;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * Builder class for the {@link SdkMeterProvider}. Has fully functional default implementations of
 * all three required interfaces.
 */
public final class SdkMeterProviderBuilder {

  private Clock clock = SystemClock.getInstance();
  private Resource resource = Resource.getDefault();

  SdkMeterProviderBuilder() {}

  /**
   * Assign a {@link Clock}.
   *
   * @param clock The clock to use for all temporal needs.
   * @return this
   */
  public SdkMeterProviderBuilder setClock(@Nonnull Clock clock) {
    Objects.requireNonNull(clock, "clock");
    this.clock = clock;
    return this;
  }

  /**
   * Assign a {@link Resource} to be attached to all Spans created by Tracers.
   *
   * @param resource A Resource implementation.
   * @return this
   */
  public SdkMeterProviderBuilder setResource(@Nonnull Resource resource) {
    Objects.requireNonNull(resource, "resource");
    this.resource = resource;
    return this;
  }

  /**
   * Create a new TracerSdkFactory instance.
   *
   * @return An initialized TracerSdkFactory.
   */
  public SdkMeterProvider build() {
    return new SdkMeterProvider(clock, resource);
  }
}
