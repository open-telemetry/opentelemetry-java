/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_CLIENT;
import static io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_CONSUMER;
import static io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_INTERNAL;
import static io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_PRODUCER;
import static io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_SERVER;
import static io.opentelemetry.proto.trace.v1.Status.DeprecatedStatusCode.DEPRECATED_STATUS_CODE_OK;
import static io.opentelemetry.proto.trace.v1.Status.DeprecatedStatusCode.DEPRECATED_STATUS_CODE_UNKNOWN_ERROR;

import com.google.protobuf.ByteString;
import com.google.protobuf.UnsafeByteOperations;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.proto.trace.v1.InstrumentationLibrarySpans;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Status;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Converter from SDK {@link SpanData} to OTLP {@link ResourceSpans}. */
public final class SpanAdapter {

  // In practice, there is often only one thread that calls this code in the BatchSpanProcessor so
  // reusing buffers for the thread is almost free. Even with multiple threads, it should still be
  // worth it and is common practice in serialization libraries such as Jackson.
  private static final ThreadLocal<ThreadLocalCache> THREAD_LOCAL_CACHE = new ThreadLocal<>();

  // Still set DeprecatedCode
  @SuppressWarnings("deprecation")
  private static final Status STATUS_OK =
      Status.newBuilder()
          .setCode(Status.StatusCode.STATUS_CODE_OK)
          .setDeprecatedCode(DEPRECATED_STATUS_CODE_OK)
          .build();

  // Still set DeprecatedCode
  @SuppressWarnings("deprecation")
  private static final Status STATUS_ERROR =
      Status.newBuilder()
          .setCode(Status.StatusCode.STATUS_CODE_ERROR)
          .setDeprecatedCode(DEPRECATED_STATUS_CODE_UNKNOWN_ERROR)
          .build();

  // Still set DeprecatedCode
  @SuppressWarnings("deprecation")
  private static final Status STATUS_UNSET =
      Status.newBuilder()
          .setCode(Status.StatusCode.STATUS_CODE_UNSET)
          .setDeprecatedCode(DEPRECATED_STATUS_CODE_OK)
          .build();

  /** Converts the provided {@link SpanData} to {@link ResourceSpans}. */
  public static List<ResourceSpans> toProtoResourceSpans(Collection<SpanData> spanDataList) {
    Map<Resource, Map<InstrumentationLibraryInfo, List<Span>>> resourceAndLibraryMap =
        groupByResourceAndLibrary(spanDataList);
    List<ResourceSpans> resourceSpans = new ArrayList<>(resourceAndLibraryMap.size());
    resourceAndLibraryMap.forEach(
        (resource, librarySpans) -> {
          ResourceSpans.Builder resourceSpansBuilder =
              ResourceSpans.newBuilder().setResource(ResourceAdapter.toProtoResource(resource));
          librarySpans.forEach(
              (library, spans) ->
                  resourceSpansBuilder.addInstrumentationLibrarySpans(
                      InstrumentationLibrarySpans.newBuilder()
                          .setInstrumentationLibrary(
                              CommonAdapter.toProtoInstrumentationLibrary(library))
                          .addAllSpans(spans)
                          .build()));
          resourceSpans.add(resourceSpansBuilder.build());
        });
    return resourceSpans;
  }

  private static Map<Resource, Map<InstrumentationLibraryInfo, List<Span>>>
      groupByResourceAndLibrary(Collection<SpanData> spanDataList) {
    Map<Resource, Map<InstrumentationLibraryInfo, List<Span>>> result = new HashMap<>();
    ThreadLocalCache threadLocalCache = getThreadLocalCache();
    for (SpanData spanData : spanDataList) {
      Map<InstrumentationLibraryInfo, List<Span>> libraryInfoListMap =
          result.computeIfAbsent(spanData.getResource(), unused -> new HashMap<>());
      List<Span> spanList =
          libraryInfoListMap.computeIfAbsent(
              spanData.getInstrumentationLibraryInfo(), unused -> new ArrayList<>());
      spanList.add(toProtoSpan(spanData, threadLocalCache));
    }
    threadLocalCache.idBytesCache.clear();
    return result;
  }

