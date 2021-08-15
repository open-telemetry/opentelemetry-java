/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerBuilder;
import io.opentelemetry.sdk.internal.ComponentRegistry;
import javax.annotation.Nullable;

class SdkTracerBuilder implements TracerBuilder {

  private final ComponentRegistry<SdkTracer> registry;
  private final String instrumentationName;
  @Nullable private String instrumentationVersion;
  @Nullable private String schemaUrl;

  SdkTracerBuilder(ComponentRegistry<SdkTracer> registry, String instrumentationName) {
    this.registry = registry;
    this.instrumentationName = instrumentationName;
  }

  @Override
  public TracerBuilder setSchemaUrl(String schemaUrl) {
    this.schemaUrl = schemaUrl;
    return this;
  }

  @Override
  public TracerBuilder setInstrumentationVersion(String instrumentationVersion) {
    this.instrumentationVersion = instrumentationVersion;
    return this;
  }

  @Override
  public Tracer build() {
    return registry.get(instrumentationName, instrumentationVersion, schemaUrl);
  }
}
