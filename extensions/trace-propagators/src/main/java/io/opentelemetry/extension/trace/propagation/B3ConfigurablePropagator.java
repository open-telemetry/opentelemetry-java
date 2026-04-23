/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurablePropagatorProvider;
import java.util.Objects;

/**
 * A {@link ConfigurablePropagatorProvider} which allows enabling the {@linkplain
 * B3Propagator#injectingSingleHeader()} B3-single propagator} with the propagator name {@code b3}.
 */
public final class B3ConfigurablePropagator implements ConfigurablePropagatorProvider {
  @Override
  public TextMapPropagator getPropagator(ConfigProperties config) {
    Objects.requireNonNull(config, "config");
    return B3Propagator.injectingSingleHeader();
  }

  @Override
  public String getName() {
    return "b3";
  }
}
