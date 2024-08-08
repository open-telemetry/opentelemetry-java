/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.stream.metrics;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.otlp.stream.StreamExporterBuilder;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.io.OutputStream;

/** Builder for {@link OtlpStdoutMetricExporter}. */
public final class OtlpStdoutMetricExporterBuilder {

  private static final MemoryMode DEFAULT_MEMORY_MODE = MemoryMode.IMMUTABLE_DATA;
  private static final AggregationTemporalitySelector DEFAULT_AGGREGATION_TEMPORALITY_SELECTOR =
      AggregationTemporalitySelector.alwaysCumulative();

  // Visible for testing
  final StreamExporterBuilder<Marshaler> delegate;
  private MemoryMode memoryMode;
  private AggregationTemporalitySelector aggregationTemporalitySelector =
      DEFAULT_AGGREGATION_TEMPORALITY_SELECTOR;

  private DefaultAggregationSelector defaultAggregationSelector =
      DefaultAggregationSelector.getDefault();

  OtlpStdoutMetricExporterBuilder(
      StreamExporterBuilder<Marshaler> delegate, MemoryMode memoryMode) {
    this.delegate = delegate;
    this.memoryMode = memoryMode;
  }

  OtlpStdoutMetricExporterBuilder() {
    this(new StreamExporterBuilder<>(), DEFAULT_MEMORY_MODE);
  }

  /**
   * Set the {@link MemoryMode}. If unset, defaults to {@link #DEFAULT_MEMORY_MODE}.
   *
   * <p>When memory mode is {@link MemoryMode#REUSABLE_DATA}, serialization is optimized to reduce
   * memory allocation.
   */
  public OtlpStdoutMetricExporterBuilder setMemoryMode(MemoryMode memoryMode) {
    requireNonNull(memoryMode, "memoryMode");
    this.memoryMode = memoryMode;
    return this;
  }

  public OtlpStdoutMetricExporterBuilder setOutputStream(OutputStream outputStream) {
    requireNonNull(outputStream, "outputStream");
    this.delegate.setOutputStream(outputStream);
    return this;
  }

  /**
   * Set the {@link AggregationTemporalitySelector} used for {@link
   * MetricExporter#getAggregationTemporality(InstrumentType)}.
   *
   * <p>If unset, defaults to {@link AggregationTemporalitySelector#alwaysCumulative()}.
   *
   * <p>{@link AggregationTemporalitySelector#deltaPreferred()} is a common configuration for delta
   * backends.
   */
  public OtlpStdoutMetricExporterBuilder setAggregationTemporalitySelector(
      AggregationTemporalitySelector aggregationTemporalitySelector) {
    requireNonNull(aggregationTemporalitySelector, "aggregationTemporalitySelector");
    this.aggregationTemporalitySelector = aggregationTemporalitySelector;
    return this;
  }

  /**
   * Set the {@link DefaultAggregationSelector} used for {@link
   * MetricExporter#getDefaultAggregation(InstrumentType)}.
   *
   * <p>If unset, defaults to {@link DefaultAggregationSelector#getDefault()}.
   */
  public OtlpStdoutMetricExporterBuilder setDefaultAggregationSelector(
      DefaultAggregationSelector defaultAggregationSelector) {
    requireNonNull(defaultAggregationSelector, "defaultAggregationSelector");
    this.defaultAggregationSelector = defaultAggregationSelector;
    return this;
  }

  /**
   * Constructs a new instance of the exporter based on the builder's values.
   *
   * @return a new exporter's instance
   */
  public OtlpStdoutMetricExporter build() {
    return new OtlpStdoutMetricExporter(
        delegate,
        delegate.build(),
        aggregationTemporalitySelector,
        defaultAggregationSelector,
        memoryMode);
  }
}
