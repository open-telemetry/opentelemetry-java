/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.propagation.TextMapPropagator;
import java.util.Objects;

public class OpenTracingPropagatorsBuilder {

  private TextMapPropagator textMapPropagator;
  private TextMapPropagator httpHeadersPropagator;

  /** Set propagator for {@link io.opentracing.propagation.Format.Builtin#TEXT_MAP} format. */
  public OpenTracingPropagatorsBuilder setTextMap(TextMapPropagator textMapPropagator) {
    Objects.requireNonNull(textMapPropagator, "textMapPropagator");
    this.textMapPropagator = textMapPropagator;
    return this;
  }

  /** Set propagator for {@link io.opentracing.propagation.Format.Builtin#HTTP_HEADERS} format. */
  public OpenTracingPropagatorsBuilder setHttpHeaders(TextMapPropagator httpHeadersPropagator) {
    Objects.requireNonNull(httpHeadersPropagator, "httpHeadersPropagator");
    this.httpHeadersPropagator = httpHeadersPropagator;
    return this;
  }

  /**
   * Constructs a new instance of the {@link OpenTracingPropagators} based on the builder's values.
   * If propagators are not set then global are used.
   *
   * @return a new Propagators instance.
   */
  public OpenTracingPropagators build() {
    if (textMapPropagator == null) {
      textMapPropagator = GlobalOpenTelemetry.getPropagators().getTextMapPropagator();
    }
    if (httpHeadersPropagator == null) {
      httpHeadersPropagator = GlobalOpenTelemetry.getPropagators().getTextMapPropagator();
    }

    return new OpenTracingPropagators(textMapPropagator, httpHeadersPropagator);
  }
}
