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

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Span.Attributes;
import io.opentelemetry.proto.trace.v1.Span.Links;
import io.opentelemetry.proto.trace.v1.Span.SpanKind;
import io.opentelemetry.proto.trace.v1.Span.TimedEvents;
import io.opentelemetry.resources.Resource;
import io.opentelemetry.sdk.internal.TimestampConverter;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.Tracestate;
import java.util.Collection;
import java.util.Map;

// Utilities to convert Span SDK to proto representation of the Span.
final class TraceProtoUtils {

  private TraceProtoUtils() {}

  static ByteString toProtoTraceId(TraceId traceId) {
    byte[] traceIdBytes = new byte[TraceId.getSize()];
    traceId.copyBytesTo(traceIdBytes, 0);
    return ByteString.copyFrom(traceIdBytes);
  }

  static ByteString toProtoSpanId(SpanId spanId) {
    byte[] spanIdBytes = new byte[SpanId.getSize()];
    spanId.copyBytesTo(spanIdBytes, 0);
    return ByteString.copyFrom(spanIdBytes);
  }

  static Span.Tracestate toProtoTracestate(Tracestate tracestate) {
    Span.Tracestate.Builder builder = Span.Tracestate.newBuilder();
    for (Tracestate.Entry entry : tracestate.getEntries()) {
      builder.addEntries(
          Span.Tracestate.Entry.newBuilder().setKey(entry.getKey()).setValue(entry.getValue()));
    }
    return builder.build();
  }

  static io.opentelemetry.proto.resource.v1.Resource toProtoResource(Resource resource) {
    io.opentelemetry.proto.resource.v1.Resource.Builder builder =
        io.opentelemetry.proto.resource.v1.Resource.newBuilder();
    for (Map.Entry<String, String> entry : resource.getLabels().entrySet()) {
      builder.putLabels(entry.getKey(), entry.getValue());
    }
    return builder.build();
  }

  static SpanKind toProtoKind(Kind kind) {
    switch (kind) {
      case CLIENT:
        return SpanKind.CLIENT;
      case SERVER:
        return SpanKind.SERVER;
      case CONSUMER:
        return SpanKind.CONSUMER;
      case PRODUCER:
        return SpanKind.PRODUCER;
      case INTERNAL:
        return SpanKind.INTERNAL;
    }
    return SpanKind.SPAN_KIND_UNSPECIFIED;
  }

  static Attributes toProtoAttributes(Map<String, AttributeValue> attributes, int droppedCount) {
    Attributes.Builder builder = Attributes.newBuilder();
    builder.setDroppedAttributesCount(droppedCount);
    for (Map.Entry<String, AttributeValue> attribute : attributes.entrySet()) {
      builder.putAttributeMap(attribute.getKey(), toProtoAttributeValue(attribute.getValue()));
    }
    return builder.build();
  }

  @VisibleForTesting
  static io.opentelemetry.proto.trace.v1.AttributeValue toProtoAttributeValue(
      AttributeValue attributeValue) {
    io.opentelemetry.proto.trace.v1.AttributeValue.Builder builder =
        io.opentelemetry.proto.trace.v1.AttributeValue.newBuilder();
    switch (attributeValue.getType()) {
      case BOOLEAN:
        builder.setBoolValue(attributeValue.getBooleanValue());
        break;
      case DOUBLE:
        builder.setDoubleValue(attributeValue.getDoubleValue());
        break;
      case LONG:
        builder.setIntValue(attributeValue.getLongValue());
        break;
      case STRING:
        builder.setStringValue(attributeValue.getStringValue());
    }
    return builder.build();
  }

  static TimedEvents toProtoTimedEvents(
      Collection<TimedEvent> events, int droppedCount, TimestampConverter converter) {
    TimedEvents.Builder builder = TimedEvents.newBuilder();
    builder.setDroppedTimedEventsCount(droppedCount);
    for (TimedEvent timedEvent : events) {
      builder.addTimedEvent(toProtoTimedEvent(timedEvent, converter));
    }
    return builder.build();
  }

  @VisibleForTesting
  static Span.TimedEvent toProtoTimedEvent(TimedEvent timedEvent, TimestampConverter converter) {
    Span.TimedEvent.Builder builder = Span.TimedEvent.newBuilder();
    builder.setTime(converter.convertNanoTime(timedEvent.getNanotime()));
    builder.setEvent(
        Span.TimedEvent.Event.newBuilder()
            .setName(timedEvent.getName())
            .setAttributes(toProtoAttributes(timedEvent.getAttributes(), 0))
            .build());
    return builder.build();
  }

  static Links toProtoLinks(Collection<Link> links, int droppedCount) {
    Links.Builder builder = Links.newBuilder();
    builder.setDroppedLinksCount(droppedCount);
    for (Link link : links) {
      builder.addLink(toProtoLink(link));
    }
    return builder.build();
  }

  @VisibleForTesting
  static Span.Link toProtoLink(Link link) {
    Span.Link.Builder builder = Span.Link.newBuilder();
    SpanContext context = link.getContext();
    builder
        .setTraceId(toProtoTraceId(context.getTraceId()))
        .setSpanId(toProtoSpanId(context.getSpanId()))
        .setTracestate(toProtoTracestate(context.getTracestate()))
        .setAttributes(toProtoAttributes(link.getAttributes(), 0));
    return builder.build();
  }

  static io.opentelemetry.proto.trace.v1.Status toProtoStatus(Status status) {
    io.opentelemetry.proto.trace.v1.Status.Builder builder =
        io.opentelemetry.proto.trace.v1.Status.newBuilder()
            .setCode(status.getCanonicalCode().value());
    if (status.getDescription() != null) {
      builder.setMessage(status.getDescription());
    }
    return builder.build();
  }

  static Timestamp toProtoTimestamp(io.opentelemetry.trace.Timestamp timestamp) {
    return Timestamp.newBuilder()
        .setSeconds(timestamp.getSeconds())
        .setNanos(timestamp.getNanos())
        .build();
  }
}
