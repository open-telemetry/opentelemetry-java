/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.trace;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerBuilder;

class ExtendedDefaultTracerBuilder implements TracerBuilder {
  private static final ExtendedDefaultTracerBuilder INSTANCE = new ExtendedDefaultTracerBuilder();

  static TracerBuilder getInstance() {
    return INSTANCE;
  }

  @Override
  public TracerBuilder setSchemaUrl(String schemaUrl) {
    return this;
  }

  @Override
  public TracerBuilder setInstrumentationVersion(String instrumentationScopeVersion) {
    return this;
  }

  @Override
  public Tracer build() {
    return ExtendedDefaultTracer.getNoop();
  }
}
