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

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.proto.trace.v1.InstrumentationLibrarySpans;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Span.SpanKind;
import io.opentelemetry.proto.trace.v1.Status;
import io.opentelemetry.proto.trace.v1.Status.StatusCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.contrib.otproto.TraceProtoUtils;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Link;
import io.opentelemetry.sdk.trace.data.SpanData.TimedEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class SpanAdapter {
  static List<ResourceSpans> toProtoResourceSpans(Collection<SpanData> spanDataList) {
    Map<Resource, Map<InstrumentationLibraryInfo, List<Span>>> resourceAndLibraryMap =
        groupByResourceAndLibrary(spanDataList);
    List<ResourceSpans> resourceSpans = new ArrayList<>(resourceAndLibraryMap.size());
    for (Map.Entry<Resource, Map<InstrumentationLibraryInfo, List<Span>>> entryResource :
        resourceAndLibraryMap.entrySet()) {
      List<InstrumentationLibrarySpans> instrumentationLibrarySpans =
          new ArrayList<>(entryResource.getValue().size());
      for (Map.Entry<InstrumentationLibraryInfo, List<Span>> entryLibrary :
          entryResource.getValue().entrySet()) {
        instrumentationLibrarySpans.add(
            InstrumentationLibrarySpans.newBuilder()
                .setInstrumentationLibrary(
                    CommonAdapter.toProtoInstrumentationLibrary(entryLibrary.getKey()))
                .addAllSpans(entryLibrary.getValue())
                .build());
      }
      resourceSpans.add(
          ResourceSpans.newBuilder()
              .setResource(ResourceAdapter.toProtoResource(entryResource.getKey()))
              .addAllInstrumentationLibrarySpans(instrumentationLibrarySpans)
              .build());
    }
    return resourceSpans;
  }

  private static Map<Resource, Map<InstrumentationLibraryInfo, List<Span>>>
      groupByResourceAndLibrary(Collection<SpanData> spanDataList) {
    Map<Resource, Map<InstrumentationLibraryInfo, List<Span>>> result = new HashMap<>();
    for (SpanData spanData : spanDataList) {
      Resource resource = spanData.getResource();
      Map<InstrumentationLibraryInfo, List<Span>> libraryInfoListMap =
          result.get(spanData.getResource());
      if (libraryInfoListMap == null) {
        libraryInfoListMap = new HashMap<>();
        result.put(resource, libraryInfoListMap);
      }
      List<Span> spanList = libraryInfoListMap.get(spanData.getInstrumentationLibraryInfo());
      if (spanList == null) {
        spanList = new ArrayList<>();
        libraryInfoListMap.put(spanData.getInstrumentationLibraryInfo(), spanList);
      }
      spanList.add(toProtoSpan(spanData));
    }
    return result;
  }

  static Span toProtoSpan(SpanData spanData) {
    Span.Builder builder = Span.newBuilder();
    builder.setTraceId(TraceProtoUtils.toProtoTraceId(spanData.getTraceId()));
    builder.setSpanId(TraceProtoUtils.toProtoSpanId(spanData.getSpanId()));
    // TODO: Set TraceState;
    builder.setParentSpanId(TraceProtoUtils.toProtoSpanId(spanData.getParentSpanId()));
    builder.setName(spanData.getName());
    builder.setKind(toProtoSpanKind(spanData.getKind()));
    builder.setStartTimeUnixNano(spanData.getStartEpochNanos());
    builder.setEndTimeUnixNano(spanData.getEndEpochNanos());
    for (Map.Entry<String, AttributeValue> resourceEntry : spanData.getAttributes().entrySet()) {
      builder.addAttributes(
          CommonAdapter.toProtoAttribute(resourceEntry.getKey(), resourceEntry.getValue()));
    }
    builder.setDroppedAttributesCount(
        spanData.getTotalAttributeCount() - spanData.getAttributes().size());
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
    builder.setTimeUnixNano(timedEvent.getEpochNanos());
    for (Map.Entry<String, AttributeValue> resourceEntry : timedEvent.getAttributes().entrySet()) {
      builder.addAttributes(
          CommonAdapter.toProtoAttribute(resourceEntry.getKey(), resourceEntry.getValue()));
    }
    builder.setDroppedAttributesCount(
        timedEvent.getTotalAttributeCount() - timedEvent.getAttributes().size());
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
    builder.setDroppedAttributesCount(link.getTotalAttributeCount() - link.getAttributes().size());
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
