/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

/** This class is a temporary solution until metrics SDK is marked stable. */
public class GlobalMeterProvider {
  private static volatile MeterProvider globalMeterProvider = MeterProvider.noop();

  private GlobalMeterProvider() {}

  /** Returns the globally registered {@link MeterProvider}. */
  public static MeterProvider get() {
    return globalMeterProvider;
  }

  /**
   * Sets the {@link MeterProvider} that should be the global instance. Future calls to {@link
   * #get()} will return the provided {@link MeterProvider} instance. This should be called once as
   * early as possible in your application initialization logic, often in a {@code static} block in
   * your main class.
   */
  public static void set(MeterProvider provider) {
    globalMeterProvider = (provider == null) ? MeterProvider.noop() : provider;
  }
}
