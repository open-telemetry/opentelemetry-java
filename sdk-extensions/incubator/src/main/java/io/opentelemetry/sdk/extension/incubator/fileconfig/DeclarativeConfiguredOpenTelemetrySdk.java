/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import javax.annotation.concurrent.Immutable;

/**
 * A declaratively configured OpenTelemetry SDK. As an alternative to programmatically configuring
 * the SDK using {@link OpenTelemetrySdk#builder()} and auto-configured SDK using {@link
 * AutoConfiguredOpenTelemetrySdk#builder()}. This class can be used to configure the SDK using the
 * configuration file in the OpenTelemetry specified format.
 */
@Immutable
@AutoValue
public abstract class DeclarativeConfiguredOpenTelemetrySdk {

  /**
   * Returns the {@link OpenTelemetrySdk} that was declaratively configured, or an effectively noop
   * instance if the SDK has been disabled.
   *
   * <p>The instance returned if the SDK is disabled is equivalent to {@code
   * OpenTelemetrySdk.builder().build()}, which is notably not the same as {@link
   * OpenTelemetry#noop()}.
   */
  public abstract OpenTelemetrySdk getOpenTelemetrySdk();

  /**
   * Returns a new {@link DeclarativeConfiguredOpenTelemetrySdkBuilder} which can be used to build
   * {@link OpenTelemetrySdk} based on file configuration with customizations made.
   */
  public static DeclarativeConfiguredOpenTelemetrySdkBuilder builder() {
    return new DeclarativeConfiguredOpenTelemetrySdkBuilder();
  }

  static DeclarativeConfiguredOpenTelemetrySdk create(OpenTelemetrySdk sdk) {
    return new AutoValue_DeclarativeConfiguredOpenTelemetrySdk(sdk);
  }
}
