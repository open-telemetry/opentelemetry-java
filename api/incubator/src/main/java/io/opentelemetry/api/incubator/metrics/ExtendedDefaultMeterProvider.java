/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterBuilder;
import io.opentelemetry.api.metrics.MeterProvider;

/** A {@link MeterProvider} that does nothing. */
public class ExtendedDefaultMeterProvider implements MeterProvider {
  @Override
  public MeterBuilder meterBuilder(String instrumentationScopeName) {
    return BUILDER_INSTANCE;
  }

  private static final ExtendedDefaultMeterProvider INSTANCE = new ExtendedDefaultMeterProvider();
  private static final MeterBuilder BUILDER_INSTANCE = new NoopMeterBuilder();

  public static MeterProvider getNoop() {
    return INSTANCE;
  }

  private ExtendedDefaultMeterProvider() {}

  private static class NoopMeterBuilder implements MeterBuilder {

    @Override
    public MeterBuilder setSchemaUrl(String schemaUrl) {
      return this;
    }

    @Override
    public MeterBuilder setInstrumentationVersion(String instrumentationScopeVersion) {
      return this;
    }

    @Override
    public Meter build() {
      return ExtendedDefaultMeter.getNoop();
    }
  }
}
