/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.traces;

import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import java.util.function.BiFunction;

/**
 * A service provider interface (SPI) for performing additional programmatic configuration of a
 * {@link SdkTracerProviderBuilder} during initialization. When using auto-configuration, you should
 * prefer to use system properties or environment variables for configuration, but this may be
 * useful to register components that are not part of the SDK such as custom exporters.
 *
 * @deprecated Use {@link AutoConfigurationCustomizer#addTracerProviderCustomizer(BiFunction)}.
 */
@Deprecated
public interface SdkTracerProviderConfigurer {
  /** Configures the {@link SdkTracerProviderBuilder}. */
  void configure(SdkTracerProviderBuilder tracerProviderBuilder, ConfigProperties config);
}
