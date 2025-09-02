/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import javax.annotation.Nullable;

/** A service provider interface (SPI) for providing a declarative configuration model. */
public interface DeclarativeConfigurationProvider {
  /**
   * Returns an OpenTelemetry configuration model to be used when configuring the SDK, or {@code
   * null} if no configuration is provided by this provider.
   */
  @Nullable
  OpenTelemetryConfigurationModel getConfigurationModel();
}
