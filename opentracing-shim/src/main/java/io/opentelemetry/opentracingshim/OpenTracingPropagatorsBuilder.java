/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.propagation.TextMapPropagator;
import java.util.Objects;

/** Builder for {@link OpenTracingPropagators}. */
public class OpenTracingPropagatorsBuilder {

  private TextMapPropagator textMapPropagator =
      GlobalOpenTelemetry.getPropagators().getTextMapPropagator();
  private TextMapPropagator httpHeadersPropagator =
      GlobalOpenTelemetry.getPropagators().getTextMapPropagator();

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
   * If propagators are not set then {@code GlobalOpenTelemetry.getPropagators()} is used.
   *
   * @return a new Propagators instance.
   */
  public OpenTracingPropagators build() {
    return new OpenTracingPropagators(textMapPropagator, httpHeadersPropagator);
  }
}
