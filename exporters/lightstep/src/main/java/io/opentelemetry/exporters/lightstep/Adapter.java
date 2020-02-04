/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.exporters.lightstep;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Durations;
import com.google.protobuf.util.Timestamps;
import com.lightstep.tracer.grpc.KeyValue;
import com.lightstep.tracer.grpc.Log;
import com.lightstep.tracer.grpc.Reference;
import com.lightstep.tracer.grpc.Reference.Relationship;
import com.lightstep.tracer.grpc.Span;
import com.lightstep.tracer.grpc.SpanContext;
import io.opentelemetry.exporters.otprotocol.TraceProtoUtils;
import io.opentelemetry.sdk.trace.SpanData;
import io.opentelemetry.sdk.trace.SpanData.TimedEvent;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Adapter {
  static final String KEY_LOG_MESSAGE = "message";
  static final String KEY_SPAN_KIND = "span.kind";
  static final String KEY_SPAN_STATUS_MESSAGE = "span.status.message";
  static final String KEY_SPAN_STATUS_CODE = "span.status.code";

  private Adapter() {}

  static List<Span> toLightStepSpans(List<SpanData> spanDatas) {
    List<Span> spans = new ArrayList<>();
    for (SpanData spanData : spanDatas) {
      spans.add(toLightStepSpan(spanData));
    }
    return spans;
  }

  static Span toLightStepSpan(SpanData spanData) {
    final Span.Builder builder = Span.newBuilder();
    builder.setOperationName(spanData.getName());

    long traceId = traceIdToLong(spanData.getTraceId());
    long spanId = spanIdToLong(spanData.getSpanId());

    final SpanContext spanContext =
        SpanContext.newBuilder().setTraceId(traceId).setSpanId(spanId).build();

    builder.setSpanContext(spanContext);

    final Timestamp startTimestamp = Timestamps.fromNanos(spanData.getStartEpochNanos());
    final Timestamp endTimestamp = Timestamps.fromNanos(spanData.getEndEpochNanos());
    builder.setStartTimestamp(startTimestamp);
    builder.setDurationMicros(Durations.toMicros(Timestamps.between(startTimestamp, endTimestamp)));

    builder.addAllTags(toKeyValues(spanData.getAttributes()));

    builder.addAllLogs(toLogs(spanData.getTimedEvents()));

    builder.addAllReferences(toReferences(spanData.getLinks()));

    // add the parent span
    if (spanData.getParentSpanId().isValid()) {
      final Reference.Builder referenceBuilder = Reference.newBuilder();
      final long parentSpanId = spanIdToLong(spanData.getParentSpanId());
      referenceBuilder.setSpanContext(
          SpanContext.newBuilder().setTraceId(traceId).setSpanId(parentSpanId).build());
      referenceBuilder.setRelationship(Relationship.CHILD_OF);
      builder.addReferences(referenceBuilder.build());
    }

    if (spanData.getKind() != null) {
      builder.addTags(
          KeyValue.newBuilder()
              .setKey(KEY_SPAN_KIND)
              .setStringValue(spanData.getKind().name())
              .build());
    }

    if (spanData.getStatus().getDescription() != null) {
      builder.addTags(
          KeyValue.newBuilder()
              .setKey(KEY_SPAN_STATUS_MESSAGE)
              .setStringValue(spanData.getStatus().getDescription())
              .build());
    }

    builder.addTags(
        KeyValue.newBuilder()
            .setKey(KEY_SPAN_STATUS_CODE)
            .setIntValue(spanData.getStatus().getCanonicalCode().value())
            .build());

    return builder.build();
  }

  @VisibleForTesting
  static List<Reference> toReferences(List<Link> links) {
    final List<Reference> references = new ArrayList<>();
    for (Link link : links) {
      references.add(toReference(link));
    }
    return references;
  }

  @VisibleForTesting
  static Reference toReference(Link link) {
    final Reference.Builder builder = Reference.newBuilder();
    final long traceId = traceIdToLong(link.getContext().getTraceId());
    final long spanId = spanIdToLong(link.getContext().getSpanId());
    builder.setSpanContext(SpanContext.newBuilder().setTraceId(traceId).setSpanId(spanId).build());
    builder.setRelationship(Relationship.FOLLOWS_FROM);

    return builder.build();
  }

  @VisibleForTesting
  static List<Log> toLogs(List<TimedEvent> events) {
    final List<Log> logs = new ArrayList<>();
    for (TimedEvent event : events) {
      logs.add(toLog(event));
    }
    return logs;
  }

  @VisibleForTesting
  static Log toLog(TimedEvent event) {
    final Log.Builder builder = Log.newBuilder();
    builder.setTimestamp(Timestamps.fromNanos(event.getEpochNanos()));
    builder.addFields(
        KeyValue.newBuilder().setKey(KEY_LOG_MESSAGE).setStringValue(event.getName()).build());
    builder.addAllFields(toKeyValues(event.getAttributes()));

    return builder.build();
  }

  @VisibleForTesting
  static List<KeyValue> toKeyValues(Map<String, AttributeValue> attributes) {
    final List<KeyValue> keyValues = new ArrayList<>();
    for (Entry<String, AttributeValue> entry : attributes.entrySet()) {
      keyValues.add(toKeyValue(entry.getKey(), entry.getValue()));
    }

    return keyValues;
  }

  @VisibleForTesting
  static KeyValue toKeyValue(String key, AttributeValue value) {
    final KeyValue.Builder builder = KeyValue.newBuilder().setKey(key);

    switch (value.getType()) {
      case STRING:
        builder.setStringValue(value.getStringValue());
        break;
      case LONG:
        builder.setIntValue(value.getLongValue());
        break;
      case BOOLEAN:
        builder.setBoolValue(value.getBooleanValue());
        break;
      case DOUBLE:
        builder.setDoubleValue(value.getDoubleValue());
        break;
    }

    return builder.build();
  }

  private static long fromByteArray(byte[] bytes) {
    return ByteBuffer.wrap(bytes).getLong();
  }

  private static long traceIdToLong(final TraceId traceId) {
    if (traceId == null) {
      return 0L;
    }
    final ByteString protoTraceId = TraceProtoUtils.toProtoTraceId(traceId);
    return fromByteArray(protoTraceId.toByteArray());
  }

  private static long spanIdToLong(final SpanId spanId) {
    if (spanId == null) {
      return 0L;
    }
    final ByteString protoSpanId = TraceProtoUtils.toProtoSpanId(spanId);
    return fromByteArray(protoSpanId.toByteArray());
  }
}
