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

import static io.opentelemetry.opentracingshim.testbed.TestUtils.createTracerShim;
import static io.opentelemetry.opentracingshim.testbed.TestUtils.getOneByName;
import static io.opentelemetry.opentracingshim.testbed.TestUtils.sortByStartTime;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import io.opentelemetry.proto.trace.v1.Span.SpanKind;
import io.opentelemetry.sdk.trace.export.InMemorySpanExporter;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

/**
 * There is only one instance of 'RequestHandler' per 'Client'. Methods of 'RequestHandler' are
 * executed concurrently in different threads which are reused (common pool). Therefore we cannot
 * use current active span and activate span. So one issue here is setting correct parent span.
 */
public class HandlerTest {

  private final InMemorySpanExporter exporter = new InMemorySpanExporter();
  private final Tracer tracer = createTracerShim(exporter);
  private final Client client = new Client(new RequestHandler(tracer));

  @Before
  public void before() {
    exporter.reset();
  }

  @Test
  public void two_requests() throws Exception {
    Future<String> responseFuture = client.send("message");
    Future<String> responseFuture2 = client.send("message2");

    assertEquals("message:response", responseFuture.get(15, TimeUnit.SECONDS));
    assertEquals("message2:response", responseFuture2.get(15, TimeUnit.SECONDS));

    List<io.opentelemetry.proto.trace.v1.Span> finished = exporter.getFinishedSpanItems();
    assertEquals(2, finished.size());

    for (io.opentelemetry.proto.trace.v1.Span spanData : finished) {
      assertEquals(SpanKind.CLIENT, spanData.getKind());
    }

    assertNotEquals(finished.get(0).getTraceId(), finished.get(1).getTraceId());
    assertTrue(finished.get(0).getParentSpanId().isEmpty());
    assertTrue(finished.get(1).getParentSpanId().isEmpty());

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

    List<io.opentelemetry.proto.trace.v1.Span> finished = exporter.getFinishedSpanItems();
    assertEquals(2, finished.size());

    io.opentelemetry.proto.trace.v1.Span child =
        getOneByName(finished, RequestHandler.OPERATION_NAME);
    assertNotNull(child);

    io.opentelemetry.proto.trace.v1.Span parent = getOneByName(finished, "parent");
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

    List<io.opentelemetry.proto.trace.v1.Span> finished = exporter.getFinishedSpanItems();
    assertEquals(3, finished.size());

    finished = sortByStartTime(finished);

    io.opentelemetry.proto.trace.v1.Span parent = getOneByName(finished, "parent");
    assertNotNull(parent);

    // now there is parent/child relation between first and second span:
    assertEquals(parent.getSpanId(), finished.get(1).getParentSpanId());

    // third span should not have parent, but it has, damn it
    assertEquals(parent.getSpanId(), finished.get(2).getParentSpanId());
  }
}
