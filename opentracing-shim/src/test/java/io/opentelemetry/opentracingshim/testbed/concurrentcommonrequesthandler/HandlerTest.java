/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed.concurrentcommonrequesthandler;

import static io.opentelemetry.opentracingshim.testbed.TestUtils.getOneByName;
import static io.opentelemetry.opentracingshim.testbed.TestUtils.sortByStartTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.SpanId;
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

    assertEquals("message:response", responseFuture.get(15, TimeUnit.SECONDS));
    assertEquals("message2:response", responseFuture2.get(15, TimeUnit.SECONDS));

    List<SpanData> finished = otelTesting.getSpans();
    assertEquals(2, finished.size());

    for (SpanData spanData : finished) {
      assertEquals(Kind.CLIENT, spanData.getKind());
    }

    assertNotEquals(finished.get(0).getTraceId(), finished.get(1).getTraceId());
    assertFalse(SpanId.isValid(finished.get(0).getParentSpanId()));
    assertFalse(SpanId.isValid(finished.get(1).getParentSpanId()));

    assertNull(tracer.scopeManager().activeSpan());
  }

  /** Active parent is not picked up by child. */
  @Test
  void parent_not_picked_up() throws Exception {
    Span parentSpan = tracer.buildSpan("parent").start();
    try (Scope parentScope = tracer.activateSpan(parentSpan)) {
      String response = client.send("no_parent").get(15, TimeUnit.SECONDS);
      assertEquals("no_parent:response", response);
    } finally {
      parentSpan.finish();
    }

    List<SpanData> finished = otelTesting.getSpans();
    assertEquals(2, finished.size());

    SpanData child = getOneByName(finished, RequestHandler.OPERATION_NAME);
    assertNotNull(child);

    SpanData parent = getOneByName(finished, "parent");
    assertNotNull(parent);

    // Here check that there is no parent-child relation although it should be because child is
    // created when parent is active
    assertNotEquals(parent.getSpanId(), child.getParentSpanId());
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
      assertEquals("correct_parent:response", response);
    } finally {
      parentSpan.finish();
    }

    // Send second request, now there is no active parent, but it will be set, ups
    String response = client.send("wrong_parent").get(15, TimeUnit.SECONDS);
    assertEquals("wrong_parent:response", response);

    List<SpanData> finished = otelTesting.getSpans();
    assertEquals(3, finished.size());

    finished = sortByStartTime(finished);

    SpanData parent = getOneByName(finished, "parent");
    assertNotNull(parent);

    // now there is parent/child relation between first and second span:
    assertEquals(parent.getSpanId(), finished.get(1).getParentSpanId());

    // third span should not have parent, but it has, damn it
    assertEquals(parent.getSpanId(), finished.get(2).getParentSpanId());
  }
}
