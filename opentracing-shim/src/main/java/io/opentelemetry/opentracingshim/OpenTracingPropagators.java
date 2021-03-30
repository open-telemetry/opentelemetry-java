/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentelemetry.context.propagation.TextMapPropagator;

/**
 * Container for {@link io.opentracing.propagation.Format.Builtin#TEXT_MAP} and {@link
 * io.opentracing.propagation.Format.Builtin#HTTP_HEADERS} format propagators.
 */
public class OpenTracingPropagators {
  private final TextMapPropagator textMapPropagator;
  private final TextMapPropagator httpHeadersPropagator;

  OpenTracingPropagators(
      TextMapPropagator textMapPropagator, TextMapPropagator httpHeadersPropagator) {
    this.textMapPropagator = textMapPropagator;
    this.httpHeadersPropagator = httpHeadersPropagator;
  }

  /**
   * Returns a new builder instance for {@link OpenTracingPropagators}.
   *
   * @return a new builder instance for {@link OpenTracingPropagators}.
   */
  public static OpenTracingPropagatorsBuilder builder() {
    return new OpenTracingPropagatorsBuilder();
  }

  /**
   * Returns the propagator for {@link io.opentracing.propagation.Format.Builtin#TEXT_MAP} format.
   *
   * @return the propagator for {@link io.opentracing.propagation.Format.Builtin#TEXT_MAP} format.
   */
  public TextMapPropagator textMapPropagator() {
    return textMapPropagator;
  }

  /**
   * Returns the propagator for {@link io.opentracing.propagation.Format.Builtin#HTTP_HEADERS}
   * format.
   *
   * @return the propagator for {@link io.opentracing.propagation.Format.Builtin#HTTP_HEADERS}
   *     format.
   */
  public TextMapPropagator httpHeadersPropagator() {
    return httpHeadersPropagator;
  }
}
