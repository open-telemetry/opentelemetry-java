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

package io.opentelemetry.sdk.contrib.trace.testbed.concurrentcommonrequesthandler;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.contrib.trace.testbed.TestUtils;
import io.opentelemetry.sdk.trace.export.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.SpanData;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Tracer;
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

  private final InMemorySpanExporter exporter = InMemorySpanExporter.create();
  private final Tracer tracer = TestUtils.createTracerShim(exporter);
  private final Client client = new Client(new RequestHandler(tracer));

  @Before
  public void before() {
    exporter.reset();
  }

  @Test
  public void two_requests() throws Exception {
    Future<String> responseFuture = client.send("message");
    Future<String> responseFuture2 = client.send("message2");

    assertThat(responseFuture.get(15, TimeUnit.SECONDS)).isEqualTo("message:response");
    assertThat(responseFuture2.get(15, TimeUnit.SECONDS)).isEqualTo("message2:response");

    List<SpanData> finished = exporter.getFinishedSpanItems();
    assertThat(finished).hasSize(2);

    for (SpanData spanProto : finished) {
      assertThat(spanProto.getKind()).isEqualTo(Span.Kind.CLIENT);
    }

    assertThat(finished.get(0).getTraceId()).isNotEqualTo(finished.get(1).getTraceId());
    assertThat(finished.get(0).getParentSpanId()).isEqualTo(SpanId.getInvalid());
    assertThat(finished.get(1).getParentSpanId()).isEqualTo(SpanId.getInvalid());

    assertThat(tracer.getCurrentSpan()).isSameInstanceAs(DefaultSpan.getInvalid());
  }

  /** Active parent is not picked up by child. */
  @Test
  public void parent_not_picked_up() throws Exception {
    Span parentSpan = tracer.spanBuilder("parent").startSpan();
    try (Scope ignored = tracer.withSpan(parentSpan)) {
      String response = client.send("no_parent").get(15, TimeUnit.SECONDS);
      assertThat(response).isEqualTo("no_parent:response");
    } finally {
      parentSpan.end();
    }

    List<SpanData> finished = exporter.getFinishedSpanItems();
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
  public void bad_solution_to_set_parent() throws Exception {
    Client client;
    Span parentSpan = tracer.spanBuilder("parent").startSpan();
    try (Scope ignored = tracer.withSpan(parentSpan)) {
      client = new Client(new RequestHandler(tracer, parentSpan.getContext()));
      String response = client.send("correct_parent").get(15, TimeUnit.SECONDS);
      assertThat(response).isEqualTo("correct_parent:response");
    } finally {
      parentSpan.end();
    }

    // Send second request, now there is no active parent, but it will be set, ups
    String response = client.send("wrong_parent").get(15, TimeUnit.SECONDS);
    assertThat(response).isEqualTo("wrong_parent:response");

    List<SpanData> finished = exporter.getFinishedSpanItems();
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
