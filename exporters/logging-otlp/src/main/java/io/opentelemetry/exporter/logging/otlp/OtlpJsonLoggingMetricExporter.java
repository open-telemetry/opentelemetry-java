/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.metrics.MetricReusableDataMarshaler;
import io.opentelemetry.exporter.internal.otlp.metrics.ResourceMetricsMarshaler;
import io.opentelemetry.exporter.logging.otlp.internal.InternalBuilder;
import io.opentelemetry.exporter.logging.otlp.internal.metrics.InternalMetricBuilder;
import io.opentelemetry.exporter.logging.otlp.internal.metrics.MetricBuilderAccessUtil;
import io.opentelemetry.exporter.logging.otlp.internal.metrics.OtlpJsonLoggingMetricExporterBuilder;
import io.opentelemetry.exporter.logging.otlp.internal.writer.JsonWriter;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link MetricExporter} which writes {@linkplain MetricData metrics} to a {@link Logger} in OTLP
 * JSON format. Each log line will include a single {@code ResourceMetrics}.
 */
public final class OtlpJsonLoggingMetricExporter implements MetricExporter {

  private static final Logger logger =
      Logger.getLogger(OtlpJsonLoggingMetricExporter.class.getName());

  private final AtomicBoolean isShutdown = new AtomicBoolean();

  private final JsonWriter jsonWriter;

  private final AggregationTemporality aggregationTemporality;
  private final AggregationTemporalitySelector aggregationTemporalitySelector;
  private final DefaultAggregationSelector defaultAggregationSelector;

  private final Function<Collection<MetricData>, CompletableResultCode> marshaler;
  private final MemoryMode memoryMode;
  private final boolean wrapperJsonObject;

  static {
    MetricBuilderAccessUtil.setToExporter(
        builder ->
            new OtlpJsonLoggingMetricExporter(
                builder.getJsonWriter(),
                builder.getMemoryMode(),
                builder.isWrapperJsonObject(),
                builder.getPreferredTemporality(),
                builder.getAggregationTemporalitySelector(),
                builder.getDefaultAggregationSelector()));
    MetricBuilderAccessUtil.setToBuilder(
        exporter -> {
          InternalMetricBuilder builder =
              InternalBuilder.forMetrics()
                  .setPreferredTemporality(exporter.aggregationTemporality)
                  .setAggregationTemporalitySelector(exporter.aggregationTemporalitySelector)
                  .setDefaultAggregationSelector(exporter.defaultAggregationSelector);
          builder
              .setJsonWriter(exporter.jsonWriter)
              .setWrapperJsonObject(exporter.wrapperJsonObject)
              .setMemoryMode(exporter.memoryMode);
          return builder;
        });
  }

  /**
   * Returns a new {@link OtlpJsonLoggingMetricExporter} with an aggregation temporality of {@link
   * AggregationTemporality#CUMULATIVE}.
   */
  public static MetricExporter create() {
    return OtlpJsonLoggingMetricExporterBuilder.create().build();
  }

  /**
   * Returns a new {@link OtlpJsonLoggingMetricExporter} with the given {@code
   * aggregationTemporality}.
   */
  public static MetricExporter create(AggregationTemporality aggregationTemporality) {
    return MetricBuilderAccessUtil.getToExporter()
        .apply(InternalBuilder.forMetrics().setPreferredTemporality(aggregationTemporality));
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
    return aggregationTemporalitySelector.getAggregationTemporality(instrumentType);
  }

  @Override
  public Aggregation getDefaultAggregation(InstrumentType instrumentType) {
    return defaultAggregationSelector.getDefaultAggregation(instrumentType);
  }

  OtlpJsonLoggingMetricExporter(
      JsonWriter jsonWriter,
      MemoryMode memoryMode,
      boolean wrapperJsonObject,
      AggregationTemporality preferredTemporality,
      AggregationTemporalitySelector aggregationTemporalitySelector,
      DefaultAggregationSelector defaultAggregationSelector) {
    this.memoryMode = memoryMode;
    this.wrapperJsonObject = wrapperJsonObject;
    this.jsonWriter = jsonWriter;
    aggregationTemporality = preferredTemporality;
    this.aggregationTemporalitySelector = aggregationTemporalitySelector;
    this.defaultAggregationSelector = defaultAggregationSelector;

    marshaler = createMarshaler(jsonWriter, memoryMode, wrapperJsonObject);
  }

  private static Function<Collection<MetricData>, CompletableResultCode> createMarshaler(
      JsonWriter jsonWriter, MemoryMode memoryMode, boolean wrapperJsonObject) {

    if (wrapperJsonObject) {
      MetricReusableDataMarshaler reusableDataMarshaler =
          new MetricReusableDataMarshaler(memoryMode) {
            @Override
            public CompletableResultCode doExport(Marshaler exportRequest, int numItems) {
              return jsonWriter.write(exportRequest);
            }
          };

      return reusableDataMarshaler::export;
    } else {
      return metrics -> {
        // not support for low allocation marshaler

        for (ResourceMetricsMarshaler resourceMetrics : ResourceMetricsMarshaler.create(metrics)) {
          CompletableResultCode resultCode = jsonWriter.write(resourceMetrics);
          if (!resultCode.isSuccess()) {
            // already logged
            return resultCode;
          }
        }
        return CompletableResultCode.ofSuccess();
      };
    }
  }

  @Override
  public CompletableResultCode export(Collection<MetricData> metrics) {
    if (isShutdown.get()) {
      return CompletableResultCode.ofFailure();
    }

    return marshaler.apply(metrics);
  }

  @Override
  public CompletableResultCode flush() {
    return jsonWriter.flush();
  }

  @Override
  public CompletableResultCode shutdown() {
    if (!isShutdown.compareAndSet(false, true)) {
      logger.log(Level.INFO, "Calling shutdown() multiple times.");
    }
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(", ", "OtlpJsonLoggingMetricExporter{", "}");
    joiner.add("memoryMode=" + memoryMode);
    joiner.add("wrapperJsonObject=" + wrapperJsonObject);
    joiner.add("jsonWriter=" + jsonWriter);
    joiner.add(
        "aggregationTemporalitySelector="
            + AggregationTemporalitySelector.asString(aggregationTemporalitySelector));
    joiner.add(
        "defaultAggregationSelector="
            + DefaultAggregationSelector.asString(defaultAggregationSelector));
    return joiner.toString();
  }
}
