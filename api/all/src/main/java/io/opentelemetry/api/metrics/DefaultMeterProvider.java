/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

/** A {@link MeterProvider} that does nothing. */
class DefaultMeterProvider implements MeterProvider {
  @Override
  public MeterBuilder meterBuilder(String instrumentationName) {
    return BUILDER_INSTANCE;
  }

  private static final DefaultMeterProvider INSTANCE = new DefaultMeterProvider();
  private static final MeterBuilder BUILDER_INSTANCE = new NoopMeterBuilder();

  public static MeterProvider getInstance() {
    return INSTANCE;
  }

  private DefaultMeterProvider() {}

  private static class NoopMeterBuilder implements MeterBuilder {

    @Override
    public MeterBuilder setSchemaUrl(String schemaUrl) {
      return this;
    }

    @Override
    public MeterBuilder setInstrumentationVersion(String instrumentationVersion) {
      return this;
    }

    @Override
    public Meter build() {
      return DefaultMeter.getInstance();
    }
  }
}
