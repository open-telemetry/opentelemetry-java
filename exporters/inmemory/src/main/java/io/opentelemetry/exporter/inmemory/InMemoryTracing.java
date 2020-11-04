/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.inmemory;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.propagation.HttpTraceContext;
import io.opentelemetry.context.propagation.DefaultContextPropagators;
import io.opentelemetry.sdk.trace.TracerSdkManagement;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import javax.annotation.concurrent.Immutable;

/**
 * InMemoryTracing is an utility class that helps installing the {@link SimpleSpanProcessor} and an
 * instance of the {@link InMemorySpanExporter} to a given {@link TracerSdkManagement}. Can be used
 * to test OpenTelemetry integration.
 *
 * <p>Example usage:
 *
 * <pre><code>
 *   {@literal @}Test
 *   public void testCondition() {
 *     TracerSdkProvider tracerSdkProvider = TracerSdkProvider.builder().build()
 *     InMemoryTracing tracing =
 *         InMemoryTracing.builder().setTracerSdkManagement(tracerSdkProvider).build();
 *     Tracer tracer = tracerSdkProvider.getTracer("MyTestClass");
 *     tracer.spanBuilder("span").startSpan().end();
 *
 *     List&lt;io.opentelemetry.sdk.trace.data.SpanData&gt; spans = tracing.getFinishedSpanItems();
 *     assertThat(spans.size()).isEqualTo(1);
 *     assertThat(spans.get(0).getName()).isEqualTo("span");
 *   }
 * </code></pre>
 */
@AutoValue
@Immutable
public abstract class InMemoryTracing {
  /**
   * Returns the {@code TracerSdkManagement} passed during construction.
   *
   * @return the {@code TracerSdkManagement} passed during construction.
   */
  abstract TracerSdkManagement getTracerSdkManagement();

  /**
   * Returns the installed {@link InMemorySpanExporter}.
   *
   * @return the installed {@link InMemorySpanExporter}.
   */
  public abstract InMemorySpanExporter getSpanExporter();

  /**
   * Returns a new {@link Builder} for {@link InMemoryTracing}.
   *
   * @return a new {@link Builder} for {@link InMemoryTracing}.
   */
  public static Builder builder() {
    return new AutoValue_InMemoryTracing.Builder();
  }

  /** Builder for {@link InMemoryTracing}. */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setTracerSdkManagement(TracerSdkManagement tracerSdkManagement);

    abstract Builder setSpanExporter(InMemorySpanExporter exporter);

    abstract TracerSdkManagement getTracerSdkManagement();

    abstract InMemoryTracing autoBuild();

    /**
     * Builds a new {@link InMemoryTracing} with current settings.
     *
     * @return a {@code InMemoryTracing}.
     */
    public final InMemoryTracing build() {
      // install the HttpTraceContext propagator into the API for testing with.
      OpenTelemetry.setGlobalPropagators(
          DefaultContextPropagators.builder()
              .addTextMapPropagator(HttpTraceContext.getInstance())
              .build());
      InMemorySpanExporter exporter = InMemorySpanExporter.create();
      getTracerSdkManagement().addSpanProcessor(SimpleSpanProcessor.builder(exporter).build());
      return setSpanExporter(exporter).autoBuild();
    }
  }
}
