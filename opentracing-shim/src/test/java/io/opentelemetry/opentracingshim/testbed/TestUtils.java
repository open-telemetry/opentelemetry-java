/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

public final class TestUtils {
  private TestUtils() {}

  /** Returns the number of finished {@code Span}s in the specified {@code InMemorySpanExporter}. */
  public static Callable<Integer> finishedSpansSize(final OpenTelemetryExtension otelTesting) {
    return () -> otelTesting.getSpans().size();
  }

  /** Returns a {@code List} with the {@code Span} matching the specified attribute. */
  public static <T> List<SpanData> getByAttr(
      List<SpanData> spans, final AttributeKey<T> key, final T value) {
    return getByCondition(
        spans,
        spanData -> {
          T attrValue = spanData.getAttributes().get(key);
          if (attrValue == null) {
            return false;
          }

          return value.equals(attrValue);
        });
  }

  /** Returns a {@code List} with the {@code Span} matching the specified kind. */
  public static List<SpanData> getByKind(List<SpanData> spans, final SpanKind kind) {
    return getByCondition(spans, span -> span.getKind() == kind);
  }

  /**
   * Returns one {@code Span} instance matching the specified kind. In case of more than one
   * instance being matched, an {@code IllegalArgumentException} will be thrown.
   */
  @Nullable
  public static SpanData getOneByKind(List<SpanData> spans, final SpanKind kind) {

    List<SpanData> found = getByKind(spans, kind);
    if (found.size() > 1) {
      throw new IllegalArgumentException("there is more than one span with kind '" + kind + "'");
    }

    return found.isEmpty() ? null : found.get(0);
  }

  /** Returns a {@code List} with the {@code Span} matching the specified name. */
  private static List<SpanData> getByName(List<SpanData> spans, final String name) {
    return getByCondition(spans, span -> span.getName().equals(name));
  }

  /**
   * Returns one {@code Span} instance matching the specified name. In case of more than one
   * instance being matched, an {@code IllegalArgumentException} will be thrown.
   */
  @Nullable
  public static SpanData getOneByName(List<SpanData> spans, final String name) {

    List<SpanData> found = getByName(spans, name);
    if (found.size() > 1) {
      throw new IllegalArgumentException("there is more than one span with name '" + name + "'");
    }

    return found.isEmpty() ? null : found.get(0);
  }

  interface Condition {
    boolean check(SpanData span);
  }

  private static List<SpanData> getByCondition(List<SpanData> spans, Condition cond) {
    List<SpanData> found = new ArrayList<>();
    for (SpanData span : spans) {
      if (cond.check(span)) {
        found.add(span);
      }
    }

    return found;
  }

  /** Sleeps for a random period of time, expected to be under 1 second. */
  public static void sleep() {
    try {
      TimeUnit.MILLISECONDS.sleep(new Random().nextInt(500));
    } catch (InterruptedException e) {
      e.printStackTrace();
      Thread.currentThread().interrupt();
    }
  }

  /** Sleeps the specified milliseconds. */
  public static void sleep(long milliseconds) {
    try {
      TimeUnit.MILLISECONDS.sleep(milliseconds);
    } catch (InterruptedException e) {
      e.printStackTrace();
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Sorts the specified {@code List} of {@code Span} by their start epoch timestamp values,
   * returning it as a new {@code List}.
   */
  public static List<SpanData> sortByStartTime(List<SpanData> spans) {
    List<SpanData> sortedSpans = new ArrayList<>(spans);
    Collections.sort(
        sortedSpans, (o1, o2) -> Long.compare(o1.getStartEpochNanos(), o2.getStartEpochNanos()));
    return sortedSpans;
  }

  /** Asserts the specified {@code List} of {@code Span} belongs to the same trace. */
  public static void assertSameTrace(List<SpanData> spans) {
    for (int i = 0; i < spans.size() - 1; i++) {
      // TODO - Include nanos in this comparison.
      assertThat(spans.get(i).getEndEpochNanos())
          .isLessThanOrEqualTo(spans.get(spans.size() - 1).getEndEpochNanos());
      assertThat(spans.get(i).getTraceIdHex())
          .isEqualTo(spans.get(spans.size() - 1).getTraceIdHex());
      assertThat(spans.get(i).getParentSpanIdHex())
          .isEqualTo(spans.get(spans.size() - 1).getSpanIdHex());
    }
  }
}
