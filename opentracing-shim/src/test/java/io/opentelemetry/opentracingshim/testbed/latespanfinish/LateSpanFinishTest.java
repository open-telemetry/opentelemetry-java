/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed.latespanfinish;

import static io.opentelemetry.opentracingshim.testbed.TestUtils.assertSameTrace;
import static io.opentelemetry.opentracingshim.testbed.TestUtils.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.opentelemetry.opentracingshim.OpenTracingShim;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@SuppressWarnings("FutureReturnValueIgnored")
public final class LateSpanFinishTest {
  @RegisterExtension
  static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

  private final Tracer tracer = OpenTracingShim.createTracerShim(otelTesting.getOpenTelemetry());
  private final ExecutorService executor = Executors.newCachedThreadPool();

  @Test
  void test() throws Exception {
    // Create a Span manually and use it as parent of a pair of subtasks
    Span parentSpan = tracer.buildSpan("parent").start();
    submitTasks(parentSpan);

    // Wait for the threadpool to be done first, instead of polling/waiting
    executor.shutdown();
    executor.awaitTermination(15, TimeUnit.SECONDS);

    // Late-finish the parent Span now
    parentSpan.finish();

    // Children finish order is not guaranteed, but parent should finish *last*.
    List<SpanData> spans = otelTesting.getSpans();
    assertEquals(3, spans.size());
    assertTrue(spans.get(0).getName().startsWith("task"));
    assertTrue(spans.get(1).getName().startsWith("task"));
    assertEquals("parent", spans.get(2).getName());

    assertSameTrace(spans);

    assertNull(tracer.scopeManager().activeSpan());
  }

  /*
   * Fire away a few subtasks, passing a parent Span whose lifetime
   * is not tied at-all to the children
   */
  private void submitTasks(final Span parentSpan) {

    executor.submit(
        () -> {
          /* Alternative to calling activate() is to pass it manually to asChildOf() for each
           * created Span. */
          try (Scope scope = tracer.scopeManager().activate(parentSpan)) {
            Span childSpan = tracer.buildSpan("task1").start();
            try (Scope childScope = tracer.scopeManager().activate(childSpan)) {
              sleep(55);
            } finally {
              childSpan.finish();
            }
          }
        });

    executor.submit(
        () -> {
          try (Scope scope = tracer.scopeManager().activate(parentSpan)) {
            Span childSpan = tracer.buildSpan("task2").start();
            try (Scope childScope = tracer.scopeManager().activate(childSpan)) {
              sleep(85);
            } finally {
              childSpan.finish();
            }
          }
        });
  }
}
