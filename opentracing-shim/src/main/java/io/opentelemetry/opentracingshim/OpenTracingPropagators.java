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

  public TextMapPropagator textMapPropagator() {
    return textMapPropagator;
  }

  public TextMapPropagator httpHeadersPropagator() {
    return httpHeadersPropagator;
  }

  public OpenTracingPropagators(
      TextMapPropagator textMapPropagator, TextMapPropagator httpHeadersPropagator) {
    this.textMapPropagator = textMapPropagator;
    this.httpHeadersPropagator = httpHeadersPropagator;
  }

  public static OpenTracingPropagatorsBuilder builder() {
    return new OpenTracingPropagatorsBuilder();
  }
}
