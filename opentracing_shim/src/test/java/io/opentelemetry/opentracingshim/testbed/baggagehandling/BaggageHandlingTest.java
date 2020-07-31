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

package io.opentelemetry.opentracingshim.testbed.baggagehandling;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.opentelemetry.exporters.inmemory.InMemoryTracing;
import io.opentelemetry.opentracingshim.TraceShim;
import io.opentelemetry.sdk.correlationcontext.CorrelationContextManagerSdk;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentracing.Span;
import io.opentracing.Tracer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

public final class BaggageHandlingTest {
  private final TracerSdkProvider sdk = TracerSdkProvider.builder().build();
  private final InMemoryTracing inMemoryTracing =
      InMemoryTracing.builder().setTracerProvider(sdk).build();
  private final Tracer tracer = TraceShim.createTracerShim(sdk, new CorrelationContextManagerSdk());
  private final ExecutorService executor = Executors.newCachedThreadPool();

  @Test
  void test_multithreaded() throws Exception {
    final Span span = tracer.buildSpan("one").start();
    span.setBaggageItem("key1", "value1");

    Future<?> f =
        executor.submit(
            () -> {
              /* Override the previous value... */
              span.setBaggageItem("key1", "value2");

              /* add a new baggage item... */
              span.setBaggageItem("newkey", "newvalue");

              /* have a child that updates its own baggage
               * (should not be reflected in the original Span). */
              Span childSpan = tracer.buildSpan("child").start();
              try {
                childSpan.setBaggageItem("key1", "childvalue");
              } finally {
                childSpan.finish();
              }

              /* and finish the Span. */
              span.finish();
            });

    /* Single call, no need to use await() */
    f.get(5, TimeUnit.SECONDS);

    assertEquals(2, inMemoryTracing.getSpanExporter().getFinishedSpanItems().size());
    assertEquals(span.getBaggageItem("key1"), "value2");
    assertEquals(span.getBaggageItem("newkey"), "newvalue");
  }
}
