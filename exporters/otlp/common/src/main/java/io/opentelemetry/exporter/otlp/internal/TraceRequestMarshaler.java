/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.proto.collector.trace.v1.internal.ExportTraceServiceRequest;
import io.opentelemetry.proto.trace.v1.internal.InstrumentationLibrarySpans;
import io.opentelemetry.proto.trace.v1.internal.ResourceSpans;
import io.opentelemetry.proto.trace.v1.internal.Span;
import io.opentelemetry.proto.trace.v1.internal.Status;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * {@link Marshaler} to convert SDK {@link SpanData} to OTLP ExportTraceServiceRequest.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class TraceRequestMarshaler extends MarshalerWithSize {

  private final ResourceSpansMarshaler[] resourceSpansMarshalers;

  /**
   * Returns a {@link TraceRequestMarshaler} that can be used to convert the provided {@link
   * SpanData} into a serialized OTLP ExportTraceServiceRequest.
   */
  public static TraceRequestMarshaler create(Collection<SpanData> spanDataList) {
    Map<Resource, Map<InstrumentationLibraryInfo, List<SpanMarshaler>>> resourceAndLibraryMap =
        groupByResourceAndLibrary(spanDataList);

    final ResourceSpansMarshaler[] resourceSpansMarshalers =
        new ResourceSpansMarshaler[resourceAndLibraryMap.size()];
    int posResource = 0;
    for (Map.Entry<Resource, Map<InstrumentationLibraryInfo, List<SpanMarshaler>>> entry :
        resourceAndLibraryMap.entrySet()) {
      final InstrumentationLibrarySpansMarshaler[] instrumentationLibrarySpansMarshalers =
          new InstrumentationLibrarySpansMarshaler[entry.getValue().size()];
      int posInstrumentation = 0;
      for (Map.Entry<InstrumentationLibraryInfo, List<SpanMarshaler>> entryIs :
          entry.getValue().entrySet()) {
        instrumentationLibrarySpansMarshalers[posInstrumentation++] =
            new InstrumentationLibrarySpansMarshaler(
                InstrumentationLibraryMarshaler.create(entryIs.getKey()),
                MarshalerUtil.toBytes(entryIs.getKey().getSchemaUrl()),
                entryIs.getValue());
      }
      resourceSpansMarshalers[posResource++] =
          new ResourceSpansMarshaler(
              ResourceMarshaler.create(entry.getKey()),
              MarshalerUtil.toBytes(entry.getKey().getSchemaUrl()),
              instrumentationLibrarySpansMarshalers);
    }

    return new TraceRequestMarshaler(resourceSpansMarshalers);
  }

  private TraceRequestMarshaler(ResourceSpansMarshaler[] resourceSpansMarshalers) {
    super(
        MarshalerUtil.sizeRepeatedMessage(
            ExportTraceServiceRequest.RESOURCE_SPANS, resourceSpansMarshalers));
    this.resourceSpansMarshalers = resourceSpansMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedMessage(
        ExportTraceServiceRequest.RESOURCE_SPANS, resourceSpansMarshalers);
  }

  private static final class ResourceSpansMarshaler extends MarshalerWithSize {
    private final ResourceMarshaler resourceMarshaler;
    private final byte[] schemaUrlUtf8;
    private final InstrumentationLibrarySpansMarshaler[] instrumentationLibrarySpansMarshalers;

    private ResourceSpansMarshaler(
        ResourceMarshaler resourceMarshaler,
        byte[] schemaUrlUtf8,
        InstrumentationLibrarySpansMarshaler[] instrumentationLibrarySpansMarshalers) {
      super(calculateSize(resourceMarshaler, schemaUrlUtf8, instrumentationLibrarySpansMarshalers));
      this.resourceMarshaler = resourceMarshaler;
      this.schemaUrlUtf8 = schemaUrlUtf8;
      this.instrumentationLibrarySpansMarshalers = instrumentationLibrarySpansMarshalers;
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      output.serializeMessage(ResourceSpans.RESOURCE, resourceMarshaler);
      output.serializeRepeatedMessage(
          ResourceSpans.INSTRUMENTATION_LIBRARY_SPANS, instrumentationLibrarySpansMarshalers);
      output.serializeString(ResourceSpans.SCHEMA_URL, schemaUrlUtf8);
    }

    private static int calculateSize(
        ResourceMarshaler resourceMarshaler,
        byte[] schemaUrlUtf8,
        InstrumentationLibrarySpansMarshaler[] instrumentationLibrarySpansMarshalers) {
      int size = 0;
      size += MarshalerUtil.sizeMessage(ResourceSpans.RESOURCE, resourceMarshaler);
      size += MarshalerUtil.sizeBytes(ResourceSpans.SCHEMA_URL, schemaUrlUtf8);
      size +=
          MarshalerUtil.sizeRepeatedMessage(
              ResourceSpans.INSTRUMENTATION_LIBRARY_SPANS, instrumentationLibrarySpansMarshalers);
      return size;
    }
  }

  private static final class InstrumentationLibrarySpansMarshaler extends MarshalerWithSize {
    private final InstrumentationLibraryMarshaler instrumentationLibrary;
    private final List<SpanMarshaler> spanMarshalers;
    private final byte[] schemaUrlUtf8;

    private InstrumentationLibrarySpansMarshaler(
        InstrumentationLibraryMarshaler instrumentationLibrary,
        byte[] schemaUrlUtf8,
        List<SpanMarshaler> spanMarshalers) {
      super(calculateSize(instrumentationLibrary, schemaUrlUtf8, spanMarshalers));
      this.instrumentationLibrary = instrumentationLibrary;
      this.schemaUrlUtf8 = schemaUrlUtf8;
      this.spanMarshalers = spanMarshalers;
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      output.serializeMessage(
          InstrumentationLibrarySpans.INSTRUMENTATION_LIBRARY, instrumentationLibrary);
      output.serializeRepeatedMessage(InstrumentationLibrarySpans.SPANS, spanMarshalers);
      output.serializeString(InstrumentationLibrarySpans.SCHEMA_URL, schemaUrlUtf8);
    }

    private static int calculateSize(
        InstrumentationLibraryMarshaler instrumentationLibrary,
        byte[] schemaUrlUtf8,
        List<SpanMarshaler> spanMarshalers) {
      int size = 0;
      size +=
          MarshalerUtil.sizeMessage(
              InstrumentationLibrarySpans.INSTRUMENTATION_LIBRARY, instrumentationLibrary);
      size += MarshalerUtil.sizeBytes(InstrumentationLibrarySpans.SCHEMA_URL, schemaUrlUtf8);
      size += MarshalerUtil.sizeRepeatedMessage(InstrumentationLibrarySpans.SPANS, spanMarshalers);
      return size;
    }
  }

  private static final class SpanMarshaler extends MarshalerWithSize {
    private final String traceId;
    private final String spanId;
    @Nullable private final String parentSpanId;
    private final byte[] nameUtf8;
    private final int spanKind;
    private final long startEpochNanos;
    private final long endEpochNanos;
    private final KeyValueMarshaler[] attributeMarshalers;
    private final int droppedAttributesCount;
    private final SpanEventMarshaler[] spanEventMarshalers;
    private final int droppedEventsCount;
    private final SpanLinkMarshaler[] spanLinkMarshalers;
    private final int droppedLinksCount;
    private final SpanStatusMarshaler spanStatusMarshaler;

    // Because SpanMarshaler is always part of a repeated field, it cannot return "null".
    static SpanMarshaler create(SpanData spanData) {
      KeyValueMarshaler[] attributeMarshalers =
          KeyValueMarshaler.createRepeated(spanData.getAttributes());
      SpanEventMarshaler[] spanEventMarshalers = SpanEventMarshaler.create(spanData.getEvents());
      SpanLinkMarshaler[] spanLinkMarshalers = SpanLinkMarshaler.create(spanData.getLinks());

      String parentSpanId =
          spanData.getParentSpanContext().isValid()
              ? spanData.getParentSpanContext().getSpanId()
              : null;

      return new SpanMarshaler(
          spanData.getSpanContext().getTraceId(),
          spanData.getSpanContext().getSpanId(),
          parentSpanId,
          MarshalerUtil.toBytes(spanData.getName()),
          toProtoSpanKind(spanData.getKind()),
          spanData.getStartEpochNanos(),
          spanData.getEndEpochNanos(),
          attributeMarshalers,
          spanData.getTotalAttributeCount() - spanData.getAttributes().size(),
          spanEventMarshalers,
          spanData.getTotalRecordedEvents() - spanData.getEvents().size(),
          spanLinkMarshalers,
          spanData.getTotalRecordedLinks() - spanData.getLinks().size(),
          SpanStatusMarshaler.create(spanData.getStatus()));
    }

    private SpanMarshaler(
        String traceId,
        String spanId,
        @Nullable String parentSpanId,
        byte[] nameUtf8,
        int spanKind,
        long startEpochNanos,
        long endEpochNanos,
        KeyValueMarshaler[] attributeMarshalers,
        int droppedAttributesCount,
        SpanEventMarshaler[] spanEventMarshalers,
        int droppedEventsCount,
        SpanLinkMarshaler[] spanLinkMarshalers,
        int droppedLinksCount,
        SpanStatusMarshaler spanStatusMarshaler) {
      super(
          calculateSize(
              traceId,
              spanId,
              parentSpanId,
              nameUtf8,
              spanKind,
              startEpochNanos,
              endEpochNanos,
              attributeMarshalers,
              droppedAttributesCount,
              spanEventMarshalers,
              droppedEventsCount,
              spanLinkMarshalers,
              droppedLinksCount,
              spanStatusMarshaler));
      this.traceId = traceId;
      this.spanId = spanId;
      this.parentSpanId = parentSpanId;
      this.nameUtf8 = nameUtf8;
      this.spanKind = spanKind;
      this.startEpochNanos = startEpochNanos;
      this.endEpochNanos = endEpochNanos;
      this.attributeMarshalers = attributeMarshalers;
      this.droppedAttributesCount = droppedAttributesCount;
      this.spanEventMarshalers = spanEventMarshalers;
      this.droppedEventsCount = droppedEventsCount;
      this.spanLinkMarshalers = spanLinkMarshalers;
      this.droppedLinksCount = droppedLinksCount;
      this.spanStatusMarshaler = spanStatusMarshaler;
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      output.serializeTraceId(Span.TRACE_ID, traceId);
      output.serializeSpanId(Span.SPAN_ID, spanId);
      // TODO: Set TraceState;
      output.serializeSpanId(Span.PARENT_SPAN_ID, parentSpanId);
      output.serializeString(Span.NAME, nameUtf8);

      output.serializeEnum(Span.KIND, spanKind);

      output.serializeFixed64(Span.START_TIME_UNIX_NANO, startEpochNanos);
      output.serializeFixed64(Span.END_TIME_UNIX_NANO, endEpochNanos);

      output.serializeRepeatedMessage(Span.ATTRIBUTES, attributeMarshalers);
      output.serializeUInt32(Span.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);

      output.serializeRepeatedMessage(Span.EVENTS, spanEventMarshalers);
      output.serializeUInt32(Span.DROPPED_EVENTS_COUNT, droppedEventsCount);

      output.serializeRepeatedMessage(Span.LINKS, spanLinkMarshalers);
      output.serializeUInt32(Span.DROPPED_LINKS_COUNT, droppedLinksCount);

      output.serializeMessage(Span.STATUS, spanStatusMarshaler);
    }

    private static int calculateSize(
        String traceId,
        String spanId,
        @Nullable String parentSpanId,
        byte[] nameUtf8,
        int spanKind,
        long startEpochNanos,
        long endEpochNanos,
        KeyValueMarshaler[] attributeMarshalers,
        int droppedAttributesCount,
        SpanEventMarshaler[] spanEventMarshalers,
        int droppedEventsCount,
        SpanLinkMarshaler[] spanLinkMarshalers,
        int droppedLinksCount,
        SpanStatusMarshaler spanStatusMarshaler) {
      int size = 0;
      size += MarshalerUtil.sizeTraceId(Span.TRACE_ID, traceId);
      size += MarshalerUtil.sizeSpanId(Span.SPAN_ID, spanId);
      // TODO: Set TraceState;
      size += MarshalerUtil.sizeSpanId(Span.PARENT_SPAN_ID, parentSpanId);
      size += MarshalerUtil.sizeBytes(Span.NAME, nameUtf8);

      size += MarshalerUtil.sizeEnum(Span.KIND, spanKind);

      size += MarshalerUtil.sizeFixed64(Span.START_TIME_UNIX_NANO, startEpochNanos);
      size += MarshalerUtil.sizeFixed64(Span.END_TIME_UNIX_NANO, endEpochNanos);

      size += MarshalerUtil.sizeRepeatedMessage(Span.ATTRIBUTES, attributeMarshalers);
      size += MarshalerUtil.sizeUInt32(Span.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);

      size += MarshalerUtil.sizeRepeatedMessage(Span.EVENTS, spanEventMarshalers);
      size += MarshalerUtil.sizeUInt32(Span.DROPPED_EVENTS_COUNT, droppedEventsCount);

      size += MarshalerUtil.sizeRepeatedMessage(Span.LINKS, spanLinkMarshalers);
      size += MarshalerUtil.sizeUInt32(Span.DROPPED_LINKS_COUNT, droppedLinksCount);

      size += MarshalerUtil.sizeMessage(Span.STATUS, spanStatusMarshaler);
      return size;
    }
  }

  private static final class SpanEventMarshaler extends MarshalerWithSize {
    private static final SpanEventMarshaler[] EMPTY = new SpanEventMarshaler[0];
    private final long epochNanos;
    private final byte[] name;
    private final KeyValueMarshaler[] attributeMarshalers;
    private final int droppedAttributesCount;

    static SpanEventMarshaler[] create(List<EventData> events) {
      if (events.isEmpty()) {
        return EMPTY;
      }

      SpanEventMarshaler[] result = new SpanEventMarshaler[events.size()];
      int pos = 0;
      for (EventData event : events) {
        result[pos++] =
            new SpanEventMarshaler(
                event.getEpochNanos(),
                MarshalerUtil.toBytes(event.getName()),
                KeyValueMarshaler.createRepeated(event.getAttributes()),
                event.getTotalAttributeCount() - event.getAttributes().size());
      }

      return result;
    }

    private SpanEventMarshaler(
        long epochNanos,
        byte[] name,
        KeyValueMarshaler[] attributeMarshalers,
        int droppedAttributesCount) {
      super(calculateSize(epochNanos, name, attributeMarshalers, droppedAttributesCount));
      this.epochNanos = epochNanos;
      this.name = name;
      this.attributeMarshalers = attributeMarshalers;
      this.droppedAttributesCount = droppedAttributesCount;
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      output.serializeFixed64(Span.Event.TIME_UNIX_NANO, epochNanos);
      output.serializeString(Span.Event.NAME, name);
      output.serializeRepeatedMessage(Span.Event.ATTRIBUTES, attributeMarshalers);
      output.serializeUInt32(Span.Event.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);
    }

    private static int calculateSize(
        long epochNanos,
        byte[] name,
        KeyValueMarshaler[] attributeMarshalers,
        int droppedAttributesCount) {
      int size = 0;
      size += MarshalerUtil.sizeFixed64(Span.Event.TIME_UNIX_NANO, epochNanos);
      size += MarshalerUtil.sizeBytes(Span.Event.NAME, name);
      size += MarshalerUtil.sizeRepeatedMessage(Span.Event.ATTRIBUTES, attributeMarshalers);
      size += MarshalerUtil.sizeUInt32(Span.Event.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);
      return size;
    }
  }

  private static final class SpanLinkMarshaler extends MarshalerWithSize {
    private static final SpanLinkMarshaler[] EMPTY = new SpanLinkMarshaler[0];
    private final String traceId;
    private final String spanId;
    private final KeyValueMarshaler[] attributeMarshalers;
    private final int droppedAttributesCount;

    static SpanLinkMarshaler[] create(List<LinkData> links) {
      if (links.isEmpty()) {
        return EMPTY;
      }

      SpanLinkMarshaler[] result = new SpanLinkMarshaler[links.size()];
      int pos = 0;
      for (LinkData link : links) {
        result[pos++] =
            new SpanLinkMarshaler(
                link.getSpanContext().getTraceId(),
                link.getSpanContext().getSpanId(),
                KeyValueMarshaler.createRepeated(link.getAttributes()),
                link.getTotalAttributeCount() - link.getAttributes().size());
      }

      return result;
    }

    private SpanLinkMarshaler(
        String traceId,
        String spanId,
        KeyValueMarshaler[] attributeMarshalers,
        int droppedAttributesCount) {
      super(calculateSize(traceId, spanId, attributeMarshalers, droppedAttributesCount));
      this.traceId = traceId;
      this.spanId = spanId;
      this.attributeMarshalers = attributeMarshalers;
      this.droppedAttributesCount = droppedAttributesCount;
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      output.serializeTraceId(Span.Link.TRACE_ID, traceId);
      output.serializeSpanId(Span.Link.SPAN_ID, spanId);
      // TODO: Set TraceState;
      output.serializeRepeatedMessage(Span.Link.ATTRIBUTES, attributeMarshalers);
      output.serializeUInt32(Span.Link.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);
    }

    private static int calculateSize(
        String traceId,
        String spanId,
        KeyValueMarshaler[] attributeMarshalers,
        int droppedAttributesCount) {
      int size = 0;
      size += MarshalerUtil.sizeTraceId(Span.Link.TRACE_ID, traceId);
      size += MarshalerUtil.sizeSpanId(Span.Link.SPAN_ID, spanId);
      // TODO: Set TraceState;
      size += MarshalerUtil.sizeRepeatedMessage(Span.Link.ATTRIBUTES, attributeMarshalers);
      size += MarshalerUtil.sizeUInt32(Span.Link.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);
      return size;
    }
  }

  private static final class SpanStatusMarshaler extends MarshalerWithSize {
    private final int protoStatusCode;
    private final int deprecatedStatusCode;
    private final byte[] descriptionUtf8;

    static SpanStatusMarshaler create(StatusData status) {
      int protoStatusCode = Status.StatusCode.STATUS_CODE_UNSET_VALUE;
      int deprecatedStatusCode = Status.DeprecatedStatusCode.DEPRECATED_STATUS_CODE_OK_VALUE;
      if (status.getStatusCode() == StatusCode.OK) {
        protoStatusCode = Status.StatusCode.STATUS_CODE_OK_VALUE;
      } else if (status.getStatusCode() == StatusCode.ERROR) {
        protoStatusCode = Status.StatusCode.STATUS_CODE_ERROR_VALUE;
        deprecatedStatusCode =
            Status.DeprecatedStatusCode.DEPRECATED_STATUS_CODE_UNKNOWN_ERROR_VALUE;
      }
      byte[] description = MarshalerUtil.toBytes(status.getDescription());
      return new SpanStatusMarshaler(protoStatusCode, deprecatedStatusCode, description);
    }

    private SpanStatusMarshaler(
        int protoStatusCode, int deprecatedStatusCode, byte[] descriptionUtf8) {
      super(computeSize(protoStatusCode, deprecatedStatusCode, descriptionUtf8));
      this.protoStatusCode = protoStatusCode;
      this.deprecatedStatusCode = deprecatedStatusCode;
      this.descriptionUtf8 = descriptionUtf8;
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      output.serializeEnum(Status.DEPRECATED_CODE, deprecatedStatusCode);
      output.serializeString(Status.MESSAGE, descriptionUtf8);
      output.serializeEnum(Status.CODE, protoStatusCode);
    }

    private static int computeSize(
        int protoStatusCode, int deprecatedStatusCode, byte[] descriptionUtf8) {
      int size = 0;
      size += MarshalerUtil.sizeEnum(Status.DEPRECATED_CODE, deprecatedStatusCode);
      size += MarshalerUtil.sizeBytes(Status.MESSAGE, descriptionUtf8);
      size += MarshalerUtil.sizeEnum(Status.CODE, protoStatusCode);
      return size;
    }
  }

  private static Map<Resource, Map<InstrumentationLibraryInfo, List<SpanMarshaler>>>
      groupByResourceAndLibrary(Collection<SpanData> spanDataList) {
    return MarshalerUtil.groupByResourceAndLibrary(
        spanDataList,
        // TODO(anuraaga): Replace with an internal SdkData type of interface that exposes these
        // two.
        SpanData::getResource,
        SpanData::getInstrumentationLibraryInfo,
        data -> SpanMarshaler.create(data));
  }

  private static int toProtoSpanKind(SpanKind kind) {
    switch (kind) {
      case INTERNAL:
        return Span.SpanKind.SPAN_KIND_INTERNAL_VALUE;
      case SERVER:
        return Span.SpanKind.SPAN_KIND_SERVER_VALUE;
      case CLIENT:
        return Span.SpanKind.SPAN_KIND_CLIENT_VALUE;
      case PRODUCER:
        return Span.SpanKind.SPAN_KIND_PRODUCER_VALUE;
      case CONSUMER:
        return Span.SpanKind.SPAN_KIND_CONSUMER_VALUE;
    }
    return -1;
  }
}
