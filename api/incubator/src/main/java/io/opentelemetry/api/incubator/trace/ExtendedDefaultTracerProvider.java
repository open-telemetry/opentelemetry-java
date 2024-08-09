/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.trace;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class ExtendedDefaultTracerProvider implements TracerProvider {

  private static final TracerProvider INSTANCE = new ExtendedDefaultTracerProvider();

  public static TracerProvider getNoop() {
    return INSTANCE;
  }

  @Override
  public Tracer get(String instrumentationScopeName) {
    return ExtendedDefaultTracer.getNoop();
  }

  @Override
  public Tracer get(String instrumentationScopeName, String instrumentationScopeVersion) {
    return ExtendedDefaultTracer.getNoop();
  }

  private ExtendedDefaultTracerProvider() {}
}
