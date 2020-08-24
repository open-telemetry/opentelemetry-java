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

import io.opentelemetry.sdk.common.export.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A {@link SpanExporter} implementation that can be used to test OpenTelemetry integration.
 *
 * <p>Example usage:
 *
 * <pre><code>
 * class MyClassTest {
 *   private final Tracer tracer = new TracerSdk();
 *   private final InMemorySpanExporter testExporter = InMemorySpanExporter.create();
 *
 *   {@literal @}Before
 *   public void setup() {
 *     tracer.addSpanProcessor(SimpleSampledSpansProcessor.newBuilder(testExporter).build());
 *   }
 *
 *   {@literal @}Test
 *   public void getFinishedSpanData() {
 *     tracer.spanBuilder("span").startSpan().end();
 *
 *     List&lt;Span&gt; spanItems = exporter.getFinishedSpanItems();
 *     assertThat(spanItems).isNotNull();
 *     assertThat(spanItems.size()).isEqualTo(1);
 *     assertThat(spanItems.get(0).getName()).isEqualTo("span");
 *   }
 * </code></pre>
 */
public final class InMemorySpanExporter implements SpanExporter {
  private final List<SpanData> finishedSpanItems = new ArrayList<>();
  private boolean isStopped = false;

  /**
   * Returns a new instance of the {@code InMemorySpanExporter}.
   *
   * @return a new instance of the {@code InMemorySpanExporter}.
   */
  public static InMemorySpanExporter create() {
    return new InMemorySpanExporter();
  }

  /**
   * Returns a {@code List} of the finished {@code Span}s, represented by {@code SpanData}.
   *
   * @return a {@code List} of the finished {@code Span}s.
   */
  public List<SpanData> getFinishedSpanItems() {
    synchronized (this) {
      return Collections.unmodifiableList(new ArrayList<>(finishedSpanItems));
    }
  }

  /**
   * Clears the internal {@code List} of finished {@code Span}s.
   *
   * <p>Does not reset the state of this exporter if already shutdown.
   */
  public void reset() {
    synchronized (this) {
      finishedSpanItems.clear();
    }
  }

  @Override
  public CompletableResultCode export(Collection<SpanData> spans) {
    synchronized (this) {
      if (isStopped) {
        return CompletableResultCode.ofFailure();
      }
      finishedSpanItems.addAll(spans);
    }
    return CompletableResultCode.ofSuccess();
  }

  /**
   * The InMemory exporter does not batch spans, so this method will immediately return with
   * success.
   *
   * @return always Success
   */
  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public void shutdown() {
    synchronized (this) {
      finishedSpanItems.clear();
      isStopped = true;
    }
  }

  private InMemorySpanExporter() {}
}
