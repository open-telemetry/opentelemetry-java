/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.metrics;

import io.opentelemetry.exporter.internal.otlp.metrics.MetricsRequestMarshaler;
import io.opentelemetry.exporter.internal.otlp.metrics.ResourceMetricsMarshaler;
import io.opentelemetry.exporter.logging.otlp.internal.writer.JsonWriter;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.Collection;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Exporter for sending OTLP metrics to stdout.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class OtlpStdoutMetricExporter implements MetricExporter {

  private static final Logger LOGGER = Logger.getLogger(OtlpStdoutMetricExporter.class.getName());

  private final AtomicBoolean isShutdown = new AtomicBoolean();

  private final Logger logger;
  private final JsonWriter jsonWriter;
  private final boolean wrapperJsonObject;
  private final AggregationTemporalitySelector aggregationTemporalitySelector;
  private final DefaultAggregationSelector defaultAggregationSelector;

  OtlpStdoutMetricExporter(
      Logger logger,
      JsonWriter jsonWriter,
      boolean wrapperJsonObject,
      AggregationTemporalitySelector aggregationTemporalitySelector,
      DefaultAggregationSelector defaultAggregationSelector) {
    this.logger = logger;
    this.jsonWriter = jsonWriter;
    this.wrapperJsonObject = wrapperJsonObject;
    this.aggregationTemporalitySelector = aggregationTemporalitySelector;
    this.defaultAggregationSelector = defaultAggregationSelector;
  }

  /** Returns a new {@link OtlpStdoutMetricExporterBuilder}. */
  @SuppressWarnings("SystemOut")
  public static OtlpStdoutMetricExporterBuilder builder() {
    return new OtlpStdoutMetricExporterBuilder(LOGGER).setOutput(System.out);
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
    if (isShutdown.get()) {
      return CompletableResultCode.ofFailure();
    }

    if (wrapperJsonObject) {
      MetricsRequestMarshaler request = MetricsRequestMarshaler.create(metrics);
      return jsonWriter.write(request);
    } else {
      for (ResourceMetricsMarshaler resourceMetrics : ResourceMetricsMarshaler.create(metrics)) {
        CompletableResultCode resultCode = jsonWriter.write(resourceMetrics);
        if (!resultCode.isSuccess()) {
          // already logged
          return resultCode;
        }
      }
      return CompletableResultCode.ofSuccess();
    }
  }

  @Override
  public CompletableResultCode flush() {
    return jsonWriter.flush();
  }

  @Override
  public CompletableResultCode shutdown() {
    if (!isShutdown.compareAndSet(false, true)) {
      logger.log(Level.INFO, "Calling shutdown() multiple times.");
    } else {
      jsonWriter.close();
    }
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(", ", "OtlpStdoutMetricExporter{", "}");
    joiner.add("jsonWriter=" + jsonWriter);
    joiner.add("wrapperJsonObject=" + wrapperJsonObject);
    return joiner.toString();
  }
}
