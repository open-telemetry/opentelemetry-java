/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed.clientserver;

import static io.opentelemetry.opentracingshim.testbed.TestUtils.finishedSpansSize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.opentracingshim.OpenTracingShim;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentracing.Tracer;
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
    client.send(false);
    verify();
  }

  @Test
  public void testUpperCaseKeys() throws Exception {
    Client client = new Client(queue, tracer);
    client.send(true);
    verify();
  }

  private void verify() {
    await().atMost(Duration.ofSeconds(15)).until(finishedSpansSize(otelTesting), equalTo(2));

    List<SpanData> finished = otelTesting.getSpans();
    assertThat(finished).hasSize(2);

    assertThat(finished.get(1).getSpanContext().getTraceIdHex())
        .isEqualTo(finished.get(0).getSpanContext().getTraceIdHex());
    SpanKind firstSpanKind = finished.get(0).getKind();
    switch (firstSpanKind) {
      case CLIENT:
        assertThat(finished.get(1).getKind()).isEqualTo(SpanKind.SERVER);
        break;
      case SERVER:
        assertThat(finished.get(1).getKind()).isEqualTo(SpanKind.CLIENT);
        break;
      default:
        fail("Unexpected first span kind: " + firstSpanKind);
    }

    assertThat(tracer.scopeManager().activeSpan()).isNull();
  }
}
