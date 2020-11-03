/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed.baggagehandling;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.opentelemetry.opentracingshim.OpenTracingShim;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentracing.Span;
import io.opentracing.Tracer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public final class BaggageHandlingTest {
  @RegisterExtension
  static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

  private final Tracer tracer = OpenTracingShim.createTracerShim(otelTesting.getOpenTelemetry());
  private final ExecutorService executor = Executors.newCachedThreadPool();

  @Test
  void test_multithreaded() throws Exception {
    final Span span = tracer.buildSpan("one").start();
    span.setBaggageItem("key1", "value1");

    Future<?> f =
        executor.submit(
            () -> {
              /* Override the previous value... */
              span.setBaggageItem("key1", "value2");

              /* add a new baggage item... */
              span.setBaggageItem("newkey", "newvalue");

              /* have a child that updates its own baggage
               * (should not be reflected in the original Span). */
              Span childSpan = tracer.buildSpan("child").start();
              try {
                childSpan.setBaggageItem("key1", "childvalue");
              } finally {
                childSpan.finish();
              }

              /* and finish the Span. */
              span.finish();
            });

    /* Single call, no need to use await() */
    f.get(5, TimeUnit.SECONDS);

    assertEquals(2, otelTesting.getSpans().size());
    assertEquals(span.getBaggageItem("key1"), "value2");
    assertEquals(span.getBaggageItem("newkey"), "newvalue");
  }
}
