/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.zpages;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TracezDataAggregator}. */
public final class TracezDataAggregatorTest {
  private static final String SPAN_NAME_ONE = "one";
  private static final String SPAN_NAME_TWO = "two";
  private final TestClock testClock = TestClock.create();
  private final SdkTracerProvider sdkTracerProvider =
      SdkTracerProvider.builder().setClock(testClock).build();
  private final Tracer tracer = sdkTracerProvider.get("TracezDataAggregatorTest");
  private final TracezSpanProcessor spanProcessor = TracezSpanProcessor.builder().build();
  private final TracezDataAggregator dataAggregator = new TracezDataAggregator(spanProcessor);

  @BeforeEach
  void setup() {
    sdkTracerProvider.addSpanProcessor(spanProcessor);
  }

  @Test
  void getSpanNames_noSpans() {
    assertThat(dataAggregator.getSpanNames()).isEmpty();
  }

  @Test
  void getSpanNames_twoSpanNames() {
    Span span1 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    Span span2 = tracer.spanBuilder(SPAN_NAME_TWO).startSpan();
    Span span3 = tracer.spanBuilder(SPAN_NAME_TWO).startSpan();
    /* getSpanNames should return a set with 2 span names */
    Set<String> names = dataAggregator.getSpanNames();
    assertThat(names).containsExactly(SPAN_NAME_ONE, SPAN_NAME_TWO);
    span1.end();
    span2.end();
    span3.end();
    /* getSpanNames should still return a set with 2 span names */
    names = dataAggregator.getSpanNames();
    assertThat(names).containsExactly(SPAN_NAME_ONE, SPAN_NAME_TWO);
  }

  @Test
  void getRunningSpanCounts_noSpans() {
    /* getRunningSpanCounts should return a an empty map */
    Map<String, Integer> counts = dataAggregator.getRunningSpanCounts();
    assertThat(counts).isEmpty();
  }

  @Test
  void getRunningSpanCounts_oneSpanName() {
    Span span1 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    Span span2 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    Span span3 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    /* getRunningSpanCounts should return a map with 1 span name */
    Map<String, Integer> counts = dataAggregator.getRunningSpanCounts();
    assertThat(counts.get(SPAN_NAME_ONE)).isEqualTo(3);
    span1.end();
    span2.end();
    span3.end();
    /* getRunningSpanCounts should return a map with no span names */
    counts = dataAggregator.getRunningSpanCounts();
    assertThat(counts).isEmpty();
  }

  @Test
  void getRunningSpanCounts_twoSpanNames() {
    Span span1 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    Span span2 = tracer.spanBuilder(SPAN_NAME_TWO).startSpan();
    /* getRunningSpanCounts should return a map with 2 different span names */
    Map<String, Integer> counts = dataAggregator.getRunningSpanCounts();
    assertThat(counts.get(SPAN_NAME_ONE)).isEqualTo(1);
    assertThat(counts.get(SPAN_NAME_TWO)).isEqualTo(1);

    span1.end();
    /* getRunningSpanCounts should return a map with 1 unique span name */
    counts = dataAggregator.getRunningSpanCounts();
    assertThat(counts.get(SPAN_NAME_ONE)).isNull();
    assertThat(counts.get(SPAN_NAME_TWO)).isEqualTo(1);

    span2.end();
    /* getRunningSpanCounts should return a map with no span names */
    counts = dataAggregator.getRunningSpanCounts();
    assertThat(counts).isEmpty();
  }

  @Test
  void getRunningSpans_noSpans() {
    /* getRunningSpans should return an empty List */
    assertThat(dataAggregator.getRunningSpans(SPAN_NAME_ONE)).isEmpty();
    assertThat(dataAggregator.getRunningSpans(SPAN_NAME_TWO)).isEmpty();
  }

  @Test
  void getRunningSpans_oneSpanName() {
    Span span1 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    Span span2 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    Span span3 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    /* getRunningSpans should return a List with all 3 spans */
    List<SpanData> spans = dataAggregator.getRunningSpans(SPAN_NAME_ONE);
    assertThat(spans)
        .containsExactlyInAnyOrder(
            ((ReadableSpan) span1).toSpanData(),
            ((ReadableSpan) span2).toSpanData(),
            ((ReadableSpan) span3).toSpanData());
    span1.end();
    span2.end();
    span3.end();
    /* getRunningSpans should return an empty List */
    assertThat(dataAggregator.getRunningSpans(SPAN_NAME_ONE)).isEmpty();
  }

