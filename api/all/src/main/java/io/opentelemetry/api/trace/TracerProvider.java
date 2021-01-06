/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A registry for creating named {@link Tracer}s. The name <i>Provider</i> is for consistency with *
 * other languages and it is <b>NOT</b> loaded using reflection.
 *
 * @see Tracer
 */
@ThreadSafe
public interface TracerProvider {

  /**
   * Returns a no-op {@link TracerProvider} which only creates no-op {@link Span}s which do not
   * record nor are emitted.
   */
  static TracerProvider getDefault() {
    return DefaultTracerProvider.getInstance();
  }

  /**
   * Gets or creates a named tracer instance.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library (e.g., "io.opentelemetry.contrib.mongodb"). Must not be null.
   * @return a tracer instance.
   */
  Tracer get(String instrumentationName);

  /**
   * Gets or creates a named and versioned tracer instance.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library (e.g., "io.opentelemetry.contrib.mongodb"). Must not be null.
   * @param instrumentationVersion The version of the instrumentation library (e.g., "1.0.0").
   * @return a tracer instance.
   */
  Tracer get(String instrumentationName, String instrumentationVersion);
}
