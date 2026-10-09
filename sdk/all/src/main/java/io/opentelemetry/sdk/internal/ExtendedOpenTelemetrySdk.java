/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import io.opentelemetry.api.incubator.ExtendedOpenTelemetry;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.internal.SdkLoggerProviderUtil;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.internal.SdkResourceProvider;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.internal.SdkTracerProviderUtil;
import javax.annotation.Nullable;
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
  @Nullable private final SdkResourceProvider resourceProvider;

  private ExtendedOpenTelemetrySdk(
      OpenTelemetrySdk openTelemetrySdk, SdkConfigProvider configProvider) {
    super(
        openTelemetrySdk.getSdkTracerProvider(),
        openTelemetrySdk.getSdkMeterProvider(),
        openTelemetrySdk.getSdkLoggerProvider(),
        openTelemetrySdk.getPropagators());
    this.openTelemetrySdk = openTelemetrySdk;
    this.configProvider = new ObfuscatedConfigProvider(configProvider);
    this.resourceProvider = resolveSdkResourceProvider(openTelemetrySdk);
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

  /**
   * Returns the {@link SdkResourceProvider} shared by the SDK's tracer, meter, and logger
   * providers.
   *
   * @throws IllegalStateException if the three signal providers were not configured with equivalent
   *     resources.
   */
  public SdkResourceProvider getSdkResourceProvider() {
    if (resourceProvider == null) {
      throw new IllegalStateException(
          "SdkTracerProvider, SdkMeterProvider, and SdkLoggerProvider are configured with"
              + " different resources. Configure all three signal providers with the same"
              + " SdkResourceProvider (or the same Resource) to resolve this.");
    }
    return resourceProvider;
  }

  @Nullable
  private static SdkResourceProvider resolveSdkResourceProvider(OpenTelemetrySdk sdk) {
    SdkTracerProvider tracerProvider = sdk.getSdkTracerProvider();
    SdkMeterProvider meterProvider = sdk.getSdkMeterProvider();
    SdkLoggerProvider loggerProvider = sdk.getSdkLoggerProvider();
    SdkResourceProvider tracerRp = SdkTracerProviderUtil.getSdkResourceProvider(tracerProvider);
    SdkResourceProvider meterRp = SdkMeterProviderUtil.getSdkResourceProvider(meterProvider);
    SdkResourceProvider loggerRp = SdkLoggerProviderUtil.getSdkResourceProvider(loggerProvider);
    Resource tracerResource = tracerRp.getResource();
    if (tracerResource.equals(meterRp.getResource())
        && tracerResource.equals(loggerRp.getResource())) {
      return tracerRp;
    }
    return null;
  }

  @Override
  public String toString() {
    return "ExtendedOpenTelemetrySdk{"
        + "openTelemetrySdk="
        + openTelemetrySdk
        + ", configProvider="
        + configProvider.unobfuscate()
        + ", resourceProvider="
        + resourceProvider
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
