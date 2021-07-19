/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.data.DoubleExemplar;
import io.opentelemetry.sdk.metrics.data.Exemplar;
import io.opentelemetry.sdk.metrics.data.LongExemplar;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Helper methods for implementing an exemplar resorvoir.
 *
 * <p>Mostly utiltiies for dealing with {@link Span}s and the trace signals.
 */
public abstract class AbstractExemplarReservoir implements ExemplarReservoir {
  private Class<?> recordableSpanClass = null;
  private Method getLatencyNanosMethod = null;
  private final Clock clock;

  AbstractExemplarReservoir(Clock clock) {
    this.clock = clock;
  }

  protected final boolean hasSamplingSpan(Context context) {
    Span current = Span.fromContext(context);
    return current.getSpanContext().isValid() && current.getSpanContext().isSampled();
  }

  protected Exemplar makeLongExmeplar(long value, Attributes attributes, Context context) {
    Span current = Span.fromContext(context);
    if (current.getSpanContext().isValid()) {
      String spanId = current.getSpanContext().getSpanId();
      String traceId = current.getSpanContext().getTraceId();
      // This wierd logic attempts to pull active time from the span, as it will have
      // a more accurate clock.
      if (current.isRecording()) {
        return LongExemplar.create(
            attributes, hackyGetRecordTimeNanosFromSpan(current), spanId, traceId, value);
      }
      return LongExemplar.create(attributes, clock.nanoTime(), spanId, traceId, value);
    }
    return LongExemplar.create(
        attributes, clock.nanoTime(), /*spanId=*/ null, /*traceId=*/ null, value);
  }

  protected Exemplar makeDoubleExmeplar(double value, Attributes attributes, Context context) {
    Span current = Span.fromContext(context);
    if (current.getSpanContext().isValid()) {
      String spanId = current.getSpanContext().getSpanId();
      String traceId = current.getSpanContext().getTraceId();
      // This wierd logic attempts to pull active time from the span, as it will have
      // a more accurate clock.
      if (current.isRecording()) {
        return DoubleExemplar.create(
            attributes, hackyGetRecordTimeNanosFromSpan(current), spanId, traceId, value);
      }
      return DoubleExemplar.create(attributes, clock.nanoTime(), spanId, traceId, value);
    }
    return DoubleExemplar.create(
        attributes, clock.nanoTime(), /*spanId=*/ null, /*traceId=*/ null, value);
  }

  /**
   * Temporary hack to allow us to access timestamps from recording spans without explicit
   * dependnecy on trace sdk.
   */
  private long hackyGetRecordTimeNanosFromSpan(Span span) {
    try {
      // TODO: pull relative time from recording span by casting to SDK span.
      if (recordableSpanClass == null) {
        Class<?> spanClass = span.getClass();
        if ("io.opentelemetry.sdk.trace.RecordEventsReadableSpan"
            .equals(spanClass.getCanonicalName())) {
          this.recordableSpanClass = spanClass;
          this.getLatencyNanosMethod = spanClass.getMethod("getLatencyNanos");
        }
      }
      if (span.getClass() == recordableSpanClass) {
        Long timeNanos = (Long) getLatencyNanosMethod.invoke(span);
        return (timeNanos == null) ? clock.nanoTime() : timeNanos;
      }
    } catch (NoSuchMethodException ex) {
      // Ignore and return 0.
    } catch (SecurityException ex) {
      // Ignore and return 0.
    } catch (IllegalAccessException ex) {
      // Ignore and return 0.
    } catch (InvocationTargetException ex) {
      // Ignoore and return 0.
    }
    return 0;
  }
}
