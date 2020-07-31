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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.opentelemetry.exporters.inmemory.InMemoryTracing;
import io.opentelemetry.opentracingshim.TraceShim;
import io.opentelemetry.sdk.correlationcontext.CorrelationContextManagerSdk;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.trace.Span;
import io.opentracing.Tracer;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Each request has own instance of ResponseListener. */
class ListenerTest {
  private final TracerSdkProvider sdk = TracerSdkProvider.builder().build();
  private final InMemoryTracing inMemoryTracing =
      InMemoryTracing.builder().setTracerProvider(sdk).build();
  private final Tracer tracer = TraceShim.createTracerShim(sdk, new CorrelationContextManagerSdk());

  @Test
  void test() throws Exception {
    Client client = new Client(tracer);
    Object response = client.send("message").get();
    assertEquals("message:response", response);

    List<SpanData> finished = inMemoryTracing.getSpanExporter().getFinishedSpanItems();
    assertEquals(1, finished.size());
    assertEquals(finished.get(0).getKind(), Span.Kind.CLIENT);

    assertNull(tracer.scopeManager().activeSpan());
  }
}
