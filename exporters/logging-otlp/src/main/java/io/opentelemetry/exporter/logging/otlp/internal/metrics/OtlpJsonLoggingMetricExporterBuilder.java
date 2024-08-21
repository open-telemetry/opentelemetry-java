/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.metrics;

import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingMetricExporter;
import io.opentelemetry.exporter.logging.otlp.internal.InternalBuilder;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.io.OutputStream;

/**
 * Builder for {@link OtlpJsonLoggingMetricExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class OtlpJsonLoggingMetricExporterBuilder {

  private final InternalMetricBuilder builder;

  OtlpJsonLoggingMetricExporterBuilder(InternalMetricBuilder builder) {
    this.builder = builder;
  }

  /**
   * Creates a new {@link OtlpJsonLoggingMetricExporterBuilder} with default settings.
   *
   * @return a new {@link OtlpJsonLoggingMetricExporterBuilder}.
   */
  public static OtlpJsonLoggingMetricExporterBuilder create() {
    return new OtlpJsonLoggingMetricExporterBuilder(InternalBuilder.forMetrics());
  }

  /**
   * Creates a new {@link OtlpJsonLoggingMetricExporterBuilder} from an existing exporter.
   *
   * @param exporter the existing exporter.
   * @return a new {@link OtlpJsonLoggingMetricExporterBuilder}.
   */
  public static OtlpJsonLoggingMetricExporterBuilder createFromExporter(
      OtlpJsonLoggingMetricExporter exporter) {
    return new OtlpJsonLoggingMetricExporterBuilder(
        MetricBuilderAccessUtil.getToBuilder().apply(exporter));
  }

  /**
   * Sets the exporter to use the specified JSON object wrapper.
   *
   * @param wrapperJsonObject whether to wrap the JSON object in an outer JSON "resourceMetrics"
   *     object.
   */
  public OtlpJsonLoggingMetricExporterBuilder setWrapperJsonObject(boolean wrapperJsonObject) {
    builder.setWrapperJsonObject(wrapperJsonObject);
    return this;
  }

  /**
   * Set the {@link MemoryMode}. If unset, defaults to {@link MemoryMode#IMMUTABLE_DATA}.
   *
   * <p>When memory mode is {@link MemoryMode#REUSABLE_DATA}, serialization is optimized to reduce
   * memory allocation.
   */
  public OtlpJsonLoggingMetricExporterBuilder setMemoryMode(MemoryMode memoryMode) {
    builder.setMemoryMode(memoryMode);
    return this;
  }

  /** Sets the exporter to use the logger for output. */
  public OtlpJsonLoggingMetricExporterBuilder setUseLogger() {
    builder.setUseLogger();
    return this;
  }

  /**
   * Sets the exporter to use the specified output stream.
   *
   * @param outputStream the output stream to use.
   */
  public OtlpJsonLoggingMetricExporterBuilder setOutputStream(OutputStream outputStream) {
    builder.setOutputStream(outputStream);
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
  public OtlpJsonLoggingMetricExporterBuilder setAggregationTemporalitySelector(
      AggregationTemporalitySelector aggregationTemporalitySelector) {
    builder.setAggregationTemporalitySelector(aggregationTemporalitySelector);
    return this;
  }

  /**
   * Set the {@link DefaultAggregationSelector} used for {@link
   * MetricExporter#getDefaultAggregation(InstrumentType)}.
   *
   * <p>If unset, defaults to {@link DefaultAggregationSelector#getDefault()}.
   */
  public OtlpJsonLoggingMetricExporterBuilder setDefaultAggregationSelector(
      DefaultAggregationSelector defaultAggregationSelector) {
    builder.setDefaultAggregationSelector(defaultAggregationSelector);
    return this;
  }

  /**
   * Constructs a new instance of the exporter based on the builder's values.
   *
   * @return a new exporter's instance
   */
  @SuppressWarnings("ReturnValueIgnored")
  public OtlpJsonLoggingMetricExporter build() {
    return MetricBuilderAccessUtil.getToExporter().apply(builder);
  }
}
