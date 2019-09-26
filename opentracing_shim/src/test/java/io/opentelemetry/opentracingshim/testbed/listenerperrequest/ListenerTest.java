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

package io.opentelemetry.opentracingshim.testbed.listenerperrequest;

import static io.opentelemetry.opentracingshim.testbed.TestUtils.createTracerShim;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import io.opentelemetry.sdk.trace.export.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.SpanData;
import io.opentelemetry.trace.Span;
import io.opentracing.Tracer;
import java.util.List;
import org.junit.Test;

/** Each request has own instance of ResponseListener. */
public class ListenerTest {
  private final InMemorySpanExporter exporter = InMemorySpanExporter.create();
  private final Tracer tracer = createTracerShim(exporter);

  @Test
  public void test() throws Exception {
    Client client = new Client(tracer);
    Object response = client.send("message").get();
    assertEquals("message:response", response);

    List<SpanData> finished = exporter.getFinishedSpanItems();
    assertEquals(1, finished.size());
    assertEquals(finished.get(0).getKind(), Span.Kind.CLIENT);

    assertNull(tracer.scopeManager().activeSpan());
  }
}
