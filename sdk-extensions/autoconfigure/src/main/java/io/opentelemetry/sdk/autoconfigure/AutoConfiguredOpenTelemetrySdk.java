/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * An auto-configured OpenTelemetry SDK. As an alternative to programmatically configuring the SDK
 * using {@link OpenTelemetrySdk#builder()}, this package can be used to automatically configure the
 * SDK using environment properties specified by OpenTelemetry.
 *
 * @since 1.28.0
 */
@Immutable
@AutoValue
public abstract class AutoConfiguredOpenTelemetrySdk {

  /**
   * Returns an {@link AutoConfiguredOpenTelemetrySdk} automatically initialized through recognized
   * system properties and environment variables.
   *
   * <p>This will automatically set the resulting SDK as the {@link GlobalOpenTelemetry} instance.
   */
  public static AutoConfiguredOpenTelemetrySdk initialize() {
    return builder().setResultAsGlobal().build();
  }

  /**
   * Returns a new {@link AutoConfiguredOpenTelemetrySdkBuilder} which can be used to customize
   * auto-configuration behavior.
   */
  public static AutoConfiguredOpenTelemetrySdkBuilder builder() {
    return new AutoConfiguredOpenTelemetrySdkBuilder();
  }

  static AutoConfiguredOpenTelemetrySdk create(
      OpenTelemetrySdk sdk,
      Resource resource,
      @Nullable ConfigProperties config,
      @Nullable ConfigProvider configProvider) {
    return new AutoValue_AutoConfiguredOpenTelemetrySdk(sdk, resource, config, configProvider);
  }

  /**
   * Returns the {@link OpenTelemetrySdk} that was auto-configured, or an effectively noop instance
   * if the SDK has been disabled.
   *
   * <p>The instance returned if the SDK is disabled is equivalent to {@code
   * OpenTelemetrySdk.builder().build()}, which is notably not the same as {@link
   * OpenTelemetry#noop()}.
   */
  public abstract OpenTelemetrySdk getOpenTelemetrySdk();

  /** Returns the {@link Resource} that was auto-configured. */
  abstract Resource getResource();

  /**
   * Returns the {@link ConfigProperties} used for auto-configuration, or {@code null} if
   * declarative configuration was used.
   *
   * @see #getConfigProvider()
   */
  @Nullable
  abstract ConfigProperties getConfig();

  /**
   * Returns the {@link ConfigProvider} representing the config used for auto-configuration, or
   * {@code null} if declarative configuration was not used.
   *
   * @see #getConfig()
   */
  @Nullable
  abstract ConfigProvider getConfigProvider();

  AutoConfiguredOpenTelemetrySdk() {}
}
