/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import java.util.concurrent.atomic.AtomicReference;

/**
 * IMPORTANT: This is a temporary class, and solution for the metrics package until it will be
 * marked as stable.
 */
public final class GlobalMetricsProvider {
  private static final Object mutex = new Object();
  private static final AtomicReference<MeterProvider> globalMeterProvider = new AtomicReference<>();

  private GlobalMetricsProvider() {}

  /** Returns the globally registered {@link MeterProvider}. */
  public static MeterProvider get() {
    MeterProvider meterProvider = globalMeterProvider.get();
    if (meterProvider == null) {
      synchronized (mutex) {
        if (globalMeterProvider.get() == null) {
          return MeterProvider.noop();
        }
      }
    }
    return meterProvider;
  }

  /**
   * Sets the {@link MeterProvider} that should be the global instance. Future calls to {@link
   * #get()} will return the provided {@link MeterProvider} instance. This should be called once as
   * early as possible in your application initialization logic, often in a {@code static} block in
   * your main class.
   */
  public static void set(MeterProvider meterProvider) {
    globalMeterProvider.set(meterProvider);
  }

  /**
   * Gets or creates a named meter instance from the globally registered {@link MeterProvider}.
   *
   * <p>This is a shortcut method for {@code getGlobalMeterProvider().get(instrumentationName)}
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library.
   * @return a tracer instance.
   */
  public static Meter getMeter(String instrumentationName) {
    return get().get(instrumentationName);
  }

  /**
   * Gets or creates a named and versioned meter instance from the globally registered {@link
   * MeterProvider}.
   *
   * <p>This is a shortcut method for {@code getGlobalMeterProvider().get(instrumentationName,
   * instrumentationVersion)}
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library.
   * @param instrumentationVersion The version of the instrumentation library.
   * @return a tracer instance.
   */
  public static Meter getMeter(String instrumentationName, String instrumentationVersion) {
    return get().get(instrumentationName, instrumentationVersion);
  }
}
