/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.jaeger;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.opentelemetry.api.common.AttributeConsumer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.ReadableAttributes;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.exporter.jaeger.proto.api_v2.Model;
import io.opentelemetry.sdk.extension.otproto.TraceProtoUtils;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Event;
import io.opentelemetry.sdk.trace.data.SpanData.Link;
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
  static final String KEY_SPAN_STATUS_MESSAGE = "span.status.message";
  static final String KEY_SPAN_STATUS_CODE = "span.status.code";
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

    target.setTraceId(TraceProtoUtils.toProtoTraceId(span.getTraceId()));
    target.setSpanId(TraceProtoUtils.toProtoSpanId(span.getSpanId()));
    target.setOperationName(span.getName());
    Timestamp startTimestamp = Timestamps.fromNanos(span.getStartEpochNanos());
    target.setStartTime(startTimestamp);
    target.setDuration(
        Timestamps.between(startTimestamp, Timestamps.fromNanos(span.getEndEpochNanos())));

    target.addAllTags(toKeyValues(span.getAttributes()));
    target.addAllLogs(toJaegerLogs(span.getEvents()));
    target.addAllReferences(toSpanRefs(span.getLinks()));

    // add the parent span
    if (SpanId.isValid(span.getParentSpanId())) {
      target.addReferences(
          Model.SpanRef.newBuilder()
              .setTraceId(TraceProtoUtils.toProtoTraceId(span.getTraceId()))
              .setSpanId(TraceProtoUtils.toProtoSpanId(span.getParentSpanId()))
              .setRefType(Model.SpanRefType.CHILD_OF));
    }

    if (span.getKind() != null) {
      target.addTags(
          Model.KeyValue.newBuilder()
              .setKey(KEY_SPAN_KIND)
              .setVStr(span.getKind().name().toLowerCase(Locale.ROOT))
              .build());
    }

    target.addTags(
        Model.KeyValue.newBuilder()
            .setKey(KEY_SPAN_STATUS_MESSAGE)
            .setVStr(
                span.getStatus().getDescription() == null ? "" : span.getStatus().getDescription())
            .build());

    target.addTags(
        Model.KeyValue.newBuilder()
            .setKey(KEY_SPAN_STATUS_CODE)
            .setVInt64(span.getStatus().getStatusCode().value())
            .setVType(Model.ValueType.INT64)
            .build());

    target.addTags(
        Model.KeyValue.newBuilder()
            .setKey(KEY_INSTRUMENTATION_LIBRARY_NAME)
            .setVStr(span.getInstrumentationLibraryInfo().getName())
            .build());

    if (span.getInstrumentationLibraryInfo().getVersion() != null) {
      target.addTags(
          Model.KeyValue.newBuilder()
              .setKey(KEY_INSTRUMENTATION_LIBRARY_VERSION)
              .setVStr(span.getInstrumentationLibraryInfo().getVersion())
              .build());
    }

    if (!span.getStatus().isOk()) {
      target.addTags(toKeyValue(KEY_ERROR, true));
    }

    return target.build();
  }

  /**
   * Converts {@link Event}s into a collection of Jaeger's {@link Model.Log}.
   *
   * @param timeEvents the timed events to be converted
   * @return a collection of Jaeger logs
   * @see #toJaegerLog(Event)
   */
  @VisibleForTesting
  static Collection<Model.Log> toJaegerLogs(List<Event> timeEvents) {
    List<Model.Log> logs = new ArrayList<>(timeEvents.size());
    for (Event e : timeEvents) {
      logs.add(toJaegerLog(e));
    }
    return logs;
  }

  /**
   * Converts a {@link Event} into Jaeger's {@link Model.Log}.
   *
   * @param event the timed event to be converted
   * @return a Jaeger log
   */
  @VisibleForTesting
  static Model.Log toJaegerLog(Event event) {
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
  static Collection<Model.KeyValue> toKeyValues(ReadableAttributes attributes) {
    final List<Model.KeyValue> tags = new ArrayList<>(attributes.size());
    attributes.forEach(
        new AttributeConsumer() {
          @Override
          public <T> void accept(AttributeKey<T> key, T value) {
            tags.add(toKeyValue(key, value));
          }
        });
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
  static <T> Model.KeyValue toKeyValue(AttributeKey<T> key, T value) {
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
   * Converts {@link Link}s into a collection of Jaeger's {@link Model.SpanRef}.
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
   * Converts a single {@link Link} into a Jaeger's {@link Model.SpanRef}.
   *
   * @param link the OpenTelemetry link to be converted
   * @return the Jaeger span reference
   */
  @VisibleForTesting
  static Model.SpanRef toSpanRef(Link link) {
    Model.SpanRef.Builder builder = Model.SpanRef.newBuilder();
    builder.setTraceId(TraceProtoUtils.toProtoTraceId(link.getContext().getTraceIdAsHexString()));
    builder.setSpanId(TraceProtoUtils.toProtoSpanId(link.getContext().getSpanIdAsHexString()));

    // we can assume that all links are *follows from*
    // https://github.com/open-telemetry/opentelemetry-java/issues/475
    // https://github.com/open-telemetry/opentelemetry-java/pull/481/files#r312577862
    builder.setRefType(Model.SpanRefType.FOLLOWS_FROM);

    return builder.build();
  }
}
