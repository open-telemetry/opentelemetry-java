/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import io.opentelemetry.api.OpenTelemetry;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A registry for creating named {@link Meter}s. The name <i>Provider</i> is for consistency with
 * other languages and it is <b>NOT</b> loaded using reflection.
 *
 * @see OpenTelemetry
 * @see io.opentelemetry.api.metrics.Meter
 */
@ThreadSafe
public interface MeterProvider {

  /**
   * Returns a {@link MeterProvider} that only creates no-op {@link Instrument}s that neither record
   * nor are emitted.
   */
  static MeterProvider getDefault() {
    return DefaultMeterProvider.getInstance();
  }

  /**
   * Gets or creates a named meter instance.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library.
   * @return a tracer instance.
   */
  Meter get(String instrumentationName);

  /**
   * Gets or creates a named and versioned meter instance.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library.
   * @param instrumentationVersion The version of the instrumentation library.
   * @return a tracer instance.
   */
  Meter get(String instrumentationName, String instrumentationVersion);
}
