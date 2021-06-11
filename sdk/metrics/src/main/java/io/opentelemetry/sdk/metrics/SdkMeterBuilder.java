/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterBuilder;
import io.opentelemetry.sdk.internal.ComponentRegistry;

class SdkMeterBuilder implements MeterBuilder {

  private final ComponentRegistry<SdkMeter> registry;
  private final String instrumentationName;
  private String instrumentationVersion;
  private String schemaUrl;

  SdkMeterBuilder(ComponentRegistry<SdkMeter> registry, String instrumentationName) {
    this.registry = registry;
    this.instrumentationName = instrumentationName;
  }

  @Override
  public MeterBuilder setSchemaUrl(String schemaUrl) {
    this.schemaUrl = schemaUrl;
    return this;
  }

  @Override
  public MeterBuilder setInstrumentationVersion(String instrumentationVersion) {
    this.instrumentationVersion = instrumentationVersion;
    return this;
  }

  @Override
  public Meter build() {
    return registry.get(instrumentationName, instrumentationVersion, schemaUrl);
  }
}
