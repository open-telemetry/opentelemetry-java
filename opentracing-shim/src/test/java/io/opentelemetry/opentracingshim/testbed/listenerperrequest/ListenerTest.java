/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed.listenerperrequest;

import static io.opentelemetry.api.trace.SpanKind.CLIENT;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.opentracingshim.OpenTracingShim;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentracing.Tracer;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/** Each request has own instance of ResponseListener. */
class ListenerTest {
  @RegisterExtension
  static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

  private final Tracer tracer = OpenTracingShim.createTracerShim(otelTesting.getOpenTelemetry());

  @Test
  void test() throws Exception {
    Client client = new Client(tracer);
    Object response = client.send("message").get();
    assertThat(response).isEqualTo("message:response");

    List<SpanData> finished = otelTesting.getSpans();
    assertThat(finished).hasSize(1);
    assertThat(CLIENT).isEqualTo(finished.get(0).getKind());

    assertThat(tracer.scopeManager().activeSpan()).isNull();
  }
}
