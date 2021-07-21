/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

class DefaultTracerBuilder implements TracerBuilder {
  private static final DefaultTracerBuilder INSTANCE = new DefaultTracerBuilder();

  static TracerBuilder getInstance() {
    return INSTANCE;
  }

  @Override
  public TracerBuilder setSchemaUrl(String schemaUrl) {
    return this;
  }

  @Override
  public TracerBuilder setInstrumentationVersion(String instrumentationVersion) {
    return this;
  }

  @Override
  public Tracer build() {
    return DefaultTracer.getInstance();
  }
}
