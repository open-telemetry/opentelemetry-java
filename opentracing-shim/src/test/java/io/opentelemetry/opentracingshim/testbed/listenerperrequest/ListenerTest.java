/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed.listenerperrequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.exporters.inmemory.InMemoryTracing;
import io.opentelemetry.opentracingshim.OpenTracingShim;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentracing.Tracer;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Each request has own instance of ResponseListener. */
class ListenerTest {
  private final TracerSdkProvider sdk = TracerSdkProvider.builder().build();
  private final OpenTelemetry openTelemetry =
      OpenTelemetry.get().toBuilder().setTracerProvider(sdk).build();
  private final InMemoryTracing inMemoryTracing =
      InMemoryTracing.builder().setTracerSdkManagement(sdk).build();
  private final Tracer tracer = OpenTracingShim.createTracerShim(openTelemetry);

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
