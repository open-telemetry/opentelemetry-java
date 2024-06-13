/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.sdk;

import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.util.Collection;
import java.util.function.Supplier;

public class OpenTelemetryTestSdk {
  private final OpenTelemetrySdk openTelemetry;
  private final InMemorySpanExporter spanExporter;
  private final Supplier<Collection<MetricData>> metricReader;
  private final InMemoryLogRecordExporter logRecordExporter;

  OpenTelemetryTestSdk(
      OpenTelemetrySdk openTelemetry,
      InMemorySpanExporter spanExporter,
      Supplier<Collection<MetricData>> metricReader,
      InMemoryLogRecordExporter logRecordExporter) {
    this.openTelemetry = openTelemetry;
    this.spanExporter = spanExporter;
    this.metricReader = metricReader;
    this.logRecordExporter = logRecordExporter;
  }

  public static OpenTelemetryTestSdk create() {
    InMemorySpanExporter spanExporter = InMemorySpanExporter.create();

    SdkTracerProvider tracerProvider =
        SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
            .build();

    InMemoryMetricReader metricReader = InMemoryMetricReader.create();

    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder().registerMetricReader(metricReader).build();

    InMemoryLogRecordExporter logRecordExporter = InMemoryLogRecordExporter.create();

    SdkLoggerProvider loggerProvider =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(logRecordExporter))
            .build();

    OpenTelemetrySdk openTelemetry =
        OpenTelemetrySdk.builder()
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .setTracerProvider(tracerProvider)
            .setMeterProvider(meterProvider)
            .setLoggerProvider(loggerProvider)
            .build();

    return new OpenTelemetryTestSdk(
        openTelemetry, spanExporter, metricReader::collectAllMetrics, logRecordExporter);
  }

  public OpenTelemetrySdk getOpenTelemetry() {
    return openTelemetry;
  }

  public InMemorySpanExporter getSpanExporter() {
    return spanExporter;
  }

  public Supplier<Collection<MetricData>> getMetricReader() {
    return metricReader;
  }

  public InMemoryLogRecordExporter getLogRecordExporter() {
    return logRecordExporter;
  }
}
