/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import static io.opentelemetry.exporter.logging.otlp.JsonUtil.JSON_FACTORY;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import io.opentelemetry.exporter.otlp.internal.metrics.ResourceMetricsMarshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link MetricExporter} which writes {@linkplain MetricData spans} to a {@link Logger} in OTLP
 * JSON format. Each log line will include a single {@code ResourceMetrics}.
 */
public final class OtlpJsonLoggingMetricExporter implements MetricExporter {

  private static final Logger logger =
      Logger.getLogger(OtlpJsonLoggingMetricExporter.class.getName());

  /** Returns a new {@link OtlpJsonLoggingMetricExporter}. */
  public static MetricExporter create() {
    return new OtlpJsonLoggingMetricExporter();
  }

  private OtlpJsonLoggingMetricExporter() {}

  @Override
  public CompletableResultCode export(Collection<MetricData> metrics) {
    ResourceMetricsMarshaler[] allResourceMetrics = ResourceMetricsMarshaler.create(metrics);
    for (ResourceMetricsMarshaler resourceMetrics : allResourceMetrics) {
      SegmentedStringWriter sw = new SegmentedStringWriter(JSON_FACTORY._getBufferRecycler());
      try (JsonGenerator gen = JsonUtil.create(sw)) {
        resourceMetrics.writeJsonTo(gen);
      } catch (IOException e) {
        // Shouldn't happen in practice, just skip it.
        continue;
      }
      logger.log(Level.INFO, sw.getAndClear());
    }
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    return CompletableResultCode.ofSuccess();
  }
}
