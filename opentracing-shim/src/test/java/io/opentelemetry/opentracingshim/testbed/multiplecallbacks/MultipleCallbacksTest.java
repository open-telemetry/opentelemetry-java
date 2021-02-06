/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed.multiplecallbacks;

import static io.opentelemetry.opentracingshim.testbed.TestUtils.finishedSpansSize;
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
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * These tests are intended to simulate a task with independent, asynchronous callbacks.
 *
 * <p>For improved readability, ignore the CountDownLatch lines as those are there to ensure
 * deterministic execution for the tests without sleeps.
 */
@SuppressWarnings("FutureReturnValueIgnored")
class MultipleCallbacksTest {
  @RegisterExtension
  static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

  private final Tracer tracer = OpenTracingShim.createTracerShim(otelTesting.getOpenTelemetry());

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

    await().atMost(Duration.ofSeconds(15)).until(finishedSpansSize(otelTesting), equalTo(4));

    List<SpanData> spans = otelTesting.getSpans();
    assertThat(spans).hasSize(4);
    assertThat(spans.get(0).getName()).isEqualTo("parent");

    SpanData parentSpan = spans.get(0);
    for (int i = 1; i < 4; i++) {
      assertThat(spans.get(i).getSpanContext().getTraceIdHex())
          .isEqualTo(parentSpan.getSpanContext().getTraceIdHex());
      assertThat(spans.get(i).getParentSpanContext().getSpanIdHex())
          .isEqualTo(parentSpan.getSpanContext().getSpanIdHex());
    }

    assertThat(tracer.scopeManager().activeSpan()).isNull();
  }
}
