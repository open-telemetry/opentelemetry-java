/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.metrics;

import io.opentelemetry.exporter.internal.http.HttpExporter;
import io.opentelemetry.exporter.internal.http.HttpExporterBuilder;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
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
 * Exports metrics using OTLP via HTTP, using OpenTelemetry's protobuf model.
 *
 * @since 1.14.0
 */
@ThreadSafe
public final class OtlpHttpMetricExporter implements MetricExporter {

  private final HttpExporterBuilder<Marshaler> builder;
  private final HttpExporter<Marshaler> delegate;
  private final AggregationTemporalitySelector aggregationTemporalitySelector;
  private final DefaultAggregationSelector defaultAggregationSelector;
  private final MetricReusableDataMarshaler marshaler;

  OtlpHttpMetricExporter(
      HttpExporterBuilder<Marshaler> builder,
      HttpExporter<Marshaler> delegate,
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
   * Returns a new {@link OtlpHttpMetricExporter} using the default values.
   *
   * <p>To load configuration values from environment variables and system properties, use <a
   * href="https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure">opentelemetry-sdk-extension-autoconfigure</a>.
   *
   * @return a new {@link OtlpHttpMetricExporter} instance.
   */
  public static OtlpHttpMetricExporter getDefault() {
    return builder().build();
  }

  /**
   * Returns a new builder instance for this exporter.
   *
   * @return a new builder instance for this exporter.
   */
  public static OtlpHttpMetricExporterBuilder builder() {
    return new OtlpHttpMetricExporterBuilder();
  }

  /**
   * Returns a builder with configuration values equal to those for this exporter.
   *
   * <p>IMPORTANT: Be sure to {@link #shutdown()} this instance if it will no longer be used.
   *
   * @since 1.29.0
   */
  public OtlpHttpMetricExporterBuilder toBuilder() {
    return new OtlpHttpMetricExporterBuilder(builder.copy(), marshaler.getMemoryMode());
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
   * @param metrics the list of sampled Metrics to be exported.
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

  /** Shutdown the exporter. */
  @Override
  public CompletableResultCode shutdown() {
    return delegate.shutdown();
  }

  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(", ", "OtlpHttpMetricExporter{", "}");
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
