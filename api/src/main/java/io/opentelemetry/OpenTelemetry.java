/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry;

import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.DefaultContextPropagators;
import io.opentelemetry.correlationcontext.CorrelationContextManager;
import io.opentelemetry.correlationcontext.DefaultCorrelationContextManager;
import io.opentelemetry.correlationcontext.DefaultCorrelationContextManagerProvider;
import io.opentelemetry.correlationcontext.spi.CorrelationContextManagerProvider;
import io.opentelemetry.internal.Utils;
import io.opentelemetry.metrics.DefaultMeterProvider;
import io.opentelemetry.metrics.DefaultMetricsProvider;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.metrics.MeterProvider;
import io.opentelemetry.metrics.spi.MetricsProvider;
import io.opentelemetry.trace.DefaultTraceProvider;
import io.opentelemetry.trace.DefaultTracerProvider;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracerProvider;
import io.opentelemetry.trace.propagation.HttpTraceContext;
import io.opentelemetry.trace.spi.TraceProvider;
import java.util.ServiceLoader;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * This class provides a static global accessor for telemetry objects {@link Tracer}, {@link Meter}
 * and {@link CorrelationContextManager}.
 *
 * <p>The telemetry objects are lazy-loaded singletons resolved via {@link ServiceLoader} mechanism.
 *
 * @see TracerProvider
 * @see MetricsProvider
 * @see CorrelationContextManagerProvider
 */
@ThreadSafe
public final class OpenTelemetry {
  private static final Object mutex = new Object();

  @Nullable private static volatile OpenTelemetry instance;

  private final TracerProvider tracerProvider;
  private final MeterProvider meterProvider;
  private final CorrelationContextManager contextManager;

  private volatile ContextPropagators propagators =
      DefaultContextPropagators.builder().addHttpTextFormat(new HttpTraceContext()).build();

  /**
   * Returns a singleton {@link TracerProvider}.
   *
   * @return registered TracerProvider of default via {@link DefaultTracerProvider#getInstance()}.
   * @throws IllegalStateException if a specified TracerProvider (via system properties) could not
   *     be found.
   * @since 0.1.0
   */
  public static TracerProvider getTracerProvider() {
    return getInstance().tracerProvider;
  }

  /**
   * Returns a singleton {@link MeterProvider}.
   *
   * @return registered MeterProvider or default via {@link DefaultMeterProvider#getInstance()}.
   * @throws IllegalStateException if a specified MeterProvider (via system properties) could not be
   *     found.
   * @since 0.1.0
   */
  public static MeterProvider getMeterProvider() {
    return getInstance().meterProvider;
  }

  /**
   * Returns a singleton {@link CorrelationContextManager}.
   *
   * @return registered manager or default via {@link
   *     DefaultCorrelationContextManager#getInstance()}.
   * @throws IllegalStateException if a specified manager (via system properties) could not be
   *     found.
   * @since 0.1.0
   */
  public static CorrelationContextManager getCorrelationContextManager() {
    return getInstance().contextManager;
  }

  /**
   * Returns a {@link ContextPropagators} object, which can be used to access the set of registered
   * propagators for each supported format.
   *
   * @return registered propagators container, defaulting to a {@link ContextPropagators} object
   *     with {@link HttpTraceContext} registered.
   * @throws IllegalStateException if a specified manager (via system properties) could not be
   *     found.
   * @since 0.3.0
   */
  public static ContextPropagators getPropagators() {
    return getInstance().propagators;
  }

  /**
   * Sets the {@link ContextPropagators} object, which can be used to access the set of registered
   * propagators for each supported format.
   *
   * @param propagators the {@link ContextPropagators} object to be registered.
   * @throws IllegalStateException if a specified manager (via system properties) could not be
   *     found.
   * @throws NullPointerException if {@code propagators} is {@code null}.
   * @since 0.3.0
   */
  public static void setPropagators(ContextPropagators propagators) {
    Utils.checkNotNull(propagators, "propagators");
    getInstance().propagators = propagators;
  }

  /** Lazy loads an instance. */
  private static OpenTelemetry getInstance() {
    if (instance == null) {
      synchronized (mutex) {
        if (instance == null) {
          instance = new OpenTelemetry();
        }
      }
    }
    return instance;
  }

  private OpenTelemetry() {
    TraceProvider traceProvider = loadSpi(TraceProvider.class);
    this.tracerProvider =
        traceProvider != null
            ? traceProvider.create()
            : DefaultTraceProvider.getInstance().create();

    MetricsProvider metricsProvider = loadSpi(MetricsProvider.class);
    meterProvider =
        metricsProvider != null
            ? metricsProvider.create()
            : DefaultMetricsProvider.getInstance().create();
    CorrelationContextManagerProvider contextManagerProvider =
        loadSpi(CorrelationContextManagerProvider.class);
    contextManager =
        contextManagerProvider != null
            ? contextManagerProvider.create()
            : DefaultCorrelationContextManagerProvider.getInstance().create();
  }

  /**
   * Load provider class via {@link ServiceLoader}. A specific provider class can be requested via
   * setting a system property with FQCN.
   *
   * @param providerClass a provider class
   * @param <T> provider type
   * @return a provider or null if not found
   * @throws IllegalStateException if a specified provider is not found
   */
  @Nullable
  private static <T> T loadSpi(Class<T> providerClass) {
    String specifiedProvider = System.getProperty(providerClass.getName());
    ServiceLoader<T> providers = ServiceLoader.load(providerClass);
    for (T provider : providers) {
      if (specifiedProvider == null || specifiedProvider.equals(provider.getClass().getName())) {
        return provider;
      }
    }
    if (specifiedProvider != null) {
      throw new IllegalStateException(
          String.format("Service provider %s not found", specifiedProvider));
    }
    return null;
  }

  // for testing
  static void reset() {
    instance = null;
  }
}
