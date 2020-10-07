/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.baggage.BaggageManager;
import io.opentelemetry.baggage.DefaultBaggageManager;
import io.opentelemetry.baggage.spi.BaggageManagerFactory;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.DefaultContextPropagators;
import io.opentelemetry.metrics.DefaultMeterProvider;
import io.opentelemetry.metrics.MeterProvider;
import io.opentelemetry.metrics.spi.MeterProviderFactory;
import io.opentelemetry.spi.OpenTelemetryFactory;
import io.opentelemetry.trace.DefaultTracerProvider;
import io.opentelemetry.trace.TracerProvider;
import io.opentelemetry.trace.spi.TracerProviderFactory;
import java.util.ServiceLoader;
import javax.annotation.Nullable;

/**
 * The default OpenTelemetry API, which tries to find API implementations via SPI or otherwise
 * fallsback to no-op default implementations.
 */
class DefaultOpenTelemetry implements OpenTelemetry {

  static Builder newBuilder() {
    return new Builder();
  }

  static OpenTelemetry getGlobalOpenTelemetry() {
    if (globalOpenTelemetry == null) {
      synchronized (DefaultOpenTelemetry.class) {
        if (globalOpenTelemetry == null) {
          OpenTelemetryFactory openTelemetryFactory = loadSpi(OpenTelemetryFactory.class);
          if (openTelemetryFactory != null) {
            globalOpenTelemetry = openTelemetryFactory.create();
          } else {
            globalOpenTelemetry = newBuilder().build();
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
  public Builder toBuilder() {
    return new Builder()
        .setTracerProvider(tracerProvider)
        .setMeterProvider(meterProvider)
        .setBaggageManager(baggageManager)
        .setPropagators(contextPropagators);
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

  public static class Builder implements OpenTelemetry.Builder<Builder> {
    private ContextPropagators propagators = DefaultContextPropagators.builder().build();

    private TracerProvider tracerProvider;
    private MeterProvider meterProvider;
    private BaggageManager baggageManager;

    @Override
    public Builder setTracerProvider(TracerProvider tracerProvider) {
      requireNonNull(tracerProvider, "tracerProvider");
      this.tracerProvider = tracerProvider;
      return this;
    }

    @Override
    public Builder setMeterProvider(MeterProvider meterProvider) {
      requireNonNull(meterProvider, "meterProvider");
      this.meterProvider = meterProvider;
      return this;
    }

    @Override
    public Builder setBaggageManager(BaggageManager baggageManager) {
      requireNonNull(baggageManager, "baggageManager");
      this.baggageManager = baggageManager;
      return this;
    }

    @Override
    public Builder setPropagators(ContextPropagators propagators) {
      requireNonNull(propagators, "propagators");
      this.propagators = propagators;
      return this;
    }

    @Override
    public DefaultOpenTelemetry build() {
      BaggageManager baggageManager = this.baggageManager;
      if (baggageManager == null) {
        BaggageManagerFactory baggageManagerFactory = loadSpi(BaggageManagerFactory.class);
        if (baggageManagerFactory != null) {
          baggageManager = baggageManagerFactory.create();
        } else {
          baggageManager = DefaultBaggageManager.getInstance();
        }
      }

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
          tracerProvider = tracerProviderFactory.create();
        } else {
          tracerProvider = DefaultTracerProvider.getInstance();
        }
      }

      return new DefaultOpenTelemetry(tracerProvider, meterProvider, baggageManager, propagators);
    }
  }
}
