/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSamplerProvider;
import io.opentelemetry.sdk.trace.samplers.Sampler;

public class ThrowingConfigurableSamplerProvider implements ConfigurableSamplerProvider {
  @Override
  public Sampler createSampler(ConfigProperties config) {
    throw new IllegalStateException("always throws");
  }

  @Override
  public String getName() {
    return "throwing";
  }
}
