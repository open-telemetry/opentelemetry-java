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
public class OTPropagators {
  private final TextMapPropagator textMapPropagator;
  private final TextMapPropagator httpHeadersPropagator;

  public TextMapPropagator textMapPropagator() {
    return textMapPropagator;
  }

  public TextMapPropagator httpHeadersPropagator() {
    return httpHeadersPropagator;
  }

  public OTPropagators(
      TextMapPropagator textMapPropagator, TextMapPropagator httpHeadersPropagator) {
    this.textMapPropagator = textMapPropagator;
    this.httpHeadersPropagator = httpHeadersPropagator;
  }

  public static OTPropagatorsBuilder builder() {
    return new OTPropagatorsBuilder();
  }

  public static class OTPropagatorsBuilder {

    private TextMapPropagator textMapPropagator;
    private TextMapPropagator httpHeadersPropagator;

    /** Set propagator for {@link io.opentracing.propagation.Format.Builtin#TEXT_MAP} format. */
    public OTPropagatorsBuilder setTextMap(TextMapPropagator textMapPropagator) {
      this.textMapPropagator = textMapPropagator;
      return this;
    }

    /** Set propagator for {@link io.opentracing.propagation.Format.Builtin#HTTP_HEADERS} format. */
    public OTPropagatorsBuilder setHttpHeaders(TextMapPropagator httpHeadersPropagator) {
      this.httpHeadersPropagator = httpHeadersPropagator;
      return this;
    }

    public OTPropagators build() {
      if (textMapPropagator == null) {
        textMapPropagator = GlobalOpenTelemetry.getPropagators().getTextMapPropagator();
      }
      if (httpHeadersPropagator == null) {
        httpHeadersPropagator = GlobalOpenTelemetry.getPropagators().getTextMapPropagator();
      }

      return new OTPropagators(textMapPropagator, httpHeadersPropagator);
    }
  }
}
