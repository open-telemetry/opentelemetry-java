/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.jaeger;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.exporter.jaeger.proto.api_v2.Model;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import javax.annotation.concurrent.ThreadSafe;

/** Adapts OpenTelemetry objects to Jaeger objects. */
@ThreadSafe
final class Adapter {
  static final AttributeKey<Boolean> KEY_ERROR = booleanKey("error");
  static final String KEY_LOG_EVENT = "event";
  static final String KEY_EVENT_DROPPED_ATTRIBUTES_COUNT = "otel.event.dropped_attributes_count";
  static final String KEY_SPAN_KIND = "span.kind";
  static final String KEY_SPAN_STATUS_MESSAGE = "otel.status_description";
  static final String KEY_SPAN_STATUS_CODE = "otel.status_code";
  static final String KEY_INSTRUMENTATION_LIBRARY_NAME = "otel.library.name";
  static final String KEY_INSTRUMENTATION_LIBRARY_VERSION = "otel.library.version";

  private Adapter() {}

  /**
   * Converts a list of {@link SpanData} into a collection of Jaeger's {@link Model.Span}.
   *
   * @param spans the list of spans to be converted
   * @return the collection of Jaeger spans
   * @see #toJaeger(SpanData)
   */
  static Collection<Model.Span> toJaeger(Collection<SpanData> spans) {
    List<Model.Span> convertedList = new ArrayList<>(spans.size());
    for (SpanData span : spans) {
      convertedList.add(toJaeger(span));
    }
    return convertedList;
  }

  /**
   * Converts a single {@link SpanData} into a Jaeger's {@link Model.Span}.
   *
   * @param span the span to be converted
   * @return the Jaeger span
   */
  static Model.Span toJaeger(SpanData span) {
    Model.Span.Builder target = Model.Span.newBuilder();

    SpanContext spanContext = span.getSpanContext();
    target.setTraceId(ByteString.copyFrom(spanContext.getTraceIdBytes()));
    target.setSpanId(ByteString.copyFrom(spanContext.getSpanIdBytes()));
    target.setOperationName(span.getName());
    Timestamp startTimestamp = Timestamps.fromNanos(span.getStartEpochNanos());
    target.setStartTime(startTimestamp);
    target.setDuration(
        Timestamps.between(startTimestamp, Timestamps.fromNanos(span.getEndEpochNanos())));

    target.addAllTags(toKeyValues(span.getAttributes()));
    target.addAllLogs(toJaegerLogs(span.getEvents()));
    target.addAllReferences(toSpanRefs(span.getLinks()));

    // add the parent span
    SpanContext parentSpanContext = span.getParentSpanContext();
    if (parentSpanContext.isValid()) {
      target.addReferences(
          Model.SpanRef.newBuilder()
              .setTraceId(ByteString.copyFrom(parentSpanContext.getTraceIdBytes()))
              .setSpanId(ByteString.copyFrom(parentSpanContext.getSpanIdBytes()))
              .setRefType(Model.SpanRefType.CHILD_OF));
    }

    if (span.getKind() != SpanKind.INTERNAL) {
      target.addTags(
          Model.KeyValue.newBuilder()
              .setKey(KEY_SPAN_KIND)
              .setVStr(span.getKind().name().toLowerCase(Locale.ROOT))
              .build());
    }

    if (!span.getStatus().getDescription().isEmpty()) {
      target.addTags(
          Model.KeyValue.newBuilder()
              .setKey(KEY_SPAN_STATUS_MESSAGE)
              .setVStr(span.getStatus().getDescription())
              .build());
    }

    if (span.getStatus().getStatusCode() != StatusCode.UNSET) {
      target.addTags(
          Model.KeyValue.newBuilder()
              .setKey(KEY_SPAN_STATUS_CODE)
              .setVStr(span.getStatus().getStatusCode().name())
              .build());
    }

    if (span.getInstrumentationLibraryInfo().getName() != null) {
      target.addTags(
          Model.KeyValue.newBuilder()
              .setKey(KEY_INSTRUMENTATION_LIBRARY_NAME)
              .setVStr(span.getInstrumentationLibraryInfo().getName())
              .build());
    }

    if (span.getInstrumentationLibraryInfo().getVersion() != null) {
      target.addTags(
          Model.KeyValue.newBuilder()
              .setKey(KEY_INSTRUMENTATION_LIBRARY_VERSION)
              .setVStr(span.getInstrumentationLibraryInfo().getVersion())
              .build());
    }

    if (span.getStatus().getStatusCode() == StatusCode.ERROR) {
      target.addTags(toKeyValue(KEY_ERROR, true));
    }

    return target.build();
  }

