/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.metrics;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A registry for creating named {@link Meter}s. The name <i>Provider</i> is for consistency with
 * other languages and it is <b>NOT</b> loaded using reflection.
 *
 * @see io.opentelemetry.OpenTelemetry
 * @see io.opentelemetry.metrics.Meter
 * @since 0.1.0
 */
@ThreadSafe
public interface MeterProvider {

  /**
   * Gets or creates a named meter instance.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library.
   * @return a tracer instance.
   * @since 0.1.0
   */
  Meter get(String instrumentationName);

  /**
   * Gets or creates a named and versioned meter instance.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library.
   * @param instrumentationVersion The version of the instrumentation library.
   * @return a tracer instance.
   * @since 0.1.0
   */
  Meter get(String instrumentationName, String instrumentationVersion);
}
