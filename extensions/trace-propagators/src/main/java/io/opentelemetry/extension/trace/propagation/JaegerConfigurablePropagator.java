/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurablePropagatorProvider;

/**
 * A {@link ConfigurablePropagatorProvider} which allows enabling the {@link JaegerPropagator} with
 * the propagator name {@code jaeger}.
 */
public class JaegerConfigurablePropagator implements ConfigurablePropagatorProvider {
  @Override
  public TextMapPropagator getPropagator() {
    return JaegerPropagator.getInstance();
  }

  @Override
  public String getName() {
    return "jaeger";
  }
}
