/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator;

import io.opentelemetry.api.incubator.ExtendedOpenTelemetry;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.extension.incubator.fileconfig.SdkConfigProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import java.io.Closeable;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/** A new interface for creating OpenTelemetrySdk that supports getting {@link ConfigProvider}. */
public class ExtendedOpenTelemetrySdk extends OpenTelemetrySdk
    implements ExtendedOpenTelemetry, Closeable {

  private final ObfuscatedConfigProvider configProvider;

  public ExtendedOpenTelemetrySdk(
      SdkTracerProvider tracerProvider,
      SdkMeterProvider meterProvider,
      SdkLoggerProvider loggerProvider,
      ContextPropagators propagators,
      SdkConfigProvider configProvider) {
    super(tracerProvider, meterProvider, loggerProvider, propagators);
    this.configProvider = new ObfuscatedConfigProvider(configProvider);
  }

  @Override
  public ConfigProvider getConfigProvider() {
    return configProvider.unobfuscate();
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
  static class ObfuscatedConfigProvider implements ConfigProvider {

    private final SdkConfigProvider delegate;

    ObfuscatedConfigProvider(SdkConfigProvider delegate) {
      this.delegate = delegate;
    }

    @Override
    @Nullable
    public DeclarativeConfigProperties getInstrumentationConfig() {
      if (delegate == null) {
        return null;
      }
      return delegate.getInstrumentationConfig();
    }

    public SdkConfigProvider unobfuscate() {
      return delegate;
    }
  }
}
