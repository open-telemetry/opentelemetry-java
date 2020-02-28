/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.exporters.inmemory;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpansProcessor;
import javax.annotation.concurrent.Immutable;

/**
 * InMemoryTracing is an utility class that helps installing the {@link SimpleSpansProcessor} and an
 * instance of the {@link InMemorySpanExporter} to a given {@link TracerSdkProvider}. Can be used to
 * test OpenTelemetry integration.
 *
 * <p>Example usage:
 *
 * <pre><code>
 *   {@literal @}Test
 *   public void testCondition() {
 *     TracerSdkProvider tracerSdkProvider = TracerSdkProvider.builder().build()
 *     InMemoryTracing tracing =
 *         InMemoryTracing.builder().setTracerSdkProvider(tracerSdkProvider).build();
 *     Tracer tracer = tracerSdkProvider.getTracer("MyTestClass");
 *     tracer.spanBuilder("span").startSpan().end();
 *
 *     List&lt;io.opentelemetry.sdk.trace.data.SpanData&gt; spans = tracing.getFinishedSpanItems();
 *     assertThat(spans.size()).isEqualTo(1);
 *     assertThat(spans.get(0).getName()).isEqualTo("span");
 *   }
 * </code></pre>
 *
 * @since 0.1.0
 */
@AutoValue
@Immutable
public abstract class InMemoryTracing {
  /**
   * Returns the {@code TracerSdkProvider} passed during construction.
   *
   * @return the {@code TracerSdkProvider} passed during construction.
   * @since 0.1.0
   */
  abstract TracerSdkProvider getTracerProvider();

  /**
   * Returns the installed {@link InMemorySpanExporter}.
   *
   * @return the installed {@link InMemorySpanExporter}.
   * @since 0.1.0
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

  /**
   * Builder for {@link InMemoryTracing}.
   *
   * @since 0.3.0
   */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setTracerProvider(TracerSdkProvider tracerProvider);

    abstract Builder setSpanExporter(InMemorySpanExporter exporter);

    abstract TracerSdkProvider getTracerProvider();

    abstract InMemoryTracing autoBuild();

    /**
     * Builds a new {@link InMemoryTracing} with current settings.
     *
     * @return a {@code InMemoryTracing}.
     * @since 0.3.0
     */
    public final InMemoryTracing build() {
      InMemorySpanExporter exporter = InMemorySpanExporter.create();
      getTracerProvider().addSpanProcessor(SimpleSpansProcessor.newBuilder(exporter).build());
      return setSpanExporter(exporter).autoBuild();
    }
  }
}