  @Test
  void getRunningSpans_twoSpanNames() {
    Span span1 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    Span span2 = tracer.spanBuilder(SPAN_NAME_TWO).startSpan();
    /* getRunningSpans should return a List with only the corresponding span */
    assertThat(dataAggregator.getRunningSpans(SPAN_NAME_ONE))
        .containsExactly(((ReadableSpan) span1).toSpanData());
    assertThat(dataAggregator.getRunningSpans(SPAN_NAME_TWO))
        .containsExactly(((ReadableSpan) span2).toSpanData());
    span1.end();
    span2.end();
    /* getRunningSpans should return an empty List for each span name */
    assertThat(dataAggregator.getRunningSpans(SPAN_NAME_ONE)).isEmpty();
    assertThat(dataAggregator.getRunningSpans(SPAN_NAME_TWO)).isEmpty();
  }

  @Test
  void getSpanLatencyCounts_noSpans() {
    /* getSpanLatencyCounts should return a an empty map */
    Map<String, Map<LatencyBoundary, Integer>> counts = dataAggregator.getSpanLatencyCounts();
    assertThat(counts).isEmpty();
  }

  @Test
  void getSpanLatencyCounts_noCompletedSpans() {
    /* getSpanLatencyCounts should return a an empty map */
    Span span = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    Map<String, Map<LatencyBoundary, Integer>> counts = dataAggregator.getSpanLatencyCounts();
    span.end();
    assertThat(counts).isEmpty();
  }

  @Test
  void getSpanLatencyCounts_oneSpanPerLatencyBucket() {
    for (LatencyBoundary bucket : LatencyBoundary.values()) {
      Span span = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
      testClock.advanceNanos(bucket.getLatencyLowerBound());
      span.end();
    }
    /* getSpanLatencyCounts should return 1 span per latency bucket */
    Map<String, Map<LatencyBoundary, Integer>> counts = dataAggregator.getSpanLatencyCounts();
    for (LatencyBoundary bucket : LatencyBoundary.values()) {
      assertThat(counts.get(SPAN_NAME_ONE).get(bucket)).isEqualTo(1);
    }
  }

  @Test
  void getOkSpans_noSpans() {
    /* getOkSpans should return an empty List */
    assertThat(dataAggregator.getOkSpans(SPAN_NAME_ONE, 0, Long.MAX_VALUE)).isEmpty();
    assertThat(dataAggregator.getOkSpans(SPAN_NAME_TWO, 0, Long.MAX_VALUE)).isEmpty();
  }

  @Test
  void getOkSpans_oneSpanNameWithDifferentLatencies() {
    Span span1 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    Span span2 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    /* getOkSpans should return an empty List */
    assertThat(dataAggregator.getOkSpans(SPAN_NAME_ONE, 0, Long.MAX_VALUE)).isEmpty();
    span1.end();
    testClock.advanceNanos(1000);
    span2.end();
    /* getOkSpans should return a List with both spans */
    List<SpanData> spans = dataAggregator.getOkSpans(SPAN_NAME_ONE, 0, Long.MAX_VALUE);
    assertThat(spans)
        .containsExactly(((ReadableSpan) span1).toSpanData(), ((ReadableSpan) span2).toSpanData());
    /* getOkSpans should return a List with only the first span */
    spans = dataAggregator.getOkSpans(SPAN_NAME_ONE, 0, 1000);
    assertThat(spans).containsExactly(((ReadableSpan) span1).toSpanData());
    /* getOkSpans should return a List with only the second span */
    spans = dataAggregator.getOkSpans(SPAN_NAME_ONE, 1000, Long.MAX_VALUE);
    assertThat(spans).containsExactly(((ReadableSpan) span2).toSpanData());
  }

