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

package io.opentelemetry.sdk.contrib.trace.export;

import static io.opentelemetry.trace.AttributeValue.booleanAttributeValue;
import static io.opentelemetry.trace.AttributeValue.doubleAttributeValue;
import static io.opentelemetry.trace.AttributeValue.longAttributeValue;
import static io.opentelemetry.trace.AttributeValue.stringAttributeValue;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import io.opentelemetry.proto.trace.v1.AttributeValue;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Span.Attributes;
import io.opentelemetry.proto.trace.v1.Span.TimedEvent;
import io.opentelemetry.proto.trace.v1.Span.TimedEvent.Event;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.Tracestate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO - Properly set TraceFlags/Tracestate for SpanContext and Links.
final class SpanDataUtils {
  private SpanDataUtils() {}

  public static SpanData getFromProto(Span span) {
    ByteString parentSpanId = span.getParentSpanId();

    return SpanData.create(
        getSpanContextFromProto(span),
        parentSpanId.isEmpty() ? null : SpanId.fromBytes(parentSpanId.toByteArray(), 0),
        Resource.getEmpty(),
        span.getName(),
        getKindFromProto(span),
        getTimestampFromProto(span.getStartTime()),
        getAttributesFromProto(span.getAttributes()),
        getEventsFromProto(span),
        getLinksFromProto(span),
        getStatusFromProto(span),
        getTimestampFromProto(span.getEndTime()));
  }

  static SpanContext getSpanContextFromProto(Span span) {
    return SpanContext.create(
        TraceId.fromBytes(span.getTraceId().toByteArray(), 0),
        SpanId.fromBytes(span.getSpanId().toByteArray(), 0),
        TraceFlags.getDefault(),
        Tracestate.getDefault());
  }

  static SpanData.Timestamp getTimestampFromProto(Timestamp protoTimestamp) {
    return SpanData.Timestamp.create(protoTimestamp.getSeconds(), protoTimestamp.getNanos());
  }

  static Map<String, io.opentelemetry.trace.AttributeValue> getAttributesFromProto(
      Attributes attributes) {
    Map<String, AttributeValue> protoAttrs = attributes.getAttributeMapMap();
    Map<String, io.opentelemetry.trace.AttributeValue> attrs = new HashMap<>(protoAttrs.size());

    for (Map.Entry<String, AttributeValue> entry : protoAttrs.entrySet()) {
      String attrName = entry.getKey();
      AttributeValue protoAttrValue = entry.getValue();

      io.opentelemetry.trace.AttributeValue attrValue = null;
      switch (protoAttrValue.getValueCase()) {
        case STRING_VALUE:
          attrValue = stringAttributeValue(protoAttrValue.getStringValue());
          break;
        case INT_VALUE:
          attrValue = longAttributeValue(protoAttrValue.getIntValue());
          break;
        case BOOL_VALUE:
          attrValue = booleanAttributeValue(protoAttrValue.getBoolValue());
          break;
        case DOUBLE_VALUE:
          attrValue = doubleAttributeValue(protoAttrValue.getDoubleValue());
          break;
        default:
          break;
      }

      if (attrValue == null) {
        continue;
      }

      attrs.put(attrName, attrValue);
    }

    return attrs;
  }

  static List<Link> getLinksFromProto(Span span) {
    List<Span.Link> protoLinks = span.getLinks().getLinkList();
    List<Link> links = new ArrayList<>(protoLinks.size());

    for (Span.Link link : protoLinks) {
      SpanContext linkContext =
          SpanContext.create(
              TraceId.fromBytes(link.getTraceId().toByteArray(), 0),
              SpanId.fromBytes(link.getSpanId().toByteArray(), 0),
              TraceFlags.getDefault(),
              Tracestate.getDefault());
      links.add(SpanData.Link.create(linkContext, getAttributesFromProto(link.getAttributes())));
    }

    return links;
  }

  static List<SpanData.TimedEvent> getEventsFromProto(Span span) {
    List<TimedEvent> protoEvents = span.getTimeEvents().getTimedEventList();
    List<SpanData.TimedEvent> events = new ArrayList<>(protoEvents.size());

    for (TimedEvent timedEvent : protoEvents) {
      Event event = timedEvent.getEvent();
      events.add(
          SpanData.TimedEvent.create(
              getTimestampFromProto(timedEvent.getTime()),
              SpanData.Event.create(
                  event.getName(), getAttributesFromProto(event.getAttributes()))));
    }

    return events;
  }

  static Status getStatusFromProto(Span span) {
    Status.CanonicalCode statusCode = Status.CanonicalCode.OK;
    for (Status.CanonicalCode value : Status.CanonicalCode.values()) {
      if (value.value() == span.getStatus().getCode()) {
        statusCode = value;
        break;
      }
    }

    Status status = statusCode.toStatus();

    if (span.getStatus().getMessage().length() > 0) {
      status = status.withDescription(span.getStatus().getMessage());
    }

    return status;
  }

  static Kind getKindFromProto(Span span) {
    switch (span.getKind()) {
      case INTERNAL:
        return Kind.INTERNAL;
      case SERVER:
        return Kind.SERVER;
      case CLIENT:
        return Kind.CLIENT;
      case PRODUCER:
        return Kind.PRODUCER;
      case CONSUMER:
        return Kind.CONSUMER;
      default:
        break;
    }

    // Fallback to INTERNAL.
    return Kind.INTERNAL;
  }
}
