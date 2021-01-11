/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.propagation.TextMapPropagator;

/**
 * A service provider interface (SPI) for providing additional propagators to the autoconfigured
 * SDK. The {@link TextMapPropagator} returned from any found {@link PropagatorProvider} will be
 * available as part of {@link OpenTelemetry#getPropagators()}. This should generally only be used
 * for custom propagators - for any propagator supported by OpenTelemetry, it is recommended to use
 * the {@code otel.propagators} property instead.
 */
public interface PropagatorProvider {
  /** Returns a {@link TextMapPropagator} to register to OpenTelemetry. */
  TextMapPropagator get();
}
