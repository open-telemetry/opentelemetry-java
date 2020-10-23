/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.baggage.BaggageManager;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.DefaultContextPropagators;
import io.opentelemetry.internal.Obfuscated;
import io.opentelemetry.metrics.DefaultMeterProvider;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.metrics.MeterProvider;
import io.opentelemetry.metrics.spi.MeterProviderFactory;
import io.opentelemetry.trace.DefaultTracerProvider;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracerProvider;
import io.opentelemetry.trace.spi.TracerProviderFactory;
import java.util.ServiceLoader;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * This class provides a static global accessor for telemetry objects {@link Tracer}, {@link Meter}
 * and {@link BaggageManager}.
 *
 * <p>The telemetry objects are lazy-loaded singletons resolved via {@link ServiceLoader} mechanism.
 *
 * @see TracerProvider
 * @see MeterProviderFactory
 */
@ThreadSafe
final class DefaultOpenTelemetry implements OpenTelemetry {
  private static final Object mutex = new Object();

  static OpenTelemetry getGlobalOpenTelemetry() {
    if (globalOpenTelemetry == null) {
      synchronized (mutex) {
        if (globalOpenTelemetry == null) {
          globalOpenTelemetry = DefaultOpenTelemetry.builder().build();
        }
      }
    }
    return globalOpenTelemetry;
  }

  static void setGlobalOpenTelemetry(OpenTelemetry openTelemetry) {
    globalOpenTelemetry = openTelemetry;
  }

  static Builder builder() {
    return new Builder();
  }

  @Nullable private static volatile OpenTelemetry globalOpenTelemetry;

  private final TracerProvider tracerProvider;
  private final MeterProvider meterProvider;

  private final ContextPropagators propagators;

  @Override
  public TracerProvider getTracerProvider() {
    return tracerProvider;
  }

  @Override
  public MeterProvider getMeterProvider() {
    return meterProvider;
  }

  @Override
  public ContextPropagators getPropagators() {
    return propagators;
  }

  DefaultOpenTelemetry(
      TracerProvider tracerProvider,
      MeterProvider meterProvider,
      BaggageManager baggageManager,
      ContextPropagators propagators) {
    this.tracerProvider = tracerProvider;
    this.meterProvider = meterProvider;
    this.propagators = propagators;
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
    globalOpenTelemetry = null;
  }

  Builder toBuilder() {
    return new Builder()
        .setTracerProvider(tracerProvider)
        .setMeterProvider(meterProvider)
        .setPropagators(propagators);
  }

  static class Builder {
    private ContextPropagators propagators = DefaultContextPropagators.builder().build();

    private TracerProvider tracerProvider;
    private MeterProvider meterProvider;
    private BaggageManager baggageManager;

    Builder setTracerProvider(TracerProvider tracerProvider) {
      requireNonNull(tracerProvider, "tracerProvider");
      this.tracerProvider = tracerProvider;
      return this;
    }

    Builder setMeterProvider(MeterProvider meterProvider) {
      requireNonNull(meterProvider, "meterProvider");
      this.meterProvider = meterProvider;
      return this;
    }

    Builder setBaggageManager(BaggageManager baggageManager) {
      requireNonNull(baggageManager, "baggageManager");
      this.baggageManager = baggageManager;
      return this;
    }

    Builder setPropagators(ContextPropagators propagators) {
      requireNonNull(propagators, "propagators");
      this.propagators = propagators;
      return this;
    }

    DefaultOpenTelemetry build() {
      MeterProvider meterProvider = this.meterProvider;
      if (meterProvider == null) {
        MeterProviderFactory meterProviderFactory = loadSpi(MeterProviderFactory.class);
        if (meterProviderFactory != null) {
          meterProvider = meterProviderFactory.create();
        } else {
          meterProvider = DefaultMeterProvider.getInstance();
        }
      }

      TracerProvider tracerProvider = this.tracerProvider;
      if (tracerProvider == null) {
        TracerProviderFactory tracerProviderFactory = loadSpi(TracerProviderFactory.class);
        if (tracerProviderFactory != null) {
          tracerProvider = new ObfuscatedTracerProvider(tracerProviderFactory.create());
        } else {
          tracerProvider = DefaultTracerProvider.getInstance();
        }
      }

      return new DefaultOpenTelemetry(tracerProvider, meterProvider, baggageManager, propagators);
    }
  }

  /**
   * A {@link TracerProvider} wrapper that forces users to access the SDK specific implementation
   * via the SDK, instead of via the API and casting it to the SDK specific implementation.
   *
   * @see Obfuscated
   */
  @ThreadSafe
  private static class ObfuscatedTracerProvider
      implements TracerProvider, Obfuscated<TracerProvider> {

    private final TracerProvider delegate;

    private ObfuscatedTracerProvider(TracerProvider delegate) {
      this.delegate = delegate;
    }

    @Override
    public Tracer get(String instrumentationName) {
      return delegate.get(instrumentationName);
    }

    @Override
    public Tracer get(String instrumentationName, String instrumentationVersion) {
      return delegate.get(instrumentationName, instrumentationVersion);
    }

    @Override
    public TracerProvider unobfuscate() {
      return delegate;
    }
  }
}
