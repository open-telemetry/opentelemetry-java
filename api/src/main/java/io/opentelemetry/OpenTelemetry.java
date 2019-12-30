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

import io.opentelemetry.correlationcontext.CorrelationContextManager;
import io.opentelemetry.correlationcontext.DefaultCorrelationContextManager;
import io.opentelemetry.correlationcontext.spi.CorrelationContextManagerProvider;
import io.opentelemetry.metrics.DefaultMeterRegistry;
import io.opentelemetry.metrics.DefaultMeterRegistryProvider;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.metrics.MeterRegistry;
import io.opentelemetry.metrics.spi.MeterRegistryProvider;
import io.opentelemetry.trace.DefaultTracer;
import io.opentelemetry.trace.DefaultTracerRegistry;
import io.opentelemetry.trace.DefaultTracerRegistryProvider;
import io.opentelemetry.trace.DefaultTracerFactory.TracerKey;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracerRegistry;
import io.opentelemetry.trace.spi.TracerRegistryProvider;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import javax.annotation.concurrent.ThreadSafe;

/**
 * This class provides a static global accessor for telemetry objects {@link Tracer}, {@link Meter}
 * and {@link CorrelationContextManager}.
 *
 * <p>By default, the telemetry objects are lazy-loaded singletons resolved via the Java SPI {@link
 * ServiceLoader} mechanism. This can be circumvented by calling setInstance methods on a individual
 * components before any one has attempted to access the default singletons.
 *
 * @see TracerRegistry
 * @see MeterRegistryProvider
 * @see CorrelationContextManagerProvider
 */
@ThreadSafe
public final class OpenTelemetry {

  private static final Logger logger = Logger.getLogger(OpenTelemetry.class.getName());

  private static final OpenTelemetry instance = new OpenTelemetry();

  private final AtomicReference<TracerFactory> tracerFactory = new AtomicReference<>();
  private final AtomicReference<MeterFactory> meterFactory = new AtomicReference<>();
  private final AtomicReference<DistributedContextManager> contextManager = new AtomicReference<>();

  private OpenTelemetry() {}

  /**
   * Returns a singleton {@link TracerRegistry}.
   *
   * @return registered TracerRegistry of default via {@link DefaultTracerRegistry#getInstance()}.
   * @throws IllegalStateException if a specified TracerRegistry (via system properties) could not
   *     be found.
   * @since 0.1.0
   */
  public static TracerFactory getTracerFactory() {
    if (instance.tracerFactory.get() == null) {
      setTracerFactory(SpiOpenTelemetryProvider.makeSpiTracerFactory());
    }
    return instance.tracerFactory.get();
  }

  /**
   * Returns a singleton {@link MeterRegistry}.
   *
   * @return registered MeterRegistry or default via {@link DefaultMeterRegistry#getInstance()}.
   * @throws IllegalStateException if a specified MeterRegistry (via system properties) could not be
   *     found.
   * @since 0.1.0
   */
  public static MeterFactory getMeterFactory() {
    if (instance.meterFactory.get() == null) {
      setMeterFactory(SpiOpenTelemetryProvider.makeSpiMeterFactory());
    }
    return instance.meterFactory.get();
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
    if (instance.contextManager.get() == null) {
      setDistributedContextManager(SpiOpenTelemetryProvider.makeSpiContextManager());
    }
    return instance.contextManager.get();
  }

  /**
   * Assigns the global singleton MeterFactory instance. This can be done exactly once; subsequent
   * calls will be ignored.
   *
   * <p>Note, if calls to the static accessor for the MeterFactory are made before this is called,
   * the global singleton will be initialized with an SPI-provided implementation.
   *
   * <p>Therefore, if you wish to use a non-SPI provided instance, you must make sure to call this
   * method before any instrumentation will have forced the SPI implementation to be loaded.
   *
   * @param meterFactory A fully configured and ready to operate MeterFactory implementation.
   */
  public static void setMeterFactory(MeterFactory meterFactory) {
    if (!instance.meterFactory.compareAndSet(null, meterFactory)) {
      logger.warning(
          "The global OpenTelemetry MeterFactory instance has already been set. "
              + "Ignoring this assignment");
    }
  }

  /**
   * Assigns the global singleton TracerFactory instance. This can be done exactly once; subsequent
   * calls will be ignored.
   *
   * <p>Note, if calls to the static accessor for the TracerFactory are made before this is called,
   * the global singleton will be initialized with an SPI-provided implementation.
   *
   * <p>Therefore, if you wish to use a non-SPI provided instance, you must make sure to call this
   * method before any instrumentation will have forced the SPI implementation to be loaded.
   *
   * @param tracerFactory A fully configured and ready to operate {@link TracerFactory}
   *     implementation.
   */
  public static void setTracerFactory(TracerFactory tracerFactory) {
    if (!instance.tracerFactory.compareAndSet(null, tracerFactory)) {
      logger.warning(
          "The global OpenTelemetry TracerFactory instance has already been set. "
              + "Ignoring this assignment");
      return;
    }

    // swap out any tracers that have been handed out with the proper ones.
    Map<TracerKey, DefaultTracer> existingTracers = DefaultTracerFactory.getExistingTracers();
    for (Entry<TracerKey, DefaultTracer> entry : existingTracers.entrySet()) {
      TracerKey key = entry.getKey();
      entry.getValue().setImplementation(tracerFactory.get(key.getName(), key.getVersion()));
    }
  }

  private OpenTelemetry() {
    TracerFactoryProvider tracerFactoryProvider = loadSpi(TracerFactoryProvider.class);
    this.tracerFactory =
        tracerFactoryProvider != null
            ? tracerFactoryProvider.create()
            : DefaultTracerFactoryProvider.getInstance().create();

    MeterFactoryProvider meterFactoryProvider = loadSpi(MeterFactoryProvider.class);
    meterFactory =
        meterFactoryProvider != null
            ? meterFactoryProvider.create()
            : DefaultMeterFactoryProvider.getInstance().create();
    DistributedContextManagerProvider contextManagerProvider =
        loadSpi(DistributedContextManagerProvider.class);
    contextManager =
        contextManagerProvider != null
            ? contextManagerProvider.create()
            : DefaultDistributedContextManager.getInstance();
  }

  /**
   * Assigns the global singleton DistributedContextManager instance. This can be done exactly once;
   * subsequent calls will be ignored.
   *
   * <p>Note, if calls to the static accessor for the DistributedContextManager are made before this
   * is called, the global singleton will be initialized with an SPI-provided implementation.
   *
   * <p>Therefore, if you wish to use a non-SPI provided instance, you must make sure to call this
   * method before any instrumentation will have forced the SPI implementation to be loaded.
   *
   * @param contextManager A fully configured and ready to operate DistributedContextManager
   *     implementation.
   */
  public static void setDistributedContextManager(DistributedContextManager contextManager) {
    if (!instance.contextManager.compareAndSet(null, contextManager)) {
      logger.warning(
          "The global OpenTelemetry DistributedContextManager instance has already been set. "
              + "Ignoring this assignment");
    }
  }

  // for testing only
  static void reset() {
    instance.meterFactory.set(null);
    instance.tracerFactory.set(null);
    instance.contextManager.set(null);
  }
}
