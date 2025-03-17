/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.autoconfigure.spi.Ordered;

public interface DeclarativeConfigurationCustomizerProvider extends Ordered {
  void customize(DeclarativeConfigurationCustomizer customizer);
}
