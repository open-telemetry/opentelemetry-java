/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.junit5;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.propagation.HttpTraceContext;
import io.opentelemetry.context.propagation.DefaultContextPropagators;
import io.opentelemetry.exporter.inmemory.InMemorySpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.TracerSdkManagement;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
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
public class OpenTelemetryExtension
    implements BeforeEachCallback, BeforeAllCallback, AfterAllCallback {

  /**
   * Returns a {@link OpenTelemetryExtension} with a default SDK initialized with an in-memory span
   * exporter and W3C trace context propagation.
   */
  public static OpenTelemetryExtension create() {
    InMemorySpanExporter spanExporter = InMemorySpanExporter.create();

    TracerSdkProvider tracerProvider = TracerSdkProvider.builder().build();
    tracerProvider.addSpanProcessor(SimpleSpanProcessor.builder(spanExporter).build());

    OpenTelemetrySdk openTelemetry =
        OpenTelemetrySdk.builder()
            .setPropagators(
                DefaultContextPropagators.builder()
                    .addTextMapPropagator(HttpTraceContext.getInstance())
                    .build())
            .setTracerProvider(tracerProvider)
            .build();

    return new OpenTelemetryExtension(openTelemetry, spanExporter);
  }

  private final OpenTelemetrySdk openTelemetry;
  private final InMemorySpanExporter spanExporter;

  private OpenTelemetry previousGlobalOpenTelemetry;

  private OpenTelemetryExtension(
      OpenTelemetrySdk openTelemetry, InMemorySpanExporter spanExporter) {
    this.openTelemetry = openTelemetry;
    this.spanExporter = spanExporter;
  }

  /** Returns the {@link OpenTelemetrySdk} created by this extension. */
  public OpenTelemetry getOpenTelemetry() {
    return openTelemetry;
  }

  /** Returns the {@link TracerSdkManagement} created by this extension. */
  public TracerSdkManagement getTracerManagement() {
    return openTelemetry.getTracerManagement();
  }

  /** Returns all the exported {@link SpanData} so far. */
  public List<SpanData> getSpans() {
    return spanExporter.getFinishedSpanItems();
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
    previousGlobalOpenTelemetry = OpenTelemetry.get();
    OpenTelemetry.set(openTelemetry);
  }

  @Override
  public void afterAll(ExtensionContext context) {
    OpenTelemetry.set(previousGlobalOpenTelemetry);
  }
}
