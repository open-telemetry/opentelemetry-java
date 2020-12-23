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
import io.opentelemetry.sdk.testing.assertj.TracesAssert;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerManagement;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * A JUnit5 extension which sets up the {@link OpenTelemetrySdk} for testing, resetting state
 * between tests.
 *
 * <pre>{@code
 * > class CoolTest {
 * >   {@literal @}RegisterExtension
 * >   static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();
 * >
 * >   private final Tracer tracer = otelTesting.getOpenTelemetry().getTracer("test");
 * >
 * >   {@literal @}Test
 * >   void test() {
 * >     tracer.spanBuilder("name").startSpan().end();
 * >     assertThat(otelTesting.getSpans()).containsExactly(expected);
 * >   }
 * >  }
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

    OpenTelemetrySdk openTelemetry =
        OpenTelemetrySdk.builder()
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .setTracerProvider(tracerProvider)
            .build();

    return new OpenTelemetryExtension(openTelemetry, spanExporter);
  }

  private final OpenTelemetrySdk openTelemetry;
  private final InMemorySpanExporter spanExporter;

  private volatile OpenTelemetry previousGlobalOpenTelemetry;

  private OpenTelemetryExtension(
      OpenTelemetrySdk openTelemetry, InMemorySpanExporter spanExporter) {
    this.openTelemetry = openTelemetry;
    this.spanExporter = spanExporter;
  }

  /** Returns the {@link OpenTelemetrySdk} created by this extension. */
  public OpenTelemetry getOpenTelemetry() {
    return openTelemetry;
  }

  /** Returns the {@link SdkTracerManagement} created by this extension. */
  public SdkTracerManagement getTracerManagement() {
    return openTelemetry.getTracerManagement();
  }

  /** Returns all the exported {@link SpanData} so far. */
  public List<SpanData> getSpans() {
    return spanExporter.getFinishedSpanItems();
  }

  /**
   * Returns a {@link TracesAssert} for asserting on the currently exported traces. This method
   * requires AssertJ to be on the classpath.
   */
  public TracesAssert assertTraces() {
    Map<String, List<SpanData>> traces =
        getSpans().stream()
            .collect(
                Collectors.groupingBy(
                    SpanData::getTraceId, LinkedHashMap::new, Collectors.toList()));
    for (List<SpanData> trace : traces.values()) {
      trace.sort(Comparator.comparing(SpanData::getStartEpochNanos));
    }
    return assertThat(traces.values());
  }

  /**
   * Clears the collected exported {@link SpanData}. Consider making your test smaller instead of
   * manually clearing state using this method.
   */
  public void clearSpans() {
    spanExporter.reset();
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    clearSpans();
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    previousGlobalOpenTelemetry = GlobalOpenTelemetry.get();
    GlobalOpenTelemetry.set(openTelemetry);
  }

  @Override
  public void afterAll(ExtensionContext context) {
    GlobalOpenTelemetry.set(previousGlobalOpenTelemetry);
  }
}
