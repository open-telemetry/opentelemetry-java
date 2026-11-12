/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.common;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;

import io.opentelemetry.api.common.Attributes;
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
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.internal.SdkTracerProviderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Demonstrating usage of {@link ContextAttributes}. */
class ContextAttributesUsageTest {

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
    SdkTracerProviderUtil.setContextScopedAttributesFilter(
        tracerProviderBuilder, (scope, key) -> true);

    SdkLoggerProviderBuilder loggerProviderBuilder =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(logExporter));
    SdkLoggerProviderUtil.setContextScopedAttributesFilter(
        loggerProviderBuilder, (scope, key) -> true);

    SdkMeterProviderBuilder meterProviderBuilder =
        SdkMeterProvider.builder().registerMetricReader(metricReader);
    SdkMeterProviderUtil.setContextScopedAttributesFilter(
        meterProviderBuilder, (scope, key) -> true);

    OpenTelemetrySdk sdk =
        OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProviderBuilder.build())
            .setLoggerProvider(loggerProviderBuilder.build())
            .setMeterProvider(meterProviderBuilder.build())
            .build();

    tracer = sdk.getTracer("usage-test");
    logger = sdk.getLogsBridge().get("usage-test");
    Meter meter = sdk.getMeter("usage-test");
    counter = meter.counterBuilder("orders_total").build();
  }

  @Test
  void multiSignalPropagation() {
    Context ctx =
        ContextAttributes.set(
            Context.current(),
            Attributes.of(stringKey("tenant.id"), "t-123", stringKey("environment"), "production"));

    try (Scope ignored = ctx.makeCurrent()) {
      Span span =
          tracer
              .spanBuilder("process-checkout")
              .setAttribute("operation.type", "checkout")
              .startSpan();
      span.end();

      logger.logRecordBuilder().setBody("checkout initiated").emit();

      counter.add(1, Attributes.of(stringKey("currency"), "USD"));
    }

    assertThat(spanExporter.getFinishedSpanItems())
        .satisfiesExactly(span ->
            assertThat(span)
                .hasName("process-checkout")
                .hasAttributes(
                    attributeEntry("operation.type", "checkout"),
                    attributeEntry("tenant.id", "t-123"),
                    attributeEntry("environment", "production")));

    assertThat(logExporter.getFinishedLogRecordItems())
        .satisfiesExactly(log ->
            assertThat(log)
                .hasBody("checkout initiated")
                .hasAttributes(
                    attributeEntry("tenant.id", "t-123"),
                    attributeEntry("environment", "production")));

    assertThat(metricReader.collectAllMetrics())
        .satisfiesExactly(metric ->
            assertThat(metric)
                .hasName("orders_total")
                .hasLongSumPointsSatisfying(point ->
                    point
                        .hasValue(1)
                        .hasAttributes(
                            attributeEntry("currency", "USD"),
                            attributeEntry("tenant.id", "t-123"),
                            attributeEntry("environment", "production"))));
  }

  @Test
  void selectiveFiltering() {
    InMemorySpanExporter localSpanExporter = InMemorySpanExporter.create();
    InMemoryMetricReader localMetricReader = InMemoryMetricReader.create();

    SdkTracerProviderBuilder tracerBuilder =
        SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(localSpanExporter));
    SdkTracerProviderUtil.setContextScopedAttributesFilter(
        tracerBuilder, (scope, key) -> key.getKey().equals("tenant.id"));

    SdkMeterProviderBuilder meterBuilder =
        SdkMeterProvider.builder().registerMetricReader(localMetricReader);
    SdkMeterProviderUtil.setContextScopedAttributesFilter(
        meterBuilder, (scope, key) -> key.getKey().equals("region"));

    OpenTelemetrySdk sdk =
        OpenTelemetrySdk.builder()
            .setTracerProvider(tracerBuilder.build())
            .setMeterProvider(meterBuilder.build())
            .build();

    Tracer localTracer = sdk.getTracer("usage-test");
    LongCounter localCounter = sdk.getMeter("usage-test").counterBuilder("requests").build();

    Context ctx =
        ContextAttributes.set(
            Context.current(),
            Attributes.of(stringKey("tenant.id"), "t-999", stringKey("region"), "us-west-2"));

    try (Scope ignored = ctx.makeCurrent()) {
      localTracer.spanBuilder("request").startSpan().end();
      localCounter.add(1);
    }

    assertThat(localSpanExporter.getFinishedSpanItems())
        .satisfiesExactly(
            span -> assertThat(span).hasAttributes(attributeEntry("tenant.id", "t-999")));

    assertThat(localMetricReader.collectAllMetrics())
        .satisfiesExactly(metric ->
            assertThat(metric)
                .hasLongSumPointsSatisfying(point ->
                    point.hasValue(1).hasAttributes(attributeEntry("region", "us-west-2"))));
  }
}
