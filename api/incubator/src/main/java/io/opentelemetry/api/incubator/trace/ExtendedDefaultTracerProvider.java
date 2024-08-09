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

  public static TracerProvider getInstance() {
    return INSTANCE;
  }

  @Override
  public Tracer get(String instrumentationScopeName) {
    return ExtendedDefaultTracer.getInstance();
  }

  @Override
  public Tracer get(String instrumentationScopeName, String instrumentationScopeVersion) {
    return ExtendedDefaultTracer.getInstance();
  }

  private ExtendedDefaultTracerProvider() {}
}
