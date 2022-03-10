/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.micrometershim;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.NamingConvention;
import io.opentelemetry.api.OpenTelemetry;
import java.util.concurrent.TimeUnit;

/** A builder of {@link OpenTelemetryMeterRegistry}. */
public final class OpenTelemetryMeterRegistryBuilder {

  // Visible for testing
  static final String INSTRUMENTATION_NAME = "io.opentelemetry.micrometershim";

  private final OpenTelemetry openTelemetry;
  private Clock clock = Clock.SYSTEM;
  private TimeUnit baseTimeUnit = TimeUnit.MILLISECONDS;
  private NamingConvention namingConvention = NamingConvention.identity;

  OpenTelemetryMeterRegistryBuilder(OpenTelemetry openTelemetry) {
    this.openTelemetry = openTelemetry;
  }

  /** Sets a custom {@link Clock}. Useful for testing. */
  public OpenTelemetryMeterRegistryBuilder setClock(Clock clock) {
    this.clock = clock;
    return this;
  }

  /** Sets the base time unit. */
  public OpenTelemetryMeterRegistryBuilder setBaseTimeUnit(TimeUnit baseTimeUnit) {
    this.baseTimeUnit = baseTimeUnit;
    return this;
  }

  /** Sets the {@link NamingConvention} for this registry. */
  public OpenTelemetryMeterRegistryBuilder setNamingConvention(NamingConvention namingConvention) {
    this.namingConvention = namingConvention;
    return this;
  }

  /**
   * Returns a new {@link OpenTelemetryMeterRegistry} with the settings of this {@link
   * OpenTelemetryMeterRegistryBuilder}.
   */
  public MeterRegistry build() {
    return new OpenTelemetryMeterRegistry(
        clock,
        baseTimeUnit,
        namingConvention,
        openTelemetry.getMeterProvider().get(INSTRUMENTATION_NAME));
  }
}
