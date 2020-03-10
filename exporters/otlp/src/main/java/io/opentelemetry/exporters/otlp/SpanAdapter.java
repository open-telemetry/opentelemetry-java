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

package io.opentelemetry.exporters.otlp;

import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Span.SpanKind;
import io.opentelemetry.proto.trace.v1.Status;
import io.opentelemetry.proto.trace.v1.Status.StatusCode;
import io.opentelemetry.sdk.contrib.otproto.TraceProtoUtils;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.TimedEvent;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Link;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class SpanAdapter {
  static List<ResourceSpans> toProtoResourceSpans(List<SpanData> spanDataList) {
    Map<Resource, ResourceSpans.Builder> resourceSpansBuilderMap = new HashMap<>();
    for (SpanData spanData : spanDataList) {
      Resource resource = spanData.getResource();
      ResourceSpans.Builder resourceSpansBuilder =
          resourceSpansBuilderMap.get(spanData.getResource());
      if (resourceSpansBuilder == null) {
        resourceSpansBuilder =
            ResourceSpans.newBuilder().setResource(ResourceAdapter.toProtoResource(resource));
        resourceSpansBuilderMap.put(resource, resourceSpansBuilder);
      }
      resourceSpansBuilder.addSpans(toProtoSpan(spanData));
    }
    List<ResourceSpans> resourceSpans = new ArrayList<>(resourceSpansBuilderMap.size());
    for (ResourceSpans.Builder resourceSpansBuilder : resourceSpansBuilderMap.values()) {
      resourceSpans.add(resourceSpansBuilder.build());
    }
    return resourceSpans;
  }

  static Span toProtoSpan(SpanData spanData) {
    Span.Builder builder = Span.newBuilder();
    builder.setTraceId(TraceProtoUtils.toProtoTraceId(spanData.getTraceId()));
    builder.setSpanId(TraceProtoUtils.toProtoSpanId(spanData.getSpanId()));
    // TODO: Set TraceState;
    builder.setParentSpanId(TraceProtoUtils.toProtoSpanId(spanData.getParentSpanId()));
    builder.setName(spanData.getName());
    builder.setKind(toProtoSpanKind(spanData.getKind()));
    builder.setStartTimeUnixnano(spanData.getStartEpochNanos());
    builder.setEndTimeUnixnano(spanData.getEndEpochNanos());
    for (Map.Entry<String, AttributeValue> resourceEntry : spanData.getAttributes().entrySet()) {
      builder.addAttributes(
          CommonAdapter.toProtoAttribute(resourceEntry.getKey(), resourceEntry.getValue()));
    }
    // TODO: Set DroppedAttributesCount;
    for (TimedEvent timedEvent : spanData.getTimedEvents()) {
      builder.addEvents(toProtoSpanEvent(timedEvent));
    }
    builder.setDroppedEventsCount(
        spanData.getTotalRecordedEvents() - spanData.getTimedEvents().size());
    for (Link link : spanData.getLinks()) {
      builder.addLinks(toProtoSpanLink(link));
    }
    builder.setDroppedLinksCount(spanData.getTotalRecordedLinks() - spanData.getLinks().size());
    builder.setStatus(toStatusProto(spanData.getStatus()));
    return builder.build();
  }

  static Span.SpanKind toProtoSpanKind(io.opentelemetry.trace.Span.Kind kind) {
    switch (kind) {
      case INTERNAL:
        return SpanKind.INTERNAL;
      case SERVER:
        return SpanKind.SERVER;
      case CLIENT:
        return SpanKind.CLIENT;
      case PRODUCER:
        return SpanKind.PRODUCER;
      case CONSUMER:
        return SpanKind.CONSUMER;
    }
    return SpanKind.UNRECOGNIZED;
  }

  static Span.Event toProtoSpanEvent(TimedEvent timedEvent) {
    Span.Event.Builder builder = Span.Event.newBuilder();
    builder.setName(timedEvent.getName());
    builder.setTimeUnixnano(timedEvent.getEpochNanos());
    for (Map.Entry<String, AttributeValue> resourceEntry : timedEvent.getAttributes().entrySet()) {
      builder.addAttributes(
          CommonAdapter.toProtoAttribute(resourceEntry.getKey(), resourceEntry.getValue()));
    }
    // TODO: Set DroppedAttributesCount;
    return builder.build();
  }

  static Span.Link toProtoSpanLink(Link link) {
    Span.Link.Builder builder = Span.Link.newBuilder();
    builder.setTraceId(TraceProtoUtils.toProtoTraceId(link.getContext().getTraceId()));
    builder.setSpanId(TraceProtoUtils.toProtoSpanId(link.getContext().getSpanId()));
    // TODO: Set TraceState;
    for (Map.Entry<String, AttributeValue> resourceEntry : link.getAttributes().entrySet()) {
      builder.addAttributes(
          CommonAdapter.toProtoAttribute(resourceEntry.getKey(), resourceEntry.getValue()));
    }
    // TODO: Set DroppedAttributesCount;
    return builder.build();
  }

  static Status toStatusProto(io.opentelemetry.trace.Status status) {
    Status.Builder builder =
        Status.newBuilder().setCode(StatusCode.forNumber(status.getCanonicalCode().value()));
    if (status.getDescription() != null) {
      builder.setMessage(status.getDescription());
    }
    return builder.build();
  }

  private SpanAdapter() {}
}
