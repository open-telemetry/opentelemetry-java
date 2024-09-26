/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.metrics;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingMetricExporter;
import io.opentelemetry.exporter.logging.otlp.internal.writer.JsonWriter;
import io.opentelemetry.exporter.logging.otlp.internal.writer.LoggerJsonWriter;
import io.opentelemetry.exporter.logging.otlp.internal.writer.StreamJsonWriter;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * Builder for {@link OtlpJsonLoggingMetricExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class OtlpStdoutMetricExporterBuilder {

  private static final String TYPE = "metrics";

  private static final AggregationTemporalitySelector DEFAULT_AGGREGATION_TEMPORALITY_SELECTOR =
      AggregationTemporalitySelector.alwaysCumulative();

  private AggregationTemporalitySelector aggregationTemporalitySelector =
      DEFAULT_AGGREGATION_TEMPORALITY_SELECTOR;

  private DefaultAggregationSelector defaultAggregationSelector =
      DefaultAggregationSelector.getDefault();

  private final Logger logger;
  private JsonWriter jsonWriter;
  private boolean wrapperJsonObject = true;

  public OtlpStdoutMetricExporterBuilder(Logger logger) {
    this.logger = logger;
    this.jsonWriter = new LoggerJsonWriter(logger, TYPE);
  }

  /**
   * Sets the exporter to use the specified JSON object wrapper.
   *
   * @param wrapperJsonObject whether to wrap the JSON object in an outer JSON "resourceMetrics"
   *     object.
   */
  public OtlpStdoutMetricExporterBuilder setWrapperJsonObject(boolean wrapperJsonObject) {
    this.wrapperJsonObject = wrapperJsonObject;
    return this;
  }

  /**
   * Sets the exporter to use the specified output stream.
   *
   * <p>The output stream will be closed when {@link OtlpStdoutMetricExporter#shutdown()} is
   * called unless it's {@link System#out} or {@link System#err}.
   *
   * @param outputStream the output stream to use.
   */
  public OtlpStdoutMetricExporterBuilder setOutput(OutputStream outputStream) {
    requireNonNull(outputStream, "outputStream");
    this.jsonWriter = new StreamJsonWriter(outputStream, TYPE);
    return this;
  }

  /** Sets the exporter to use the specified logger. */
  public OtlpStdoutMetricExporterBuilder setOutput(Logger logger) {
    requireNonNull(logger, "logger");
    this.jsonWriter = new LoggerJsonWriter(logger, TYPE);
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
    return new OtlpStdoutMetricExporter(logger, jsonWriter, wrapperJsonObject, aggregationTemporalitySelector, defaultAggregationSelector);
  }
}
