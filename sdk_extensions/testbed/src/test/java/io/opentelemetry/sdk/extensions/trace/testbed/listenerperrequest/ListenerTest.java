/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extensions.trace.testbed.listenerperrequest;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporters.inmemory.InMemoryTracing;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Each request has own instance of ResponseListener. */
class ListenerTest {
  private final TracerSdkProvider sdk = TracerSdkProvider.builder().build();
  private final InMemoryTracing inMemoryTracing =
      InMemoryTracing.builder().setTracerSdkManagement(sdk).build();
  private final Tracer tracer = sdk.get(ListenerTest.class.getName());

  @Test
  void test() throws Exception {
    Client client = new Client(tracer);
    Object response = client.send("message").get();
    assertThat(response).isEqualTo("message:response");

    List<SpanData> finished = inMemoryTracing.getSpanExporter().getFinishedSpanItems();
    assertThat(finished).hasSize(1);
    assertThat(finished.get(0).getKind()).isEqualTo(Kind.CLIENT);

    assertThat(Span.current()).isSameAs(Span.getInvalid());
  }
}
