/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.logs;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.logs.samplers.LogSampler;

public interface ConfigurableLogSamplerProvider {

  /**
   * Returns a {@link LogSampler} that can be registered to OpenTelemetry by providing the property
   * value specified by {@link #getName()}.
   */
  LogSampler createSampler(ConfigProperties config);

  /**
   * Returns the name of this sampler, which can be specified with the {@code otel.logs.sampler}
   * property to enable it. The name returned should NOT be the same as any other exporter name. If
   * the name does conflict with another exporter name, the resulting behavior is undefined and it
   * is explicitly unspecified which exporter will actually be used.
   */
  String getName();
}
