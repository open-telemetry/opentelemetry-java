/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.internal.Obfuscated;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.metrics.spi.MeterProviderFactory;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.api.trace.spi.TracerProviderFactory;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.DefaultContextPropagators;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.internal.MillisClock;
import io.opentelemetry.sdk.metrics.MeterSdkProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.TracerSdkManagement;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/** The SDK implementation of {@link OpenTelemetry}. */
@ThreadSafe
public final class OpenTelemetrySdk implements OpenTelemetry {

  /**
   * Returns a new {@link Builder} for configuring an instance of {@linkplain OpenTelemetrySdk the
   * OpenTelemetry SDK}.
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Returns the global {@link OpenTelemetrySdk}. */
  public static OpenTelemetrySdk get() {
    return (OpenTelemetrySdk) OpenTelemetry.get();
  }

  /** Returns the global {@link TracerSdkManagement}. */
  public static TracerSdkManagement getGlobalTracerManagement() {
    OpenTelemetry openTelemetry = OpenTelemetry.get();
    if (openTelemetry instanceof OpenTelemetrySdk) {
      return ((OpenTelemetrySdk) openTelemetry).tracerSdkManagement;
    }
    TracerProvider tracerProvider = openTelemetry.getTracerProvider();
    if (tracerProvider instanceof TracerSdkManagement) {
      return (TracerSdkManagement) tracerProvider;
    }
    if (!(tracerProvider instanceof ObfuscatedTracerProvider)) {
      throw new IllegalStateException(
          "Trying to access global TracerSdkManagement but global TracerProvider is not an "
              + "instance created by this SDK.");
    }
    return (TracerSdkManagement) ((ObfuscatedTracerProvider) tracerProvider).unobfuscate();
  }

  /** Returns the global {@link MeterSdkProvider}. */
  public static MeterSdkProvider getGlobalMeterProvider() {
    return (MeterSdkProvider) OpenTelemetry.get().getMeterProvider();
  }

  private static final boolean HAS_METRICS_SDK = hasClass("io.opentelemetry.sdk.metrics.MeterSdk");
  private static final boolean HAS_TRACING_SDK = hasClass("io.opentelemetry.sdk.trace.TracerSdk");

  private static final AtomicBoolean INITIALIZED_GLOBAL = new AtomicBoolean();

  private final TracerProvider tracerProvider;
  @Nullable private final TracerSdkManagement tracerSdkManagement;
  private final MeterProvider meterProvider;
  private final ContextPropagators contextPropagators;

  private final Clock clock;
  private final Resource resource;

