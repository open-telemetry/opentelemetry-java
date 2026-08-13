/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.logs;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.ReadWriteLogRecord;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import java.util.concurrent.TimeUnit;

/**
 * A {@link LogRecordProcessor} which bridges events (i.e. log records with a non-empty {@code
 * event.name}) as span events on the current span.
 *
 * <p>A log record is bridged to a span event if and only if all of the following conditions are
 * met:
 *
 * <ul>
 *   <li>The log record has a non-empty event name.
 *   <li>The log record has a valid trace ID and span ID.
 *   <li>The resolved context contains a current span whose {@link Span#isRecording()} is {@code
 *       true}.
 *   <li>The log record's trace ID and span ID equal those of the current span in the resolved
 *       context.
 * </ul>
 *
 * <p>When bridged, a span event is added with:
 *
 * <ul>
 *   <li>Name set to the log record's event name.
 *   <li>Timestamp set to the log record's timestamp if set; otherwise, the observed timestamp.
 *   <li>All log record attributes copied to the span event attributes.
 * </ul>
 *
 * <p>Bridging does NOT prevent the log record from continuing through the normal log processing
 * pipeline.
 */
public final class EventToSpanEventBridge implements LogRecordProcessor {

  private EventToSpanEventBridge() {}

  /** Create a new instance. */
  public static EventToSpanEventBridge create() {
    return new EventToSpanEventBridge();
  }

  @Override
  public void onEmit(Context context, ReadWriteLogRecord logRecord) {
    LogRecordData logRecordData = logRecord.toLogRecordData();
    String eventName = logRecordData.getEventName();
    if (eventName == null || eventName.isEmpty()) {
      return;
    }
    SpanContext logSpanContext = logRecordData.getSpanContext();
    if (!logSpanContext.isValid()) {
      return;
    }
    Span currentSpan = Span.fromContext(context);
    if (!currentSpan.isRecording()) {
      return;
    }
    SpanContext currentSpanContext = currentSpan.getSpanContext();
    if (!currentSpanContext.getTraceId().equals(logSpanContext.getTraceId())
        || !currentSpanContext.getSpanId().equals(logSpanContext.getSpanId())) {
      return;
    }
    long timestampNanos = logRecordData.getTimestampEpochNanos();
    if (timestampNanos == 0) {
      timestampNanos = logRecordData.getObservedTimestampEpochNanos();
    }
    currentSpan.addEvent(
        eventName, logRecordData.getAttributes(), timestampNanos, TimeUnit.NANOSECONDS);
  }

  @Override
  public String toString() {
    return "EventToSpanEventBridge{}";
  }
}
