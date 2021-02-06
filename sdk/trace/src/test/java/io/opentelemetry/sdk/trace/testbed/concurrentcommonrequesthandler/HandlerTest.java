/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.testbed.concurrentcommonrequesthandler;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanIdHex;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.testbed.TestUtils;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * There is only one instance of 'RequestHandler' per 'Client'. Methods of 'RequestHandler' are
 * executed concurrently in different threads which are reused (common pool). Therefore we cannot
 * use current active span and activate span. So one issue here is setting correct parent span.
 */
class HandlerTest {

  @RegisterExtension
  static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

  private final Tracer tracer =
      otelTesting.getOpenTelemetry().getTracer(HandlerTest.class.getName());

  private final Client client = new Client(new RequestHandler(tracer));

  @Test
  void two_requests() throws Exception {
    Future<String> responseFuture = client.send("message");
    Future<String> responseFuture2 = client.send("message2");

    assertThat(responseFuture.get(15, TimeUnit.SECONDS)).isEqualTo("message:response");
    assertThat(responseFuture2.get(15, TimeUnit.SECONDS)).isEqualTo("message2:response");

    List<SpanData> finished = otelTesting.getSpans();
    assertThat(finished).hasSize(2);

    for (SpanData spanProto : finished) {
      assertThat(spanProto.getKind()).isEqualTo(SpanKind.CLIENT);
    }

    assertThat(finished.get(0).getTraceId()).isNotEqualTo(finished.get(1).getTraceId());
    assertThat(finished.get(0).getParentSpanId()).isEqualTo(SpanIdHex.getInvalid());
    assertThat(finished.get(1).getParentSpanId()).isEqualTo(SpanIdHex.getInvalid());

    assertThat(Span.current()).isSameAs(Span.getInvalid());
  }

  /** Active parent is not picked up by child. */
  @Test
  void parent_not_picked_up() throws Exception {
    Span parentSpan = tracer.spanBuilder("parent").startSpan();
    try (Scope ignored = parentSpan.makeCurrent()) {
      String response = client.send("no_parent").get(15, TimeUnit.SECONDS);
      assertThat(response).isEqualTo("no_parent:response");
    } finally {
      parentSpan.end();
    }

    List<SpanData> finished = otelTesting.getSpans();
    assertThat(finished).hasSize(2);

    SpanData child = TestUtils.getOneByName(finished, RequestHandler.OPERATION_NAME);
    assertThat(child).isNotNull();

    SpanData parent = TestUtils.getOneByName(finished, "parent");
    assertThat(parent).isNotNull();

    // Here check that there is no parent-child relation although it should be because child is
    // created when parent is active
    assertThat(parent.getSpanId()).isNotEqualTo(child.getParentSpanId());
  }

  /**
   * Solution is bad because parent is per client (we don't have better choice). Therefore all
   * client requests will have the same parent. But if client is long living and injected/reused in
   * different places then initial parent will not be correct.
   */
  @Test
  void bad_solution_to_set_parent() throws Exception {
    Client client;
    Span parentSpan = tracer.spanBuilder("parent").startSpan();
    try (Scope ignored = parentSpan.makeCurrent()) {
      client = new Client(new RequestHandler(tracer, Context.current().with(parentSpan)));
      String response = client.send("correct_parent").get(15, TimeUnit.SECONDS);
      assertThat(response).isEqualTo("correct_parent:response");
    } finally {
      parentSpan.end();
    }

    // Send second request, now there is no active parent, but it will be set, ups
    String response = client.send("wrong_parent").get(15, TimeUnit.SECONDS);
    assertThat(response).isEqualTo("wrong_parent:response");

    List<SpanData> finished = otelTesting.getSpans();
    assertThat(finished).hasSize(3);

    finished = TestUtils.sortByStartTime(finished);

    SpanData parent = TestUtils.getOneByName(finished, "parent");
    assertThat(parent).isNotNull();

    // now there is parent/child relation between first and second span:
    assertThat(finished.get(1).getParentSpanId()).isEqualTo(parent.getSpanId());

    // third span should not have parent, but it has, damn it
    assertThat(finished.get(2).getParentSpanId()).isEqualTo(parent.getSpanId());
  }
}
