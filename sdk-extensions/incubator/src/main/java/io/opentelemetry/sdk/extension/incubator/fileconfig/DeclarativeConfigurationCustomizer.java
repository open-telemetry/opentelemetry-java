/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import java.util.function.Function;

/** A service provider interface (SPI) for customizing declarative configuration. */
public interface DeclarativeConfigurationCustomizer {
  /**
   * Method invoked when configuring the SDK to allow further customization of the declarative
   * configuration.
   *
   * @param customizer the customizer to add
   */
  void addModelCustomizer(
      Function<OpenTelemetryConfigurationModel, OpenTelemetryConfigurationModel> customizer);
}
