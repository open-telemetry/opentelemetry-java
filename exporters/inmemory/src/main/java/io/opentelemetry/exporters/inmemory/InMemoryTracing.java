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

import io.opentelemetry.internal.Utils;
import io.opentelemetry.sdk.trace.SpanData;
import io.opentelemetry.sdk.trace.TracerSdkFactory;
import io.opentelemetry.sdk.trace.export.SimpleSpansProcessor;
import java.util.List;

/**
 * InMemoryTracing is an utility class that uses a {@code TracerSdk} to create {@code Span}s and
 * later expose the finished items in memory. Can be used to test OpenTelemetry integration.
 *
 * <p>Example usage:
 *
 * <pre><code>
 *   {@literal @}Test
 *   public void testCondition() {
 *     InMemoryTracing tracing = new InMemoryTracing();
 *     Tracer tracer = tracing.getTracer();
 *     tracer.spanBuilder("span").startSpan().end();
 *
 *     List&lt;io.opentelemetry.sdk.trace.SpanData&gt; spans = tracing.getFinishedSpanItems();
 *     assertThat(spans.size()).isEqualTo(1);
 *     assertThat(spans.get(0).getName()).isEqualTo("span");
 *   }
 * </code></pre>
 *
 * @since 0.1.0
 */
public final class InMemoryTracing {
  private final TracerSdkFactory tracerSdkFactory;
  private final InMemorySpanExporter exporter;

  /**
   * Creates a new {@code InMemoryTracing} with a new {@code TracerSdk}.
   *
   * @since 0.1.0
   */
  public InMemoryTracing() {
    this(TracerSdkFactory.create());
  }

  /**
   * Creates a new {@code InMemoryTracing} with the specified {@code TracerSdk}.
   *
   * @param tracerSdkFactory the {@code TracerSdkFactory} to be used.
   * @throws NullPointerException if {@code tracer} is {@code null}.
   * @since 0.1.0
   */
  public InMemoryTracing(TracerSdkFactory tracerSdkFactory) {
    Utils.checkNotNull(tracerSdkFactory, "tracerSdkFactory");

    this.tracerSdkFactory = tracerSdkFactory;
    this.exporter = InMemorySpanExporter.create();
    tracerSdkFactory.addSpanProcessor(SimpleSpansProcessor.newBuilder(exporter).build());
  }

  /**
   * Returns a {@code Tracer} that can be used to create {@code Span}s and later recovered through
   * {@link #getFinishedSpanItems()}.
   *
   * @return the {@code Tracer} to be used to create {@code Span}s.
   * @since 0.1.0
   */
  public TracerSdkFactory getTracerFactory() {
    return tracerSdkFactory;
  }

  /**
   * Clears the internal {@code List} of finished {@code Span}s.
   *
   * @since 0.1.0
   */
  public void reset() {
    exporter.reset();
  }

  /**
   * Returns a copy {@code List} of the finished {@code Span}s, represented by {@code
   * io.opentelemetry.sdk.trace.SpanData}.
   *
   * @return a {@code List} of the finished {@code Span}s.
   * @since 0.1.0
   */
  public List<SpanData> getFinishedSpanItems() {
    return exporter.getFinishedSpanItems();
  }

  /**
   * Attemps to stop all activity for the underlying tracer by calling {@code TracerSdk.shutdown()}.
   *
   * @since 0.1.0
   */
  public void shutdown() {
    tracerSdkFactory.shutdown();
  }
}
