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

package io.opentelemetry.opentracingshim.testbed.multiplecallbacks;

import static io.opentelemetry.opentracingshim.testbed.TestUtils.createTracerShim;
import static io.opentelemetry.opentracingshim.testbed.TestUtils.finishedSpansSize;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import io.opentelemetry.sdk.trace.export.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.SpanData;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

/**
 * These tests are intended to simulate a task with independent, asynchronous callbacks.
 *
 * <p>For improved readability, ignore the CountDownLatch lines as those are there to ensure
 * deterministic execution for the tests without sleeps.
 */
@SuppressWarnings("FutureReturnValueIgnored")
public class MultipleCallbacksTest {
  private final InMemorySpanExporter exporter = InMemorySpanExporter.create();
  private final Tracer tracer = createTracerShim(exporter);

  @Test
  public void test() throws Exception {
    CountDownLatch parentDoneLatch = new CountDownLatch(1);
    Client client = new Client(tracer, parentDoneLatch);

    Span span = tracer.buildSpan("parent").start();
    try (Scope scope = tracer.activateSpan(span)) {
      client.send("task1");
      client.send("task2");
      client.send("task3");
    } finally {
      span.finish();
      parentDoneLatch.countDown();
    }

    await().atMost(15, TimeUnit.SECONDS).until(finishedSpansSize(exporter), equalTo(4));

    List<SpanData> spans = exporter.getFinishedSpanItems();
    assertEquals(4, spans.size());
    assertEquals("parent", spans.get(0).getName());

    SpanData parentSpan = spans.get(0);
    for (int i = 1; i < 4; i++) {
      assertEquals(parentSpan.getTraceId(), spans.get(i).getTraceId());
      assertEquals(parentSpan.getSpanId(), spans.get(i).getParentSpanId());
    }

    assertNull(tracer.scopeManager().activeSpan());
  }
}
