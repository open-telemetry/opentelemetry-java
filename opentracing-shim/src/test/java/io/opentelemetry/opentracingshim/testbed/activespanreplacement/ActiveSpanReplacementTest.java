/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed.activespanreplacement;

import static io.opentelemetry.api.trace.SpanId.isValid;
import static io.opentelemetry.opentracingshim.testbed.TestUtils.finishedSpansSize;
import static io.opentelemetry.opentracingshim.testbed.TestUtils.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;

import io.opentelemetry.opentracingshim.OpenTracingShim;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
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

  private final Tracer tracer = OpenTracingShim.createTracerShim(otelTesting.getOpenTelemetry());
  private final ExecutorService executor = Executors.newCachedThreadPool();

  @Test
  void test() throws Exception {
    // Start an isolated task and query for its result in another task/thread
    Span span = tracer.buildSpan("initial").start();
    try (Scope scope = tracer.scopeManager().activate(span)) {
      // Explicitly pass a Span to be finished once a late calculation is done.
      submitAnotherTask(span);
    }

    await().atMost(Duration.ofSeconds(15)).until(finishedSpansSize(otelTesting), equalTo(3));

    List<SpanData> spans = otelTesting.getSpans();
    assertThat(spans).hasSize(3);
    assertThat(spans.get(0).getName()).isEqualTo("initial"); // Isolated task
    assertThat(spans.get(1).getName()).isEqualTo("subtask");
    assertThat(spans.get(2).getName()).isEqualTo("task");

    // task/subtask are part of the same trace, and subtask is a child of task
    assertThat(spans.get(2).getSpanContext().getTraceIdHex())
        .isEqualTo(spans.get(1).getSpanContext().getTraceIdHex());
    assertThat(spans.get(1).getParentSpanContext().getSpanIdHex())
        .isEqualTo(spans.get(2).getSpanContext().getSpanIdHex());

    // initial task is not related in any way to those two tasks
    assertThat(spans.get(1).getSpanContext().getTraceIdHex())
        .isNotEqualTo(spans.get(0).getSpanContext().getTraceIdHex());
    assertThat(isValid(spans.get(0).getParentSpanContext().getSpanIdHex())).isFalse();

    assertThat(tracer.scopeManager().activeSpan()).isNull();
  }

  private void submitAnotherTask(final Span initialSpan) {

    executor.submit(
        () -> {
          // Create a new Span for this task
          Span taskSpan = tracer.buildSpan("task").start();
          try (Scope scope = tracer.scopeManager().activate(taskSpan)) {

            // Simulate work strictly related to the initial Span
            // and finish it.
            try (Scope initialScope = tracer.scopeManager().activate(initialSpan)) {
              sleep(50);
            } finally {
              initialSpan.finish();
            }

            // Restore the span for this task and create a subspan
            Span subTaskSpan = tracer.buildSpan("subtask").start();
            try (Scope subTaskScope = tracer.scopeManager().activate(subTaskSpan)) {
              sleep(50);
            } finally {
              subTaskSpan.finish();
            }
          } finally {
            taskSpan.finish();
          }
        });
  }
}
