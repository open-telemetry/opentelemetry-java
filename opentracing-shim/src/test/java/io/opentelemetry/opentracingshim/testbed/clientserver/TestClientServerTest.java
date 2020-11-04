/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed.clientserver;

import static io.opentelemetry.opentracingshim.testbed.TestUtils.finishedSpansSize;
import static io.opentelemetry.opentracingshim.testbed.TestUtils.sortByStartTime;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.opentracingshim.OpenTracingShim;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentracing.Tracer;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class TestClientServerTest {
  @RegisterExtension
  static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

  private final Tracer tracer = OpenTracingShim.createTracerShim(otelTesting.getOpenTelemetry());
  private final ArrayBlockingQueue<Message> queue = new ArrayBlockingQueue<>(10);
  private Server server;

  @BeforeEach
  void before() {
    server = new Server(queue, tracer);
    server.start();
  }

  @AfterEach
  void after() throws InterruptedException {
    server.interrupt();
    server.join(5_000L);
  }

  @Test
  void test() throws Exception {
    Client client = new Client(queue, tracer);
    client.send();

    await().atMost(15, TimeUnit.SECONDS).until(finishedSpansSize(otelTesting), equalTo(2));

    List<SpanData> finished = otelTesting.getSpans();
    assertEquals(2, finished.size());

    finished = sortByStartTime(finished);
    assertEquals(finished.get(0).getTraceId(), finished.get(1).getTraceId());
    assertEquals(Kind.CLIENT, finished.get(0).getKind());
    assertEquals(Kind.SERVER, finished.get(1).getKind());

    assertNull(tracer.scopeManager().activeSpan());
  }
}
