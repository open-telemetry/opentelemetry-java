/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.metrics;

import io.opentelemetry.exporter.internal.otlp.metrics.MetricReusableDataMarshaler;
import io.opentelemetry.exporter.internal.otlp.metrics.ResourceMetricsMarshaler;
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
  private final boolean useLowAllocation;
  private final MemoryMode memoryMode;
  private final Function<Collection<MetricData>, CompletableResultCode> marshaler;
  private final AggregationTemporalitySelector aggregationTemporalitySelector;
  private final DefaultAggregationSelector defaultAggregationSelector;

  OtlpStdoutMetricExporter(
      Logger logger,
      JsonWriter jsonWriter,
      boolean useLowAllocation,
      MemoryMode memoryMode,
      AggregationTemporalitySelector aggregationTemporalitySelector,
      DefaultAggregationSelector defaultAggregationSelector) {
    this.logger = logger;
    this.jsonWriter = jsonWriter;
    this.useLowAllocation = useLowAllocation;
    this.memoryMode = memoryMode;
    this.aggregationTemporalitySelector = aggregationTemporalitySelector;
    this.defaultAggregationSelector = defaultAggregationSelector;
    marshaler = createMarshaler(jsonWriter, memoryMode, useLowAllocation);
  }

  /** Returns a new {@link OtlpStdoutMetricExporterBuilder}. */
  @SuppressWarnings("SystemOut")
  public static OtlpStdoutMetricExporterBuilder builder() {
    return new OtlpStdoutMetricExporterBuilder(LOGGER).setOutput(System.out);
  }

  private static Function<Collection<MetricData>, CompletableResultCode> createMarshaler(
      JsonWriter jsonWriter, MemoryMode memoryMode, boolean useLowAllocation) {
    if (useLowAllocation) {
      MetricReusableDataMarshaler reusableDataMarshaler =
          new MetricReusableDataMarshaler(
              memoryMode, (marshaler, numItems) -> jsonWriter.write(marshaler));
      return reusableDataMarshaler::export;
    } else {
      return metrics -> {
        // no support for low allocation marshaler
        for (ResourceMetricsMarshaler marshaler : ResourceMetricsMarshaler.create(metrics)) {
          CompletableResultCode resultCode = jsonWriter.write(marshaler);
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
  public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
    return aggregationTemporalitySelector.getAggregationTemporality(instrumentType);
  }

  @Override
  public Aggregation getDefaultAggregation(InstrumentType instrumentType) {
    return defaultAggregationSelector.getDefaultAggregation(instrumentType);
  }

  @Override
  public MemoryMode getMemoryMode() {
    return memoryMode;
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
    } else {
      jsonWriter.close();
    }
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(", ", "OtlpStdoutMetricExporter{", "}");
    joiner.add("jsonWriter=" + jsonWriter);
    joiner.add("useLowAllocation=" + useLowAllocation);
    joiner.add("memoryMode=" + memoryMode);
    joiner.add(
        "aggregationTemporalitySelector="
            + AggregationTemporalitySelector.asString(aggregationTemporalitySelector));
    joiner.add(
        "defaultAggregationSelector="
            + DefaultAggregationSelector.asString(defaultAggregationSelector));
    return joiner.toString();
  }
}
