/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.sdk.extensions.zpages;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.Status.CanonicalCode;
import io.opentelemetry.trace.Tracer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link TracezDataAggregator}. */
@RunWith(JUnit4.class)
public final class TracezDataAggregatorTest {
  private final TestClock testClock = TestClock.create();
  private final TracerSdkProvider tracerSdkProvider =
      TracerSdkProvider.builder().setClock(testClock).build();
  private final Tracer tracer = tracerSdkProvider.get("TracezDataAggregatorTest");
  private final TracezSpanProcessor spanProcessor = TracezSpanProcessor.newBuilder().build();
  private final TracezDataAggregator dataAggregator = new TracezDataAggregator(spanProcessor);
  private static final String SPAN_NAME_ONE = "one";
  private static final String SPAN_NAME_TWO = "two";

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    tracerSdkProvider.addSpanProcessor(spanProcessor);
  }

  @Test
  public void getSpanNames_noSpans() {
    assertThat(dataAggregator.getSpanNames().size()).isEqualTo(0);
  }

  @Test
  public void getSpanNames_twoSpanNames() {
    Span span1 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    Span span2 = tracer.spanBuilder(SPAN_NAME_TWO).startSpan();
    Span span3 = tracer.spanBuilder(SPAN_NAME_TWO).startSpan();
    /* getSpanNames should return a set with 2 span names */
    Set<String> names = dataAggregator.getSpanNames();
    assertThat(names.size()).isEqualTo(2);
    assertThat(names).contains(SPAN_NAME_ONE);
    assertThat(names).contains(SPAN_NAME_TWO);
    span1.end();
    span2.end();
    span3.end();
    /* getSpanNames should still return a set with 2 span names */
    names = dataAggregator.getSpanNames();
    assertThat(names.size()).isEqualTo(2);
    assertThat(names).contains(SPAN_NAME_ONE);
    assertThat(names).contains(SPAN_NAME_TWO);
  }

  @Test
  public void getRunningSpanCounts_noSpans() {
    /* getRunningSpanCounts should return a an empty map */
    Map<String, Integer> counts = dataAggregator.getRunningSpanCounts();
    assertThat(counts.size()).isEqualTo(0);
    assertThat(counts.get(SPAN_NAME_ONE)).isNull();
    assertThat(counts.get(SPAN_NAME_TWO)).isNull();
  }

  @Test
  public void getRunningSpanCounts_oneSpanName() {
    Span span1 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    Span span2 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    Span span3 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    /* getRunningSpanCounts should return a map with 1 span name */
    Map<String, Integer> counts = dataAggregator.getRunningSpanCounts();
    assertThat(counts.size()).isEqualTo(1);
    assertThat(counts.get(SPAN_NAME_ONE)).isEqualTo(3);
    span1.end();
    span2.end();
    span3.end();
    /* getRunningSpanCounts should return a map with no span names */
    counts = dataAggregator.getRunningSpanCounts();
    assertThat(counts.size()).isEqualTo(0);
    assertThat(counts.get(SPAN_NAME_ONE)).isNull();
  }

  @Test
  public void getRunningSpanCounts_twoSpanNames() {
    Span span1 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    Span span2 = tracer.spanBuilder(SPAN_NAME_TWO).startSpan();
    /* getRunningSpanCounts should return a map with 2 different span names */
    Map<String, Integer> counts = dataAggregator.getRunningSpanCounts();
    assertThat(counts.size()).isEqualTo(2);
    assertThat(counts.get(SPAN_NAME_ONE)).isEqualTo(1);
    assertThat(counts.get(SPAN_NAME_TWO)).isEqualTo(1);

    span1.end();
    /* getRunningSpanCounts should return a map with 1 unique span name */
    counts = dataAggregator.getRunningSpanCounts();
    assertThat(counts.size()).isEqualTo(1);
    assertThat(counts.get(SPAN_NAME_ONE)).isNull();
    assertThat(counts.get(SPAN_NAME_TWO)).isEqualTo(1);

    span2.end();
    /* getRunningSpanCounts should return a map with no span names */
    counts = dataAggregator.getRunningSpanCounts();
    assertThat(counts.size()).isEqualTo(0);
    assertThat(counts.get(SPAN_NAME_ONE)).isNull();
    assertThat(counts.get(SPAN_NAME_TWO)).isNull();
  }

  @Test
  public void getRunningSpans_noSpans() {
    /* getRunningSpans should return an empty List */
    assertThat(dataAggregator.getRunningSpans(SPAN_NAME_ONE).size()).isEqualTo(0);
    assertThat(dataAggregator.getRunningSpans(SPAN_NAME_TWO).size()).isEqualTo(0);
  }

  @Test
  public void getRunningSpans_oneSpanName() {
    Span span1 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    Span span2 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    Span span3 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    /* getRunningSpans should return a List with all 3 spans */
    List<SpanData> spans = dataAggregator.getRunningSpans(SPAN_NAME_ONE);
    assertThat(spans.size()).isEqualTo(3);
    assertThat(spans).contains(((ReadableSpan) span1).toSpanData());
    assertThat(spans).contains(((ReadableSpan) span2).toSpanData());
    assertThat(spans).contains(((ReadableSpan) span3).toSpanData());
    span1.end();
    span2.end();
    span3.end();
    /* getRunningSpans should return an empty List */
    assertThat(dataAggregator.getRunningSpans(SPAN_NAME_ONE).size()).isEqualTo(0);
  }

  @Test
  public void getRunningSpans_twoSpanNames() {
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
    assertThat(dataAggregator.getRunningSpans(SPAN_NAME_ONE).size()).isEqualTo(0);
    assertThat(dataAggregator.getRunningSpans(SPAN_NAME_TWO).size()).isEqualTo(0);
  }

  @Test
  public void getSpanLatencyCounts_noSpans() {
    /* getSpanLatencyCounts should return a an empty map */
    Map<String, Map<LatencyBoundaries, Integer>> counts = dataAggregator.getSpanLatencyCounts();
    assertThat(counts.size()).isEqualTo(0);
    assertThat(counts.get(SPAN_NAME_ONE)).isNull();
    assertThat(counts.get(SPAN_NAME_TWO)).isNull();
  }

  @Test
  public void getSpanLatencyCounts_noCompletedSpans() {
    /* getSpanLatencyCounts should return a an empty map */
    Span span = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    Map<String, Map<LatencyBoundaries, Integer>> counts = dataAggregator.getSpanLatencyCounts();
    span.end();
    assertThat(counts.size()).isEqualTo(0);
    assertThat(counts.get(SPAN_NAME_ONE)).isNull();
  }

  @Test
  public void getSpanLatencyCounts_oneSpanPerLatencyBucket() {
    for (LatencyBoundaries bucket : LatencyBoundaries.values()) {
      Span span = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
      testClock.advanceNanos(bucket.getLatencyLowerBound());
      span.end();
    }
    /* getSpanLatencyCounts should return 1 span per latency bucket */
    Map<String, Map<LatencyBoundaries, Integer>> counts = dataAggregator.getSpanLatencyCounts();
    for (LatencyBoundaries bucket : LatencyBoundaries.values()) {
      assertThat(counts.get(SPAN_NAME_ONE).get(bucket)).isEqualTo(1);
    }
  }

  @Test
  public void getOkSpans_noSpans() {
    /* getOkSpans should return an empty List */
    assertThat(dataAggregator.getOkSpans(SPAN_NAME_ONE, 0, Long.MAX_VALUE).size()).isEqualTo(0);
    assertThat(dataAggregator.getOkSpans(SPAN_NAME_TWO, 0, Long.MAX_VALUE).size()).isEqualTo(0);
  }

  @Test
  public void getOkSpans_oneSpanNameWithDifferentLatencies() {
    Span span1 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    Span span2 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    /* getOkSpans should return an empty List */
    assertThat(dataAggregator.getOkSpans(SPAN_NAME_ONE, 0, Long.MAX_VALUE).size()).isEqualTo(0);
    span1.end();
    testClock.advanceNanos(1000);
    span2.end();
    /* getOkSpans should return a List with both spans */
    List<SpanData> spans = dataAggregator.getOkSpans(SPAN_NAME_ONE, 0, Long.MAX_VALUE);
    assertThat(spans.size()).isEqualTo(2);
    assertThat(spans).contains(((ReadableSpan) span1).toSpanData());
    assertThat(spans).contains(((ReadableSpan) span2).toSpanData());
    /* getOkSpans should return a List with only the first span */
    spans = dataAggregator.getOkSpans(SPAN_NAME_ONE, 0, 1000);
    assertThat(spans.size()).isEqualTo(1);
    assertThat(spans).contains(((ReadableSpan) span1).toSpanData());
    /* getOkSpans should return a List with only the second span */
    spans = dataAggregator.getOkSpans(SPAN_NAME_ONE, 1000, Long.MAX_VALUE);
    assertThat(spans.size()).isEqualTo(1);
    assertThat(spans).contains(((ReadableSpan) span2).toSpanData());
  }

  @Test
  public void getOkSpans_twoSpanNames() {
    Span span1 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    Span span2 = tracer.spanBuilder(SPAN_NAME_TWO).startSpan();
    /* getOkSpans should return an empty List for each span name */
    assertThat(dataAggregator.getOkSpans(SPAN_NAME_ONE, 0, Long.MAX_VALUE).size()).isEqualTo(0);
    assertThat(dataAggregator.getOkSpans(SPAN_NAME_TWO, 0, Long.MAX_VALUE).size()).isEqualTo(0);
    span1.end();
    span2.end();
    /* getOkSpans should return a List with only the corresponding span */
    assertThat(dataAggregator.getOkSpans(SPAN_NAME_ONE, 0, Long.MAX_VALUE))
        .containsExactly(((ReadableSpan) span1).toSpanData());
    assertThat(dataAggregator.getOkSpans(SPAN_NAME_TWO, 0, Long.MAX_VALUE))
        .containsExactly(((ReadableSpan) span2).toSpanData());
  }

  @Test
  public void getErrorSpanCounts_noSpans() {
    Map<String, Integer> counts = dataAggregator.getErrorSpanCounts();
    assertThat(counts.size()).isEqualTo(0);
    assertThat(counts.get(SPAN_NAME_ONE)).isNull();
    assertThat(counts.get(SPAN_NAME_TWO)).isNull();
  }

  @Test
  public void getErrorSpanCounts_noCompletedSpans() {
    /* getErrorSpanCounts should return a an empty map */
    Span span = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    Map<String, Integer> counts = dataAggregator.getErrorSpanCounts();
    span.setStatus(Status.UNKNOWN);
    span.end();
    assertThat(counts.size()).isEqualTo(0);
    assertThat(counts.get(SPAN_NAME_ONE)).isNull();
    assertThat(counts.get(SPAN_NAME_TWO)).isNull();
  }

  @Test
  public void getErrorSpanCounts_oneSpanPerErrorCode() {
    for (CanonicalCode errorCode : CanonicalCode.values()) {
      if (!errorCode.equals(CanonicalCode.OK)) {
        Span span = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
        span.setStatus(errorCode.toStatus());
        span.end();
      }
    }
    /* getErrorSpanCounts should return a map with CanonicalCode.values().length - 1 spans, as every
    code, expect OK, represents an error */
    Map<String, Integer> errorCounts = dataAggregator.getErrorSpanCounts();
    assertThat(errorCounts.size()).isEqualTo(1);
    assertThat(errorCounts.get(SPAN_NAME_ONE)).isEqualTo(CanonicalCode.values().length - 1);
  }

  @Test
  public void getErrorSpanCounts_twoSpanNames() {
    Span span1 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    span1.setStatus(Status.UNKNOWN);
    span1.end();
    Span span2 = tracer.spanBuilder(SPAN_NAME_TWO).startSpan();
    span2.setStatus(Status.UNKNOWN);
    span2.end();
    /* getErrorSpanCounts should return a map with 2 different span names */
    Map<String, Integer> errorCounts = dataAggregator.getErrorSpanCounts();
    assertThat(errorCounts.size()).isEqualTo(2);
    assertThat(errorCounts.get(SPAN_NAME_ONE)).isEqualTo(1);
    assertThat(errorCounts.get(SPAN_NAME_TWO)).isEqualTo(1);
  }

  @Test
  public void getErrorSpans_noSpans() {
    /* getErrorSpans should return an empty List */
    assertThat(dataAggregator.getErrorSpans(SPAN_NAME_ONE).size()).isEqualTo(0);
    assertThat(dataAggregator.getErrorSpans(SPAN_NAME_TWO).size()).isEqualTo(0);
  }

  @Test
  public void getErrorSpans_oneSpanNameWithDifferentErrors() {
    Span span1 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    Span span2 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    /* getErrorSpans should return an empty List */
    assertThat(dataAggregator.getErrorSpans(SPAN_NAME_ONE).size()).isEqualTo(0);
    span1.setStatus(Status.UNKNOWN);
    span1.end();
    span2.setStatus(Status.ABORTED);
    span2.end();
    /* getErrorSpans should return a List with both spans */
    List<SpanData> errorSpans = dataAggregator.getErrorSpans(SPAN_NAME_ONE);
    assertThat(errorSpans.size()).isEqualTo(2);
    assertThat(errorSpans).contains(((ReadableSpan) span1).toSpanData());
    assertThat(errorSpans).contains(((ReadableSpan) span2).toSpanData());
  }

  @Test
  public void getErrorSpans_twoSpanNames() {
    Span span1 = tracer.spanBuilder(SPAN_NAME_ONE).startSpan();
    Span span2 = tracer.spanBuilder(SPAN_NAME_TWO).startSpan();
    /* getErrorSpans should return an empty List for each span name */
    assertThat(dataAggregator.getErrorSpans(SPAN_NAME_ONE).size()).isEqualTo(0);
    assertThat(dataAggregator.getErrorSpans(SPAN_NAME_TWO).size()).isEqualTo(0);
    span1.setStatus(Status.UNKNOWN);
    span1.end();
    span2.setStatus(Status.UNKNOWN);
    span2.end();
    /* getErrorSpans should return a List with only the corresponding span */
    assertThat(dataAggregator.getErrorSpans(SPAN_NAME_ONE))
        .containsExactly(((ReadableSpan) span1).toSpanData());
    assertThat(dataAggregator.getErrorSpans(SPAN_NAME_TWO))
        .containsExactly(((ReadableSpan) span2).toSpanData());
  }
}
