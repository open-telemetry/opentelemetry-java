/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.autoconfigure.spi.Ordered;

/** A service provider interface (SPI) for customizing declarative configuration. */
public interface DeclarativeConfigurationCustomizerProvider extends Ordered {
  /**
   * Method invoked when configuring the SDK to allow further customization of the declarative
   * configuration.
   *
   * @param customizer the customizer to add
   */
  void customize(DeclarativeConfigurationCustomizer customizer);
}
