/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.trace;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class DefaultTracerProvider implements TracerProvider {

  private static final TracerProvider instance = new DefaultTracerProvider();

  /**
   * Returns a {@code TracerProvider} singleton that is the default implementation for {@link
   * TracerProvider}.
   *
   * @return a {@code TracerProvider} singleton that is the default implementation for {@link
   *     TracerProvider}.
   */
  public static TracerProvider getInstance() {
    return instance;
  }

  @Override
  public Tracer get(String instrumentationName) {
    return get(instrumentationName, null);
  }

  @Override
  public Tracer get(String instrumentationName, String instrumentationVersion) {
    return DefaultTracer.getInstance();
  }

  private DefaultTracerProvider() {}
}
