/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed.multiplecallbacks;

import static io.opentelemetry.opentracingshim.testbed.TestUtils.finishedSpansSize;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporters.inmemory.InMemoryTracing;
import io.opentelemetry.opentracingshim.OpenTracingShim;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/**
 * These tests are intended to simulate a task with independent, asynchronous callbacks.
 *
 * <p>For improved readability, ignore the CountDownLatch lines as those are there to ensure
 * deterministic execution for the tests without sleeps.
 */
@SuppressWarnings("FutureReturnValueIgnored")
class MultipleCallbacksTest {
  private final TracerSdkProvider sdk = TracerSdkProvider.builder().build();
  private final OpenTelemetry openTelemetry =
      OpenTelemetry.get().toBuilder().setTracerProvider(sdk).build();
  private final InMemoryTracing inMemoryTracing =
      InMemoryTracing.builder().setTracerSdkManagement(sdk).build();
  private final Tracer tracer = OpenTracingShim.createTracerShim(openTelemetry);

  @Test
  void test() {
    CountDownLatch parentDoneLatch = new CountDownLatch(1);
    Client client = new Client(tracer, parentDoneLatch);

    Span span = tracer.buildSpan("parent").start();
    try (Scope scope = tracer.activateSpan(span)) {
      client.send("task1");
      client.send("task2");
      client.send("task3");
    } finally {
      span.finish();
      parentDoneLatch.countDown();
    }

    await()
        .atMost(15, TimeUnit.SECONDS)
        .until(finishedSpansSize(inMemoryTracing.getSpanExporter()), equalTo(4));

    List<SpanData> spans = inMemoryTracing.getSpanExporter().getFinishedSpanItems();
    assertEquals(4, spans.size());
    assertEquals("parent", spans.get(0).getName());

    SpanData parentSpan = spans.get(0);
    for (int i = 1; i < 4; i++) {
      assertEquals(parentSpan.getTraceId(), spans.get(i).getTraceId());
      assertEquals(parentSpan.getSpanId(), spans.get(i).getParentSpanId());
    }

    assertNull(tracer.scopeManager().activeSpan());
  }
}
