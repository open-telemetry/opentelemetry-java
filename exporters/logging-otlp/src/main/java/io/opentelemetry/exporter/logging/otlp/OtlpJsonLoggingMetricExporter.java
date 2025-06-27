/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import io.opentelemetry.exporter.logging.otlp.internal.metrics.OtlpStdoutMetricExporter;
import io.opentelemetry.exporter.logging.otlp.internal.metrics.OtlpStdoutMetricExporterBuilder;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * A {@link MetricExporter} which writes {@linkplain MetricData metrics} to a {@link Logger} in OTLP
 * JSON format. Each log line will include a single {@code ResourceMetrics}.
 */
public final class OtlpJsonLoggingMetricExporter implements MetricExporter {

  private static final Logger logger =
      Logger.getLogger(OtlpJsonLoggingMetricExporter.class.getName());

  private final AggregationTemporality aggregationTemporality;

  private final OtlpStdoutMetricExporter delegate;

  /**
   * Returns a new {@link OtlpJsonLoggingMetricExporter} with a aggregation temporality of {@link
   * AggregationTemporality#CUMULATIVE}.
   */
  public static MetricExporter create() {
    return create(AggregationTemporality.CUMULATIVE);
  }

  /**
   * Returns a new {@link OtlpJsonLoggingMetricExporter} with the given {@code
   * aggregationTemporality}.
   */
  public static MetricExporter create(AggregationTemporality aggregationTemporality) {
    OtlpStdoutMetricExporter delegate =
        new OtlpStdoutMetricExporterBuilder(logger).setWrapperJsonObject(false).build();
    return new OtlpJsonLoggingMetricExporter(delegate, aggregationTemporality);
  }

  /**
   * Returns a new {@link OtlpJsonLoggingMetricExporter} with the given {@code
   * aggregationTemporality}.
   *
   * @param aggregationTemporality the aggregation temporality to use
   * @param wrapperJsonObject whether to wrap the JSON object in an outer JSON "resourceMetrics"
   *     object. When {@code true}, uses low allocation OTLP marshalers with {@link
   *     MemoryMode#REUSABLE_DATA}. When {@code false}, uses {@link MemoryMode#IMMUTABLE_DATA}.
   */
  public static MetricExporter create(
      AggregationTemporality aggregationTemporality, boolean wrapperJsonObject) {
    MemoryMode memoryMode =
        wrapperJsonObject ? MemoryMode.REUSABLE_DATA : MemoryMode.IMMUTABLE_DATA;
    OtlpStdoutMetricExporter delegate =
        new OtlpStdoutMetricExporterBuilder(logger)
            .setWrapperJsonObject(wrapperJsonObject)
            .setMemoryMode(memoryMode)
            .build();
    return new OtlpJsonLoggingMetricExporter(delegate, aggregationTemporality);
  }

  OtlpJsonLoggingMetricExporter(
      OtlpStdoutMetricExporter delegate, AggregationTemporality aggregationTemporality) {
    this.delegate = delegate;
    this.aggregationTemporality = aggregationTemporality;
  }

  @Override
  public CompletableResultCode export(Collection<MetricData> metrics) {
    return delegate.export(metrics);
  }

  @Override
  public CompletableResultCode flush() {
    return delegate.flush();
  }

  @Override
  public CompletableResultCode shutdown() {
    return delegate.shutdown();
  }

  /**
   * Return the aggregation temporality.
   *
   * @deprecated Use {@link #getAggregationTemporality(InstrumentType)}.
   */
  @Deprecated
  public AggregationTemporality getPreferredTemporality() {
    return aggregationTemporality;
  }

  @Override
  public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
    return aggregationTemporality;
  }
}
