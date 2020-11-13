/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import io.opentelemetry.api.DefaultOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.internal.Obfuscated;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.internal.MillisClock;
import io.opentelemetry.sdk.metrics.MeterSdkProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.TracerSdkManagement;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.concurrent.ThreadSafe;

/** The SDK implementation of {@link OpenTelemetry}. */
@ThreadSafe
public final class OpenTelemetrySdk extends DefaultOpenTelemetry {

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
    TracerProvider tracerProvider = OpenTelemetry.get().getTracerProvider();
    if (!(tracerProvider instanceof ObfuscatedTracerProvider)) {
      throw new IllegalStateException(
          "Trying to access global TracerSdkManagement but global TracerProvider is not an "
              + "instance created by this SDK.");
    }
    return (TracerSdkProvider) ((ObfuscatedTracerProvider) tracerProvider).unobfuscate();
  }

  /** Returns the global {@link MeterSdkProvider}. */
  public static MeterSdkProvider getGlobalMeterProvider() {
    return (MeterSdkProvider) OpenTelemetry.get().getMeterProvider();
  }

  private static final AtomicBoolean INITIALIZED_GLOBAL = new AtomicBoolean();

  private final Clock clock;
  private final Resource resource;

  private OpenTelemetrySdk(
      TracerProvider tracerProvider,
      MeterProvider meterProvider,
      ContextPropagators contextPropagators,
      Clock clock,
      Resource resource) {
    super(tracerProvider, meterProvider, contextPropagators);
    this.clock = clock;
    this.resource = resource;
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
    return (TracerSdkProvider) ((ObfuscatedTracerProvider) getTracerProvider()).unobfuscate();
  }

  /** Returns a new {@link Builder} initialized with the values of this {@link OpenTelemetrySdk}. */
  @Override
  public Builder toBuilder() {
    return new Builder()
        .setTracerProvider(getTracerProvider())
        .setMeterProvider(getMeterProvider())
        .setPropagators(getPropagators())
        .setClock(clock)
        .setResource(resource);
  }

  /** A builder for configuring an {@link OpenTelemetrySdk}. */
  public static class Builder extends DefaultOpenTelemetry.Builder {
    private Clock clock;
    private Resource resource;

    /**
     * Sets the {@link TracerSdkProvider} to use. This can be used to configure tracing settings by
     * returning the instance created by a {@link TracerSdkProvider.Builder}.
     *
     * <p>Note: the parameter passed in here must be a {@link TracerSdkProvider} instance.
     *
     * @see TracerSdkProvider#builder()
     * @param tracerProvider A {@link TracerSdkProvider} to use with this instance.
     */
    @Override
    public Builder setTracerProvider(TracerProvider tracerProvider) {
      if (!(tracerProvider instanceof TracerSdkProvider)) {
        throw new IllegalArgumentException(
            "The OpenTelemetrySdk can only be configured with a TracerSdkProvider");
      }
      super.setTracerProvider(tracerProvider);
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
      if (!(meterProvider instanceof MeterSdkProvider)) {
        throw new IllegalArgumentException(
            "The OpenTelemetrySdk can only be configured with a MeterSdkProvider");
      }
      super.setMeterProvider(meterProvider);
      return this;
    }

    /** Sets the {@link ContextPropagators} to use. */
    @Override
    public Builder setPropagators(ContextPropagators propagators) {
      super.setPropagators(propagators);
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
      MeterProvider meterProvider = buildMeterProvider();
      TracerProvider tracerProvider = buildTracerProvider();

      OpenTelemetrySdk sdk =
          new OpenTelemetrySdk(
              new ObfuscatedTracerProvider(tracerProvider),
              meterProvider,
              super.propagators,
              clock== null ? MillisClock.getInstance() : clock,
              resource == null ? Resource.getDefault() :
              resource);
      // Automatically initialize global OpenTelemetry with the first SDK we build.
      if (INITIALIZED_GLOBAL.compareAndSet(/* expectedValue= */ false, /* newValue= */ true)) {
        OpenTelemetry.set(sdk);
      }
      return sdk;
    }

    private TracerProvider buildTracerProvider() {
      TracerProvider tracerProvider = super.tracerProvider;
      if (tracerProvider != null) {
        if (!(tracerProvider instanceof TracerSdkProvider)) {
          throw new IllegalStateException(
              "The OpenTelemetrySdk can only be configured with a TracerSdkProvider");
        }
        return tracerProvider;
      }
      TracerSdkProvider.Builder tracerProviderBuilder = TracerSdkProvider.builder();
      if (clock != null) {
        tracerProviderBuilder.setClock(clock);
      }
      if (resource != null) {
        tracerProviderBuilder.setResource(resource);
      }
      return tracerProviderBuilder.build();
    }

    private MeterProvider buildMeterProvider() {
      if (super.meterProvider != null) {
        return super.meterProvider;
      }
      MeterSdkProvider.Builder meterProviderBuilder = MeterSdkProvider.builder();
      if (clock != null) {
        meterProviderBuilder.setClock(clock);
      }
      if (resource != null) {
        meterProviderBuilder.setResource(resource);
      }
      return meterProviderBuilder.build();
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
