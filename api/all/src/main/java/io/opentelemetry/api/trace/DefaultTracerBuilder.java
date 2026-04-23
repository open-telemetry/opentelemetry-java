/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import java.util.Objects;

class DefaultTracerBuilder implements TracerBuilder {
  private static final DefaultTracerBuilder INSTANCE = new DefaultTracerBuilder();

  static TracerBuilder getInstance() {
    return INSTANCE;
  }

  @Override
  public TracerBuilder setSchemaUrl(String schemaUrl) {
    Objects.requireNonNull(schemaUrl, "schemaUrl");
    return this;
  }

  @Override
  public TracerBuilder setInstrumentationVersion(String instrumentationScopeVersion) {
    Objects.requireNonNull(instrumentationScopeVersion, "instrumentationScopeVersion");
    return this;
  }

  @Override
  public Tracer build() {
    return DefaultTracer.getInstance();
  }
}