  private OpenTelemetrySdk(
      TracerProvider tracerProvider,
      @Nullable TracerSdkManagement tracerSdkManagement,
      MeterProvider meterProvider,
      ContextPropagators contextPropagators,
      Clock clock,
      Resource resource) {
    this.tracerProvider = tracerProvider;
    this.tracerSdkManagement = tracerSdkManagement;
    this.meterProvider = meterProvider;
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
  public ContextPropagators getPropagators() {
    return contextPropagators;
  }

  /** Returns the {@link Resource} for this {@link OpenTelemetrySdk}. */
  public Resource getResource() {
    return resource;
  }

  /** Returns the {@link Clock} for this {@link OpenTelemetrySdk}. */
  public Clock getClock() {
    return clock;
  }

  /** Returns the {@link TracerSdkManagement} for this {@link OpenTelemetrySdk}. */
  public TracerSdkManagement getTracerManagement() {
    if (tracerSdkManagement != null) {
      return tracerSdkManagement;
    }
    return (TracerSdkProvider) ((ObfuscatedTracerProvider) tracerProvider).unobfuscate();
  }

  /** Returns a new {@link Builder} initialized with the values of this {@link OpenTelemetrySdk}. */
  @Override
  public Builder toBuilder() {
    return builder()
        .setTracerProvider(tracerProvider)
        .setMeterProvider(meterProvider)
        .setPropagators(getPropagators())
        .setClock(clock)
        .setResource(resource);
  }

  /** A builder for configuring an {@link OpenTelemetrySdk}. */
  public static class Builder implements OpenTelemetry.Builder<Builder> {
    private Clock clock = MillisClock.getInstance();
    private Resource resource = Resource.getDefault();
    private ContextPropagators propagators = DefaultContextPropagators.builder().build();

    private TracerProvider tracerProvider;
    private MeterProvider meterProvider;
    private TracerSdkManagement tracerSdkManagement;

    /**
     * Sets the {@link TracerProvider} to use. This can be used to configure tracing settings by
     * returning the instance created by a {@link TracerSdkProvider.Builder}. If the TracerProvider
     * also implements the {@link TracerSdkManagement} interface, it will be used for that as well.
     * Otherwise, you must explicitly set a {@link TracerSdkManagement} instance via the {@link
     * #setTracerSdkManagement(TracerSdkManagement)} method.
     *
     * @see TracerSdkProvider#builder()
     * @see #setTracerSdkManagement(TracerSdkManagement)
     */
    @Override
    public Builder setTracerProvider(TracerProvider tracerProvider) {
      requireNonNull(tracerProvider, "tracerProvider");
      this.tracerProvider = tracerProvider;
      if (tracerProvider instanceof TracerSdkManagement) {
        this.tracerSdkManagement = (TracerSdkManagement) tracerProvider;
      }
      return this;
    }

    /**
     * Sets the {@link TracerSdkManagement} to use. Note that it is not necessary to set this
     * explicitly if your {@link TracerProvider} implementation also implements the {@link
     * TracerSdkManagement} interface.
     *
     * @see TracerSdkProvider#builder()
     * @see #setTracerProvider(TracerProvider)
     */
    public Builder setTracerSdkManagement(TracerSdkManagement tracerSdkManagement) {
      requireNonNull(tracerSdkManagement, "tracerSdkManagement");
      this.tracerSdkManagement = tracerSdkManagement;
      return this;
    }

    /**
     * Sets the {@link MeterProvider} to use.. This can be used to configure tracing settings by
     * returning the instance created by a {@link MeterSdkProvider.Builder}.
     *
     * @see MeterSdkProvider#builder()
     */
    @Override
    public Builder setMeterProvider(MeterProvider meterProvider) {
      requireNonNull(meterProvider, "meterProvider");
      this.meterProvider = meterProvider;
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
     */
    @Override
    public Builder setPropagators(ContextPropagators propagators) {
      requireNonNull(propagators, "propagators");
      this.propagators = propagators;
      return this;
    }

    /**
     * Sets the {@link Clock} to be used for measuring timings.
     *
     * @param clock The clock to use for all temporal needs.
     * @return this
     */
    public Builder setClock(Clock clock) {
      requireNonNull(clock, "clock");
      this.clock = clock;
      return this;
    }

    /**
     * Sets the {@link Resource} to be attached to all telemetry reported by this SDK.
     *
     * @param resource A Resource implementation.
     * @return this
     */
    public Builder setResource(Resource resource) {
      requireNonNull(resource, "resource");
      this.resource = resource;
      return this;
    }

    /**
     * Returns a new {@link OpenTelemetrySdk} built with the configuration of this {@link Builder}.
     */
    @Override
    public OpenTelemetrySdk build() {
      MeterProvider meterProvider = this.meterProvider;
      if (meterProvider == null) {
        MeterProviderFactory meterProviderFactory = loadSpi(MeterProviderFactory.class);
        if (meterProviderFactory != null) {
          meterProvider = meterProviderFactory.create();
        } else if (HAS_METRICS_SDK) {
          meterProvider = MeterSdkProvider.builder().setClock(clock).setResource(resource).build();
        } else {
          meterProvider = MeterProvider.getDefault();
        }
      }

      TracerProvider tracerProvider = this.tracerProvider;
      if (tracerProvider == null) {
        TracerProviderFactory tracerProviderFactory = loadSpi(TracerProviderFactory.class);
        if (tracerProviderFactory != null) {
          TracerProvider bareTracerProvider = tracerProviderFactory.create();
          tracerProvider = new ObfuscatedTracerProvider(bareTracerProvider);
          if (bareTracerProvider instanceof TracerSdkManagement) {
            tracerSdkManagement = (TracerSdkManagement) bareTracerProvider;
          }
        } else if (HAS_TRACING_SDK) {
          TracerSdkProvider bareTracerProvider =
              TracerSdkProvider.builder().setClock(clock).setResource(resource).build();
          tracerProvider = new ObfuscatedTracerProvider(bareTracerProvider);
          tracerSdkManagement = bareTracerProvider;
        } else {
          tracerProvider = TracerProvider.getDefault();
        }
      }

      if (tracerSdkManagement == null) {
        throw new IllegalStateException(
            "An OpenTelemetrySdk cannot be created without a valid TracerSdkManagement instance");
      }
      OpenTelemetrySdk sdk =
          new OpenTelemetrySdk(
              tracerProvider, tracerSdkManagement, meterProvider, propagators, clock, resource);
      // Automatically initialize global OpenTelemetry with the first SDK we build.
      if (INITIALIZED_GLOBAL.compareAndSet(/* expectedValue= */ false, /* newValue= */ true)) {
        OpenTelemetry.set(sdk);
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
  @VisibleForTesting
  static class ObfuscatedTracerProvider implements TracerProvider, Obfuscated<TracerProvider> {

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
