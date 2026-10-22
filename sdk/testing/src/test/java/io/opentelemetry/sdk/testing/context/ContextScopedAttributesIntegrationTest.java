/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.context;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.common.ContextAttributes;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.logs.internal.SdkLoggerProviderUtil;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.internal.SdkTracerProviderUtil;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ContextScopedAttributesIntegrationTest {

  private InMemorySpanExporter spanExporter;
  private InMemoryLogRecordExporter logExporter;
  private InMemoryMetricReader metricReader;

  private Tracer tracer;
  private Logger logger;
  private LongCounter counter;

  @BeforeEach
  void setUp() {
    spanExporter = InMemorySpanExporter.create();
    logExporter = InMemoryLogRecordExporter.create();
    metricReader = InMemoryMetricReader.create();

    SdkTracerProviderBuilder tracerProviderBuilder =
        SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(spanExporter));
    SdkTracerProviderUtil.setContextScopedAttributesEnabled(tracerProviderBuilder, true);

    SdkLoggerProviderBuilder loggerProviderBuilder =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(logExporter));
    SdkLoggerProviderUtil.setContextScopedAttributesEnabled(loggerProviderBuilder, true);

    SdkMeterProviderBuilder meterProviderBuilder =
        SdkMeterProvider.builder().registerMetricReader(metricReader);
    SdkMeterProviderUtil.setContextScopedAttributesEnabled(meterProviderBuilder, true);

    OpenTelemetrySdk sdk =
        OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProviderBuilder.build())
            .setLoggerProvider(loggerProviderBuilder.build())
            .setMeterProvider(meterProviderBuilder.build())
            .build();

    tracer = sdk.getTracer("integration-test");
    logger = sdk.getLogsBridge().get("integration-test");
    Meter meter = sdk.getMeter("integration-test");
    counter = meter.counterBuilder("orders_total").build();
  }

  @Test
  void multiSignalPropagation_inheritsContextAttributesAcrossAllSignals() {
    Context ctx =
        ContextAttributes.add(
            Context.current(),
            Attributes.of(stringKey("tenant.id"), "t-123", stringKey("environment"), "production"));

    try (Scope ignored = ctx.makeCurrent()) {
      Span span =
          tracer
              .spanBuilder("process-checkout")
              .setAttribute("operation.type", "checkout")
              .startSpan();
      span.end();

      logger
          .logRecordBuilder()
          .setBody("Order placed")
          .setAttribute(stringKey("order.status"), "SUCCESS")
          .emit();

      counter.add(1, Attributes.of(stringKey("payment.method"), "CREDIT_CARD"));
    }

    // 1. Verify Trace Span
    List<SpanData> spans = spanExporter.getFinishedSpanItems();
    assertThat(spans).hasSize(1);
    assertThat(spans.get(0))
        .hasAttributes(
            Attributes.of(
                stringKey("tenant.id"),
                "t-123",
                stringKey("environment"),
                "production",
                stringKey("operation.type"),
                "checkout"));

    // 2. Verify Log Record
    List<LogRecordData> logs = logExporter.getFinishedLogRecordItems();
    assertThat(logs).hasSize(1);
    assertThat(logs.get(0).getBodyValue().asString()).isEqualTo("Order placed");
    assertThat(logs.get(0).getAttributes())
        .isEqualTo(
            Attributes.of(
                stringKey("tenant.id"),
                "t-123",
                stringKey("environment"),
                "production",
                stringKey("order.status"),
                "SUCCESS"));

    // 3. Verify Metric Measurement
    assertThat(metricReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("orders_total")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(1)
                                        .hasAttributes(
                                            attributeEntry("tenant.id", "t-123"),
                                            attributeEntry("environment", "production"),
                                            attributeEntry("payment.method", "CREDIT_CARD")))));
  }

  @Test
  void multiSignalPropagation_explicitAttributesOverrideContextAttributes() {
    Context ctx =
        ContextAttributes.add(
            Context.current(),
            Attributes.of(
                stringKey("tenant.id"), "context-tenant", stringKey("shared.key"), "from-context"));

    try (Scope ignored = ctx.makeCurrent()) {
      tracer
          .spanBuilder("span-with-override")
          .setAttribute("shared.key", "span-explicit")
          .startSpan()
          .end();

      logger
          .logRecordBuilder()
          .setBody("log-with-override")
          .setAttribute(stringKey("shared.key"), "log-explicit")
          .emit();

      counter.add(5, Attributes.of(stringKey("shared.key"), "metric-explicit"));
    }

    // Trace: span-explicit wins
    List<SpanData> spans = spanExporter.getFinishedSpanItems();
    assertThat(spans).hasSize(1);
    assertThat(spans.get(0))
        .hasAttributes(
            Attributes.of(
                stringKey("tenant.id"),
                "context-tenant",
                stringKey("shared.key"),
                "span-explicit"));

    // Log: log-explicit wins
    List<LogRecordData> logs = logExporter.getFinishedLogRecordItems();
    assertThat(logs).hasSize(1);
    assertThat(logs.get(0).getAttributes())
        .isEqualTo(
            Attributes.of(
                stringKey("tenant.id"), "context-tenant", stringKey("shared.key"), "log-explicit"));

    // Metric: metric-explicit wins
    assertThat(metricReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("orders_total")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(5)
                                        .hasAttributes(
                                            attributeEntry("tenant.id", "context-tenant"),
                                            attributeEntry("shared.key", "metric-explicit")))));
  }

  @Test
  void outsideScope_signalsDoNotInheritContextAttributes() {
    Context ctx =
        ContextAttributes.add(Context.current(), Attributes.of(stringKey("tenant.id"), "t-999"));

    try (Scope ignored = ctx.makeCurrent()) {
      // inside scope
    }

    // Outside scope: emitted signals must NOT inherit "tenant.id"
    tracer.spanBuilder("outside-span").startSpan().end();
    logger.logRecordBuilder().setBody("outside-log").emit();
    counter.add(1);

    List<SpanData> spans = spanExporter.getFinishedSpanItems();
    assertThat(spans).hasSize(1);
    assertThat(spans.get(0).getAttributes()).isEmpty();

    List<LogRecordData> logs = logExporter.getFinishedLogRecordItems();
    assertThat(logs).hasSize(1);
    assertThat(logs.get(0).getAttributes()).isEmpty();

    assertThat(metricReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("orders_total")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point -> point.hasValue(1).hasAttributes(Attributes.empty()))));
  }
}
