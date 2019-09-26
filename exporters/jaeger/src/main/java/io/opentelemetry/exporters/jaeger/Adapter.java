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

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Model;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.sdk.trace.export.SpanData;
import io.opentelemetry.sdk.trace.export.SpanData.TimedEvent;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Link;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.concurrent.ThreadSafe;

/** Adapts OpenTelemetry objects to Jaeger objects. */
@ThreadSafe
final class Adapter {
  private static final String KEY_LOG_MESSAGE = "message";
  private static final String KEY_SPAN_KIND = "span.kind";
  private static final String KEY_SPAN_STATUS_MESSAGE = "span.status.message";
  private static final String KEY_SPAN_STATUS_CODE = "span.status.code";

  private Adapter() {}

  /**
   * Converts a list of {@link Span} into a collection of Jaeger's {@link Model.Span}.
   *
   * @param spans the list of spans to be converted
   * @return the collection of Jaeger spans
   * @see #toJaeger(SpanData)
   */
  static Collection<Model.Span> toJaeger(List<SpanData> spans) {
    List<Model.Span> convertedList = new ArrayList<>(spans.size());
    for (SpanData span : spans) {
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
  static Model.Span toJaeger(SpanData span) {
    Model.Span.Builder target = Model.Span.newBuilder();

    target.setTraceId(TraceProtoUtils.toProtoTraceId(span.getTraceId()));
    target.setSpanId(TraceProtoUtils.toProtoSpanId(span.getSpanId()));
    target.setOperationName(span.getName());
    Timestamp startTimestamp = toProtoTimestamp(span.getStartTimestamp());
    target.setStartTime(startTimestamp);
    target.setDuration(
        Timestamps.between(startTimestamp, toProtoTimestamp(span.getEndTimestamp())));

    target.addAllTags(toKeyValues(span.getAttributes()));
    target.addAllLogs(toJaegerLogs(span.getTimedEvents()));
    target.addAllReferences(toSpanRefs(span.getLinks()));

    // add the parent span
    if (span.getParentSpanId().isValid()) {
      target.addReferences(
          Model.SpanRef.newBuilder()
              .setTraceId(TraceProtoUtils.toProtoTraceId(span.getTraceId()))
              .setSpanId(TraceProtoUtils.toProtoSpanId(span.getParentSpanId()))
              .setRefType(Model.SpanRefType.CHILD_OF));
    }

    if (span.getKind() != null) {
      target.addTags(
          Model.KeyValue.newBuilder().setKey(KEY_SPAN_KIND).setVStr(span.getKind().name()).build());
    }

    target.addTags(
        Model.KeyValue.newBuilder()
            .setKey(KEY_SPAN_STATUS_MESSAGE)
            .setVStr(span.getStatus().isOk() ? "" : span.getStatus().getDescription())
            .build());

    target.addTags(
        Model.KeyValue.newBuilder()
            .setKey(KEY_SPAN_STATUS_CODE)
            .setVInt64(span.getStatus().getCanonicalCode().value())
            .build());

    return target.build();
  }

  private static Timestamp toProtoTimestamp(SpanData.Timestamp startTimestamp) {
    return Timestamp.newBuilder()
        .setNanos(startTimestamp.getNanos())
        .setSeconds(startTimestamp.getSeconds())
        .build();
  }

  /**
   * Converts {@link Span.TimedEvents} into a collection of Jaeger's {@link Model.Log}.
   *
   * @param timeEvents the timed events to be converted
   * @return a collection of Jaeger logs
   * @see #toJaegerLog(TimedEvent)
   */
  @VisibleForTesting
  static Collection<Model.Log> toJaegerLogs(List<TimedEvent> timeEvents) {
    List<Model.Log> logs = new ArrayList<>(timeEvents.size());
    for (TimedEvent e : timeEvents) {
      logs.add(toJaegerLog(e));
    }
    return logs;
  }

  /**
   * Converts a {@link Span.TimedEvent} into Jaeger's {@link Model.Log}.
   *
   * @param timedEvent the timed event to be converted
   * @return a Jaeger log
   */
  @VisibleForTesting
  static Model.Log toJaegerLog(TimedEvent timedEvent) {
    Model.Log.Builder builder = Model.Log.newBuilder();
    builder.setTimestamp(toProtoTimestamp(timedEvent.getTimestamp()));

    // name is a top-level property in OpenTelemetry
    builder.addFields(
        Model.KeyValue.newBuilder()
            .setKey(KEY_LOG_MESSAGE)
            .setVStr(timedEvent.getEvent().getName())
            .build());
    builder.addAllFields(toKeyValues(timedEvent.getEvent().getAttributes()));

    return builder.build();
  }

  /**
   * Converts {@link Span.Attributes} into a collection of Jaeger's {@link Model.KeyValue}.
   *
   * @param attributes the span attributes
   * @return a collection of Jaeger key values
   * @see #toKeyValue(String, AttributeValue)
   */
  @VisibleForTesting
  static Collection<Model.KeyValue> toKeyValues(Map<String, AttributeValue> attributes) {
    ArrayList<Model.KeyValue> tags = new ArrayList<>(attributes.size());
    for (Entry<String, AttributeValue> entry : attributes.entrySet()) {
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
  @VisibleForTesting
  static Model.KeyValue toKeyValue(String key, AttributeValue value) {
    Model.KeyValue.Builder builder = Model.KeyValue.newBuilder();
    builder.setKey(key);

    switch (value.getType()) {
      case STRING:
        builder.setVStr(value.getStringValue());
        break;
      case LONG:
        builder.setVInt64(value.getLongValue());
        break;
      case BOOLEAN:
        builder.setVBool(value.getBooleanValue());
        break;
      case DOUBLE:
        builder.setVFloat64(value.getDoubleValue());
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
  @VisibleForTesting
  static Collection<Model.SpanRef> toSpanRefs(List<Link> links) {
    List<Model.SpanRef> spanRefs = new ArrayList<>(links.size());
    for (Link link : links) {
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
  @VisibleForTesting
  static Model.SpanRef toSpanRef(Link link) {
    Model.SpanRef.Builder builder = Model.SpanRef.newBuilder();
    builder.setTraceId(TraceProtoUtils.toProtoTraceId(link.getContext().getTraceId()));
    builder.setSpanId(TraceProtoUtils.toProtoSpanId(link.getContext().getSpanId()));

    // we can assume that all links are *follows from*
    // https://github.com/open-telemetry/opentelemetry-java/issues/475
    // https://github.com/open-telemetry/opentelemetry-java/pull/481/files#r312577862
    builder.setRefType(Model.SpanRefType.FOLLOWS_FROM);

    return builder.build();
  }
}
