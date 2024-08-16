/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.incubator.events.EventBuilder;
import io.opentelemetry.api.incubator.events.EventLogger;
import io.opentelemetry.api.incubator.events.EventLoggerProvider;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** A processor that records span events as events. */
public class SpanEventToEventBridge implements SpanProcessor {

  private final EventLoggerProvider eventLoggerProvider;
  private final LongCounter processedCounter;

  private SpanEventToEventBridge(
      EventLoggerProvider eventLoggerProvider, MeterProvider meterProvider) {
    this.eventLoggerProvider = eventLoggerProvider;
    this.processedCounter =
        meterProvider
            .meterBuilder(SpanEventToEventBridge.class.getName())
            .build()
            .counterBuilder("span_event_event_bridge.processed")
            .build();
  }

  public static SpanEventToEventBridge create(
      EventLoggerProvider eventLoggerProvider, MeterProvider meterProvider) {
    return new SpanEventToEventBridge(eventLoggerProvider, meterProvider);
  }

  public static SpanEventToEventBridge create(EventLoggerProvider eventLoggerProvider) {
    return create(eventLoggerProvider, MeterProvider.noop());
  }

  @Override
  public void onStart(Context parentContext, ReadWriteSpan span) {}

  @Override
  public boolean isStartRequired() {
    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onEnd(ReadableSpan span) {
    SpanData spanData = span.toSpanData();
    List<EventData> spanEvents = spanData.getEvents();
    if (spanEvents.size() == 0) {
      return;
    }
    // TODO: scopeVersion, scopeSchemaUrl
    EventLogger eventLogger =
        eventLoggerProvider.get(spanData.getInstrumentationScopeInfo().getName());
    for (EventData spanEvent : spanEvents) {
      EventBuilder builder =
          eventLogger
              .builder("span-event." + spanEvent.getName())
              .setTimestamp(spanEvent.getEpochNanos(), TimeUnit.NANOSECONDS);
      spanEvent
          .getAttributes()
          .forEach(
              (key, value) -> putInBuilder(builder, (AttributeKey<? super Object>) key, value));
      builder.emit();
    }
    processedCounter.add(spanEvents.size());
  }

  @Override
  public boolean isEndRequired() {
    return true;
  }

  private static <T> void putInBuilder(EventBuilder builder, AttributeKey<T> key, T value) {
    builder.put(key, value);
  }
}
