/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

/** A service provider interface (SPI) for customizing auto-configuration. */
public interface OpenTelemetrySdkAutoConfigurationCustomizer {

  /**
   * Method invoked when auto-configuring the SDK to allow further customization of
   * auto-configuration.
   */
  void customize(OpenTelemetrySdkAutoConfigurationBuilder autoConfiguration);
}
