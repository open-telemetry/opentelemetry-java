/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import static io.opentelemetry.sdk.common.ScopeConfiguratorBuilder.nameEquals;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.OpenTelemetry;
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

class ScopeConfiguratorTest {

  private final InMemoryLogRecordExporter logRecordExporter = InMemoryLogRecordExporter.create();
  private final InMemoryMetricReader metricReader = InMemoryMetricReader.create();
  private final InMemorySpanExporter spanExporter = InMemorySpanExporter.create();

  /** Disable "scopeB". All other scopes are enabled by default. */
  @Test
  void disableScopeB() {
    OpenTelemetrySdk sdk =
        OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
                    .addTracerConfiguratorMatcher(nameEquals("scopeB"), TracerConfig.disabled())
                    .build())
            .setMeterProvider(
                SdkMeterProvider.builder()
                    .registerMetricReader(metricReader)
                    .addMeterConfiguratorMatcher(nameEquals("scopeB"), MeterConfig.disabled())
                    .build())
            .setLoggerProvider(
                SdkLoggerProvider.builder()
                    .addLogRecordProcessor(SimpleLogRecordProcessor.create(logRecordExporter))
                    .addLoggerConfiguratorMatcher(nameEquals("scopeB"), LoggerConfig.disabled())
                    .build())
            .build();

    simulateInstrumentation(sdk);

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

  /** Disable all scopes by default and enable a single scope. */
  @Test
  void disableAllScopesExceptB() {
    OpenTelemetrySdk sdk =
        OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
                    .setTracerConfigurator(
                        TracerConfig.configuratorBuilder()
                            .setDefault(TracerConfig.disabled())
                            .addCondition(nameEquals("scopeB"), TracerConfig.enabled())
                            .build())
                    .build())
            .setMeterProvider(
                SdkMeterProvider.builder()
                    .registerMetricReader(metricReader)
                    .setMeterConfigurator(
                        MeterConfig.configuratorBuilder()
                            .setDefault(MeterConfig.disabled())
                            .addCondition(nameEquals("scopeB"), MeterConfig.enabled())
                            .build())
                    .build())
            .setLoggerProvider(
                SdkLoggerProvider.builder()
                    .addLogRecordProcessor(SimpleLogRecordProcessor.create(logRecordExporter))
                    .setLoggerConfigurator(
                        LoggerConfig.configuratorBuilder()
                            .setDefault(LoggerConfig.disabled())
                            .addCondition(nameEquals("scopeB"), LoggerConfig.enabled())
                            .build())
                    .build())
            .build();

    simulateInstrumentation(sdk);

    // Collect all the telemetry. Ensure we only see telemetry from scopeB, since other scopes have
    // been disabled by default.
    assertThat(spanExporter.getFinishedSpanItems())
        .satisfies(
            spans -> {
              Map<InstrumentationScopeInfo, List<SpanData>> spansByScope =
                  spans.stream()
                      .collect(Collectors.groupingBy(SpanData::getInstrumentationScopeInfo));
              assertThat(spansByScope.get(InstrumentationScopeInfo.create("scopeA"))).isNull();
              assertThat(spansByScope.get(InstrumentationScopeInfo.create("scopeB"))).hasSize(1);
              assertThat(spansByScope.get(InstrumentationScopeInfo.create("scopeC"))).isNull();
            });
    assertThat(metricReader.collectAllMetrics())
        .satisfies(
            metrics -> {
              Map<InstrumentationScopeInfo, List<MetricData>> metricsByScope =
                  metrics.stream()
                      .collect(Collectors.groupingBy(MetricData::getInstrumentationScopeInfo));
              assertThat(metricsByScope.get(InstrumentationScopeInfo.create("scopeA"))).isNull();
              assertThat(metricsByScope.get(InstrumentationScopeInfo.create("scopeB"))).hasSize(1);
              assertThat(metricsByScope.get(InstrumentationScopeInfo.create("scopeC"))).isNull();
            });
    assertThat(logRecordExporter.getFinishedLogRecordItems())
        .satisfies(
            logs -> {
              Map<InstrumentationScopeInfo, List<LogRecordData>> logsByScope =
                  logs.stream()
                      .collect(Collectors.groupingBy(LogRecordData::getInstrumentationScopeInfo));
              assertThat(logsByScope.get(InstrumentationScopeInfo.create("scopeA"))).isNull();
              assertThat(logsByScope.get(InstrumentationScopeInfo.create("scopeB"))).hasSize(1);
              assertThat(logsByScope.get(InstrumentationScopeInfo.create("scopeC"))).isNull();
            });
  }

  /**
   * Emit spans, metrics and logs in a hierarchy of 3 scopes: scopeA -> scopeB -> scopeC. Exercise
   * the scope config which is common across all signals.
   */
  private static void simulateInstrumentation(OpenTelemetry openTelemetry) {
    // Start scopeA
    Tracer scopeATracer = openTelemetry.getTracer("scopeA");
    Meter scopeAMeter = openTelemetry.getMeter("scopeA");
    Logger scopeALogger = openTelemetry.getLogsBridge().get("scopeA");
    Span spanA = scopeATracer.spanBuilder("spanA").startSpan();
    try (Scope spanAScope = spanA.makeCurrent()) {
      scopeALogger.logRecordBuilder().setBody("scopeA log message").emit();

      // Start scopeB
      Tracer scopeBTracer = openTelemetry.getTracer("scopeB");
      Meter scopeBMeter = openTelemetry.getMeter("scopeB");
      Logger scopeBLogger = openTelemetry.getLogsBridge().get("scopeB");
      Span spanB = scopeBTracer.spanBuilder("spanB").startSpan();
      try (Scope spanBScope = spanB.makeCurrent()) {
        scopeBLogger.logRecordBuilder().setBody("scopeB log message").emit();

        // Start scopeC
        Tracer scopeCTracer = openTelemetry.getTracer("scopeC");
        Meter scopeCMeter = openTelemetry.getMeter("scopeC");
        Logger scopeCLogger = openTelemetry.getLogsBridge().get("scopeC");
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
  }
}
