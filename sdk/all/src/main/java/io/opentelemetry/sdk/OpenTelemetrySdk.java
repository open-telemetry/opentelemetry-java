/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.baggage.BaggageManager;
import io.opentelemetry.baggage.DefaultBaggageManager;
import io.opentelemetry.baggage.spi.BaggageManagerFactory;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.DefaultContextPropagators;
import io.opentelemetry.internal.Obfuscated;
import io.opentelemetry.metrics.DefaultMeterProvider;
import io.opentelemetry.metrics.MeterProvider;
import io.opentelemetry.metrics.spi.MeterProviderFactory;
import io.opentelemetry.sdk.baggage.BaggageManagerSdk;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.internal.MillisClock;
import io.opentelemetry.sdk.metrics.MeterSdkProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.TracerSdkManagement;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.trace.DefaultTracerProvider;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracerProvider;
import io.opentelemetry.trace.spi.TracerProviderFactory;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * This class provides a static global accessor for SDK telemetry objects {@link
 * TracerSdkManagement}, {@link MeterSdkProvider} and {@link BaggageManagerSdk}.
 *
 * <p>This is a convenience class getting and casting the telemetry objects from {@link
 * OpenTelemetry}.
 *
 * @see OpenTelemetry
 */
@ThreadSafe
public final class OpenTelemetrySdk implements OpenTelemetry {

  /**
   * Returns a {@link TracerSdkManagement}.
   *
   * @return TracerProvider returned by {@link OpenTelemetry#getGlobalTracerProvider()}.
   */
  public static TracerSdkManagement getGlobalTracerManagement() {
    return (TracerSdkProvider)
        ((Obfuscated<?>) OpenTelemetry.getGlobalTracerProvider()).unobfuscate();
  }

  /**
   * Returns a {@link MeterSdkProvider}.
   *
   * @return MeterProvider returned by {@link OpenTelemetry#getGlobalMeterProvider()}.
   */
  public static MeterSdkProvider getGlobalMeterProvider() {
    return (MeterSdkProvider) OpenTelemetry.getGlobalMeterProvider();
  }

  /**
   * Returns a {@link BaggageManagerSdk}.
   *
   * @return context manager returned by {@link OpenTelemetry#getGlobalBaggageManager()}.
   */
  public static BaggageManagerSdk getGlobalBaggageManager() {
    return (BaggageManagerSdk) OpenTelemetry.getGlobalBaggageManager();
  }

  private static final boolean HAS_BAGGAGE_SDK =
      hasClass("io.opentelemetry.sdk.baggage.BaggageSdk");
  private static final boolean HAS_METRICS_SDK = hasClass("io.opentelemetry.sdk.metrics.MeterSdk");
  private static final boolean HAS_TRACING_SDK = hasClass("io.opentelemetry.sdk.trace.TracerSdk");

  private static final AtomicBoolean INITIALIZED_GLOBAL = new AtomicBoolean();

  private final TracerProvider tracerProvider;
  private final MeterProvider meterProvider;
  private final BaggageManager baggageManager;
  private final ContextPropagators contextPropagators;

  private final Clock clock;
  private final Resource resource;

  OpenTelemetrySdk(
      TracerProvider tracerProvider,
      MeterProvider meterProvider,
      BaggageManager baggageManager,
      ContextPropagators contextPropagators,
      Clock clock,
      Resource resource) {
    this.tracerProvider = tracerProvider;
    this.meterProvider = meterProvider;
    this.baggageManager = baggageManager;
    this.contextPropagators = contextPropagators;
    this.clock = clock;
    this.resource = resource;
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
    return new OpenTelemetrySdk(
        tracerProvider, meterProvider, baggageManager, propagators, clock, resource);
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    return builder().setClock(clock).setPropagators(getPropagators()).setResource(resource);
  }

  public static class Builder {
    private Clock clock = MillisClock.getInstance();
    private Resource resource = Resource.getDefault();
    private ContextPropagators propagators = DefaultContextPropagators.builder().build();

    private TracerProvider tracerProvider;
    private MeterProvider meterProvider;
    private BaggageManager baggageManager;

    /**
     * Assign a {@link Clock}.
     *
     * @param clock The clock to use for all temporal needs.
     * @return this
     */
    public Builder setClock(Clock clock) {
      Objects.requireNonNull(clock, "clock");
      this.clock = clock;
      return this;
    }

    /**
     * Assign a {@link Resource} to be attached to all Spans created by Tracers.
     *
     * @param resource A Resource implementation.
     * @return this
     */
    public Builder setResource(Resource resource) {
      Objects.requireNonNull(resource, "resource");
      this.resource = resource;
      return this;
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
    public Builder setPropagators(ContextPropagators propagators) {
      Objects.requireNonNull(propagators, "propagators");
      this.propagators = propagators;
      return this;
    }

    /**
     * Returns a new {@link OpenTelemetrySdk} built with the configuration of this {@link Builder}.
     */
    public OpenTelemetrySdk build() {
      if (baggageManager == null) {
        BaggageManagerFactory baggageManagerFactory = loadSpi(BaggageManagerFactory.class);
        if (baggageManagerFactory != null) {
          baggageManager = baggageManagerFactory.create();
        } else if (HAS_BAGGAGE_SDK) {
          baggageManager = new BaggageManagerSdk();
        } else {
          baggageManager = DefaultBaggageManager.getInstance();
        }
      }

      if (meterProvider == null) {
        MeterProviderFactory meterProviderFactory = loadSpi(MeterProviderFactory.class);
        if (meterProviderFactory != null) {
          meterProvider = meterProviderFactory.create();
        } else if (HAS_METRICS_SDK) {
          meterProvider = MeterSdkProvider.builder().setClock(clock).setResource(resource).build();
        } else {
          meterProvider = DefaultMeterProvider.getInstance();
        }
      }

      if (tracerProvider == null) {
        TracerProviderFactory tracerProviderFactory = loadSpi(TracerProviderFactory.class);
        if (tracerProviderFactory != null) {
          tracerProvider = new ObfuscatedTracerProvider(tracerProviderFactory.create());
        } else if (HAS_TRACING_SDK) {
          tracerProvider =
              new ObfuscatedTracerProvider(
                  TracerSdkProvider.builder().setClock(clock).setResource(resource).build());
        } else {
          tracerProvider = DefaultTracerProvider.getInstance();
        }
      }

      OpenTelemetrySdk sdk =
          new OpenTelemetrySdk(
              tracerProvider, meterProvider, baggageManager, propagators, clock, resource);
      // Automatically initialize global OpenTelemetry with the first SDK we build.
      if (INITIALIZED_GLOBAL.compareAndSet(/* expectedValue= */ false, /* newValue= */ true)) {
        OpenTelemetry.setGlobalOpenTelemetry(sdk);
      }
      return sdk;
    }
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

  private static boolean hasClass(String className) {
    try {
      Class.forName(className);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
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
