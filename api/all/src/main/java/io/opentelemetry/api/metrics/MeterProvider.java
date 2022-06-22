/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A registry for creating named {@link Meter}s.
 *
 * <p>The name <i>Provider</i> is for consistency with other languages and it is <b>NOT</b> loaded
 * using reflection.
 *
 * @see Meter
 * @since 1.10.0
 */
@ThreadSafe
public interface MeterProvider {
  /**
   * Gets or creates a named Meter instance.
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @return a Meter instance.
   */
  default Meter get(String instrumentationScopeName) {
    return meterBuilder(instrumentationScopeName).build();
  }

  /**
   * Creates a MeterBuilder for a named Meter instance.
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @return a MeterBuilder instance.
   */
  MeterBuilder meterBuilder(String instrumentationScopeName);

  /** Returns a no-op {@link MeterProvider} which provides meters which do not record or emit. */
  static MeterProvider noop() {
    return DefaultMeterProvider.getInstance();
  }
}
