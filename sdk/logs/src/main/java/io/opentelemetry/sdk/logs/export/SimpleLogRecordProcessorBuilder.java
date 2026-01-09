/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.export;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.metrics.MeterProvider;
import java.util.function.Supplier;

/**
 * Builder for {@link SimpleLogRecordProcessor}.
 *
 * @since 1.58.0
 */
public final class SimpleLogRecordProcessorBuilder {
  private final LogRecordExporter exporter;
  private Supplier<MeterProvider> meterProvider = MeterProvider::noop;

  SimpleLogRecordProcessorBuilder(LogRecordExporter exporter) {
    this.exporter = requireNonNull(exporter, "exporter");
  }

  /**
   * Sets the {@link MeterProvider} to use to generate <a
   * href="https://opentelemetry.io/docs/specs/semconv/otel/sdk-metrics/#log-metrics">SDK Log
   * Metrics</a>.
   */
  public SimpleLogRecordProcessorBuilder setMeterProvider(Supplier<MeterProvider> meterProvider) {
    requireNonNull(meterProvider, "meterProvider");
    this.meterProvider = meterProvider;
    return this;
  }

  // TODO: add `setInternalTelemetryVersion when we support more than one version

  /**
   * Returns a new {@link SimpleLogRecordProcessor} with the configuration of this builder.
   *
   * @return a new {@link SimpleLogRecordProcessor}.
   */
  public SimpleLogRecordProcessor build() {
    return new SimpleLogRecordProcessor(exporter, meterProvider);
  }
}
