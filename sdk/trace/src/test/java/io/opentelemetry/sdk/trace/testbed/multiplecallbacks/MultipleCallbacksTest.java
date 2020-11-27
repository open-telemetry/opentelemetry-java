/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.testbed.multiplecallbacks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.testbed.TestUtils;
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

  private final Tracer tracer =
      otelTesting.getOpenTelemetry().getTracer(MultipleCallbacksTest.class.getName());

  @Test
  void test() {
    CountDownLatch parentDoneLatch = new CountDownLatch(1);
    Client client = new Client(tracer, parentDoneLatch);

    Span span = tracer.spanBuilder("parent").startSpan();
    try (Scope scope = span.makeCurrent()) {
      client.send("task1");
      client.send("task2");
      client.send("task3");
    } finally {
      span.end();
      parentDoneLatch.countDown();
    }

    await()
        .atMost(Duration.ofSeconds(15))
        .until(TestUtils.finishedSpansSize(otelTesting), equalTo(4));

    List<SpanData> spans = otelTesting.getSpans();
    assertThat(spans).hasSize(4);
    assertThat(spans.get(0).getName()).isEqualTo("parent");

    SpanData parentSpan = spans.get(0);
    for (int i = 1; i < 4; i++) {
      assertThat(spans.get(i).getTraceId()).isEqualTo(parentSpan.getTraceId());
      assertThat(spans.get(i).getParentSpanContext().getSpanIdAsHexString())
          .isEqualTo(parentSpan.getSpanId());
    }

    assertThat(Span.current()).isSameAs(Span.getInvalid());
  }
}
