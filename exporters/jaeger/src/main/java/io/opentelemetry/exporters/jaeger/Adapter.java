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

package io.opentelemetry.exporters.jaeger;

import com.google.protobuf.Duration;
import com.google.protobuf.Timestamp;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Model;
import io.opentelemetry.proto.trace.v1.AttributeValue;
import io.opentelemetry.proto.trace.v1.Span;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/** Adapts OpenTelemetry objects to Jaeger objects. */
class Adapter {
  private Adapter() {}

  /**
   * Converts a list of {@link Span} into a collection of Jaeger's {@link Model.Span}.
   *
   * @param spans the list of spans to be converted
   * @return the collection of Jaeger spans
   * @see #toJaeger(Span)
   */
  static Collection<Model.Span> toJaeger(List<Span> spans) {
    List<Model.Span> convertedList = new ArrayList<>(spans.size());
    for (Span span : spans) {
      convertedList.add(toJaeger(span));
    }
    return convertedList;
  }

  /**
   * Converts a single {@link Span} into a Jaeger's {@link Model.Span}.
   *
   * @param span the span to be converted
   * @return the Jaeger span
   */
  static Model.Span toJaeger(Span span) {
    Model.Span.Builder target = Model.Span.newBuilder();

    target.setTraceId(span.getTraceId());
    target.setSpanId(span.getSpanId());
    target.setOperationName(span.getName());
    target.setStartTime(span.getStartTime());
    target.setDuration(getDuration(span.getStartTime(), span.getEndTime()));

    target.addAllTags(toKeyValues(span.getAttributes()));
    target.addAllLogs(toJaegerLogs(span.getTimeEvents()));
    target.addAllReferences(toSpanRefs(span.getLinks()));

    if (span.getKind() != Span.SpanKind.SPAN_KIND_UNSPECIFIED) {
      target.addTags(
          Model.KeyValue.newBuilder()
              .setKey("span.kind")
              .setVStr(span.getKind().getValueDescriptor().getName())
              .build());
    }

    return target.build();
  }

  /**
   * Converts {@link Span.TimedEvents} into a collection of Jaeger's {@link Model.Log}.
   *
   * @param timeEvents the timed events to be converted
   * @return a collection of Jaeger logs
   * @see #toJaegerLog(Span.TimedEvent)
   */
  static Collection<Model.Log> toJaegerLogs(Span.TimedEvents timeEvents) {
    List<Model.Log> logs = new ArrayList<>(timeEvents.getTimedEventCount());
    for (Span.TimedEvent e : timeEvents.getTimedEventList()) {
      logs.add(toJaegerLog(e));
    }
    return logs;
  }

  /**
   * Converts a {@link Span.TimedEvent} into Jaeger's {@link Model.Log}.
   *
   * @param timeEvent the timed event to be converted
   * @return a Jaeger log
   */
  static Model.Log toJaegerLog(Span.TimedEvent timeEvent) {
    Model.Log.Builder builder = Model.Log.newBuilder();
    builder.setTimestamp(timeEvent.getTime());

    Span.TimedEvent.Event event = timeEvent.getEvent();

    // name is a top-level property in OpenTelemetry
    builder.addFields(
        Model.KeyValue.newBuilder().setKey("message").setVStr(event.getName()).build());
    builder.addAllFields(toKeyValues(event.getAttributes()));

    return builder.build();
  }

  /**
   * Converts {@link Span.Attributes} into a collection of Jaeger's {@link Model.KeyValue}.
   *
   * @param attributes the span attributes
   * @return a collection of Jaeger key values
   * @see #toKeyValue(String, AttributeValue)
   */
  static Collection<Model.KeyValue> toKeyValues(Span.Attributes attributes) {
    ArrayList<Model.KeyValue> tags = new ArrayList<>(attributes.getAttributeMapCount());
    for (Map.Entry<String, AttributeValue> entry : attributes.getAttributeMapMap().entrySet()) {
      tags.add(toKeyValue(entry.getKey(), entry.getValue()));
    }
    return tags;
  }

  /**
   * Converts the given key and {@link AttributeValue} into Jaeger's {@link Model.KeyValue}.
   *
   * @param key the entry key as string
   * @param value the entry value
   * @return a Jaeger key value
   */
  static Model.KeyValue toKeyValue(String key, AttributeValue value) {
    Model.KeyValue.Builder builder = Model.KeyValue.newBuilder();
    builder.setKey(key);

    switch (value.getValueCase()) {
      case STRING_VALUE:
        builder.setVStr(value.getStringValue());
        break;
      case INT_VALUE:
        builder.setVInt64(value.getIntValue());
        break;
      case BOOL_VALUE:
        builder.setVBool(value.getBoolValue());
        break;
      case DOUBLE_VALUE:
        builder.setVFloat64(value.getDoubleValue());
        break;
      case VALUE_NOT_SET:
        break;
    }

    return builder.build();
  }

  /**
   * Converts {@link Span.Links} into a collection of Jaeger's {@link Model.SpanRef}.
   *
   * @param links the span's links property to be converted
   * @return a collection of Jaeger span references
   */
  static Collection<Model.SpanRef> toSpanRefs(Span.Links links) {
    List<Model.SpanRef> spanRefs = new ArrayList<>(links.getLinkCount());
    for (Span.Link link : links.getLinkList()) {
      spanRefs.add(toSpanRef(link));
    }
    return spanRefs;
  }

  /**
   * Converts a single {@link Span.Link} into a Jaeger's {@link Model.SpanRef}.
   *
   * @param link the OpenTelemetry link to be converted
   * @return the Jaeger span reference
   */
  static Model.SpanRef toSpanRef(Span.Link link) {
    Model.SpanRef.Builder builder = Model.SpanRef.newBuilder();
    builder.setTraceId(link.getTraceId());
    builder.setSpanId(link.getSpanId());

    // we can assume that all links are parents
    // https://github.com/open-telemetry/opentelemetry-java/issues/475
    builder.setRefType(Model.SpanRefType.CHILD_OF);

    return builder.build();
  }

  /**
   * Calculates the duration between two timestamps.
   *
   * @param start the timestamp representing the span's start time
   * @param end the timestamp representing the span's end time
   * @return the span's duration
   */
  static Duration getDuration(Timestamp start, Timestamp end) {
    long secs = end.getSeconds() - start.getSeconds();
    int nanos = end.getNanos() - start.getNanos();

    if (secs < 0 && nanos > 0) { // this happens when the end is *before* the start
      secs += 1;
      nanos -= 1000000000;
    } else if (secs > 0 && nanos < 0) {
      secs -= 1;
      nanos += 1000000000;
    }

    return Duration.newBuilder().setSeconds(secs).setNanos(nanos).build();
  }
}
