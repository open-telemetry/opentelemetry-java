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

package io.opentelemetry.opentracingshim.testbed;

import static org.junit.Assert.assertEquals;

import io.opentelemetry.opentracingshim.InMemoryTracer;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanData;
import io.opentelemetry.trace.SpanData.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public final class TestUtils {
  private TestUtils() {}

  /** A line so that Javadoc does not complain. */
  public static Callable<Integer> finishedSpansSize(final InMemoryTracer tracer) {
    return new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        return tracer.getFinishedSpanDataItems().size();
      }
    };
  }

  /** A line so that Javadoc does not complain. */
  public static List<SpanData> getByAttr(List<SpanData> spans, String key, String value) {
    return getByAttr(spans, key, AttributeValue.stringAttributeValue(value));
  }

  /** A line so that Javadoc does not complain. */
  public static List<SpanData> getByAttr(
      List<SpanData> spans, final String key, final AttributeValue value) {
    return getByCondition(
        spans,
        new Condition() {
          @Override
          public boolean check(SpanData span) {
            return span.getAttributes().get(key).equals(value);
          }
        });
  }

  /** A line so that Javadoc does not complain. */
  public static SpanData getOneByAttr(List<SpanData> spans, String key, String value) {
    return getOneByAttr(spans, key, AttributeValue.stringAttributeValue(value));
  }

  /** A line so that Javadoc does not complain. */
  public static SpanData getOneByAttr(List<SpanData> spans, String key, AttributeValue value) {
    List<SpanData> found = getByAttr(spans, key, value);
    if (found.size() > 1) {
      throw new IllegalArgumentException(
          "there is more than one span with tag '" + key + "' and value '" + value + "'");
    }

    return found.isEmpty() ? null : found.get(0);
  }

  /** A line so that Javadoc does not complain. */
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

  /** A line so that Javadoc does not complain. */
  public static SpanData getOneByKind(List<SpanData> spans, final Kind kind) {

    List<SpanData> found = getByKind(spans, kind);
    if (found.size() > 1) {
      throw new IllegalArgumentException("there is more than one span with kind '" + kind + "'");
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

  /** A line so that Javadoc does not complain. */
  public static void sleep() {
    try {
      TimeUnit.MILLISECONDS.sleep(new Random().nextInt(500));
    } catch (InterruptedException e) {
      e.printStackTrace();
      Thread.currentThread().interrupt();
    }
  }

  /** A line so that Javadoc does not complain. */
  public static void sleep(long milliseconds) {
    try {
      TimeUnit.MILLISECONDS.sleep(milliseconds);
    } catch (InterruptedException e) {
      e.printStackTrace();
      Thread.currentThread().interrupt();
    }
  }

  /** A line so that Javadoc does not complain. */
  public static void sortByStartTime(List<SpanData> spans) {
    Collections.sort(
        spans,
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
  }

  /** A line so that Javadoc does not complain. */
  public static void assertSameTrace(List<SpanData> spans) {
    for (int i = 0; i < spans.size() - 1; i++) {
      // assertEquals(true, spans.get(spans.size() - 1).getEndTimestamp() >=
      // spans.get(i).finishMicros());
      assertEquals(
          spans.get(spans.size() - 1).getContext().getTraceId(),
          spans.get(i).getContext().getTraceId());
      assertEquals(
          spans.get(spans.size() - 1).getContext().getSpanId(), spans.get(i).getParentSpanId());
    }
  }
}
