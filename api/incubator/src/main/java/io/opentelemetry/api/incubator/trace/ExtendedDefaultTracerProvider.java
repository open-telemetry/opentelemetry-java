/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.trace;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerBuilder;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.common.impl.ApiUsageLogger;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class ExtendedDefaultTracerProvider implements TracerProvider {

  private static final TracerProvider INSTANCE = new ExtendedDefaultTracerProvider();

  public static TracerProvider getNoop() {
    return INSTANCE;
  }

  @Override
  public Tracer get(String instrumentationScopeName) {
    if (instrumentationScopeName == null) {
      ApiUsageLogger.logNullParam(
          ExtendedDefaultTracerProvider.class, "get", "instrumentationScopeName");
    }
    return ExtendedDefaultTracer.getNoop();
  }

  @Override
  public Tracer get(String instrumentationScopeName, String instrumentationScopeVersion) {
    if (instrumentationScopeName == null) {
      ApiUsageLogger.logNullParam(
          ExtendedDefaultTracerProvider.class, "get", "instrumentationScopeName");
    }
    if (instrumentationScopeVersion == null) {
      ApiUsageLogger.logNullParam(
          ExtendedDefaultTracerProvider.class, "get", "instrumentationScopeVersion");
    }
    return ExtendedDefaultTracer.getNoop();
  }

  @Override
  public TracerBuilder tracerBuilder(String instrumentationScopeName) {
    if (instrumentationScopeName == null) {
      ApiUsageLogger.logNullParam(
          ExtendedDefaultTracerProvider.class, "tracerBuilder", "instrumentationScopeName");
    }
    return ExtendedDefaultTracerBuilder.getInstance();
  }

  private ExtendedDefaultTracerProvider() {}
}
