/*
 * Copyright 2019, OpenTelemetry Authors
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

package io.opentelemetry.sdk.contrib.trace.testbed;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.sdk.trace.TracerSdk;
import io.opentelemetry.sdk.trace.export.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.SimpleSampledSpansProcessor;
import io.opentelemetry.sdk.trace.export.SpanData;
import io.opentelemetry.sdk.trace.export.SpanData.Timestamp;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.Tracer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

public final class TestUtils {
  private TestUtils() {}

  /**
   * Creates a new {@code io.opentracing.Tracer} out of the {@code TracerSdk} implementation and
   * exporting to the specified {@code InMemorySpanExporter}.
   */
  public static Tracer createTracerShim(InMemorySpanExporter exporter) {
    TracerSdk tracer = new TracerSdk();
    tracer.addSpanProcessor(SimpleSampledSpansProcessor.newBuilder(exporter).build());
    return tracer;
  }

  /** Returns the number of finished {@code Span}s in the specified {@code InMemorySpanExporter}. */
  public static Callable<Integer> finishedSpansSize(final InMemorySpanExporter tracer) {
    return new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        return tracer.getFinishedSpanItems().size();
      }
    };
  }

  /** Returns a {@code List} with the {@code Span} matching the specified attribute. */
  public static List<SpanData> getByAttr(
      List<SpanData> spans, final String key, final Object value) {
    return getByCondition(
        spans,
        new Condition() {
          @Override
          public boolean check(SpanData span) {
            AttributeValue attrValue = span.getAttributes().get(key);
            if (attrValue == null) {
              return false;
            }

            switch (attrValue.getType()) {
              case BOOLEAN:
                return value.equals(attrValue.getBooleanValue());
              case STRING:
                return value.equals(attrValue.getStringValue());
              case DOUBLE:
                return value.equals(attrValue.getDoubleValue());
              case LONG:
                return value.equals(attrValue.getLongValue());
            }
            return false;
          }
        });
  }

  /**
   * Returns one {@code Span} instance matching the specified attribute. In case of more than one
   * instance being matched, an {@code IllegalArgumentException} will be thrown.
   */
  @Nullable
  public static SpanData getOneByAttr(List<SpanData> spans, String key, Object value) {
    List<SpanData> found = getByAttr(spans, key, value);
    if (found.size() > 1) {
      throw new IllegalArgumentException(
          "there is more than one span with tag '" + key + "' and value '" + value + "'");
    }

    return found.isEmpty() ? null : found.get(0);
  }

  /** Returns a {@code List} with the {@code Span} matching the specified kind. */
  public static List<SpanData> getByKind(List<SpanData> spans, final Kind kind) {
    return getByCondition(
        spans,
        new Condition() {
          @Override
          public boolean check(SpanData span) {
            return span.getKind() == kind;
          }
        });
  }

  /**
   * Returns one {@code Span} instance matching the specified kind. In case of more than one
   * instance being matched, an {@code IllegalArgumentException} will be thrown.
   */
  @Nullable
  public static SpanData getOneByKind(List<SpanData> spans, final Kind kind) {

    List<SpanData> found = getByKind(spans, kind);
    if (found.size() > 1) {
      throw new IllegalArgumentException("there is more than one span with kind '" + kind + "'");
    }

    return found.isEmpty() ? null : found.get(0);
  }

  /** Returns a {@code List} with the {@code Span} matching the specified name. */
  public static List<SpanData> getByName(List<SpanData> spans, final String name) {
    return getByCondition(
        spans,
        new Condition() {
          @Override
          public boolean check(SpanData span) {
            return span.getName().equals(name);
          }
        });
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

  static List<SpanData> getByCondition(List<SpanData> spans, Condition cond) {
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
   * Sorts the specified {@code List} of {@code Span} by their {@code Span.Timestamp} values,
   * returning it as a new {@code List}.
   */
  public static List<SpanData> sortByStartTime(List<SpanData> spans) {
    List<SpanData> sortedSpans = new ArrayList<>(spans);
    Collections.sort(
        sortedSpans,
        new Comparator<SpanData>() {
          @Override
          public int compare(SpanData o1, SpanData o2) {
            Timestamp t1 = o1.getStartTimestamp();
            Timestamp t2 = o2.getStartTimestamp();

            if (t1.getSeconds() == t2.getSeconds()) {
              return Long.compare(t1.getNanos(), t2.getNanos());
            } else {
              return Long.compare(t1.getSeconds(), t2.getSeconds());
            }
          }
        });
    return sortedSpans;
  }

  /** Asserts the specified {@code List} of {@code Span} belongs to the same trace. */
  public static void assertSameTrace(List<SpanData> spans) {
    for (int i = 0; i < spans.size() - 1; i++) {
      // TODO - Include nanos in this comparison.
      assertThat(
              spans.get(spans.size() - 1).getEndTimestamp().getSeconds()
                  >= spans.get(i).getEndTimestamp().getSeconds())
          .isTrue();
      assertThat(spans.get(spans.size() - 1).getContext().getTraceId())
          .isEqualTo(spans.get(i).getContext().getTraceId());
      assertThat(spans.get(spans.size() - 1).getContext().getSpanId())
          .isEqualTo(spans.get(i).getParentSpanId());
    }
  }
}
