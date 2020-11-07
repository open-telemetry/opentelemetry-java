/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.testbed.statelesscommonrequesthandler;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * There is only one instance of 'RequestHandler' per 'Client'. Methods of 'RequestHandler' are
 * executed in the same thread (beforeRequest() and its resulting afterRequest(), that is).
 */
public final class HandlerTest {

  @RegisterExtension
  static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

  private final Tracer tracer =
      otelTesting.getOpenTelemetry().getTracer(HandlerTest.class.getName());
  private final Client client = new Client(new RequestHandler(tracer));

  @Test
  void test_requests() throws Exception {
    Future<String> responseFuture = client.send("message");
    Future<String> responseFuture2 = client.send("message2");
    Future<String> responseFuture3 = client.send("message3");

    assertThat(responseFuture3.get(5, TimeUnit.SECONDS)).isEqualTo("message3:response");
    assertThat(responseFuture2.get(5, TimeUnit.SECONDS)).isEqualTo("message2:response");
    assertThat(responseFuture.get(5, TimeUnit.SECONDS)).isEqualTo("message:response");

    List<SpanData> finished = otelTesting.getSpans();
    assertThat(finished).hasSize(3);
  }
}
