/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterBuilder;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.common.impl.ApiUsageLogger;

/** A {@link MeterProvider} that does nothing. */
public class ExtendedDefaultMeterProvider implements MeterProvider {
  @Override
  public MeterBuilder meterBuilder(String instrumentationScopeName) {
    if (instrumentationScopeName == null) {
      ApiUsageLogger.logNullParam(
          ExtendedDefaultMeterProvider.class, "meterBuilder", "instrumentationScopeName");
    }
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
      if (schemaUrl == null) {
        ApiUsageLogger.logNullParam(
            ExtendedDefaultMeterProvider.class, "setSchemaUrl", "schemaUrl");
      }
      return this;
    }

    @Override
    public MeterBuilder setInstrumentationVersion(String instrumentationScopeVersion) {
      if (instrumentationScopeVersion == null) {
        ApiUsageLogger.logNullParam(
            ExtendedDefaultMeterProvider.class,
            "setInstrumentationVersion",
            "instrumentationScopeVersion");
      }
      return this;
    }

    @Override
    public Meter build() {
      return ExtendedDefaultMeter.getNoop();
    }
  }
}
