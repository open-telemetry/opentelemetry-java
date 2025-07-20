/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator;

import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.logs.LoggerBuilder;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.metrics.MeterBuilder;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerBuilder;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.shutdown.WithShutdown;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.annotation.concurrent.ThreadSafe;

/** The SDK implementation of {@link ExtendedOpenTelemetrySdk}. */
public final class ObfuscatedExtendedOpenTelemetrySdk
    implements ExtendedOpenTelemetrySdk, WithShutdown {

  private static final Logger LOGGER =
      Logger.getLogger(ObfuscatedExtendedOpenTelemetrySdk.class.getName());
  private final AtomicBoolean isShutdown = new AtomicBoolean(false);
  private final ObfuscatedTracerProvider tracerProvider;
  private final ObfuscatedMeterProvider meterProvider;
  private final ObfuscatedLoggerProvider loggerProvider;
  private final ConfigProvider configProvider;
  private final ContextPropagators propagators;

  public ObfuscatedExtendedOpenTelemetrySdk(
      ConfigProvider configProvider,
      SdkTracerProvider tracerProvider,
      SdkMeterProvider meterProvider,
      SdkLoggerProvider loggerProvider,
      ContextPropagators propagators) {
    this.configProvider = configProvider;
    this.tracerProvider = new ObfuscatedTracerProvider(tracerProvider);
    this.meterProvider = new ObfuscatedMeterProvider(meterProvider);
    this.loggerProvider = new ObfuscatedLoggerProvider(loggerProvider);
    this.propagators = propagators;
  }

  @Override
  public CompletableResultCode shutdown() {
    if (!isShutdown.compareAndSet(false, true)) {
      LOGGER.info("Multiple shutdown calls");
      return CompletableResultCode.ofSuccess();
    }
    List<CompletableResultCode> results = new ArrayList<>();
    results.add(tracerProvider.unobfuscate().shutdown());
    results.add(meterProvider.unobfuscate().shutdown());
    results.add(loggerProvider.unobfuscate().shutdown());
    return CompletableResultCode.ofAll(results);
  }

  @Override
  public ConfigProvider getConfigProvider() {
    return configProvider;
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
  public LoggerProvider getLogsBridge() {
    return loggerProvider;
  }

  @Override
  public ContextPropagators getPropagators() {
    return propagators;
  }

  @Override
  public String toString() {
    return "ExtendedOpenTelemetrySdk{"
        + "configProvider="
        + configProvider
        + ", tracerProvider="
        + tracerProvider.unobfuscate()
        + ", meterProvider="
        + meterProvider.unobfuscate()
        + ", loggerProvider="
        + loggerProvider.unobfuscate()
        + ", propagators="
        + propagators
        + "}";
  }

  /**
   * This class allows the SDK to unobfuscate an obfuscated static global provider.
   *
   * <p>Static global providers are obfuscated when they are returned from the API to prevent users
   * from casting them to their SDK specific implementation. For example, we do not want users to
   * use patterns like {@code (SdkTracerProvider) openTelemetry.getTracerProvider()}.
   */
  @ThreadSafe
  // Visible for testing
  static class ObfuscatedTracerProvider implements TracerProvider {

    private final SdkTracerProvider delegate;

    ObfuscatedTracerProvider(SdkTracerProvider delegate) {
      this.delegate = delegate;
    }

    @Override
    public Tracer get(String instrumentationScopeName) {
      return delegate.get(instrumentationScopeName);
    }

    @Override
    public Tracer get(String instrumentationScopeName, String instrumentationScopeVersion) {
      return delegate.get(instrumentationScopeName, instrumentationScopeVersion);
    }

    @Override
    public TracerBuilder tracerBuilder(String instrumentationScopeName) {
      return delegate.tracerBuilder(instrumentationScopeName);
    }

    public SdkTracerProvider unobfuscate() {
      return delegate;
    }
  }

  /**
   * This class allows the SDK to unobfuscate an obfuscated static global provider.
   *
   * <p>Static global providers are obfuscated when they are returned from the API to prevent users
   * from casting them to their SDK specific implementation. For example, we do not want users to
   * use patterns like {@code (SdkMeterProvider) openTelemetry.getMeterProvider()}.
   */
  @ThreadSafe
  // Visible for testing
  static class ObfuscatedMeterProvider implements MeterProvider {

    private final SdkMeterProvider delegate;

    ObfuscatedMeterProvider(SdkMeterProvider delegate) {
      this.delegate = delegate;
    }

    @Override
    public MeterBuilder meterBuilder(String instrumentationScopeName) {
      return delegate.meterBuilder(instrumentationScopeName);
    }

    public SdkMeterProvider unobfuscate() {
      return delegate;
    }
  }

  /**
   * This class allows the SDK to unobfuscate an obfuscated static global provider.
   *
   * <p>Static global providers are obfuscated when they are returned from the API to prevent users
   * from casting them to their SDK specific implementation. For example, we do not want users to
   * use patterns like {@code (SdkMeterProvider) openTelemetry.getMeterProvider()}.
   */
  @ThreadSafe
  // Visible for testing
  static class ObfuscatedLoggerProvider implements LoggerProvider {

    private final SdkLoggerProvider delegate;

    ObfuscatedLoggerProvider(SdkLoggerProvider delegate) {
      this.delegate = delegate;
    }

    @Override
    public LoggerBuilder loggerBuilder(String instrumentationScopeName) {
      return delegate.loggerBuilder(instrumentationScopeName);
    }

    public SdkLoggerProvider unobfuscate() {
      return delegate;
    }
  }
}
