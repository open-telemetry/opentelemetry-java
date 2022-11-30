/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerBuilder;
import io.opentelemetry.sdk.internal.ComponentRegistry;
import javax.annotation.Nullable;

class SdkTracerBuilder implements TracerBuilder {

  private final ComponentRegistry<SdkTracer> registry;
  private final String instrumentationScopeName;
  @Nullable private String instrumentationScopeVersion;
  @Nullable private String schemaUrl;

  SdkTracerBuilder(ComponentRegistry<SdkTracer> registry, String instrumentationScopeName) {
    this.registry = registry;
    this.instrumentationScopeName = instrumentationScopeName;
  }

  @Override
  public TracerBuilder setSchemaUrl(String schemaUrl) {
    this.schemaUrl = schemaUrl;
    return this;
  }

  @Override
  public TracerBuilder setInstrumentationVersion(String instrumentationScopeVersion) {
    this.instrumentationScopeVersion = instrumentationScopeVersion;
    return this;
  }

  @Override
  public Tracer build() {
    return registry.get(
        instrumentationScopeName, instrumentationScopeVersion, schemaUrl, Attributes.empty());
  }
}
