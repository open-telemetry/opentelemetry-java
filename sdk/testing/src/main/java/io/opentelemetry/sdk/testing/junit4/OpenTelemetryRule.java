/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.junit4;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.util.ArrayList;
import java.util.List;
import org.junit.rules.ExternalResource;

/**
 * A JUnit4 rule which sets up the {@link OpenTelemetrySdk} for testing, resetting state between
 * tests. This rule cannot be used with {@link org.junit.ClassRule}.
 *
 * <pre>{@code
 * // public class CoolTest {
 * //   @Rule public OpenTelemetryRule otelTesting = OpenTelemetryRule.create();
 * //
 * //   private Tracer tracer;
 * //   private Meter meter;
 * //
 * //   @Before
 * //   public void setUp() {
 * //     tracer = otelTesting.getOpenTelemetry().getTracer("test");
 * //     meter = otelTesting.getOpenTelemetry().getMeter("test");
 * //   }
 * //
 * //   @Test
 * //   public void test() {
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
public final class OpenTelemetryRule extends ExternalResource {

  /**
   * Returns a {@link OpenTelemetryRule} with a default SDK initialized with an in-memory span
   * exporter and W3C trace context propagation.
   */
  public static OpenTelemetryRule create() {
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
            .setPropagators(
                ContextPropagators.create(
                    TextMapPropagator.composite(
                        W3CTraceContextPropagator.getInstance(),
                        W3CBaggagePropagator.getInstance())))
            .setTracerProvider(tracerProvider)
            .setMeterProvider(meterProvider)
            .setLoggerProvider(loggerProvider)
            .build();

    return new OpenTelemetryRule(openTelemetry, spanExporter, metricReader, logRecordExporter);
  }

  private final OpenTelemetrySdk openTelemetry;
  private final InMemorySpanExporter spanExporter;
  private final InMemoryMetricReader metricReader;
  private final InMemoryLogRecordExporter logRecordExporter;

  private OpenTelemetryRule(
      OpenTelemetrySdk openTelemetry,
      InMemorySpanExporter spanExporter,
      InMemoryMetricReader metricReader,
      InMemoryLogRecordExporter logRecordExporter) {
    this.openTelemetry = openTelemetry;
    this.spanExporter = spanExporter;
    this.metricReader = metricReader;
    this.logRecordExporter = logRecordExporter;
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
   * Returns all the exported {@link LogRecordData} so far.
   *
   * @since 1.32.0
   */
  public List<LogRecordData> getLogRecords() {
    return new ArrayList<>(logRecordExporter.getFinishedLogRecordItems());
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

  /**
   * Clears the collected exported {@link LogRecordData}. Consider making your test smaller instead
   * of manually clearing state using this method.
   *
   * @since 1.32.0
   */
  public void clearLogRecords() {
    logRecordExporter.reset();
  }

  @Override
  protected void before() {
    GlobalOpenTelemetry.resetForTest();
    GlobalOpenTelemetry.set(openTelemetry);
    clearSpans();
    clearMetrics();
    clearLogRecords();
  }

  @Override
  protected void after() {
    GlobalOpenTelemetry.resetForTest();
  }
}
