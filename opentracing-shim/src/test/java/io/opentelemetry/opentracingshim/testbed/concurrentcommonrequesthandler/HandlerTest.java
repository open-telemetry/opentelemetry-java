/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed.concurrentcommonrequesthandler;

import static io.opentelemetry.opentracingshim.testbed.TestUtils.getOneByName;
import static io.opentelemetry.opentracingshim.testbed.TestUtils.sortByStartTime;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.SpanIdHex;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.opentracingshim.OpenTracingShim;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
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

  private final Tracer tracer = OpenTracingShim.createTracerShim(otelTesting.getOpenTelemetry());
  private final Client client = new Client(new RequestHandler(tracer));

  @Test
  void two_requests() throws Exception {
    Future<String> responseFuture = client.send("message");
    Future<String> responseFuture2 = client.send("message2");

    assertThat(responseFuture.get(15, TimeUnit.SECONDS)).isEqualTo("message:response");
    assertThat(responseFuture2.get(15, TimeUnit.SECONDS)).isEqualTo("message2:response");

    List<SpanData> finished = otelTesting.getSpans();
    assertThat(finished).hasSize(2);

    for (SpanData spanData : finished) {
      assertThat(spanData.getKind()).isEqualTo(SpanKind.CLIENT);
    }

    assertThat(finished.get(1).getTraceId()).isNotEqualTo(finished.get(0).getTraceId());
    assertThat(SpanIdHex.isValid(finished.get(0).getParentSpanId())).isFalse();
    assertThat(SpanIdHex.isValid(finished.get(1).getParentSpanId())).isFalse();

    assertThat(tracer.scopeManager().activeSpan()).isNull();
  }

  /** Active parent is not picked up by child. */
  @Test
  void parent_not_picked_up() throws Exception {
    Span parentSpan = tracer.buildSpan("parent").start();
    try (Scope parentScope = tracer.activateSpan(parentSpan)) {
      String response = client.send("no_parent").get(15, TimeUnit.SECONDS);
      assertThat(response).isEqualTo("no_parent:response");
    } finally {
      parentSpan.finish();
    }

    List<SpanData> finished = otelTesting.getSpans();
    assertThat(finished).hasSize(2);

    SpanData child = getOneByName(finished, RequestHandler.OPERATION_NAME);
    assertThat(child).isNotNull();

    SpanData parent = getOneByName(finished, "parent");
    assertThat(parent).isNotNull();

    // Here check that there is no parent-child relation although it should be because child is
    // created when parent is active
    assertThat(child.getParentSpanId()).isNotEqualTo(parent.getSpanId());
  }

  /**
   * Solution is bad because parent is per client (we don't have better choice). Therefore all
   * client requests will have the same parent. But if client is long living and injected/reused in
   * different places then initial parent will not be correct.
   */
  @Test
  void bad_solution_to_set_parent() throws Exception {
    Client client;
    Span parentSpan = tracer.buildSpan("parent").start();
    try (Scope parentScope = tracer.activateSpan(parentSpan)) {
      client = new Client(new RequestHandler(tracer, parentSpan.context()));
      String response = client.send("correct_parent").get(15, TimeUnit.SECONDS);
      assertThat(response).isEqualTo("correct_parent:response");
    } finally {
      parentSpan.finish();
    }

    // Send second request, now there is no active parent, but it will be set, ups
    String response = client.send("wrong_parent").get(15, TimeUnit.SECONDS);
    assertThat(response).isEqualTo("wrong_parent:response");

    List<SpanData> finished = otelTesting.getSpans();
    assertThat(finished).hasSize(3);

    finished = sortByStartTime(finished);

    SpanData parent = getOneByName(finished, "parent");
    assertThat(parent).isNotNull();

    // now there is parent/child relation between first and second span:
    assertThat(finished.get(1).getParentSpanId()).isEqualTo(parent.getSpanId());

    // third span should not have parent, but it has, damn it
    assertThat(finished.get(2).getParentSpanId()).isEqualTo(parent.getSpanId());
  }
}
