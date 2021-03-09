/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.google.common.testing.GcFinalization;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class LeakDetectingSpanProcessorTest {

  @Test
  void basics() {
    assertThat(LeakDetectingSpanProcessor.create().isEndRequired()).isTrue();
    assertThat(LeakDetectingSpanProcessor.create().isStartRequired()).isTrue();
  }

  @Test
  void garbageCollectedUnendedSpan() {
    List<Throwable> logs = new ArrayList<>();
    LeakDetectingSpanProcessor spanProcessor =
        new LeakDetectingSpanProcessor((message, callerStackTrace) -> logs.add(callerStackTrace));

    SdkTracerProvider tracerProvider =
        SdkTracerProvider.builder().addSpanProcessor(spanProcessor).build();

    Tracer tracer = tracerProvider.get("test");

    tracer.spanBuilder("testSpan").startSpan();

    await()
        .atMost(Duration.ofSeconds(30))
        .untilAsserted(
            () -> {
              System.gc();
              assertThat(logs)
                  .singleElement()
                  .satisfies(
                      callerStackTrace ->
                          assertThat(callerStackTrace.getMessage())
                              .matches(
                                  "Span garbage collected before being ended\\. "
                                      + "Thread: \\[.*\\] started span : .*"));
            });
  }

  @Test
  void garbageCollectedEndedSpan() {
    List<Throwable> logs = new ArrayList<>();
    LeakDetectingSpanProcessor spanProcessor =
        new LeakDetectingSpanProcessor((message, callerStackTrace) -> logs.add(callerStackTrace));

    SdkTracerProvider tracerProvider =
        SdkTracerProvider.builder().addSpanProcessor(spanProcessor).build();

    Tracer tracer = tracerProvider.get("test");

    Span testSpan = tracer.spanBuilder("testSpan").startSpan();
    WeakReference<Span> spanRef = new WeakReference<>(testSpan);

    testSpan.end();
    testSpan = null;

    GcFinalization.awaitClear(spanRef);

    assertThat(logs).isEmpty();
  }
}
