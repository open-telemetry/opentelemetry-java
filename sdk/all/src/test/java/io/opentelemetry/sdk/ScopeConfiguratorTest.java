/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import static io.opentelemetry.sdk.common.internal.ScopeConfiguratorBuilder.nameEquals;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.logs.internal.LoggerConfig;
import io.opentelemetry.sdk.logs.internal.SdkLoggerProviderUtil;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.MeterConfig;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.internal.SdkTracerProviderUtil;
import io.opentelemetry.sdk.trace.internal.TracerConfig;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ScopeConfiguratorTest {

  private final InMemoryLogRecordExporter logRecordExporter = InMemoryLogRecordExporter.create();
  private final InMemoryMetricReader metricReader = InMemoryMetricReader.create();
  private final InMemorySpanExporter spanExporter = InMemorySpanExporter.create();

  private static final InstrumentationScopeInfo scopeA = InstrumentationScopeInfo.create("scopeA");
  private static final InstrumentationScopeInfo scopeB = InstrumentationScopeInfo.create("scopeB");
  private static final InstrumentationScopeInfo scopeC = InstrumentationScopeInfo.create("scopeC");

  /** Disable "scopeB". All other scopes are enabled by default. */
  @Test
  void disableScopeB() {
    // Configuration ergonomics will improve after APIs stabilize
    SdkTracerProviderBuilder tracerProviderBuilder = SdkTracerProvider.builder();
    SdkTracerProviderUtil.addTracerConfiguratorCondition(
        tracerProviderBuilder, nameEquals(scopeB.getName()), TracerConfig.disabled());
    SdkMeterProviderBuilder meterProviderBuilder = SdkMeterProvider.builder();
    SdkMeterProviderUtil.addMeterConfiguratorCondition(
        meterProviderBuilder, nameEquals(scopeB.getName()), MeterConfig.disabled());
    SdkLoggerProviderBuilder loggerProviderBuilder = SdkLoggerProvider.builder();
    SdkLoggerProviderUtil.addLoggerConfiguratorCondition(
        loggerProviderBuilder, nameEquals(scopeB.getName()), LoggerConfig.disabled());

    OpenTelemetrySdk sdk =
        OpenTelemetrySdk.builder()
            .setTracerProvider(
                tracerProviderBuilder
                    .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
                    .build())
            .setMeterProvider(meterProviderBuilder.registerMetricReader(metricReader).build())
            .setLoggerProvider(
                loggerProviderBuilder
                    .addLogRecordProcessor(SimpleLogRecordProcessor.create(logRecordExporter))
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
              assertThat(spansByScope.get(scopeA)).hasSize(1);
              assertThat(spansByScope.get(scopeB)).isNull();
              assertThat(spansByScope.get(scopeC)).hasSize(1);
            });
    assertThat(metricReader.collectAllMetrics())
        .satisfies(
            metrics -> {
              Map<InstrumentationScopeInfo, List<MetricData>> metricsByScope =
                  metrics.stream()
                      .collect(Collectors.groupingBy(MetricData::getInstrumentationScopeInfo));
              assertThat(metricsByScope.get(scopeA)).hasSize(1);
              assertThat(metricsByScope.get(scopeB)).isNull();
              assertThat(metricsByScope.get(scopeC)).hasSize(1);
            });
    assertThat(logRecordExporter.getFinishedLogRecordItems())
        .satisfies(
            logs -> {
              Map<InstrumentationScopeInfo, List<LogRecordData>> logsByScope =
                  logs.stream()
                      .collect(Collectors.groupingBy(LogRecordData::getInstrumentationScopeInfo));
              assertThat(logsByScope.get(scopeA)).hasSize(1);
              assertThat(logsByScope.get(scopeB)).isNull();
              assertThat(logsByScope.get(scopeC)).hasSize(1);
            });
  }

  /** Disable all scopes by default and enable a single scope. */
  @Test
  void disableAllScopesExceptB() {
    // Configuration ergonomics will improve after APIs stabilize
    SdkTracerProviderBuilder tracerProviderBuilder = SdkTracerProvider.builder();
    SdkTracerProviderUtil.setTracerConfigurator(
        tracerProviderBuilder,
        TracerConfig.configuratorBuilder()
            .setDefault(TracerConfig.disabled())
            .addCondition(nameEquals(scopeB.getName()), TracerConfig.enabled())
            .build());
    SdkMeterProviderBuilder meterProviderBuilder = SdkMeterProvider.builder();
    SdkMeterProviderUtil.setMeterConfigurator(
        meterProviderBuilder,
        MeterConfig.configuratorBuilder()
            .setDefault(MeterConfig.disabled())
            .addCondition(nameEquals(scopeB.getName()), MeterConfig.enabled())
            .build());
    SdkLoggerProviderBuilder loggerProviderBuilder = SdkLoggerProvider.builder();
    SdkLoggerProviderUtil.setLoggerConfigurator(
        loggerProviderBuilder,
        LoggerConfig.configuratorBuilder()
            .setDefault(LoggerConfig.disabled())
            .addCondition(nameEquals(scopeB.getName()), LoggerConfig.enabled())
            .build());

    OpenTelemetrySdk sdk =
        OpenTelemetrySdk.builder()
            .setTracerProvider(
                tracerProviderBuilder
                    .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
                    .build())
            .setMeterProvider(meterProviderBuilder.registerMetricReader(metricReader).build())
            .setLoggerProvider(
                loggerProviderBuilder
                    .addLogRecordProcessor(SimpleLogRecordProcessor.create(logRecordExporter))
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
              assertThat(spansByScope.get(scopeA)).isNull();
              assertThat(spansByScope.get(scopeB)).hasSize(1);
              assertThat(spansByScope.get(scopeC)).isNull();
            });
    assertThat(metricReader.collectAllMetrics())
        .satisfies(
            metrics -> {
              Map<InstrumentationScopeInfo, List<MetricData>> metricsByScope =
                  metrics.stream()
                      .collect(Collectors.groupingBy(MetricData::getInstrumentationScopeInfo));
              assertThat(metricsByScope.get(scopeA)).isNull();
              assertThat(metricsByScope.get(scopeB)).hasSize(1);
              assertThat(metricsByScope.get(scopeC)).isNull();
            });
    assertThat(logRecordExporter.getFinishedLogRecordItems())
        .satisfies(
            logs -> {
              Map<InstrumentationScopeInfo, List<LogRecordData>> logsByScope =
                  logs.stream()
                      .collect(Collectors.groupingBy(LogRecordData::getInstrumentationScopeInfo));
              assertThat(logsByScope.get(scopeA)).isNull();
              assertThat(logsByScope.get(scopeB)).hasSize(1);
              assertThat(logsByScope.get(scopeC)).isNull();
            });
  }

  /**
   * Emit spans, metrics and logs in a hierarchy of 3 scopes: scopeA -> scopeB -> scopeC. Exercise
   * the scope config which is common across all signals.
   */
  private static void simulateInstrumentation(OpenTelemetry openTelemetry) {
    // Start scopeA
    Tracer scopeATracer = openTelemetry.getTracer(scopeA.getName());
    Meter scopeAMeter = openTelemetry.getMeter(scopeA.getName());
    Logger scopeALogger = openTelemetry.getLogsBridge().get(scopeA.getName());
    Span spanA = scopeATracer.spanBuilder("spanA").startSpan();
    try (Scope spanAScope = spanA.makeCurrent()) {
      scopeALogger.logRecordBuilder().setBody("scopeA log message").emit();

      // Start scopeB
      Tracer scopeBTracer = openTelemetry.getTracer(scopeB.getName());
      Meter scopeBMeter = openTelemetry.getMeter(scopeB.getName());
      Logger scopeBLogger = openTelemetry.getLogsBridge().get(scopeB.getName());
      Span spanB = scopeBTracer.spanBuilder("spanB").startSpan();
      try (Scope spanBScope = spanB.makeCurrent()) {
        scopeBLogger.logRecordBuilder().setBody("scopeB log message").emit();

        // Start scopeC
        Tracer scopeCTracer = openTelemetry.getTracer(scopeC.getName());
        Meter scopeCMeter = openTelemetry.getMeter(scopeC.getName());
        Logger scopeCLogger = openTelemetry.getLogsBridge().get(scopeC.getName());
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
