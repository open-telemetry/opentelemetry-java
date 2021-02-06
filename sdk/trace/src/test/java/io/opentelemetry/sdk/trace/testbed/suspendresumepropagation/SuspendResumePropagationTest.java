/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.testbed.suspendresumepropagation;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.SpanIdHex;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.testbed.statelesscommonrequesthandler.HandlerTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * These tests are intended to simulate the kind of async models that are common in java async
 * frameworks.
 */
class SuspendResumePropagationTest {
  @RegisterExtension
  static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

  private final Tracer tracer =
      otelTesting.getOpenTelemetry().getTracer(HandlerTest.class.getName());

  @BeforeEach
  void before() {}

  @Test
  void testContinuationInterleaving() {
    SuspendResume job1 = new SuspendResume(1, tracer);
    SuspendResume job2 = new SuspendResume(2, tracer);

    // Pretend that the framework is controlling actual execution here.
    job1.doPart("some work for 1");
    job2.doPart("some work for 2");
    job1.doPart("other work for 1");
    job2.doPart("other work for 2");
    job2.doPart("more work for 2");
    job1.doPart("more work for 1");

    job1.done();
    job2.done();

    List<SpanData> finished = otelTesting.getSpans();
    assertThat(finished.size()).isEqualTo(2);

    assertThat(finished.get(0).getName()).isEqualTo("job 1");
    assertThat(finished.get(1).getName()).isEqualTo("job 2");

    assertThat(SpanIdHex.isValid(finished.get(0).getParentSpanId())).isFalse();
    assertThat(SpanIdHex.isValid(finished.get(1).getParentSpanId())).isFalse();
  }
}
