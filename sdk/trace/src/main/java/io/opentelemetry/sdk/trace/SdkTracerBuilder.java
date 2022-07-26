/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerBuilder;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.InstrumentationScopeInfoBuilder;
import io.opentelemetry.sdk.internal.ComponentRegistry;

class SdkTracerBuilder implements TracerBuilder {

  private final ComponentRegistry<SdkTracer> registry;
  private final InstrumentationScopeInfoBuilder scopeBuilder;

  SdkTracerBuilder(ComponentRegistry<SdkTracer> registry, String instrumentationScopeName) {
    this.registry = registry;
    this.scopeBuilder = InstrumentationScopeInfo.builder(instrumentationScopeName);
  }

  @Override
  public TracerBuilder setSchemaUrl(String schemaUrl) {
    scopeBuilder.setSchemaUrl(schemaUrl);
    return this;
  }

  @Override
  public TracerBuilder setInstrumentationVersion(String instrumentationScopeVersion) {
    scopeBuilder.setVersion(instrumentationScopeVersion);
    return this;
  }

  @Override
  public Tracer build() {
    return registry.get(scopeBuilder.build());
  }
}
