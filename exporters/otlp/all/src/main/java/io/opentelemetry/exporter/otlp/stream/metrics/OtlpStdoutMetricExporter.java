/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.stream.metrics;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.OtlpMetricExporter;
import io.opentelemetry.exporter.otlp.stream.StreamExporter;
import io.opentelemetry.exporter.otlp.stream.StreamExporterBuilder;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.Collection;
import java.util.StringJoiner;
import javax.annotation.concurrent.ThreadSafe;

/** Exports logs using OTLP via gRPC, using OpenTelemetry's protobuf model. */
@ThreadSafe
public final class OtlpStdoutMetricExporter implements MetricExporter {

  private final StreamExporterBuilder<Marshaler> builder;

  private final OtlpMetricExporter otlpExporter;
  private final StreamExporter<Marshaler> streamExporter;
  private final AggregationTemporalitySelector aggregationTemporalitySelector;
  private final DefaultAggregationSelector defaultAggregationSelector;

  /**
   * Returns a new {@link OtlpStdoutMetricExporter} using the default values.
   *
   * <p>To load configuration values from environment variables and system properties, use <a
   * href="https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure">opentelemetry-sdk-extension-autoconfigure</a>.
   *
   * @return a new {@link OtlpStdoutMetricExporter} instance.
   */
  public static OtlpStdoutMetricExporter getDefault() {
    return builder().build();
  }

  /**
   * Returns a new builder instance for this exporter.
   *
   * @return a new builder instance for this exporter.
   */
  public static OtlpStdoutMetricExporterBuilder builder() {
    return new OtlpStdoutMetricExporterBuilder();
  }

  OtlpStdoutMetricExporter(
      StreamExporterBuilder<Marshaler> builder,
      StreamExporter<Marshaler> streamExporter,
      AggregationTemporalitySelector aggregationTemporalitySelector,
      DefaultAggregationSelector defaultAggregationSelector,
      MemoryMode memoryMode) {
    this.streamExporter = streamExporter;
    this.aggregationTemporalitySelector = aggregationTemporalitySelector;
    this.defaultAggregationSelector = defaultAggregationSelector;
    this.otlpExporter = new OtlpMetricExporter(streamExporter, memoryMode);
    this.builder = builder;
  }

  /**
   * Returns a builder with configuration values equal to those for this exporter.
   *
   * <p>IMPORTANT: Be sure to {@link #shutdown()} this instance if it will no longer be used.
   */
  public OtlpStdoutMetricExporterBuilder toBuilder() {
    return new OtlpStdoutMetricExporterBuilder(builder.copy(), otlpExporter.getMemoryMode());
  }

  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(", ", "OtlpGrpcMetricExporter{", "}");
    joiner.add(builder.toString(false));
    joiner.add("memoryMode=" + otlpExporter.getMemoryMode());
    return joiner.toString();
  }

  @Override
  public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
    return aggregationTemporalitySelector.getAggregationTemporality(instrumentType);
  }

  @Override
  public Aggregation getDefaultAggregation(InstrumentType instrumentType) {
    return defaultAggregationSelector.getDefaultAggregation(instrumentType);
  }

  @Override
  public CompletableResultCode export(Collection<MetricData> metrics) {
    return otlpExporter.export(metrics);
  }

  @Override
  public CompletableResultCode flush() {
    return streamExporter.flush();
  }

  @Override
  public CompletableResultCode shutdown() {
    return streamExporter.shutdown();
  }
}