  @Test
  void getOkSpans_twoSpanNames() {
    Span span1 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    Span span2 = tracer.spanBuilder(SPAN_NAME_TWO).startSpan();
    /* getOkSpans should return an empty List for each span name */
    assertThat(dataAggregator.getOkSpans(SPAN_NAME_ONE, 0, Long.MAX_VALUE)).isEmpty();
    assertThat(dataAggregator.getOkSpans(SPAN_NAME_TWO, 0, Long.MAX_VALUE)).isEmpty();
    span1.end();
    span2.end();
    /* getOkSpans should return a List with only the corresponding span */
    assertThat(dataAggregator.getOkSpans(SPAN_NAME_ONE, 0, Long.MAX_VALUE))
        .containsExactly(((ReadableSpan) span1).toSpanData());
    assertThat(dataAggregator.getOkSpans(SPAN_NAME_TWO, 0, Long.MAX_VALUE))
        .containsExactly(((ReadableSpan) span2).toSpanData());
  }

  @Test
  void getErrorSpanCounts_noSpans() {
    Map<String, Integer> counts = dataAggregator.getErrorSpanCounts();
    assertThat(counts).isEmpty();
  }

  @Test
  void getErrorSpanCounts_noCompletedSpans() {
    /* getErrorSpanCounts should return a an empty map */
    Span span = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    Map<String, Integer> counts = dataAggregator.getErrorSpanCounts();
    span.setStatus(StatusCode.ERROR);
    span.end();
    assertThat(counts).isEmpty();
  }

  @Test
  void getErrorSpanCounts_oneSpanPerErrorCode() {
    for (StatusCode errorCode : StatusCode.values()) {
      if (errorCode.equals(StatusCode.ERROR)) {
        Span span = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
        span.setStatus(errorCode);
        span.end();
      }
    }
    Map<String, Integer> errorCounts = dataAggregator.getErrorSpanCounts();
    assertThat(errorCounts.get(SPAN_NAME_ONE)).isEqualTo(1);
  }

  @Test
  void getErrorSpanCounts_twoSpanNames() {
    Span span1 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    span1.setStatus(StatusCode.ERROR);
    span1.end();
    Span span2 = tracer.spanBuilder(SPAN_NAME_TWO).startSpan();
    span2.setStatus(StatusCode.ERROR);
    span2.end();
    /* getErrorSpanCounts should return a map with 2 different span names */
    Map<String, Integer> errorCounts = dataAggregator.getErrorSpanCounts();
    assertThat(errorCounts.get(SPAN_NAME_ONE)).isEqualTo(1);
    assertThat(errorCounts.get(SPAN_NAME_TWO)).isEqualTo(1);
  }

  @Test
  void getErrorSpans_noSpans() {
    /* getErrorSpans should return an empty List */
    assertThat(dataAggregator.getErrorSpans(SPAN_NAME_ONE)).isEmpty();
    assertThat(dataAggregator.getErrorSpans(SPAN_NAME_TWO)).isEmpty();
  }

  @Test
  void getErrorSpans_oneSpanNameWithDifferentErrors() {
    Span span1 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    Span span2 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    /* getErrorSpans should return an empty List */
    assertThat(dataAggregator.getErrorSpans(SPAN_NAME_ONE)).isEmpty();
    span1.setStatus(StatusCode.ERROR);
    span1.end();
    span2.setStatus(StatusCode.ERROR, "ABORTED");
    span2.end();
    /* getErrorSpans should return a List with both spans */
    List<SpanData> errorSpans = dataAggregator.getErrorSpans(SPAN_NAME_ONE);
    assertThat(errorSpans)
        .containsExactly(((ReadableSpan) span1).toSpanData(), ((ReadableSpan) span2).toSpanData());
  }

  @Test
  void getErrorSpans_twoSpanNames() {
    Span span1 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    Span span2 = tracer.spanBuilder(SPAN_NAME_TWO).startSpan();
    /* getErrorSpans should return an empty List for each span name */
    assertThat(dataAggregator.getErrorSpans(SPAN_NAME_ONE)).isEmpty();
    assertThat(dataAggregator.getErrorSpans(SPAN_NAME_TWO)).isEmpty();
    span1.setStatus(StatusCode.ERROR);
    span1.end();
    span2.setStatus(StatusCode.ERROR);
    span2.end();
    /* getErrorSpans should return a List with only the corresponding span */
    assertThat(dataAggregator.getErrorSpans(SPAN_NAME_ONE))
        .containsExactly(((ReadableSpan) span1).toSpanData());
    assertThat(dataAggregator.getErrorSpans(SPAN_NAME_TWO))
        .containsExactly(((ReadableSpan) span2).toSpanData());
  }
}
