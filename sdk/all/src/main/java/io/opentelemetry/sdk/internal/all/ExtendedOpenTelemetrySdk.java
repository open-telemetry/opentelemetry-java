/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal.all;

import io.opentelemetry.api.incubator.ExtendedOpenTelemetry;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import javax.annotation.concurrent.ThreadSafe;

/**
 * {@link ExtendedOpenTelemetrySdk} is SDK implementation of {@link ExtendedOpenTelemetry}.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public final class ExtendedOpenTelemetrySdk extends OpenTelemetrySdk
    implements ExtendedOpenTelemetry {

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
