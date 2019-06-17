/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.opentracingshim.testbed.concurrentcommonrequesthandler;

import static io.opentelemetry.opentracingshim.testbed.TestUtils.sortByStartTime;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import io.opentelemetry.opentracingshim.InMemoryTracer;
import io.opentelemetry.opentracingshim.TraceShim;
import io.opentelemetry.trace.SpanData;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;

/**
 * There is only one instance of 'RequestHandler' per 'Client'. Methods of 'RequestHandler' are
 * executed concurrently in different threads which are reused (common pool). Therefore we cannot
 * use current active span and activate span. So one issue here is setting correct parent span.
 */
public class HandlerTest {

  private final InMemoryTracer mockTracer = new InMemoryTracer();
  private final Tracer tracer = TraceShim.createTracerShim(mockTracer);
  private final Client client = new Client(new RequestHandler(tracer));

  @Before
  public void before() {
    mockTracer.reset();
  }

  @Test
  public void two_requests() throws Exception {
    Future<String> responseFuture = client.send("message");
    Future<String> responseFuture2 = client.send("message2");

    assertEquals("message:response", responseFuture.get(15, TimeUnit.SECONDS));
    assertEquals("message2:response", responseFuture2.get(15, TimeUnit.SECONDS));

    List<SpanData> finished = mockTracer.getFinishedSpanDataItems();
    assertEquals(2, finished.size());

    for (SpanData spanData : finished) {
      assertEquals(io.opentelemetry.trace.Span.Kind.CLIENT, spanData.getKind());
    }

    assertNotEquals(
        finished.get(0).getContext().getTraceId(), finished.get(1).getContext().getTraceId());
    assertEquals(null, finished.get(0).getParentSpanId());
    assertEquals(null, finished.get(1).getParentSpanId());

    assertNull(tracer.scopeManager().activeSpan());
  }

  /** Active parent is not picked up by child. */
  @Test
  public void parent_not_picked_up() throws Exception {
    Span parentSpan = tracer.buildSpan("parent").start();
    try (Scope parentScope = tracer.activateSpan(parentSpan)) {
      String response = client.send("no_parent").get(15, TimeUnit.SECONDS);
      assertEquals("no_parent:response", response);
    } finally {
      parentSpan.finish();
    }

    List<SpanData> finished = mockTracer.getFinishedSpanDataItems();
    assertEquals(2, finished.size());

    SpanData child = getOneByOperationName(finished, RequestHandler.OPERATION_NAME);
    assertNotNull(child);

    SpanData parent = getOneByOperationName(finished, "parent");
    assertNotNull(parent);

    // Here check that there is no parent-child relation although it should be because child is
    // created when parent is active
    assertNotEquals(parent.getContext().getSpanId(), child.getParentSpanId());
  }

  /**
   * Solution is bad because parent is per client (we don't have better choice). Therefore all
   * client requests will have the same parent. But if client is long living and injected/reused in
   * different places then initial parent will not be correct.
   */
  @Test
  public void bad_solution_to_set_parent() throws Exception {
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

    List<SpanData> finished = mockTracer.getFinishedSpanDataItems();
    assertEquals(3, finished.size());

    sortByStartTime(finished);

    SpanData parent = getOneByOperationName(finished, "parent");
    assertNotNull(parent);

    // now there is parent/child relation between first and second span:
    assertEquals(parent.getContext().getSpanId(), finished.get(1).getParentSpanId());

    // third span should not have parent, but it has, damn it
    assertEquals(parent.getContext().getSpanId(), finished.get(2).getParentSpanId());
  }

  @Nullable
  private static SpanData getOneByOperationName(List<SpanData> spans, String name) {
    SpanData found = null;
    for (SpanData span : spans) {
      if (name.equals(span.getName())) {
        if (found != null) {
          throw new IllegalArgumentException(
              "there is more than one span with operation name '" + name + "'");
        }
        found = span;
      }
    }
    return found;
  }
}
