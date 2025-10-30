/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.metrics;

import io.opentelemetry.exporter.internal.grpc.GrpcExporter;
import io.opentelemetry.exporter.internal.grpc.GrpcExporterBuilder;
import io.opentelemetry.exporter.internal.otlp.metrics.MetricReusableDataMarshaler;
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

/**
 * Exports metrics using OTLP via gRPC, using OpenTelemetry's protobuf model.
 *
 * @since 1.14.0
 */
@ThreadSafe
public final class OtlpGrpcMetricExporter implements MetricExporter {

  private final GrpcExporterBuilder builder;
  private final GrpcExporter delegate;
  // Visible for testing
  final AggregationTemporalitySelector aggregationTemporalitySelector;
  // Visible for testing
  final DefaultAggregationSelector defaultAggregationSelector;
  private final MetricReusableDataMarshaler marshaler;

  /**
   * Returns a new {@link OtlpGrpcMetricExporter} using the default values.
   *
   * <p>To load configuration values from environment variables and system properties, use <a
   * href="https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure">opentelemetry-sdk-extension-autoconfigure</a>.
   *
   * @return a new {@link OtlpGrpcMetricExporter} instance.
   */
  public static OtlpGrpcMetricExporter getDefault() {
    return builder().build();
  }

  /**
   * Returns a new builder instance for this exporter.
   *
   * @return a new builder instance for this exporter.
   */
  public static OtlpGrpcMetricExporterBuilder builder() {
    return new OtlpGrpcMetricExporterBuilder();
  }

  OtlpGrpcMetricExporter(
      GrpcExporterBuilder builder,
      GrpcExporter delegate,
      AggregationTemporalitySelector aggregationTemporalitySelector,
      DefaultAggregationSelector defaultAggregationSelector,
      MemoryMode memoryMode) {
    this.builder = builder;
    this.delegate = delegate;
    this.aggregationTemporalitySelector = aggregationTemporalitySelector;
    this.defaultAggregationSelector = defaultAggregationSelector;
    this.marshaler = new MetricReusableDataMarshaler(memoryMode, delegate::export);
  }

  /**
   * Returns a builder with configuration values equal to those for this exporter.
   *
   * <p>IMPORTANT: Be sure to {@link #shutdown()} this instance if it will no longer be used.
   *
   * @since 1.29.0
   */
  public OtlpGrpcMetricExporterBuilder toBuilder() {
    return new OtlpGrpcMetricExporterBuilder(
        builder.copy(),
        aggregationTemporalitySelector,
        defaultAggregationSelector,
        marshaler.getMemoryMode());
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
  public MemoryMode getMemoryMode() {
    return marshaler.getMemoryMode();
  }

  /**
   * Submits all the given metrics in a single batch to the OpenTelemetry collector.
   *
   * @param metrics the list of Metrics to be exported.
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode export(Collection<MetricData> metrics) {
    return marshaler.export(metrics);
  }

  /**
   * The OTLP exporter does not batch metrics, so this method will immediately return with success.
   *
   * @return always Success
   */
  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  /**
   * Initiates an orderly shutdown in which preexisting calls continue but new calls are immediately
   * cancelled. The channel is forcefully closed after a timeout.
   */
  @Override
  public CompletableResultCode shutdown() {
    return delegate.shutdown();
  }

  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(", ", "OtlpGrpcMetricExporter{", "}");
    joiner.add(builder.toString(false));
    joiner.add(
        "aggregationTemporalitySelector="
            + AggregationTemporalitySelector.asString(aggregationTemporalitySelector));
    joiner.add(
        "defaultAggregationSelector="
            + DefaultAggregationSelector.asString(defaultAggregationSelector));
    joiner.add("memoryMode=" + marshaler.getMemoryMode());
    return joiner.toString();
  }
}
