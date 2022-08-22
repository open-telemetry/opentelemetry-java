/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterBuilder;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.InstrumentationScopeInfoBuilder;
import io.opentelemetry.sdk.internal.ComponentRegistry;

class SdkMeterBuilder implements MeterBuilder {

  private final ComponentRegistry<SdkMeter> registry;
  private final InstrumentationScopeInfoBuilder scopeBuilder;

  SdkMeterBuilder(ComponentRegistry<SdkMeter> registry, String instrumentationScopeName) {
    this.registry = registry;
    this.scopeBuilder = InstrumentationScopeInfo.builder(instrumentationScopeName);
  }

  @Override
  public MeterBuilder setSchemaUrl(String schemaUrl) {
    scopeBuilder.setSchemaUrl(schemaUrl);
    return this;
  }

  @Override
  public MeterBuilder setInstrumentationVersion(String instrumentationScopeVersion) {
    scopeBuilder.setVersion(instrumentationScopeVersion);
    return this;
  }

  @Override
  public MeterBuilder setAttributes(Attributes attributes) {
    scopeBuilder.setAttributes(attributes);
    return this;
  }

  @Override
  public Meter build() {
    return registry.get(scopeBuilder.build());
  }
}
