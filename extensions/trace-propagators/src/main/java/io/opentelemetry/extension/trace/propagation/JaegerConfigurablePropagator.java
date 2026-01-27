/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurablePropagatorProvider;

/**
 * A {@link ConfigurablePropagatorProvider} which allows enabling the {@link
 * io.opentelemetry.extension.trace.propagation.JaegerPropagator} with the propagator name {@code
 * jaeger}.
 */
@SuppressWarnings("deprecation")
public final class JaegerConfigurablePropagator implements ConfigurablePropagatorProvider {
  @Override
  public TextMapPropagator getPropagator(ConfigProperties config) {
    return io.opentelemetry.extension.trace.propagation.JaegerPropagator.getInstance();
  }

  @Override
  public String getName() {
    return "jaeger";
  }
}
