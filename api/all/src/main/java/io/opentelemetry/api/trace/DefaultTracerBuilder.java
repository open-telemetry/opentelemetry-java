/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import io.opentelemetry.common.impl.ApiUsageLogger;

class DefaultTracerBuilder implements TracerBuilder {
  private static final DefaultTracerBuilder INSTANCE = new DefaultTracerBuilder();

  static TracerBuilder getInstance() {
    return INSTANCE;
  }

  @Override
  public TracerBuilder setSchemaUrl(String schemaUrl) {
    if (schemaUrl == null) {
      ApiUsageLogger.logNullParam(TracerBuilder.class, "setSchemaUrl", "schemaUrl");
    }
    return this;
  }

  @Override
  public TracerBuilder setInstrumentationVersion(String instrumentationScopeVersion) {
    if (instrumentationScopeVersion == null) {
      ApiUsageLogger.logNullParam(
          TracerBuilder.class, "setInstrumentationVersion", "instrumentationScopeVersion");
    }
    return this;
  }

  @Override
  public Tracer build() {
    return DefaultTracer.getInstance();
  }
}
