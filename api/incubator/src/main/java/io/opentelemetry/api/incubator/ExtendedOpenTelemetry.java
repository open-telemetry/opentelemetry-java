/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.incubator.config.ConfigProvider;

/** Extension to {@link OpenTelemetry} with experimental APIs. */
public interface ExtendedOpenTelemetry extends OpenTelemetry {
  /** Returns the {@link ConfigProvider} for this {@link OpenTelemetry}. */
  default ConfigProvider getConfigProvider() {
    return ConfigProvider.noop();
  }
}
