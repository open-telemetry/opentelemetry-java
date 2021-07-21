/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

class DefaultMeterBuilder implements MeterBuilder {
  private static final MeterBuilder INSTANCE = new DefaultMeterBuilder();

  static MeterBuilder getInstance() {
    return INSTANCE;
  }

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
