/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extensions.trace.testbed.latespanfinish;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporters.inmemory.InMemoryTracing;
import io.opentelemetry.sdk.extensions.trace.testbed.TestUtils;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

@SuppressWarnings("FutureReturnValueIgnored")
public final class LateSpanFinishTest {
  private final TracerSdkProvider sdk = TracerSdkProvider.builder().build();
  private final InMemoryTracing inMemoryTracing =
      InMemoryTracing.builder().setTracerSdkManagement(sdk).build();
  private final Tracer tracer = sdk.get(LateSpanFinishTest.class.getName());
  private final ExecutorService executor = Executors.newCachedThreadPool();

  @Test
  void test() throws Exception {
    // Create a Span manually and use it as parent of a pair of subtasks
    Span parentSpan = tracer.spanBuilder("parent").startSpan();
    submitTasks(parentSpan);

    // Wait for the threadpool to be done first, instead of polling/waiting
    executor.shutdown();
    executor.awaitTermination(15, TimeUnit.SECONDS);

    // Late-finish the parent Span now
    parentSpan.end();

    // Children finish order is not guaranteed, but parent should finish *last*.
    List<SpanData> spans = inMemoryTracing.getSpanExporter().getFinishedSpanItems();
    assertThat(spans).hasSize(3);
    assertThat(spans.get(0).getName()).startsWith("task");
    assertThat(spans.get(1).getName()).startsWith("task");
    assertThat(spans.get(2).getName()).isEqualTo("parent");

    TestUtils.assertSameTrace(spans);

    assertThat(Span.current()).isSameAs(Span.getInvalid());
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
          try (Scope scope = parentSpan.makeCurrent()) {
            Span childSpan = tracer.spanBuilder("task1").startSpan();
            try (Scope childScope = childSpan.makeCurrent()) {
              TestUtils.sleep(55);
            } finally {
              childSpan.end();
            }
          }
        });

    executor.submit(
        () -> {
          try (Scope scope = parentSpan.makeCurrent()) {
            Span childSpan = tracer.spanBuilder("task2").startSpan();
            try (Scope childScope = childSpan.makeCurrent()) {
              TestUtils.sleep(85);
            } finally {
              childSpan.end();
            }
          }
        });
  }
}
