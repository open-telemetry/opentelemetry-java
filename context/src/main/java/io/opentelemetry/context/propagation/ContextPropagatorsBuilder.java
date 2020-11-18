/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context.propagation;

/**
 * A builder of a default {@link ContextPropagators} which can have multiple {@link
 * TextMapPropagator} added, and extraction and injection will execute every {@link
 * TextMapPropagator} in order.
 */
public interface ContextPropagatorsBuilder {
  /**
   * Adds a {@link TextMapPropagator} propagator.
   *
   * <p>One propagator per concern (traces, correlations, etc) should be added if this format is
   * supported.
   *
   * @param textMapPropagator the propagator to be added.
   * @return this.
   * @throws NullPointerException if {@code textMapPropagator} is {@code null}.
   */
  ContextPropagatorsBuilder addTextMapPropagator(TextMapPropagator textMapPropagator);

  /**
   * Builds a new {@code ContextPropagators} with the specified propagators.
   *
   * @return the newly created {@code ContextPropagators} instance.
   */
  ContextPropagators build();
}
