/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.concurrent.Immutable;

/**
 * An auto-configured OpenTelemetry SDK. As an alternative to programmatically configuring the SDK
 * using {@link OpenTelemetrySdk#builder()}, this package can be used to automatically configure the
 * SDK using environment properties specified by OpenTelemetry.
 */
@Immutable
@AutoValue
public abstract class AutoConfiguredOpenTelemetrySdk {

  /**
   * Returns an {@link AutoConfiguredOpenTelemetrySdk} automatically initialized through recognized
   * system properties and environment variables.
   *
   * <p>This will automatically set the resulting SDK as the {@link
   * io.opentelemetry.api.GlobalOpenTelemetry} instance.
   */
  public static AutoConfiguredOpenTelemetrySdk initialize() {
    return builder().build();
  }

  /**
   * Returns a new {@link AutoConfiguredOpenTelemetrySdkBuilder} which can be used to customize
   * auto-configuration behavior.
   */
  public static AutoConfiguredOpenTelemetrySdkBuilder builder() {
    return new AutoConfiguredOpenTelemetrySdkBuilder();
  }

  static AutoConfiguredOpenTelemetrySdk create(
      OpenTelemetrySdk sdk, Resource resource, ConfigProperties config) {
    return new AutoValue_AutoConfiguredOpenTelemetrySdk(sdk, resource, config);
  }

  /** Returns the {@link OpenTelemetrySdk} that was auto-configured. */
  public abstract OpenTelemetrySdk getOpenTelemetrySdk();

  /** Returns the {@link Resource} that was auto-configured. */
  public abstract Resource getResource();

  /** Returns the {@link ConfigProperties} used for auto-configuration. */
  public abstract ConfigProperties getConfig();

  AutoConfiguredOpenTelemetrySdk() {}
}
