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

import com.google.common.collect.EvictingQueue;
import com.google.protobuf.ByteString;
import io.opentelemetry.proto.trace.v1.Span.Attributes;
import io.opentelemetry.proto.trace.v1.Span.Links;
import io.opentelemetry.proto.trace.v1.Span.SpanKind;
import io.opentelemetry.proto.trace.v1.Span.TimedEvents;
import io.opentelemetry.proto.trace.v1.TruncatableString;
import io.opentelemetry.resource.Resource;
import io.opentelemetry.sdk.internal.TimestampConverter;
import io.opentelemetry.sdk.trace.RecordEventsSpanImpl.AttributesWithCapacity;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.Tracestate;
import java.nio.charset.Charset;
import java.util.Map;

// Utilities to convert to trace protos.
final class TraceProtoUtils {

  private TraceProtoUtils() {}

  static ByteString toProtoTraceId(TraceId traceId) {
    return ByteString.copyFrom(traceId.toLowerBase16(), Charset.defaultCharset());
  }

  static ByteString toProtoSpanId(SpanId spanId) {
    return ByteString.copyFrom(spanId.toLowerBase16(), Charset.defaultCharset());
  }

  static io.opentelemetry.proto.trace.v1.Span.Tracestate toProtoTracestate(Tracestate tracestate) {
    io.opentelemetry.proto.trace.v1.Span.Tracestate.Builder builder =
        io.opentelemetry.proto.trace.v1.Span.Tracestate.newBuilder();
    for (Tracestate.Entry entry : tracestate.getEntries()) {
      builder.addEntries(
          io.opentelemetry.proto.trace.v1.Span.Tracestate.Entry.newBuilder()
              .setKey(entry.getKey())
              .setValue(entry.getValue()));
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
    }
    return SpanKind.UNRECOGNIZED;
  }

  static Attributes toProtoAttributes(AttributesWithCapacity attributes) {
    Attributes.Builder builder = Attributes.newBuilder();
    builder.setDroppedAttributesCount(attributes.getNumberOfDroppedAttributes());
    for (Map.Entry<String, AttributeValue> attribute : attributes.entrySet()) {
      builder.putAttributeMap(attribute.getKey(), toProtoAttributeValue(attribute.getValue()));
    }
    return builder.build();
  }

  static Attributes toProtoAttributes(Map<String, AttributeValue> attributes) {
    Attributes.Builder builder = Attributes.newBuilder();
    for (Map.Entry<String, AttributeValue> attribute : attributes.entrySet()) {
      builder.putAttributeMap(attribute.getKey(), toProtoAttributeValue(attribute.getValue()));
    }
    return builder.build();
  }

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
        builder.setStringValue(
            TruncatableString.newBuilder().setValue(attributeValue.getStringValue()).build());
    }
    return builder.build();
  }

  static TimedEvents toProtoTimedEvents(
      EvictingQueue<TimedEvent> events, int totalRecordedEvents, TimestampConverter converter) {
    TimedEvents.Builder builder = TimedEvents.newBuilder();
    builder.setDroppedTimedEventsCount(totalRecordedEvents - events.size());
    for (TimedEvent timedEvent : events) {
      builder.addTimedEvent(toProtoTimedEvent(timedEvent, converter));
    }
    return builder.build();
  }

  static io.opentelemetry.proto.trace.v1.Span.TimedEvent toProtoTimedEvent(
      TimedEvent timedEvent, TimestampConverter converter) {
    io.opentelemetry.proto.trace.v1.Span.TimedEvent.Builder builder =
        io.opentelemetry.proto.trace.v1.Span.TimedEvent.newBuilder();
    builder.setTime(converter.convertNanoTime(timedEvent.getNanotime()));
    builder.setEvent(
        io.opentelemetry.proto.trace.v1.Span.TimedEvent.Event.newBuilder()
            .setName(TruncatableString.newBuilder().setValue(timedEvent.getName()).build())
            .setAttributes(toProtoAttributes(timedEvent.getAttributes()))
            .build());
    return builder.build();
  }

  static Links toProtoLinks(EvictingQueue<Link> links, int totalRecordedLinks) {
    Links.Builder builder = Links.newBuilder();
    builder.setDroppedLinksCount(totalRecordedLinks - links.size());
    for (Link link : links) {
      builder.addLink(toProtoLink(link));
    }
    return builder.build();
  }

  static io.opentelemetry.proto.trace.v1.Span.Link toProtoLink(Link link) {
    io.opentelemetry.proto.trace.v1.Span.Link.Builder builder =
        io.opentelemetry.proto.trace.v1.Span.Link.newBuilder();
    SpanContext context = link.getContext();
    builder
        .setTraceId(toProtoTraceId(context.getTraceId()))
        .setSpanId(toProtoSpanId(context.getSpanId()))
        .setTracestate(toProtoTracestate(context.getTracestate()))
        .setAttributes(toProtoAttributes(link.getAttributes()));
    return builder.build();
  }

  static io.opentelemetry.proto.trace.v1.Status toProtoStatus(Status status) {
    return io.opentelemetry.proto.trace.v1.Status.newBuilder()
        .setCode(status.getCanonicalCode().value())
        .setMessage(status.getDescription())
        .build();
  }
}
