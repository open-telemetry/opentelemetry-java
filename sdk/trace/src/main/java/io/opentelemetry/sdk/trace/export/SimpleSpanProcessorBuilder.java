/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.trace.export.metrics.SpanProcessorMetrics;
import java.util.function.Supplier;

/**
 * Builder class for {@link SimpleSpanProcessor}.
 *
 * @since 1.34.0
 */
public final class SimpleSpanProcessorBuilder {
  private final SpanExporter spanExporter;
  private boolean exportUnsampledSpans = false;
  private Supplier<MeterProvider> meterProviderSupplier = GlobalOpenTelemetry::getMeterProvider;
  private InternalTelemetryVersion internalTelemetryVersion = InternalTelemetryVersion.LEGACY;

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
   * Sets the {@link InternalTelemetryVersion} defining which self-monitoring metrics this processor
   * collects.
   */
  public SimpleSpanProcessorBuilder setInternalTelemetryVersion(
      InternalTelemetryVersion internalTelemetryVersion) {
    requireNonNull(internalTelemetryVersion, "internalTelemetryVersion");
    this.internalTelemetryVersion = internalTelemetryVersion;
    return this;
  }

  /**
   * Sets the {@link MeterProvider} to use to collect metrics related to batch export. If not set,
   * metrics will not be collected.
   */
  public SimpleSpanProcessorBuilder setMeterProvider(
      Supplier<MeterProvider> meterProviderSupplier) {
    requireNonNull(meterProviderSupplier, "meterProviderSupplier");
    this.meterProviderSupplier = meterProviderSupplier;
    return this;
  }

  /**
   * Returns a new {@link SimpleSpanProcessor} with the configuration of this builder.
   *
   * @return a new {@link SimpleSpanProcessor}.
   */
  public SimpleSpanProcessor build() {
    return new SimpleSpanProcessor(
        spanExporter,
        SpanProcessorMetrics.createForSimpleProcessor(
            internalTelemetryVersion, meterProviderSupplier),
        exportUnsampledSpans);
  }
}
