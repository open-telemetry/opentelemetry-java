/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.junit5;

import static io.opentelemetry.sdk.testing.assertj.TracesAssert.assertThat;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.testing.assertj.TracesAssert;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * A JUnit5 extension which sets up the {@link OpenTelemetrySdk} for testing, resetting state
 * between tests.
 *
 * <pre>{@code
 * // class CoolTest {
 * //   @RegisterExtension
 * //   static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();
 * //
 * //   private final Tracer tracer = otelTesting.getOpenTelemetry().getTracer("test");
 * //   private final Meter meter = otelTesting.getOpenTelemetry().getMeter("test");
 * //
 * //   @Test
 * //   void test() {
 * //     tracer.spanBuilder("name").startSpan().end();
 * //     assertThat(otelTesting.getSpans()).containsExactly(expected);
 * //
 * //     LongCounter counter = meter.counterBuilder("counter-name").build();
 * //     counter.add(1);
 * //     assertThat(otelTesting.getMetrics()).satisfiesExactlyInAnyOrder(metricData -> {});
 * //   }
 * // }
 * }</pre>
 */
public final class OpenTelemetryExtension
    implements BeforeEachCallback, BeforeAllCallback, AfterAllCallback {

  /**
   * Returns a {@link OpenTelemetryExtension} with a default SDK initialized with an in-memory span
   * exporter and W3C trace context propagation.
   */
  public static OpenTelemetryExtension create() {
    InMemorySpanExporter spanExporter = InMemorySpanExporter.create();

    SdkTracerProvider tracerProvider =
        SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
            .build();

    InMemoryMetricReader metricReader = InMemoryMetricReader.create();

    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder().registerMetricReader(metricReader).build();

    OpenTelemetrySdk openTelemetry =
        OpenTelemetrySdk.builder()
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .setTracerProvider(tracerProvider)
            .setMeterProvider(meterProvider)
            .build();

    return new OpenTelemetryExtension(openTelemetry, spanExporter, metricReader);
  }

  private final OpenTelemetrySdk openTelemetry;
  private final InMemorySpanExporter spanExporter;
  private final InMemoryMetricReader metricReader;

  private OpenTelemetryExtension(
      OpenTelemetrySdk openTelemetry,
      InMemorySpanExporter spanExporter,
      InMemoryMetricReader metricReader) {
    this.openTelemetry = openTelemetry;
    this.spanExporter = spanExporter;
    this.metricReader = metricReader;
  }

  /** Returns the {@link OpenTelemetrySdk} created by this extension. */
  public OpenTelemetry getOpenTelemetry() {
    return openTelemetry;
  }

  /** Returns all the exported {@link SpanData} so far. */
  public List<SpanData> getSpans() {
    return spanExporter.getFinishedSpanItems();
  }

  /**
   * Returns the current {@link MetricData} in {@link AggregationTemporality#CUMULATIVE} format.
   *
   * @since 1.15.0
   */
  public List<MetricData> getMetrics() {
    return new ArrayList<>(metricReader.collectAllMetrics());
  }

  /**
   * Returns a {@link TracesAssert} for asserting on the currently exported traces. This method
   * requires AssertJ to be on the classpath.
   */
  public TracesAssert assertTraces() {
    return assertThat(spanExporter.getFinishedSpanItems());
  }

  /**
   * Clears the collected exported {@link SpanData}. Consider making your test smaller instead of
   * manually clearing state using this method.
   */
  public void clearSpans() {
    spanExporter.reset();
  }

  /**
   * Clears all registered metric instruments, such that {@link #getMetrics()} is empty.
   *
   * @since 1.15.0
   */
  public void clearMetrics() {
    SdkMeterProviderUtil.resetForTest(openTelemetry.getSdkMeterProvider());
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    clearSpans();
    clearMetrics();
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    GlobalOpenTelemetry.resetForTest();
    GlobalOpenTelemetry.set(openTelemetry);
  }

  @Override
  public void afterAll(ExtensionContext context) {
    GlobalOpenTelemetry.resetForTest();
    openTelemetry.close();
  }
}
