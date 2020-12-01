/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.testbed.activespanreplacement;

import static io.opentelemetry.sdk.trace.testbed.TestUtils.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.testbed.TestUtils;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@SuppressWarnings("FutureReturnValueIgnored")
class ActiveSpanReplacementTest {

  @RegisterExtension
  static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

  private final Tracer tracer =
      otelTesting.getOpenTelemetry().getTracer(ActiveSpanReplacementTest.class.getName());
  private final ExecutorService executor = Executors.newCachedThreadPool();

  @Test
  void test() {
    // Start an isolated task and query for its result in another task/thread
    Span span = tracer.spanBuilder("initial").startSpan();
    try (Scope scope = span.makeCurrent()) {
      // Explicitly pass a Span to be finished once a late calculation is done.
      submitAnotherTask(span);
    }

    await()
        .atMost(Duration.ofSeconds(15))
        .until(TestUtils.finishedSpansSize(otelTesting), equalTo(3));

    List<SpanData> spans = otelTesting.getSpans();
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

    assertThat(Span.current()).isSameAs(Span.getInvalid());
  }

  private void submitAnotherTask(final Span initialSpan) {

    executor.submit(
        () -> {
          // Create a new Span for this task
          Span taskSpan = tracer.spanBuilder("task").startSpan();
          try (Scope scope = taskSpan.makeCurrent()) {

            // Simulate work strictly related to the initial Span
            // and finish it.
            try (Scope initialScope = initialSpan.makeCurrent()) {
              sleep(50);
            } finally {
              initialSpan.end();
            }

            // Restore the span for this task and create a subspan
            Span subTaskSpan = tracer.spanBuilder("subtask").startSpan();
            try (Scope subTaskScope = subTaskSpan.makeCurrent()) {
              sleep(50);
            } finally {
              subTaskSpan.end();
            }
          } finally {
            taskSpan.end();
          }
        });
  }
}