  /**
   * Converts {@link EventData}s into a collection of Jaeger's {@link Model.Log}.
   *
   * @param timeEvents the timed events to be converted
   * @return a collection of Jaeger logs
   * @see #toJaegerLog(EventData)
   */
  @VisibleForTesting
  static Collection<Model.Log> toJaegerLogs(List<EventData> timeEvents) {
    List<Model.Log> logs = new ArrayList<>(timeEvents.size());
    for (EventData e : timeEvents) {
      logs.add(toJaegerLog(e));
    }
    return logs;
  }

  /**
   * Converts a {@link EventData} into Jaeger's {@link Model.Log}.
   *
   * @param event the timed event to be converted
   * @return a Jaeger log
   */
  @VisibleForTesting
  static Model.Log toJaegerLog(EventData event) {
    Model.Log.Builder builder = Model.Log.newBuilder();
    builder.setTimestamp(Timestamps.fromNanos(event.getEpochNanos()));

    // name is a top-level property in OpenTelemetry
    builder.addFields(
        Model.KeyValue.newBuilder().setKey(KEY_LOG_EVENT).setVStr(event.getName()).build());

    int droppedAttributesCount = event.getDroppedAttributesCount();
    if (droppedAttributesCount > 0) {
      builder.addFields(
          Model.KeyValue.newBuilder()
              .setKey(KEY_EVENT_DROPPED_ATTRIBUTES_COUNT)
              .setVInt64(droppedAttributesCount)
              .build());
    }
    builder.addAllFields(toKeyValues(event.getAttributes()));

    return builder.build();
  }

  /**
   * Converts a map of attributes into a collection of Jaeger's {@link Model.KeyValue}.
   *
   * @param attributes the span attributes
   * @return a collection of Jaeger key values
   * @see #toKeyValue
   */
  @VisibleForTesting
  static Collection<Model.KeyValue> toKeyValues(Attributes attributes) {
    final List<Model.KeyValue> tags = new ArrayList<>(attributes.size());
    attributes.forEach((key, value) -> tags.add(toKeyValue(key, value)));
    return tags;
  }

  /**
   * Converts the given {@link AttributeKey} and value into Jaeger's {@link Model.KeyValue}.
   *
   * @param key the entry key as string
   * @param value the entry value
   * @return a Jaeger key value
   */
  @VisibleForTesting
  static Model.KeyValue toKeyValue(AttributeKey<?> key, Object value) {
    Model.KeyValue.Builder builder = Model.KeyValue.newBuilder();
    builder.setKey(key.getKey());

    switch (key.getType()) {
      case STRING:
        builder.setVStr((String) value);
        builder.setVType(Model.ValueType.STRING);
        break;
      case LONG:
        builder.setVInt64((long) value);
        builder.setVType(Model.ValueType.INT64);
        break;
      case BOOLEAN:
        builder.setVBool((boolean) value);
        builder.setVType(Model.ValueType.BOOL);
        break;
      case DOUBLE:
        builder.setVFloat64((double) value);
        builder.setVType(Model.ValueType.FLOAT64);
        break;
      case STRING_ARRAY:
      case LONG_ARRAY:
      case BOOLEAN_ARRAY:
      case DOUBLE_ARRAY:
        builder.setVStr(new Gson().toJson(value));
        builder.setVType(Model.ValueType.STRING);
        break;
    }
    return builder.build();
  }

  /**
   * Converts {@link LinkData}s into a collection of Jaeger's {@link Model.SpanRef}.
   *
   * @param links the span's links property to be converted
   * @return a collection of Jaeger span references
   */
  @VisibleForTesting
  static Collection<Model.SpanRef> toSpanRefs(List<LinkData> links) {
    List<Model.SpanRef> spanRefs = new ArrayList<>(links.size());
    for (LinkData link : links) {
      spanRefs.add(toSpanRef(link));
    }
    return spanRefs;
  }

  /**
   * Converts a single {@link LinkData} into a Jaeger's {@link Model.SpanRef}.
   *
   * @param link the OpenTelemetry link to be converted
   * @return the Jaeger span reference
   */
  @VisibleForTesting
  static Model.SpanRef toSpanRef(LinkData link) {
    Model.SpanRef.Builder builder = Model.SpanRef.newBuilder();
    builder.setTraceId(ByteString.copyFrom(link.getSpanContext().getTraceIdBytes()));
    builder.setSpanId(ByteString.copyFrom(link.getSpanContext().getSpanIdBytes()));

    // we can assume that all links are *follows from*
    // https://github.com/open-telemetry/opentelemetry-java/issues/475
    // https://github.com/open-telemetry/opentelemetry-java/pull/481/files#r312577862
    builder.setRefType(Model.SpanRefType.FOLLOWS_FROM);

    return builder.build();
  }
}