  // Visible for testing
  static Span toProtoSpan(SpanData spanData, ThreadLocalCache threadLocalCache) {
    Map<String, ByteString> idBytesCache = threadLocalCache.idBytesCache;
    Span.Builder builder = threadLocalCache.spanBuilder;
    builder.setTraceId(
        idBytesCache.computeIfAbsent(
            spanData.getSpanContext().getTraceId(),
            unused ->
                UnsafeByteOperations.unsafeWrap(spanData.getSpanContext().getTraceIdBytes())));
    builder.setSpanId(
        idBytesCache.computeIfAbsent(
            spanData.getSpanContext().getSpanId(),
            unused -> UnsafeByteOperations.unsafeWrap(spanData.getSpanContext().getSpanIdBytes())));
    // TODO: Set TraceState;
    if (spanData.getParentSpanContext().isValid()) {
      builder.setParentSpanId(
          idBytesCache.computeIfAbsent(
              spanData.getParentSpanContext().getSpanId(),
              unused ->
                  UnsafeByteOperations.unsafeWrap(
                      spanData.getParentSpanContext().getSpanIdBytes())));
    }
    builder.setName(spanData.getName());
    builder.setKind(toProtoSpanKind(spanData.getKind()));
    builder.setStartTimeUnixNano(spanData.getStartEpochNanos());
    builder.setEndTimeUnixNano(spanData.getEndEpochNanos());
    spanData
        .getAttributes()
        .forEach((key, value) -> builder.addAttributes(CommonAdapter.toProtoAttribute(key, value)));
    builder.setDroppedAttributesCount(
        spanData.getTotalAttributeCount() - spanData.getAttributes().size());
    for (EventData event : spanData.getEvents()) {
      builder.addEvents(toProtoSpanEvent(event, threadLocalCache));
    }
    builder.setDroppedEventsCount(spanData.getTotalRecordedEvents() - spanData.getEvents().size());
    for (LinkData link : spanData.getLinks()) {
      builder.addLinks(toProtoSpanLink(link, threadLocalCache));
    }
    builder.setDroppedLinksCount(spanData.getTotalRecordedLinks() - spanData.getLinks().size());
    builder.setStatus(toStatusProto(spanData.getStatus()));
    Span span = builder.build();
    builder.clear();
    return span;
  }

  static Span.SpanKind toProtoSpanKind(SpanKind kind) {
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
    return Span.SpanKind.UNRECOGNIZED;
  }

  // Visible for testing
  static Span.Event toProtoSpanEvent(EventData event, ThreadLocalCache threadLocalCache) {
    Span.Event.Builder builder = threadLocalCache.spanEventBuilder;
    builder.setName(event.getName());
    builder.setTimeUnixNano(event.getEpochNanos());
    event
        .getAttributes()
        .forEach((key, value) -> builder.addAttributes(CommonAdapter.toProtoAttribute(key, value)));
    builder.setDroppedAttributesCount(
        event.getTotalAttributeCount() - event.getAttributes().size());
    Span.Event built = builder.build();
    builder.clear();
    return built;
  }

  // Visible for testing
  static Span.Link toProtoSpanLink(LinkData link, ThreadLocalCache threadLocalCache) {
    Map<String, ByteString> idBytesCache = threadLocalCache.idBytesCache;
    Span.Link.Builder builder = threadLocalCache.spanLinkBuilder;
    builder.setTraceId(
        idBytesCache.computeIfAbsent(
            link.getSpanContext().getTraceId(),
            unused -> UnsafeByteOperations.unsafeWrap(link.getSpanContext().getTraceIdBytes())));
    builder.setSpanId(
        idBytesCache.computeIfAbsent(
            link.getSpanContext().getSpanId(),
            unused -> UnsafeByteOperations.unsafeWrap(link.getSpanContext().getSpanIdBytes())));
    // TODO: Set TraceState;
    Attributes attributes = link.getAttributes();
    attributes.forEach(
        (key, value) -> builder.addAttributes(CommonAdapter.toProtoAttribute(key, value)));

    builder.setDroppedAttributesCount(link.getTotalAttributeCount() - attributes.size());
    Span.Link built = builder.build();
    builder.clear();
    return built;
  }

  // Visible for testing
  static Status toStatusProto(StatusData status) {
    final Status withoutDescription;
    switch (status.getStatusCode()) {
      case OK:
        withoutDescription = STATUS_OK;
        break;
      case ERROR:
        withoutDescription = STATUS_ERROR;
        break;
      case UNSET:
      default:
        withoutDescription = STATUS_UNSET;
        break;
    }
    if (status.getDescription().isEmpty()) {
      return withoutDescription;
    }
    return withoutDescription.toBuilder().setMessage(status.getDescription()).build();
  }

  private static ThreadLocalCache getThreadLocalCache() {
    ThreadLocalCache result = THREAD_LOCAL_CACHE.get();
    if (result == null) {
      result = new ThreadLocalCache();
      THREAD_LOCAL_CACHE.set(result);
    }
    return result;
  }

  static final class ThreadLocalCache {
    final Map<String, ByteString> idBytesCache = new HashMap<>();
    final Span.Builder spanBuilder = Span.newBuilder();
    final Span.Event.Builder spanEventBuilder = Span.Event.newBuilder();
    final Span.Link.Builder spanLinkBuilder = Span.Link.newBuilder();
  }

  private SpanAdapter() {}
}
