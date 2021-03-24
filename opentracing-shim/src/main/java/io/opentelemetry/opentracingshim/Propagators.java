/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.propagation.TextMapPropagator;

/**
 * Container for {@link io.opentracing.propagation.Format.Builtin#TEXT_MAP} and {@link
 * io.opentracing.propagation.Format.Builtin#HTTP_HEADERS} format propagators.
 */
public class Propagators {
  private final TextMapPropagator textMapPropagator;
  private final TextMapPropagator httpHeadersPropagator;

  public TextMapPropagator textMapPropagator() {
    return textMapPropagator;
  }

  public TextMapPropagator httpHeadersPropagator() {
    return httpHeadersPropagator;
  }

  public Propagators(TextMapPropagator textMapPropagator, TextMapPropagator httpHeadersPropagator) {
    this.textMapPropagator = textMapPropagator;
    this.httpHeadersPropagator = httpHeadersPropagator;
  }

  public static PropagatorsBuilder builder() {
    return new PropagatorsBuilder();
  }

  public static class PropagatorsBuilder {

    private TextMapPropagator textMapPropagator;
    private TextMapPropagator httpHeadersPropagator;

    /** Set propagator for {@link io.opentracing.propagation.Format.Builtin#TEXT_MAP} format. */
    public PropagatorsBuilder setTextMap(TextMapPropagator textMapPropagator) {
      this.textMapPropagator = textMapPropagator;
      return this;
    }

    /** Set propagator for {@link io.opentracing.propagation.Format.Builtin#HTTP_HEADERS} format. */
    public PropagatorsBuilder setHttpHeaders(TextMapPropagator httpHeadersPropagator) {
      this.httpHeadersPropagator = httpHeadersPropagator;
      return this;
    }

    /**
     * Constructs a new instance of the Propagators based on the builder's values.
     *
     * @return a new Propagators instance.
     */
    public Propagators build() {
      if (textMapPropagator == null) {
        textMapPropagator = GlobalOpenTelemetry.getPropagators().getTextMapPropagator();
      }
      if (httpHeadersPropagator == null) {
        httpHeadersPropagator = GlobalOpenTelemetry.getPropagators().getTextMapPropagator();
      }

      return new Propagators(textMapPropagator, httpHeadersPropagator);
    }
  }
}
