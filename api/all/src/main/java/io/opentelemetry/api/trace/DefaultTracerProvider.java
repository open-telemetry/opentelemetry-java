/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import io.opentelemetry.api.internal.IncubatingUtil;
import io.opentelemetry.common.impl.ApiUsageLogger;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
class DefaultTracerProvider implements TracerProvider {

  private static final TracerProvider INSTANCE =
      IncubatingUtil.incubatingApiIfAvailable(
          new DefaultTracerProvider(),
          "io.opentelemetry.api.incubator.trace.ExtendedDefaultTracerProvider");

  static TracerProvider getInstance() {
    return INSTANCE;
  }

  @Override
  public Tracer get(String instrumentationScopeName) {
    if (instrumentationScopeName == null) {
      ApiUsageLogger.logNullParam(TracerProvider.class, "get", "instrumentationScopeName");
    }
    return DefaultTracer.getInstance();
  }

  @Override
  public Tracer get(String instrumentationScopeName, String instrumentationScopeVersion) {
    if (instrumentationScopeName == null) {
      ApiUsageLogger.logNullParam(TracerProvider.class, "get", "instrumentationScopeName");
    }
    return DefaultTracer.getInstance();
  }

  private DefaultTracerProvider() {}
}
