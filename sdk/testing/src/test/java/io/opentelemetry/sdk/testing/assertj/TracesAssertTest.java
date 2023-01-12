/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class TracesAssertTest {
  @Test
  void spanDataComparator() {
    TestSpanData before = createBasicSpanBuilder().setStartEpochNanos(9).build();

    String traceId = TraceId.fromLongs(1, 2);
    TestSpanData parent =
        createBasicSpanBuilder()
            .setName("parent")
            .setSpanContext(
                SpanContext.create(
                    traceId, SpanId.fromLong(1), TraceFlags.getDefault(), TraceState.getDefault()))
            .setStartEpochNanos(10)
            .build();
    TestSpanData child =
        createBasicSpanBuilder()
            .setName("child")
            .setSpanContext(
                SpanContext.create(
                    traceId, SpanId.fromLong(2), TraceFlags.getDefault(), TraceState.getDefault()))
            .setStartEpochNanos(10)
            .setParentSpanContext(parent.getSpanContext())
            .build();

    TestSpanData sameTime1 =
        createBasicSpanBuilder().setName("sameTime1").setStartEpochNanos(11).build();
    TestSpanData sameTime2 =
        createBasicSpanBuilder().setName("sameTime2").setStartEpochNanos(11).build();

    assertSort(Arrays.asList(child, parent, before), before, parent, child);
    assertSort(Arrays.asList(parent, child, before), before, parent, child);

    assertSort(Arrays.asList(sameTime1, sameTime2, before), before, sameTime1, sameTime2);
    assertSort(Arrays.asList(sameTime2, sameTime1, before), before, sameTime2, sameTime1);
  }

  private static void assertSort(List<TestSpanData> spanData, TestSpanData... expected) {
    ArrayList<TestSpanData> list = new ArrayList<>(spanData);
    list.sort(TracesAssert.SPAN_DATA_COMPARATOR);
    assertThat(list).containsExactly(expected);
  }

  private static TestSpanData.Builder createBasicSpanBuilder() {
    return TestSpanData.builder()
        .setHasEnded(true)
        .setName("spanName")
        .setEndEpochNanos(100)
        .setKind(SpanKind.SERVER)
        .setStatus(StatusData.ok())
        .setTotalRecordedEvents(0)
        .setTotalRecordedLinks(0);
  }
}
