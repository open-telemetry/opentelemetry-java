/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator;

import io.opentelemetry.api.incubator.ExtendedOpenTelemetry;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.extension.incubator.fileconfig.SdkConfigProvider;
import java.io.Closeable;
import javax.annotation.concurrent.ThreadSafe;

/** A new interface for creating OpenTelemetrySdk that supports getting {@link ConfigProvider}. */
public final class ExtendedOpenTelemetrySdk extends OpenTelemetrySdk
    implements ExtendedOpenTelemetry, Closeable {

  private final OpenTelemetrySdk openTelemetrySdk;
  private final ObfuscatedConfigProvider configProvider;

  private ExtendedOpenTelemetrySdk(
      OpenTelemetrySdk openTelemetrySdk, SdkConfigProvider configProvider) {
    super(
        openTelemetrySdk.getSdkTracerProvider(),
        openTelemetrySdk.getSdkMeterProvider(),
        openTelemetrySdk.getSdkLoggerProvider(),
        openTelemetrySdk.getPropagators());
    this.openTelemetrySdk = openTelemetrySdk;
    this.configProvider = new ObfuscatedConfigProvider(configProvider);
  }

  public static ExtendedOpenTelemetrySdk create(
      OpenTelemetrySdk openTelemetrySdk, SdkConfigProvider sdkConfigProvider) {
    return new ExtendedOpenTelemetrySdk(openTelemetrySdk, sdkConfigProvider);
  }

  @Override
  public ConfigProvider getConfigProvider() {
    return configProvider;
  }

  /** Returns the {@link SdkConfigProvider} for this {@link ExtendedOpenTelemetrySdk}. */
  public SdkConfigProvider getSdkConfigProvider() {
    return configProvider.unobfuscate();
  }

  @Override
  public String toString() {
    return "ExtendedOpenTelemetrySdk{"
        + "openTelemetrySdk="
        + openTelemetrySdk
        + ", configProvider="
        + configProvider.unobfuscate()
        + "}";
  }

  /**
   * This class allows the SDK to unobfuscate an obfuscated provider.
   *
   * <p>Static global providers are obfuscated when they are returned from the API to prevent users
   * from casting them to their SDK specific implementation. For example, we do not want users to
   * use patterns like {@code (SdkConfigProvider) openTelemetry.getConfigProvider()}.
   */
  @ThreadSafe
  private static class ObfuscatedConfigProvider implements ConfigProvider {

    private final SdkConfigProvider delegate;

    private ObfuscatedConfigProvider(SdkConfigProvider delegate) {
      this.delegate = delegate;
    }

    @Override
    public DeclarativeConfigProperties getInstrumentationConfig() {
      return delegate.getInstrumentationConfig();
    }

    private SdkConfigProvider unobfuscate() {
      return delegate;
    }
  }
}
