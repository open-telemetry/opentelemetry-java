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

package io.opentelemetry.sdk.trace;

import io.opentelemetry.common.Timestamp;
import io.opentelemetry.sdk.internal.TimestampConverter;
import io.opentelemetry.sdk.trace.SpanData.TimedEvent;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.util.Events;
import java.util.ArrayList;
import java.util.List;

/** An adapter that can convert a ReadableSpan into a SpanData. */
public class ReadableSpanAdapter {

  /**
   * Converts a ReadableSpan into a new instance of SpanData.
   *
   * @param span A ReadableSpan.
   * @return A newly created SpanData instance based on the data in the ReadableSpan.
   */
  public SpanData adapt(ReadableSpan span) {
    TimestampConverter timestampConverter = span.getTimestampConverter();
    Timestamp startTimestamp = timestampConverter.nanoTimeToTimestampDelta(span.getStartNanoTime());
    Timestamp endTimestamp = timestampConverter.nanoTimeToTimestampDelta(span.getEndNanoTime());
    SpanId parentSpanId = span.getParentSpanId();
    parentSpanId = parentSpanId == null ? SpanId.getInvalid() : parentSpanId;
    return SpanData.newBuilder()
        .setName(span.getName())
        .setTraceId(span.getSpanContext().getTraceId())
        .setSpanId(span.getSpanContext().getSpanId())
        .setTraceFlags(span.getSpanContext().getTraceFlags())
        .setTracestate(span.getSpanContext().getTracestate())
        .setAttributes(span.getAttributes())
        .setStartTimestamp(startTimestamp)
        .setEndTimestamp(endTimestamp)
        .setKind(span.getKind())
        .setLinks(span.getLinks())
        .setParentSpanId(parentSpanId)
        .setResource(span.getResource())
        .setStatus(span.getStatus())
        .setTimedEvents(adaptTimedEvents(span))
        .build();
  }

  private static List<TimedEvent> adaptTimedEvents(ReadableSpan span) {
    List<io.opentelemetry.sdk.trace.TimedEvent> sourceEvents = span.getTimedEvents();
    List<TimedEvent> result = new ArrayList<>(sourceEvents.size());
    for (io.opentelemetry.sdk.trace.TimedEvent sourceEvent : sourceEvents) {
      result.add(adaptTimedEvent(sourceEvent, span.getTimestampConverter()));
    }
    return result;
  }

  private static TimedEvent adaptTimedEvent(
      io.opentelemetry.sdk.trace.TimedEvent sourceEvent, TimestampConverter timestampConverter) {
    Timestamp timestamp = timestampConverter.nanoTimeToTimestampDelta(sourceEvent.getNanotime());
    io.opentelemetry.trace.Event event =
        Events.create(sourceEvent.getName(), sourceEvent.getAttributes());
    return TimedEvent.create(timestamp, event);
  }
}
