/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.testbed.clientserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.testbed.TestUtils;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class TestClientServerTest {

  @RegisterExtension
  static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

  private final Tracer tracer =
      otelTesting.getOpenTelemetry().getTracer(TestClientServerTest.class.getName());
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

    await()
        .atMost(Duration.ofSeconds(15))
        .until(TestUtils.finishedSpansSize(otelTesting), equalTo(2));

    List<SpanData> finished = otelTesting.getSpans();
    assertThat(finished).hasSize(2);

    finished = TestUtils.sortByStartTime(finished);
    if (!finished.get(0).getKind().equals(SpanKind.CLIENT)) {
      SpanData serverData = finished.get(0);
      finished.set(0, finished.get(1));
      finished.set(1, serverData);
    }
    assertThat(finished.get(0).getTraceId()).isEqualTo(finished.get(1).getTraceId());
    assertThat(finished.get(0).getKind()).isEqualTo(SpanKind.CLIENT);
    assertThat(finished.get(1).getKind()).isEqualTo(SpanKind.SERVER);

    assertThat(Span.current()).isSameAs(Span.getInvalid());
  }
}
