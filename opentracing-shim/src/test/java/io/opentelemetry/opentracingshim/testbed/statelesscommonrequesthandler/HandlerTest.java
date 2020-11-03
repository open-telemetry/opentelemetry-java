/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed.statelesscommonrequesthandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporters.inmemory.InMemoryTracing;
import io.opentelemetry.opentracingshim.OpenTracingShim;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentracing.Tracer;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * There is only one instance of 'RequestHandler' per 'Client'. Methods of 'RequestHandler' are
 * executed in the same thread (beforeRequest() and its resulting afterRequest(), that is).
 */
public final class HandlerTest {

  private final TracerSdkProvider sdk = TracerSdkProvider.builder().build();
  private final OpenTelemetry openTelemetry =
      OpenTelemetry.get().toBuilder().setTracerProvider(sdk).build();
  private final InMemoryTracing inMemoryTracing =
      InMemoryTracing.builder().setTracerSdkManagement(sdk).build();
  private final Tracer tracer = OpenTracingShim.createTracerShim(openTelemetry);
  private final Client client = new Client(new RequestHandler(tracer));

  @BeforeEach
  void before() {
    inMemoryTracing.getSpanExporter().reset();
  }

  @Test
  void test_requests() throws Exception {
    Future<String> responseFuture = client.send("message");
    Future<String> responseFuture2 = client.send("message2");
    Future<String> responseFuture3 = client.send("message3");

    assertEquals("message3:response", responseFuture3.get(5, TimeUnit.SECONDS));
    assertEquals("message2:response", responseFuture2.get(5, TimeUnit.SECONDS));
    assertEquals("message:response", responseFuture.get(5, TimeUnit.SECONDS));

    List<SpanData> finished = inMemoryTracing.getSpanExporter().getFinishedSpanItems();
    assertEquals(3, finished.size());
  }
}
