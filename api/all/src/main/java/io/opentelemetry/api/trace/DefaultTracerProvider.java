/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
class DefaultTracerProvider implements TracerProvider {

  private static final TracerProvider INSTANCE = new DefaultTracerProvider();

  static TracerProvider getInstance() {
    return INSTANCE;
  }

  @Override
  public Tracer get(String instrumentationName) {
    return DefaultTracer.getInstance();
  }

  @Override
  public Tracer get(String instrumentationName, String instrumentationVersion) {
    return DefaultTracer.getInstance();
  }

  private DefaultTracerProvider() {}
}
