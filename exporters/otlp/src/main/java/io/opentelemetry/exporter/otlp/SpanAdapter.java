/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp;

import static io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_CLIENT;
import static io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_CONSUMER;
import static io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_INTERNAL;
import static io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_PRODUCER;
import static io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_SERVER;
import static io.opentelemetry.proto.trace.v1.Status.DeprecatedStatusCode.DEPRECATED_STATUS_CODE_OK;
import static io.opentelemetry.proto.trace.v1.Status.DeprecatedStatusCode.DEPRECATED_STATUS_CODE_UNKNOWN_ERROR;

import io.opentelemetry.api.common.AttributeConsumer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.proto.trace.v1.InstrumentationLibrarySpans;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Span.SpanKind;
import io.opentelemetry.proto.trace.v1.Status;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.extension.otproto.TraceProtoUtils;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Event;
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
    final Span.Builder builder = Span.newBuilder();
    builder.setTraceId(TraceProtoUtils.toProtoTraceId(spanData.getTraceId()));
    builder.setSpanId(TraceProtoUtils.toProtoSpanId(spanData.getSpanId()));
    // TODO: Set TraceState;
    if (SpanId.isValid(spanData.getParentSpanId())) {
      builder.setParentSpanId(TraceProtoUtils.toProtoSpanId(spanData.getParentSpanId()));
    }
    builder.setName(spanData.getName());
    builder.setKind(toProtoSpanKind(spanData.getKind()));
    builder.setStartTimeUnixNano(spanData.getStartEpochNanos());
    builder.setEndTimeUnixNano(spanData.getEndEpochNanos());
    spanData
        .getAttributes()
        .forEach(
            new AttributeConsumer() {
              @Override
              public <T> void accept(AttributeKey<T> key, T value) {
                builder.addAttributes(CommonAdapter.toProtoAttribute(key, value));
              }
            });
    builder.setDroppedAttributesCount(
        spanData.getTotalAttributeCount() - spanData.getAttributes().size());
    for (Event event : spanData.getEvents()) {
      builder.addEvents(toProtoSpanEvent(event));
    }
    builder.setDroppedEventsCount(spanData.getTotalRecordedEvents() - spanData.getEvents().size());
    for (SpanData.Link link : spanData.getLinks()) {
      builder.addLinks(toProtoSpanLink(link));
    }
    builder.setDroppedLinksCount(spanData.getTotalRecordedLinks() - spanData.getLinks().size());
    builder.setStatus(toStatusProto(spanData.getStatus()));
    return builder.build();
  }

  static Span.SpanKind toProtoSpanKind(Kind kind) {
    switch (kind) {
      case INTERNAL:
        return SPAN_KIND_INTERNAL;
      case SERVER:
        return SPAN_KIND_SERVER;
      case CLIENT:
        return SPAN_KIND_CLIENT;
      case PRODUCER:
        return SPAN_KIND_PRODUCER;
      case CONSUMER:
        return SPAN_KIND_CONSUMER;
    }
    return SpanKind.UNRECOGNIZED;
  }

  static Span.Event toProtoSpanEvent(Event event) {
    final Span.Event.Builder builder = Span.Event.newBuilder();
    builder.setName(event.getName());
    builder.setTimeUnixNano(event.getEpochNanos());
    event
        .getAttributes()
        .forEach(
            new AttributeConsumer() {
              @Override
              public <T> void accept(AttributeKey<T> key, T value) {
                builder.addAttributes(CommonAdapter.toProtoAttribute(key, value));
              }
            });
    builder.setDroppedAttributesCount(
        event.getTotalAttributeCount() - event.getAttributes().size());
    return builder.build();
  }

  static Span.Link toProtoSpanLink(SpanData.Link link) {
    final Span.Link.Builder builder = Span.Link.newBuilder();
    builder.setTraceId(TraceProtoUtils.toProtoTraceId(link.getContext().getTraceIdAsHexString()));
    builder.setSpanId(TraceProtoUtils.toProtoSpanId(link.getContext().getSpanIdAsHexString()));
    // TODO: Set TraceState;
    Attributes attributes = link.getAttributes();
    attributes.forEach(
        new AttributeConsumer() {
          @Override
          public <T> void accept(AttributeKey<T> key, T value) {
            builder.addAttributes(CommonAdapter.toProtoAttribute(key, value));
          }
        });

    builder.setDroppedAttributesCount(link.getTotalAttributeCount() - attributes.size());
    return builder.build();
  }

  static Status toStatusProto(SpanData.Status status) {
    Status.StatusCode protoStatusCode = Status.StatusCode.STATUS_CODE_UNSET;
    Status.DeprecatedStatusCode deprecatedStatusCode = DEPRECATED_STATUS_CODE_OK;
    if (status.getStatusCode() == StatusCode.OK) {
      protoStatusCode = Status.StatusCode.STATUS_CODE_OK;
    } else if (status.getStatusCode() == StatusCode.ERROR) {
      protoStatusCode = Status.StatusCode.STATUS_CODE_ERROR;
      deprecatedStatusCode = DEPRECATED_STATUS_CODE_UNKNOWN_ERROR;
    }

    @SuppressWarnings("deprecation") // setDeprecatedCode is deprecated.
    Status.Builder builder =
        Status.newBuilder().setCode(protoStatusCode).setDeprecatedCode(deprecatedStatusCode);
    if (status.getDescription() != null) {
      builder.setMessage(status.getDescription());
    }
    return builder.build();
  }

  private SpanAdapter() {}
}
