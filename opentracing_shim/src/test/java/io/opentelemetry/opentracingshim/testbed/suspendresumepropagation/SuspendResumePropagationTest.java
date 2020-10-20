/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed.suspendresumepropagation;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.exporters.inmemory.InMemoryTracing;
import io.opentelemetry.opentracingshim.TraceShim;
import io.opentelemetry.sdk.baggage.BaggageManagerSdk;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.trace.SpanId;
import io.opentracing.Tracer;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * These tests are intended to simulate the kind of async models that are common in java async
 * frameworks.
 *
 * @author tylerbenson
 */
class SuspendResumePropagationTest {
  private final TracerSdkProvider sdk = TracerSdkProvider.builder().build();
  private final InMemoryTracing inMemoryTracing =
      InMemoryTracing.builder().setTracerSdkManagement(sdk).build();
  private final Tracer tracer = TraceShim.createTracerShim(sdk, new BaggageManagerSdk());

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

    List<SpanData> finished = inMemoryTracing.getSpanExporter().getFinishedSpanItems();
    assertThat(finished.size()).isEqualTo(2);

    assertThat(finished.get(0).getName()).isEqualTo("job 1");
    assertThat(finished.get(1).getName()).isEqualTo("job 2");

    assertThat(SpanId.isValid(finished.get(0).getParentSpanId())).isFalse();
    assertThat(SpanId.isValid(finished.get(1).getParentSpanId())).isFalse();
  }
}
