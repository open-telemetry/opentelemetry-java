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

package io.opentelemetry.sdk.contrib.trace.testbed.activespanreplacement;

import static com.google.common.truth.Truth.assertThat;
import static io.opentelemetry.sdk.contrib.trace.testbed.TestUtils.createTracerShim;
import static io.opentelemetry.sdk.contrib.trace.testbed.TestUtils.finishedSpansSize;
import static io.opentelemetry.sdk.contrib.trace.testbed.TestUtils.sleep;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;

import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.trace.export.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.SpanData;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Tracer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

@SuppressWarnings("FutureReturnValueIgnored")
public class ActiveSpanReplacementTest {

  private final InMemorySpanExporter exporter = InMemorySpanExporter.create();
  private final Tracer tracer = createTracerShim(exporter);
  private final ExecutorService executor = Executors.newCachedThreadPool();

  @Test
  public void test() {
    // Start an isolated task and query for its result in another task/thread
    Span span = tracer.spanBuilder("initial").startSpan();
    try (Scope scope = tracer.withSpan(span)) {
      // Explicitly pass a Span to be finished once a late calculation is done.
      submitAnotherTask(span);
    }

    await().atMost(15, TimeUnit.SECONDS).until(finishedSpansSize(exporter), equalTo(3));

    List<SpanData> spans = exporter.getFinishedSpanItems();
    assertThat(spans).hasSize(3);
    assertThat(spans.get(0).getName()).isEqualTo("initial"); // Isolated task
    assertThat(spans.get(1).getName()).isEqualTo("subtask");
    assertThat(spans.get(2).getName()).isEqualTo("task");

    // task/subtask are part of the same trace, and subtask is a child of task
    assertThat(spans.get(1).getTraceId()).isEqualTo(spans.get(2).getTraceId());
    assertThat(spans.get(2).getSpanId()).isEqualTo(spans.get(1).getParentSpanId());

    // initial task is not related in any way to those two tasks
    assertThat(spans.get(0).getTraceId()).isNotEqualTo(spans.get(1).getTraceId());
    assertThat(spans.get(0).getParentSpanId()).isEqualTo(SpanId.getInvalid());

    assertThat(tracer.getCurrentSpan()).isSameInstanceAs(DefaultSpan.getInvalid());
  }

  private void submitAnotherTask(final Span initialSpan) {

    executor.submit(
        new Runnable() {
          @Override
          public void run() {
            // Create a new Span for this task
            Span taskSpan = tracer.spanBuilder("task").startSpan();
            try (Scope scope = tracer.withSpan(taskSpan)) {

              // Simulate work strictly related to the initial Span
              // and finish it.
              try (Scope initialScope = tracer.withSpan(initialSpan)) {
                sleep(50);
              } finally {
                initialSpan.end();
              }

              // Restore the span for this task and create a subspan
              Span subTaskSpan = tracer.spanBuilder("subtask").startSpan();
              try (Scope subTaskScope = tracer.withSpan(subTaskSpan)) {
                sleep(50);
              } finally {
                subTaskSpan.end();
              }
            } finally {
              taskSpan.end();
            }
          }
        });
  }
}
