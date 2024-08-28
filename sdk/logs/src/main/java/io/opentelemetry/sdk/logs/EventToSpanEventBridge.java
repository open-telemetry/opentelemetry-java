/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import java.util.concurrent.TimeUnit;

/**
 * A processor that records events (i.e. log records with an attribute {@code event.name}) to the
 * current span, if it is valid and {@link Span#isRecording()} is true.
 */
public class EventToSpanEventBridge implements LogRecordProcessor {

  private static final AttributeKey<String> EVENT_NAME = AttributeKey.stringKey("event.name");
  private static final AttributeKey<String> RESULT =
      AttributeKey.stringKey("event_span_event_bridge.result");
  private static final Attributes NOT_EVENT = Attributes.of(RESULT, "skipped_not_event");
  private static final Attributes SPAN_INVALID = Attributes.of(RESULT, "skipped_span_invalid");
  private static final Attributes SPAN_NOT_RECORDING =
      Attributes.of(RESULT, "skipped_span_not_recording");
  private static final Attributes BRIDGED = Attributes.of(RESULT, "success");

  private final LongCounter processedCounter;

  private EventToSpanEventBridge(MeterProvider meterProvider) {
    this.processedCounter =
        meterProvider
            .meterBuilder(EventToSpanEventBridge.class.getName())
            .build()
            .counterBuilder("event_span_event_bridge.processed")
            .build();
  }

  public static EventToSpanEventBridge create(MeterProvider meterProvider) {
    return new EventToSpanEventBridge(meterProvider);
  }

  public static EventToSpanEventBridge create() {
    return new EventToSpanEventBridge(MeterProvider.noop());
  }

  @Override
  public void onEmit(Context context, ReadWriteLogRecord logRecord) {
    LogRecordData logRecordData = logRecord.toLogRecordData();
    String eventName = logRecordData.getAttributes().get(EVENT_NAME);
    if (eventName == null) {
      processedCounter.add(1, NOT_EVENT);
      return;
    }
    if (!logRecordData.getSpanContext().isValid()) {
      processedCounter.add(1, SPAN_INVALID);
      return;
    }
    Span currentSpan = Span.current();
    if (!currentSpan.isRecording()) {
      processedCounter.add(1, SPAN_NOT_RECORDING);
      return;
    }
    currentSpan.addEvent(
        eventName,
        toSpanEventAttributes(logRecordData),
        logRecordData.getTimestampEpochNanos(),
        TimeUnit.NANOSECONDS);
    processedCounter.add(1, BRIDGED);
  }

  @SuppressWarnings("unchecked")
  private static Attributes toSpanEventAttributes(LogRecordData logRecordData) {
    AttributesBuilder builder = Attributes.builder();
    logRecordData
        .getAttributes()
        .forEach(
            (key, value) -> {
              if (key.equals(EVENT_NAME)) {
                return;
              }
              putInBuilder(builder, (AttributeKey<? super Object>) key, value);
            });
    builder.put("severity", logRecordData.getSeverity().name());
    if (logRecordData.getSeverityText() != null) {
      builder.put("severity_text", logRecordData.getSeverityText());
    }
    int droppedAttributesCount =
        logRecordData.getTotalAttributeCount() - logRecordData.getAttributes().size();
    if (droppedAttributesCount > 0) {
      builder.put("dropped_attributes_count", droppedAttributesCount);
    }
    builder.put("body", logRecordData.getBody().asString());
    return builder.build();
  }

  private static <T> void putInBuilder(AttributesBuilder builder, AttributeKey<T> key, T value) {
    builder.put(key, value);
  }
}
