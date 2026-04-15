/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.metrics.MeterProvider;
import java.util.function.Supplier;

/**
 * Builder class for {@link SimpleSpanProcessor}.
 *
 * @since 1.34.0
 */
public final class SimpleSpanProcessorBuilder {
  private final SpanExporter spanExporter;
  private Supplier<MeterProvider> meterProvider = MeterProvider::noop;
  private boolean exportUnsampledSpans = false;

  SimpleSpanProcessorBuilder(SpanExporter spanExporter) {
    this.spanExporter = requireNonNull(spanExporter, "spanExporter");
  }

  /**
   * Sets whether unsampled spans should be exported. If unset, defaults to exporting only sampled
   * spans.
   */
  public SimpleSpanProcessorBuilder setExportUnsampledSpans(boolean exportUnsampledSpans) {
    this.exportUnsampledSpans = exportUnsampledSpans;
    return this;
  }

  /**
   * Sets the {@link MeterProvider} to use to generate <a
   * href="https://opentelemetry.io/docs/specs/semconv/otel/sdk-metrics/#span-metrics">SDK Span
   * Metrics</a>.
   *
   * @since 1.58.0
   */
  public SimpleSpanProcessorBuilder setMeterProvider(Supplier<MeterProvider> meterProvider) {
    requireNonNull(meterProvider, "meterProvider");
    this.meterProvider = meterProvider;
    return this;
  }

  // TODO: add `setInternalTelemetryVersion when we support more than one version

  /**
   * Returns a new {@link SimpleSpanProcessor} with the configuration of this builder.
   *
   * @return a new {@link SimpleSpanProcessor}.
   */
  public SimpleSpanProcessor build() {
    return new SimpleSpanProcessor(spanExporter, exportUnsampledSpans, meterProvider);
  }
}
