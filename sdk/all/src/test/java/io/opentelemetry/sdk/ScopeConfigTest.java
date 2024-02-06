/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import static io.opentelemetry.sdk.common.ScopeSelector.named;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.LoggerConfig;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.metrics.MeterConfig;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.TracerConfig;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ScopeConfigTest {

  /**
   * Emit spans, metrics and logs in a hierarchy of 3 scopes: scopeA -> scopeB -> scopeC. Exercise
   * the scope config which is common across all signals and verify telemetry is as expected.
   */
  @Test
  void disableScopeAllSignals() {
    InMemoryLogRecordExporter logRecordExporter = InMemoryLogRecordExporter.create();
    InMemoryMetricReader metricReader = InMemoryMetricReader.create();
    InMemorySpanExporter spanExporter = InMemorySpanExporter.create();
    OpenTelemetrySdk sdk =
        OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
                    .addScopeConfig(named("scopeB"), TracerConfig.disabled())
                    .build())
            .setMeterProvider(
                SdkMeterProvider.builder()
                    .registerMetricReader(metricReader)
                    .addScopeConfig(named("scopeB"), MeterConfig.disabled())
                    .build())
            .setLoggerProvider(
                SdkLoggerProvider.builder()
                    .addLogRecordProcessor(SimpleLogRecordProcessor.create(logRecordExporter))
                    .addScopeConfig(named("scopeB"), LoggerConfig.disabled())
                    .build())
            .build();

    // Start scopeA
    Tracer scopeATracer = sdk.getTracer("scopeA");
    Meter scopeAMeter = sdk.getMeter("scopeA");
    Logger scopeALogger = sdk.getSdkLoggerProvider().get("scopeA");
    Span spanA = scopeATracer.spanBuilder("spanA").startSpan();
    try (Scope spanAScope = spanA.makeCurrent()) {
      scopeALogger.logRecordBuilder().setBody("scopeA log message").emit();

      // Start scopeB
      Tracer scopeBTracer = sdk.getTracer("scopeB");
      Meter scopeBMeter = sdk.getMeter("scopeB");
      Logger scopeBLogger = sdk.getSdkLoggerProvider().get("scopeB");
      Span spanB = scopeBTracer.spanBuilder("spanB").startSpan();
      try (Scope spanBScope = spanB.makeCurrent()) {
        scopeBLogger.logRecordBuilder().setBody("scopeB log message").emit();

        // Start scopeC
        Tracer scopeCTracer = sdk.getTracer("scopeC");
        Meter scopeCMeter = sdk.getMeter("scopeC");
        Logger scopeCLogger = sdk.getSdkLoggerProvider().get("scopeC");
        Span spanC = scopeCTracer.spanBuilder("spanC").startSpan();
        try (Scope spanCScope = spanB.makeCurrent()) {
          scopeCLogger.logRecordBuilder().setBody("scopeC log message").emit();
        } finally {
          spanC.end();
          scopeCMeter.counterBuilder("scopeCCounter").build().add(1);
        }
        // End scopeC

      } finally {
        spanB.end();
        scopeBMeter.counterBuilder("scopeBCounter").build().add(1);
      }
      // End scopeB

    } finally {
      spanA.end();
      scopeAMeter.counterBuilder("scopeACounter").build().add(1);
    }
    // End scopeA

    // Collect all the telemetry. Ensure we don't see any from scopeB, and that the telemetry from
    // scopeA and scopeC is valid.
    assertThat(spanExporter.getFinishedSpanItems())
        .satisfies(
            spans -> {
              Map<InstrumentationScopeInfo, List<SpanData>> spansByScope =
                  spans.stream()
                      .collect(Collectors.groupingBy(SpanData::getInstrumentationScopeInfo));
              assertThat(spansByScope.get(InstrumentationScopeInfo.create("scopeA"))).hasSize(1);
              assertThat(spansByScope.get(InstrumentationScopeInfo.create("scopeB"))).isNull();
              assertThat(spansByScope.get(InstrumentationScopeInfo.create("scopeC"))).hasSize(1);
            });
    assertThat(metricReader.collectAllMetrics())
        .satisfies(
            metrics -> {
              Map<InstrumentationScopeInfo, List<MetricData>> metricsByScope =
                  metrics.stream()
                      .collect(Collectors.groupingBy(MetricData::getInstrumentationScopeInfo));
              assertThat(metricsByScope.get(InstrumentationScopeInfo.create("scopeA"))).hasSize(1);
              assertThat(metricsByScope.get(InstrumentationScopeInfo.create("scopeB"))).isNull();
              assertThat(metricsByScope.get(InstrumentationScopeInfo.create("scopeC"))).hasSize(1);
            });
    assertThat(logRecordExporter.getFinishedLogRecordItems())
        .satisfies(
            logs -> {
              Map<InstrumentationScopeInfo, List<LogRecordData>> logsByScope =
                  logs.stream()
                      .collect(Collectors.groupingBy(LogRecordData::getInstrumentationScopeInfo));
              assertThat(logsByScope.get(InstrumentationScopeInfo.create("scopeA"))).hasSize(1);
              assertThat(logsByScope.get(InstrumentationScopeInfo.create("scopeB"))).isNull();
              assertThat(logsByScope.get(InstrumentationScopeInfo.create("scopeC"))).hasSize(1);
            });
  }
}
