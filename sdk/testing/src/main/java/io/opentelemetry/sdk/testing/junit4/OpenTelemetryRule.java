/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.junit4;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerManagement;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.util.List;
import org.junit.rules.ExternalResource;

/**
 * A JUnit4 rule which sets up the {@link OpenTelemetrySdk} for testing, resetting state between
 * tests. This rule cannot be used with {@link org.junit.ClassRule}.
 *
 * <pre>{@code
 * > public class CoolTest {
 * >   {@literal @}Rule
 * >   public OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();
 * >
 * >   private Tracer tracer;
 * >
 * >   {@literal @}Before
 * >   public void setUp() {
 * >       tracer = otelTesting.getOpenTelemetry().getTracer("test");
 * >   }
 * >
 * >   {@literal @}Test
 * >   public void test() {
 * >     tracer.spanBuilder("name").startSpan().end();
 * >     assertThat(otelTesting.getSpans()).containsExactly(expected);
 * >   }
 * >  }
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

    OpenTelemetrySdk openTelemetry =
        OpenTelemetrySdk.builder()
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .setTracerProvider(tracerProvider)
            .build();

    return new OpenTelemetryRule(openTelemetry, spanExporter);
  }

  private final OpenTelemetrySdk openTelemetry;
  private final InMemorySpanExporter spanExporter;

  private volatile OpenTelemetry previousGlobalOpenTelemetry;

  private OpenTelemetryRule(OpenTelemetrySdk openTelemetry, InMemorySpanExporter spanExporter) {
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
   * Clears the collected exported {@link SpanData}. Consider making your test smaller instead of
   * manually clearing state using this method.
   */
  public void clearSpans() {
    spanExporter.reset();
  }

  @Override
  protected void before() {
    previousGlobalOpenTelemetry = GlobalOpenTelemetry.get();
    GlobalOpenTelemetry.set(openTelemetry);
    clearSpans();
  }

  @Override
  protected void after() {
    GlobalOpenTelemetry.set(previousGlobalOpenTelemetry);
  }
}
