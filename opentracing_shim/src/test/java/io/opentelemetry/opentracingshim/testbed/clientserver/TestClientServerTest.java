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

package io.opentelemetry.opentracingshim.testbed.clientserver;

import static io.opentelemetry.opentracingshim.testbed.TestUtils.createTracerShim;
import static io.opentelemetry.opentracingshim.testbed.TestUtils.finishedSpansSize;
import static io.opentelemetry.opentracingshim.testbed.TestUtils.sortByStartTime;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import io.opentelemetry.sdk.trace.export.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.SpanData;
import io.opentelemetry.trace.Span.Kind;
import io.opentracing.Tracer;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestClientServerTest {

  private final InMemorySpanExporter exporter = InMemorySpanExporter.create();
  private final Tracer tracer = createTracerShim(exporter);
  private final ArrayBlockingQueue<Message> queue = new ArrayBlockingQueue<>(10);
  private Server server;

  @Before
  public void before() {
    server = new Server(queue, tracer);
    server.start();
  }

  @After
  public void after() throws InterruptedException {
    server.interrupt();
    server.join(5_000L);
  }

  @Test
  public void test() throws Exception {
    Client client = new Client(queue, tracer);
    client.send();

    await().atMost(15, TimeUnit.SECONDS).until(finishedSpansSize(exporter), equalTo(2));

    List<SpanData> finished = exporter.getFinishedSpanItems();
    assertEquals(2, finished.size());

    finished = sortByStartTime(finished);
    assertEquals(finished.get(0).getTraceId(), finished.get(1).getTraceId());
    assertEquals(Kind.CLIENT, finished.get(0).getKind());
    assertEquals(Kind.SERVER, finished.get(1).getKind());

    assertNull(tracer.scopeManager().activeSpan());
  }
}
