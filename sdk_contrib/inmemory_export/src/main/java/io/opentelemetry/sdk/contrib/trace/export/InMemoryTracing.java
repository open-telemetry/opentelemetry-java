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

package io.opentelemetry.sdk.contrib.trace.export;

import io.opentelemetry.internal.Utils;
import io.opentelemetry.sdk.trace.TracerSdk;
import io.opentelemetry.sdk.trace.export.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.SimpleSampledSpansProcessor;
import io.opentelemetry.sdk.trace.export.SpanData;
import io.opentelemetry.trace.Tracer;
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
 *     {@code List<io.opentelemetry.proto.trace.v1.Span>} spans = tracing.getFinishedSpanItems();
 *     assertThat(spans.size()).isEqualTo(1);
 *     assertThat(spans.get(0).getName()).isEqualTo("span");
 *   }
 * </code></pre>
 *
 * @since 0.1.0
 */
public final class InMemoryTracing {
  private final TracerSdk tracer;
  private final InMemorySpanExporter exporter;

  /**
   * Creates a new {@code InMemoryTracing} with a new {@code TracerSdk}.
   *
   * @since 0.1.0
   */
  public InMemoryTracing() {
    this(new TracerSdk());
  }

  /**
   * Creates a new {@code InMemoryTracing} with the specified {@code TracerSdk}.
   *
   * @param tracer the {@code TracerSdk} to be used.
   * @throws NullPointerException if {@code tracer} is {@code null}.
   * @since 0.1.0
   */
  public InMemoryTracing(TracerSdk tracer) {
    Utils.checkNotNull(tracer, "tracer");

    this.tracer = tracer;
    this.exporter = InMemorySpanExporter.create();
    tracer.addSpanProcessor(SimpleSampledSpansProcessor.newBuilder(exporter).build());
  }

  /**
   * Returns a {@code Tracer} that can be used to create {@code Span}s and later recovered through
   * {@link #getFinishedSpanItems()}.
   *
   * @return the {@code Tracer} to be used to create {@code Span}s.
   * @since 0.1.0
   */
  public Tracer getTracer() {
    return tracer;
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
   * io.opentelemetry.proto.trace.v1.Span}.
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
    tracer.shutdown();
  }
}
