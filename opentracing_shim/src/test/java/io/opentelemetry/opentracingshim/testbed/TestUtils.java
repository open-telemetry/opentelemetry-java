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
import static org.junit.Assert.assertTrue;

import com.google.protobuf.Timestamp;
import io.opentelemetry.distributedcontext.DefaultDistributedContextManager;
import io.opentelemetry.opentracingshim.TraceShim;
import io.opentelemetry.proto.trace.v1.AttributeValue;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Span.SpanKind;
import io.opentelemetry.sdk.trace.TracerSdk;
import io.opentelemetry.sdk.trace.export.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.SimpleSampledSpansProcessor;
import io.opentracing.Tracer;
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
    // TODO - Make SURE that for these tests we don't really need anything special here.
    // (PROBABLY we can already use the SDK portion of the Dist Context).
    return TraceShim.createTracerShim(tracer, DefaultDistributedContextManager.getInstance());
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
  public static List<Span> getByAttr(List<Span> spans, final String key, final Object value) {
    return getByCondition(
        spans,
        new Condition() {
          @Override
          public boolean check(Span span) {
            AttributeValue attrValue = span.getAttributes().getAttributeMap().get(key);
            if (attrValue == null) {
              return false;
            }

            switch (attrValue.getValueCase()) {
              case VALUE_NOT_SET:
                return false;
              case STRING_VALUE:
                return value.equals(attrValue.getStringValue());
              case INT_VALUE:
                return value.equals(attrValue.getIntValue());
              case BOOL_VALUE:
                return value.equals(attrValue.getBoolValue());
              case DOUBLE_VALUE:
                return value.equals(attrValue.getDoubleValue());
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
  public static Span getOneByAttr(List<Span> spans, String key, Object value) {
    List<Span> found = getByAttr(spans, key, value);
    if (found.size() > 1) {
      throw new IllegalArgumentException(
          "there is more than one span with tag '" + key + "' and value '" + value + "'");
    }

    return found.isEmpty() ? null : found.get(0);
  }

  /** Returns a {@code List} with the {@code Span} matching the specified kind. */
  public static List<Span> getByKind(List<Span> spans, final SpanKind kind) {
    return getByCondition(
        spans,
        new Condition() {
          @Override
          public boolean check(Span span) {
            return span.getKind() == kind;
          }
        });
  }

  /**
   * Returns one {@code Span} instance matching the specified kind. In case of more than one
   * instance being matched, an {@code IllegalArgumentException} will be thrown.
   */
  @Nullable
  public static Span getOneByKind(List<Span> spans, final SpanKind kind) {

    List<Span> found = getByKind(spans, kind);
    if (found.size() > 1) {
      throw new IllegalArgumentException("there is more than one span with kind '" + kind + "'");
    }

    return found.isEmpty() ? null : found.get(0);
  }

  /** Returns a {@code List} with the {@code Span} matching the specified name. */
  public static List<Span> getByName(List<Span> spans, final String name) {
    return getByCondition(
        spans,
        new Condition() {
          @Override
          public boolean check(Span span) {
            return span.getName().equals(name);
          }
        });
  }

  /**
   * Returns one {@code Span} instance matching the specified name. In case of more than one
   * instance being matched, an {@code IllegalArgumentException} will be thrown.
   */
  @Nullable
  public static Span getOneByName(List<Span> spans, final String name) {

    List<Span> found = getByName(spans, name);
    if (found.size() > 1) {
      throw new IllegalArgumentException("there is more than one span with name '" + name + "'");
    }

    return found.isEmpty() ? null : found.get(0);
  }

  interface Condition {
    boolean check(Span span);
  }

  static List<Span> getByCondition(List<Span> spans, Condition cond) {
    List<Span> found = new ArrayList<>();
    for (Span span : spans) {
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
  public static List<Span> sortByStartTime(List<Span> spans) {
    List<Span> sortedSpans = new ArrayList<>(spans);
    Collections.sort(
        sortedSpans,
        new Comparator<Span>() {
          @Override
          public int compare(Span o1, Span o2) {
            Timestamp t1 = o1.getStartTime();
            Timestamp t2 = o2.getStartTime();

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
  public static void assertSameTrace(List<Span> spans) {
    for (int i = 0; i < spans.size() - 1; i++) {
      // TODO - Include nanos in this comparison.
      assertTrue(
          spans.get(spans.size() - 1).getEndTime().getSeconds()
              >= spans.get(i).getEndTime().getSeconds());
      assertEquals(spans.get(spans.size() - 1).getTraceId(), spans.get(i).getTraceId());
      assertEquals(spans.get(spans.size() - 1).getSpanId(), spans.get(i).getParentSpanId());
    }
  }
}
