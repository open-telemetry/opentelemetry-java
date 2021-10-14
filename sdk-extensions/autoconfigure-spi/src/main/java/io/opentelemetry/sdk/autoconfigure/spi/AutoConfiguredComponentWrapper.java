/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi;

import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;

/**
 * A service provider interface (SPI) for wrapping autoconfigured components. For any component
 * automatically created by autoconfiguration, for example a span exporter, implementations of this
 * interface will be invoked to replace it, resulting in the final used component.
 */
public interface AutoConfiguredComponentWrapper {

  /** Wraps a {@link Resource}. */
  default Resource wrap(Resource resource, ConfigProperties config) {
    return resource;
  }

  /** Wraps a {@link Sampler}. */
  default Sampler wrap(Sampler sampler, ConfigProperties config) {
    return sampler;
  }

  /**
   * Wraps a {@link SpanExporter}. It is common to use in conjunction with {@link
   * io.opentelemetry.sdk.trace.data.DelegatingSpanData} to adjust the exported data.
   */
  default SpanExporter wrap(SpanExporter exporter, ConfigProperties config) {
    return exporter;
  }

  /** Wraps a {@link TextMapPropagator}. */
  default TextMapPropagator wrap(TextMapPropagator propagator, ConfigProperties config) {
    return propagator;
  }
}
