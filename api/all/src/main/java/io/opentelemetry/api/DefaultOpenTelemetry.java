/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.spi.OpenTelemetryFactory;
import io.opentelemetry.spi.trace.TracerProviderFactory;
import java.util.ServiceLoader;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * The default OpenTelemetry API, which tries to find API implementations via SPI or otherwise falls
 * back to no-op default implementations.
 */
@ThreadSafe
public class DefaultOpenTelemetry implements OpenTelemetry {
  private static final Object mutex = new Object();

  static OpenTelemetry getGlobalOpenTelemetry() {
    if (globalOpenTelemetry == null) {
      synchronized (mutex) {
        if (globalOpenTelemetry == null) {
          OpenTelemetryFactory openTelemetryFactory = loadSpi(OpenTelemetryFactory.class);
          if (openTelemetryFactory != null) {
            globalOpenTelemetry = openTelemetryFactory.create();
          } else {
            globalOpenTelemetry = builder().build();
          }
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

  private volatile ContextPropagators propagators;

  @Override
  public void setPropagators(ContextPropagators propagators) {
    this.propagators = propagators;
  }

  @Override
  public TracerProvider getTracerProvider() {
    return tracerProvider;
  }

  @Override
  public ContextPropagators getPropagators() {
    return propagators;
  }

  protected DefaultOpenTelemetry(TracerProvider tracerProvider, ContextPropagators propagators) {
    this.tracerProvider = tracerProvider;
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

  protected static class Builder implements OpenTelemetryBuilder<Builder> {
    protected ContextPropagators propagators = ContextPropagators.noop();

    protected TracerProvider tracerProvider;

    @Override
    public Builder setTracerProvider(TracerProvider tracerProvider) {
      requireNonNull(tracerProvider, "tracerProvider");
      this.tracerProvider = tracerProvider;
      return this;
    }

    @Override
    public Builder setPropagators(ContextPropagators propagators) {
      requireNonNull(propagators, "propagators");
      this.propagators = propagators;
      return this;
    }

    @Override
    public OpenTelemetry build() {
      TracerProvider tracerProvider = this.tracerProvider;
      if (tracerProvider == null) {
        TracerProviderFactory tracerProviderFactory = loadSpi(TracerProviderFactory.class);
        if (tracerProviderFactory != null) {
          tracerProvider = tracerProviderFactory.create();
        } else {
          tracerProvider = TracerProvider.getDefault();
        }
      }

      return new DefaultOpenTelemetry(tracerProvider, propagators);
    }
  }
}
