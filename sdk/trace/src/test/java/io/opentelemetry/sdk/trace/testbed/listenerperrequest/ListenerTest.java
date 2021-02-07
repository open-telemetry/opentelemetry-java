/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.testbed.listenerperrequest;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/** Each request has own instance of ResponseListener. */
class ListenerTest {
  @RegisterExtension
  static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

  private final Tracer tracer =
      otelTesting.getOpenTelemetry().getTracer(ListenerTest.class.getName());

  @Test
  void test() throws Exception {
    Client client = new Client(tracer);
    Object response = client.send("message").get();
    assertThat(response).isEqualTo("message:response");

    List<SpanData> finished = otelTesting.getSpans();
    assertThat(finished).hasSize(1);
    assertThat(finished.get(0).getKind()).isEqualTo(Kind.CLIENT);

    assertThat(Span.current()).isSameAs(Span.getInvalid());
  }
}
