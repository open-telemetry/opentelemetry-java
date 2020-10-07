/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry;

import io.opentelemetry.baggage.BaggageManager;
import io.opentelemetry.baggage.DefaultBaggageManager;
import io.opentelemetry.baggage.spi.BaggageManagerFactory;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.DefaultContextPropagators;
import io.opentelemetry.internal.Obfuscated;
import io.opentelemetry.metrics.DefaultMeterProvider;
import io.opentelemetry.metrics.MeterProvider;
import io.opentelemetry.metrics.spi.MeterProviderFactory;
import io.opentelemetry.spi.OpenTelemetryFactory;
import io.opentelemetry.trace.DefaultTracerProvider;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracerProvider;
import io.opentelemetry.trace.spi.TracerProviderFactory;
import java.util.ServiceLoader;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

public class DefaultOpenTelemetry implements OpenTelemetry {

  static OpenTelemetry getGlobalOpenTelemetry() {
    if (globalOpenTelemetry == null) {
      synchronized (DefaultOpenTelemetry.class) {
        if (globalOpenTelemetry == null) {
          OpenTelemetryFactory openTelemetryFactory = loadSpi(OpenTelemetryFactory.class);
          if (openTelemetryFactory != null) {
            globalOpenTelemetry = openTelemetryFactory.create();
          } else {
            globalOpenTelemetry = new DefaultOpenTelemetry();
          }
        }
      }
    }
    return globalOpenTelemetry;
  }

  static void setGlobalOpenTelemetry(OpenTelemetry openTelemetry) {
    globalOpenTelemetry = openTelemetry;
  }

  @Nullable private static volatile OpenTelemetry globalOpenTelemetry;

  private final TracerProvider tracerProvider;
  private final MeterProvider meterProvider;
  private final BaggageManager baggageManager;
  private final ContextPropagators contextPropagators;

  DefaultOpenTelemetry(
      TracerProvider tracerProvider,
      MeterProvider meterProvider,
      BaggageManager baggageManager,
      ContextPropagators contextPropagators) {
    this.tracerProvider = tracerProvider;
    this.meterProvider = meterProvider;
    this.baggageManager = baggageManager;
    this.contextPropagators = contextPropagators;
  }

  protected DefaultOpenTelemetry() {
    this(DefaultContextPropagators.builder().build());
  }

  protected DefaultOpenTelemetry(ContextPropagators propagators) {
    TracerProviderFactory tracerProviderFactory = loadSpi(TracerProviderFactory.class);
    this.tracerProvider =
        tracerProviderFactory != null
            ? new ObfuscatedTracerProvider(tracerProviderFactory.create())
            : DefaultTracerProvider.getInstance();

    MeterProviderFactory meterProviderFactory = loadSpi(MeterProviderFactory.class);
    this.meterProvider =
        meterProviderFactory != null
            ? meterProviderFactory.create()
            : DefaultMeterProvider.getInstance();
    BaggageManagerFactory baggageManagerFactory = loadSpi(BaggageManagerFactory.class);
    this.baggageManager =
        baggageManagerFactory != null
            ? baggageManagerFactory.create()
            : DefaultBaggageManager.getInstance();

    this.contextPropagators = propagators;
  }

  @Override
  public TracerProvider getTracerProvider() {
    return tracerProvider;
  }

  @Override
  public MeterProvider getMeterProvider() {
    return meterProvider;
  }

  @Override
  public BaggageManager getBaggageManager() {
    return baggageManager;
  }

  @Override
  public ContextPropagators getPropagators() {
    return contextPropagators;
  }

  @Override
  public OpenTelemetry withPropagators(ContextPropagators propagators) {
    return new DefaultOpenTelemetry(tracerProvider, meterProvider, baggageManager, propagators);
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
